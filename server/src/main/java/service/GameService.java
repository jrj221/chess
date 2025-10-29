package service;

import dataaccess.*;
import datamodel.*;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Objects;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) { // constructor
        this.dataAccess = dataAccess;
    }

    public int createGame(CreateGameRequest createGameRequest, String authToken) throws Exception {
        if (authToken == null || createGameRequest.gameName() == null) {
            throw new Exception("Bad request");
        }
        var authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("Unauthorized Logout");
        }
        String gameName = createGameRequest.gameName();
        int gameID = dataAccess.createGameData(gameName);
        return gameID;
    }

    public ArrayList<GameData> listGames(ListGamesRequest listGamesRequest) throws Exception {
        if (listGamesRequest.authToken() == null) {
            throw new Exception("Bad request");
        }
        var authData = dataAccess.getAuth(listGamesRequest.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Unauthorized Logout");
        }
        return dataAccess.getAllGames();
    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken) throws Exception {
        try {
            if (joinGameRequest.playerColor() == null || authToken == null) {
                throw new Exception("Bad request");
            }
            var authData = dataAccess.getAuth(authToken);
            if (authData == null) {
                throw new UnauthorizedException("Unauthorized");
            }
            String username = authData.username();
            dataAccess.joinGame(username, joinGameRequest.gameID(), joinGameRequest.playerColor());
        } catch (DataAccessException ex) {
            if (ex.getMessage().equals("Invalid gameID")) {
                throw new NoExistingGameException("No existing game");
            }
            else if (ex.getMessage().equals("Auth not found")) {
                throw new UnauthorizedException("Unauthorized");
            }
            else if (ex.getMessage().equals("Invalid playerColor")) {
                throw new Exception("Invalid playerColor");
            }
        }
    }

}
