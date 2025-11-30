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

    public ChessGame makeMove(int gameID, ChessMove move) throws Exception {
        try {
            GameData gameData = dataAccess.getGame(gameID);
            var game = gameData.game();
            game.makeMove(move); // modifies the ChessGame object itself
            dataAccess.updateGame(gameID, game);
            return game;
        } catch (DataAccessException ex) {
            throw new NoExistingGameException("No existing game");
        }
    }
}
