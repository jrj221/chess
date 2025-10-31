package service;

import dataaccess.*;
import datamodel.*;

import javax.xml.crypto.Data;
import java.sql.SQLException;
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
            if (ex.getMessage().equals("gameName cannot be null")) {
                throw new BadRequestException("gameName cannot be null");
            } else if (ex.getMessage().equals("failed to get connection")) {
                throw new SQLException(ex.getMessage()); // HUH? why is it caught as if it's DataAccessException?
            } else {
                throw new UnauthorizedException("Unauthorized");
            }
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
            if (ex.getMessage().equals("failed to get connection")) {
                throw new SQLException(ex.getMessage()); // HUH? why is it caught as if it's DataAccessException?
            }
            throw new UnauthorizedException("Unauthorized auth");
        }
    }

    public void joinGame(JoinGameRequest joinGameRequest, String authToken) throws Exception {
        try {
            if (joinGameRequest.playerColor() == null || authToken == null) {
                throw new BadRequestException("Bad request");
            }
            var gameData = dataAccess.getGame(joinGameRequest.gameID());
            if ((joinGameRequest.playerColor().equals("WHITE") && gameData.whiteUsername() != null) ||
                    (joinGameRequest.playerColor().equals("BLACK") && gameData.blackUsername() != null)) {
                throw new AlreadyTakenException("team already taken");
            }
            var authData = dataAccess.getAuth(authToken);
            String username = authData.username();
            dataAccess.joinGame(username, joinGameRequest.gameID(), joinGameRequest.playerColor());
        } catch (DataAccessException ex) {
            switch (ex.getMessage()) {
                case "failed to get connection" -> throw new SQLException(ex.getMessage()); // HUH? why is it caught as if it's DataAccessException?
                case "Game not found" -> throw new NoExistingGameException("No existing game"); // if getGame fails
                case "Invalid gameID" -> throw new NoExistingGameException("No existing game"); // could be thrown by joinGame
                case "Auth not found" -> throw new UnauthorizedException("Unauthorized auth");
                case "Invalid playerColor" -> throw new BadRequestException("Invalid playerColor");
            }
        }
    }
}
