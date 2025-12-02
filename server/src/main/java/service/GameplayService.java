package service;

import chess.*;
import dataaccess.*;
import datamodel.*;

public class GameplayService {
    private final DataAccess dataAccess;


    public GameplayService(DataAccess dataAccess) { // constructor
        this.dataAccess = dataAccess;
    }


    public GameData getGame(String authToken, int gameID) throws Exception {
        try {
            dataAccess.getAuth(authToken); // will throw error if unauthorized
            return dataAccess.getGame(gameID);
        } catch (DataAccessException ex) {
            if (ex.getMessage().equals("Game not found")) {
                throw new NoExistingGameException("Game not found");
            } else {
                throw new UnauthorizedException("Unauthorized");
            }
        }
    }


    public void leave(int gameID, String teamColor) throws Exception {
        dataAccess.updateGamePlayers(gameID, teamColor);
    }


    public ChessGame makeMove(int gameID, ChessMove move, ChessGame.TeamColor teamColor) throws Exception {
        try {
            GameData gameData = dataAccess.getGame(gameID);
            var game = gameData.game();
            var piece = game.getBoard().getPiece(move.getStartPosition());
            if (piece != null && piece.getTeamColor() != teamColor) { // attempting to move an enemy piece
                throw new InvalidMoveException("Can't move enemy piece");
            }
            game.makeMove(move); // modifies the ChessGame object itself
            dataAccess.updateGame(gameID, game);
            return game;
        } catch (DataAccessException ex) {
            throw new NoExistingGameException("No existing game");
        }
    }


    public UserData getPlayer(String authToken) throws Exception {
        try {
            AuthData authData = dataAccess.getAuth(authToken);
            return dataAccess.getUser(authData.username());
        } catch (DataAccessException ex) {
            throw new UnauthorizedException("Unauthorized");
        }
    }


    public ChessGame endGame(String authToken, int gameID) throws Exception {
        try {
            dataAccess.getAuth(authToken); // will throw error if unauthorized
            GameData gameData = dataAccess.getGame(gameID);
            var game = gameData.game();
            game.endGame(); // sets isGameOver to true
            dataAccess.updateGame(gameID, game);
            return game;
        } catch (DataAccessException ex) {
            if (ex.getMessage().equals("Game not found")) {
                throw new NoExistingGameException("Game not found");
            } else {
                throw new UnauthorizedException("Unauthorized");
            }
        }
    }
}
