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
        try {
            if (authToken == null || createGameRequest.gameName() == null) {
                throw new BadRequestException("Bad request");
            }
            dataAccess.getAuth(authToken);
            String gameName = createGameRequest.gameName();
            int gameID = dataAccess.createGameData(gameName);
            return gameID;
        } catch (DataAccessException ex) { // catches if getAuth throws (no auth found)
            throw new UnauthorizedException("Unauthorized");
        }
    }

    public ArrayList<GameData> listGames(ListGamesRequest listGamesRequest) throws Exception {
        try {
            if (listGamesRequest.authToken() == null) {
                throw new BadRequestException("Bad request");
            }
            dataAccess.getAuth(listGamesRequest.authToken());
            return dataAccess.getAllGames();
        } catch (DataAccessException ex) { // catches if getAuth throws (auth not found)
            throw new UnauthorizedException("Unauthorized auth");
        }
    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken) throws Exception {
        try {
            if (joinGameRequest.playerColor() == null || authToken == null) {
                throw new BadRequestException("Bad request");
            }
            var authData = dataAccess.getAuth(authToken);
            String username = authData.username();
            dataAccess.joinGame(username, joinGameRequest.gameID(), joinGameRequest.playerColor());
        } catch (DataAccessException ex) {
            switch (ex.getMessage()) {
                case "Invalid gameID" -> throw new NoExistingGameException("No existing game");
                case "Auth not found" -> throw new UnauthorizedException("Unauthorized auth");
                case "Invalid playerColor" -> throw new BadRequestException("Invalid playerColor");
            }
        }
    }
}
