package websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import datamodel.*;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import service.GameplayService;
import websocket.commands.*;
import websocket.messages.*;


public class WebsocketHandler {
    private final GameplayService gameplayService;
    private final ConnectionsManager connectionsManager;

    public WebsocketHandler(DataAccess dataAccess) {
        gameplayService = new GameplayService(dataAccess);
        connectionsManager = new ConnectionsManager();
    }


    public void onConnect(WsContext ctx) {
        System.out.println("Client connected");
    }


    public void onMessage(WsMessageContext ctx) throws Exception {
        var serializer = new Gson();
        UserGameCommand command = serializer.fromJson(ctx.message(), UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT:
                connect(command, ctx);
                break;
            case LEAVE:
                leave(command, ctx);
                break;
            case MAKE_MOVE:
                MakeMoveCommand makeMoveCommand = serializer.fromJson(ctx.message(), MakeMoveCommand.class);
                makeMove(makeMoveCommand, ctx);
                break;
            case RESIGN:
                resign(command, ctx);
                break;
            default:
                ctx.send(serializer.toJson(new ErrorMessage("Invalid command")));
        }
    }


    private void connect(UserGameCommand command, WsMessageContext ctx) {
        try {
            ctx.enableAutomaticPings(); // is this where I turn it on?
            connectionsManager.add(ctx.session, command.getGameID());

            UserData userData = gameplayService.getPlayer(command.getAuthToken());
            GameData gameData = gameplayService.getGame(command.getAuthToken(), command.getGameID());
            boolean isPlayer = false;
            String teamColor = null;
            String username = userData.username();
            if (username.equals(gameData.whiteUsername())) {
                teamColor = "WHITE";
                isPlayer = true;
            } else if (username.equals(gameData.blackUsername())) {
                teamColor = "BLACK";
                isPlayer = true;
            }

            if (isPlayer) {
                connectionsManager.broadcastNotif(ctx.session,
                        new NotificationMessage(String.format("%s connected to the game on team %s.",
                                username, teamColor)), command.getGameID());
            } else {
                connectionsManager.broadcastNotif(ctx.session,
                        new NotificationMessage(String.format("%s connected to the game as an observer",
                                username)), command.getGameID());
            }
            ctx.send(new Gson().toJson(new LoadGameMessage(gameData.game())));
        } catch (Exception ex) {
            var errorMessageString = new Gson().toJson(new ErrorMessage(ex.getMessage()));
            ctx.send(errorMessageString);
        }
    }


    private void makeMove(MakeMoveCommand command, WsMessageContext ctx) {
        try {
            UserData userData = gameplayService.getPlayer(command.getAuthToken());
            GameData gameData = gameplayService.getGame(command.getAuthToken(), command.getGameID());

            if (gameData.game().getIsGameOver()) {
                var errorMessageString = new Gson().toJson(new ErrorMessage("The game is over, no more moves"));
                ctx.send(errorMessageString);
                return;
            }

            ChessGame.TeamColor teamColor = null;
            ChessGame.TeamColor enemyColor = null;
            ChessMove move = command.move;
            String username = userData.username();
            String enemyUsername = null;
            if (username.equals(gameData.whiteUsername())) {
                teamColor = ChessGame.TeamColor.WHITE;
                enemyColor = ChessGame.TeamColor.BLACK;
                enemyUsername = gameData.blackUsername();
            } else if (username.equals(gameData.blackUsername())) {
                teamColor = ChessGame.TeamColor.BLACK;
                enemyColor = ChessGame.TeamColor.WHITE;
                enemyUsername = gameData.whiteUsername();
            }

            ChessGame updatedGame = gameplayService.makeMove(command.getGameID(), command.move, teamColor);
            connectionsManager.broadcastNotif(ctx.session,
                    new NotificationMessage(String.format("%s moved %s.",
                            username,
                            move.toString())), command.getGameID());

            // send notif if a team is in checkmate and end the game
            if (updatedGame.isInCheckmate(enemyColor)) {
                connectionsManager.broadcastNotif(null, // send to sender as well
                        new NotificationMessage(String.format("Checkmate! %s loses!\n", enemyUsername)),
                        command.getGameID());
                connectionsManager.broadcastGame(
                        new LoadGameMessage(gameplayService.endGame(command.getAuthToken(), command.getGameID())),
                        command.getGameID());
            }

            // send notif if enemy team is in check.
            else if (updatedGame.isInCheck(enemyColor)) {
                connectionsManager.broadcastNotif(null, // send to sender as well
                        new NotificationMessage(String.format("%s is in check!\n " +
                                        "Moves that do not get them out of check are considered illegal."
                                , enemyUsername)), command.getGameID());
            }

            // send notif if there is a stalemate.
            else if (updatedGame.isInStalemate(enemyColor)) {
                connectionsManager.broadcastNotif(null, // send to sender as well
                        new NotificationMessage(
                                String.format("Draw! %s is in a stalemate!\n ", enemyUsername)), command.getGameID());
                connectionsManager.broadcastGame(
                        new LoadGameMessage(gameplayService.endGame(command.getAuthToken(), command.getGameID())),
                        command.getGameID());
            } else {
                connectionsManager.broadcastGame(new LoadGameMessage(updatedGame), command.getGameID()); // send normal updated
            }
        } catch (Exception ex) {
            var errorMessage = new ErrorMessage(ex.getMessage());
            var errorMessageString = new Gson().toJson(errorMessage);
            ctx.send(errorMessageString);
        }
    }


    private void leave(UserGameCommand command, WsMessageContext ctx) {
        try {
            UserData userData = gameplayService.getPlayer(command.getAuthToken());
            GameData gameData = gameplayService.getGame(command.getAuthToken(), command.getGameID());

            String teamColor = null;
            String username = userData.username();
            if (username.equals(gameData.whiteUsername())) {
                teamColor = "WHITE";
            } else if (username.equals(gameData.blackUsername())) {
                teamColor = "BLACK";
            }

            connectionsManager.remove(ctx.session, command.getGameID());
            gameplayService.leave(command.getGameID(), teamColor);
            connectionsManager.broadcastNotif(ctx.session,
                    new NotificationMessage(String.format("%s left the game.", username)), command.getGameID());
        } catch (Exception ex) {
            var errorMessageString = new Gson().toJson(new ErrorMessage(ex.getMessage()));
            ctx.send(errorMessageString);
        }
    }


    private void resign(UserGameCommand command, WsMessageContext ctx) {
        try {
            // end game and broadcast game object where you can't do moves anymore

            UserData userData = gameplayService.getPlayer(command.getAuthToken());
            GameData gameData = gameplayService.getGame(command.getAuthToken(), command.getGameID());

            String enemyColor = null;
            boolean isPlayer = false;
            String username = userData.username();
            if (username.equals(gameData.whiteUsername())) {
                enemyColor = "BLACK";
                isPlayer = true;
            } else if (username.equals(gameData.blackUsername())) {
                enemyColor = "WHITE";
                isPlayer = true;
            }

            if (!isPlayer) {
                var errorMessageString = new Gson().toJson(new ErrorMessage("Observers cannot resign"));
                ctx.send(errorMessageString);
                return;
            }
            if (gameData.game().getIsGameOver()) {
                var errorMessageString = new Gson().toJson(new ErrorMessage("The game is over, cannot resign"));
                ctx.send(errorMessageString);
                return;
            }

            gameplayService.endGame(command.getAuthToken(), command.getGameID());
            connectionsManager.broadcastNotif(null, // sender gets this notif too
                    new NotificationMessage(String.format("%s has resigned. Team %s wins by default!",
                            username, enemyColor)), command.getGameID());
        } catch (Exception ex) {
            var errorMessageString = new Gson().toJson(new ErrorMessage(ex.getMessage()));
            ctx.send(errorMessageString);
        }
    }


    public void onClose(WsContext ctx) {
        System.out.println("Client disconnected");
    }
}
