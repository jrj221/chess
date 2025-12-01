package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import org.eclipse.jetty.server.Authentication;
import service.*;
import websocket.commands.*;
import websocket.messages.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;
    private final GameService gameService;
    private final GameplayService gameplayService;


    public Server() {
        var dataAccess = new SQLDataAccess();
        var connectionsManager = new ConnectionsManager();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        gameplayService = new GameplayService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.post("user", ctx -> register(ctx));
        server.post("session", ctx -> login(ctx));
        server.delete("session", ctx -> logout(ctx));
        server.get("game", ctx -> listGames(ctx));
        server.post("game", ctx -> createGame(ctx));
        server.put("game", ctx -> joinGame(ctx));
        server.delete("db", ctx -> clear(ctx));
        // Register your endpoints and exception handlers here.
        server.ws("/ws", ws -> {
            ws.onConnect(ctx -> System.out.println("Client connected"));
            ws.onMessage((ctx) -> {
                var serializer = new Gson();
                UserGameCommand command = serializer.fromJson(ctx.message(), UserGameCommand.class);
                switch (command.getCommandType()) {
                    // this probably needs to be expanded to have a broadcast group (the sender gets a laodgame,
                    // the others get a notification)
                    case CONNECT:
                        ConnectCommand connectCommand = serializer.fromJson(ctx.message(), ConnectCommand.class);
                        ctx.enableAutomaticPings(); // is this where I turn it on?
                        connectionsManager.add(ctx.session);
                        ctx.send(connect(connectCommand));
                        if (connectCommand.state.equals("player")) {
                            connectionsManager.broadcastNotif(
                                    new NotificationMessage(String.format("%s connected to the game on team %s.",
                                            connectCommand.username, connectCommand.teamColor)));
                        } else {
                            connectionsManager.broadcastNotif(
                                    new NotificationMessage(String.format("%s connected to the game as an observer",
                                            connectCommand.username)));
                        }
                        break;
                    case LEAVE:
                        LeaveCommand leaveCommand = serializer.fromJson(ctx.message(), LeaveCommand.class);
                        connectionsManager.remove(ctx.session);
                        connectionsManager.broadcastNotif(
                                new NotificationMessage(String.format("%s left the game.", leaveCommand.username)));
                        break;
                    case MAKE_MOVE:
                        MakeMoveCommand makeMoveCommand = serializer.fromJson(ctx.message(), MakeMoveCommand.class);
                        try {
                            var updatedGame = makeMove(makeMoveCommand);
                            connectionsManager.broadcastGame(new LoadGameMessage(updatedGame));
                            connectionsManager.broadcastNotif(
                                    new NotificationMessage(String.format("%s moved %s.",
                                            makeMoveCommand.username,
                                            makeMoveCommand.moveString)));

                            var enemy = makeMoveCommand.enemyColor;
                            // send notif if a team is in checkmate and end the game
                            if (updatedGame.isInCheckmate(enemy)) {
                                var player = gameplayService.getPlayer(makeMoveCommand.getGameID(), enemy);
                                connectionsManager.broadcastNotif(
                                        new NotificationMessage(String.format("Checkmate! %s loses!\n", player)));
                                connectionsManager.broadcastGame(
                                        new LoadGameMessage(gameplayService.endGame(makeMoveCommand.getGameID())));
                                break; // break so inCheck stuff doesn't trigger too
                            }

                            // send notif if enemy team is in check.
                            if (updatedGame.isInCheck(enemy)) {
                                var player = gameplayService.getPlayer(makeMoveCommand.getGameID(), enemy);
                                connectionsManager.broadcastNotif(
                                        new NotificationMessage(String.format("%s is in check!\n " +
                                                "Moves that do not get them out of check are considered illegal."
                                                , player)));
                            }

                            // send notif if there is a stalemate.
                            if (updatedGame.isInStalemate(enemy)) {
                                var player = gameplayService.getPlayer(makeMoveCommand.getGameID(), enemy);
                                connectionsManager.broadcastNotif(
                                        new NotificationMessage(
                                                String.format("Draw! %s is in a stalemate!\n ", player)));
                                connectionsManager.broadcastGame(
                                        new LoadGameMessage(gameplayService.endGame(makeMoveCommand.getGameID())));
                            }
                        } catch (InvalidMoveException ex) {
                            var errorMessage = new ErrorMessage(ex.getMessage());
                            var errorMessageString = serializer.toJson(errorMessage);
                            ctx.send(errorMessageString);
                        }
                        break;
                    case RESIGN:
                        ResignCommand resignCommand = serializer.fromJson(ctx.message(), ResignCommand.class);
                        connectionsManager.broadcastGame(
                                new LoadGameMessage(gameplayService.endGame(resignCommand.getGameID())));
                        var winner = gameplayService.getPlayer(resignCommand.getGameID(), resignCommand.enemyColor);
                        connectionsManager.broadcastNotif(
                                new NotificationMessage(String.format("%s has resigned. Team %s wins by default!",
                                        resignCommand.username, winner)));
                        break;
                    default:
                        ctx.send(serializer.toJson(new ErrorMessage("Invalid command")));
                }
            });
            ws.onClose(ctx -> System.out.println("Client disconnected"));
        });


    }
    // start with the simplest method (functions) and later on rework into
    // classes or interfaces to see if it needs to change
    private void register(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, RegisterRequest.class);
            var authData = userService.register(user);
            ctx.result(serializer.toJson(authData));
        } catch (AlreadyTakenException ex) {
            // when userService throws an exception
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).result(message);
        } catch (BadRequestException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    private void login(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, LoginRequest.class);
            var authData = userService.login(user);
            ctx.result(serializer.toJson(authData));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (BadRequestException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    private void logout(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var logoutRequest = new LogoutRequest(ctx.header("authorization"));
            userService.logout(logoutRequest);
            ctx.result();
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (BadRequestException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    private void listGames(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var listGamesRequest = new ListGamesRequest(ctx.header("authorization"));
            ArrayList<GameData> allGames = gameService.listGames(listGamesRequest);
            ctx.result(serializer.toJson(Map.of("games", allGames)));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (BadRequestException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    private void createGame(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var createGameRequest = serializer.fromJson(requestJson, CreateGameRequest.class);
            int gameID = gameService.createGame(createGameRequest, ctx.header("authorization"));
            ctx.result(serializer.toJson(Map.of("gameID", gameID)));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (BadRequestException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    private void joinGame(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var joinGameRequest = serializer.fromJson(requestJson, JoinGameRequest.class);
            gameService.joinGame(joinGameRequest, ctx.header("authorization"));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (BadRequestException | NoExistingGameException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (AlreadyTakenException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
        ctx.result();
    }

    private void clear(Context ctx) {
        try {
            userService.clear();
            ctx.result();
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    // WEBSOCKET SEND HANDLERS

    private String connect(ConnectCommand command) throws Exception {
        ChessGame game = gameplayService.getGame(command.getGameID());
        return new Gson().toJson(new LoadGameMessage(game));
    }

    private ChessGame makeMove(MakeMoveCommand command) throws Exception {
        ChessGame updatedGame = gameplayService.makeMove(command.getGameID(), command.move, command.teamColor);
        return updatedGame;
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
