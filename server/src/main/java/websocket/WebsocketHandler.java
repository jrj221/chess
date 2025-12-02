package websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import service.GameplayService;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

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
                ConnectCommand connectCommand = serializer.fromJson(ctx.message(), ConnectCommand.class);
                connect(connectCommand, ctx);
                break;
            case LEAVE:
                LeaveCommand leaveCommand = serializer.fromJson(ctx.message(), LeaveCommand.class);
                leave(leaveCommand, ctx);
                break;
            case MAKE_MOVE:
                MakeMoveCommand makeMoveCommand = serializer.fromJson(ctx.message(), MakeMoveCommand.class);
                makeMove(makeMoveCommand, ctx);
                break;
            case RESIGN:
                ResignCommand resignCommand = serializer.fromJson(ctx.message(), ResignCommand.class);
                resign(resignCommand);
                break;
            default:
                ctx.send(serializer.toJson(new ErrorMessage("Invalid command")));
        }
    }


    private void connect(ConnectCommand command, WsMessageContext ctx) throws Exception {
        ConnectCommand connectCommand = new Gson().fromJson(ctx.message(), ConnectCommand.class);
        ctx.enableAutomaticPings(); // is this where I turn it on?
        connectionsManager.add(ctx.session);

        if (connectCommand.state.equals("player")) {
            connectionsManager.broadcastNotif(
                    new NotificationMessage(String.format("%s connected to the game on team %s.",
                            connectCommand.username, connectCommand.teamColor)));
        } else {
            connectionsManager.broadcastNotif(
                    new NotificationMessage(String.format("%s connected to the game as an observer",
                            connectCommand.username)));
        }

        ChessGame game = gameplayService.getGame(command.getGameID());
        ctx.send(new Gson().toJson(new LoadGameMessage(game)));
    }


    private void makeMove(MakeMoveCommand command, WsMessageContext ctx) throws Exception {
        try {
            ChessGame updatedGame = gameplayService.makeMove(command.getGameID(), command.move, command.teamColor);
            connectionsManager.broadcastGame(new LoadGameMessage(updatedGame));
            connectionsManager.broadcastNotif(
                    new NotificationMessage(String.format("%s moved %s.",
                            command.username,
                            command.moveString)));

            var enemy = command.enemyColor;
            // send notif if a team is in checkmate and end the game
            if (updatedGame.isInCheckmate(enemy)) {
                var player = gameplayService.getPlayer(command.getGameID(), enemy);
                connectionsManager.broadcastNotif(
                        new NotificationMessage(String.format("Checkmate! %s loses!\n", player)));
                connectionsManager.broadcastGame(
                        new LoadGameMessage(gameplayService.endGame(command.getGameID())));
            }

            // send notif if enemy team is in check.
            else if (updatedGame.isInCheck(enemy)) {
                var player = gameplayService.getPlayer(command.getGameID(), enemy);
                connectionsManager.broadcastNotif(
                        new NotificationMessage(String.format("%s is in check!\n " +
                                        "Moves that do not get them out of check are considered illegal."
                                , player)));
            }

            // send notif if there is a stalemate.
            else if (updatedGame.isInStalemate(enemy)) {
                var player = gameplayService.getPlayer(command.getGameID(), enemy);
                connectionsManager.broadcastNotif(
                        new NotificationMessage(
                                String.format("Draw! %s is in a stalemate!\n ", player)));
                connectionsManager.broadcastGame(
                        new LoadGameMessage(gameplayService.endGame(command.getGameID())));
            }
        } catch (InvalidMoveException ex) {
            var errorMessage = new ErrorMessage(ex.getMessage());
            var errorMessageString = new Gson().toJson(errorMessage);
            ctx.send(errorMessageString);
        }
    }


    private void leave(LeaveCommand command, WsMessageContext ctx) throws Exception {
        connectionsManager.remove(ctx.session);
        gameplayService.leave(command.getGameID(), command.teamColor);
        connectionsManager.broadcastNotif(
                new NotificationMessage(String.format("%s left the game.", command.username)));
    }


    private void resign(ResignCommand command) throws Exception {
        // end game and broadcast game object where you can't do moves anymore
        connectionsManager.broadcastGame(
                new LoadGameMessage(gameplayService.endGame(command.getGameID())));
        var winner = gameplayService.getPlayer(command.getGameID(), command.enemyColor);
        connectionsManager.broadcastNotif(
                new NotificationMessage(String.format("%s has resigned. Team %s wins by default!",
                        command.username, winner)));
    }


    public void onClose(WsContext ctx) {
        System.out.println("Client disconnected");
    }
}
