package service;

import chess.*;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.NoExistingGameException;
import datamodel.GameData;
import static ui.EscapeSequences.*;

import java.util.HashMap;
import java.util.List;

public class GameplayService {
    private final DataAccess dataAccess;

    public GameplayService(DataAccess dataAccess) { // constructor
        this.dataAccess = dataAccess;
    }


    public ChessGame getGame(int gameID) throws Exception {
        try {
            GameData gameData = dataAccess.getGame(gameID);
            return gameData.game();
        } catch (DataAccessException ex) {
            throw new NoExistingGameException("No existing game");
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

    public String getPlayer(int gameID, ChessGame.TeamColor teamColor) throws Exception {
        try {
            GameData gameData = dataAccess.getGame(gameID);
            return teamColor.equals(ChessGame.TeamColor.WHITE) ? gameData.whiteUsername() : gameData.blackUsername();
        } catch (DataAccessException ex) {
            throw new NoExistingGameException("No existing game");
        }
    }


    public ChessGame endGame(int gameID) throws Exception {
        try {
            GameData gameData = dataAccess.getGame(gameID);
            var game = gameData.game();
            game.endGame();
            dataAccess.updateGame(gameID, game);
            return game;
        } catch (DataAccessException ex) {
            throw new NoExistingGameException("No existing game");
        }
    }
}
