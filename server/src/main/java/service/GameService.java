package service;

import dataaccess.*;
import datamodel.*;

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
        if ((!Objects.equals(joinGameRequest.playerColor(), "BLACK")
                && !Objects.equals(joinGameRequest.playerColor(), "WHITE"))
                || authToken == null || joinGameRequest.gameID() == 0) {
            throw new Exception("Bad request");
        }
        var authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        var gameData = dataAccess.getGame(joinGameRequest.gameID());
        if (gameData == null) {
            throw new NoExistingGameException("No game found");
        }
        if ((joinGameRequest.playerColor().equals("BLACK") && gameData.blackUsername() != null)
                || (joinGameRequest.playerColor().equals("WHITE") && gameData.whiteUsername() != null)) {
            throw new AlreadyTakenException("Color not available");
        }
        String username = authData.username();
        dataAccess.joinGame(username, joinGameRequest.gameID(), joinGameRequest.playerColor());
    }

}
