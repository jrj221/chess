package service;

import java.util.ArrayList;
import java.util.Objects;

import dataaccess.*;
import datamodel.*; // AuthData and UserData and the like


public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) { // constructor
        this.dataAccess = dataAccess;
        //var authData = new AuthData(user.username(), generateAuthToken());
    }

    public AuthData register(RegisterRequest user) throws Exception {
        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("Already exists");
        }
        dataAccess.createUser(new UserData(user.username(), user.email(), user.password()));
        return dataAccess.createAuth(user.username());
    }

    public AuthData login(LoginRequest user) throws Exception {
        UserData userData = dataAccess.getUser(user.username());
        if (userData == null) {
            throw new Exception("Unauthorized Login"); // no existing user
        }
        if (!Objects.equals(user.password(), userData.password())) { // bad match
            throw new Exception("Unauthorized Login"); // wrong password
        }
        return dataAccess.createAuth(user.username());
    }

    public void logout(LogoutRequest logoutRequest) throws Exception {
        var authData = dataAccess.getAuth(logoutRequest.authToken());
        if (authData == null) {
            throw new Exception("Unauthorized Logout");
        }
        dataAccess.deleteAuth(logoutRequest.authToken());
    }

    public int createGame(CreateGameRequest createGameRequest) throws Exception {
        var authData = dataAccess.getAuth(createGameRequest.authToken());
        if (authData == null) {
            throw new Exception("Unauthorized Logout");
        }
        String gameName = createGameRequest.gameName();
        int gameID = dataAccess.createGameData(gameName);
        return gameID;
    }

    public ArrayList<GameData> listGames(ListGamesRequest listGamesRequest) throws Exception {
        var authData = dataAccess.getAuth(listGamesRequest.authToken());
        if (authData == null) {
            throw new Exception("Unauthorized Logout");
        }
        return dataAccess.getAllGames();
    }

    public void joinGame(JoinGameRequest joinGameRequest) throws Exception {
        var authData = dataAccess.getAuth(joinGameRequest.authToken());
        if (authData == null) {
            throw new Exception("Unauthorized Logout");
        }
        var gameData = dataAccess.getGame(joinGameRequest.gameID());
        if (gameData == null) {
            throw new NoExistingGameException("No game found");
        }
        if ((joinGameRequest.playerColor().equals("BLACK") && gameData.blackUsername() != null) || (joinGameRequest.playerColor().equals("WHITE") && gameData.whiteUsername() != null)) {
            throw new AlreadyTakenException("Color not available");
        }
        String username = authData.username();
        dataAccess.joinGame(username, joinGameRequest.gameID(), joinGameRequest.playerColor());
    }


}
