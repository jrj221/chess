package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import org.eclipse.jetty.server.Authentication;
import service.*;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

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
                        connectionsManager.broadcast(ctx.session,
                                new NotificationMessage(String.format("%s connected to the game on team %s",
                                        connectCommand.username, connectCommand.teamColor)));
                    default:
                        ctx.send("Invalid command");
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

    private String connect(UserGameCommand command) throws Exception {
        ChessGame game = gameplayService.getGame(command.getGameID());
        return new Gson().toJson(new LoadGameMessage(game));
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
