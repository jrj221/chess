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

    public AuthData register(RegisterRequest registerRequest) throws Exception {
        if (registerRequest.username() == null || registerRequest.email() == null|| registerRequest.password() == null) {
            throw new Exception("Bad request");
        }
        if (dataAccess.getUser(registerRequest.username()) != null) {
            throw new AlreadyTakenException("Already exists");
        }
        dataAccess.createUser(new UserData(registerRequest.username(), registerRequest.email(), registerRequest.password()));
        return dataAccess.createAuth(registerRequest.username());
    }

    public AuthData login(LoginRequest loginRequest) throws Exception {
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw new Exception("Bad request");
        }
        UserData userData = dataAccess.getUser(loginRequest.username());
        if (userData == null) {
            throw new UnauthorizedException("Unauthorized Login"); // no existing user
        }
        if (!Objects.equals(loginRequest.password(), userData.password())) { // bad match
            throw new UnauthorizedException("Unauthorized Login"); // wrong password
        }
        return dataAccess.createAuth(loginRequest.username());
    }

    public void logout(LogoutRequest logoutRequest) throws Exception {
        if (logoutRequest.authToken() == null) {
            throw new Exception("Bad request");
        }
        var authData = dataAccess.getAuth(logoutRequest.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Unauthorized Logout");
        }
        dataAccess.deleteAuth(logoutRequest.authToken());
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
        if ((!Objects.equals(joinGameRequest.playerColor(), "BLACK") && !Objects.equals(joinGameRequest.playerColor(), "WHITE")) || authToken == null || joinGameRequest.gameID() == 0) {
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
        if ((joinGameRequest.playerColor().equals("BLACK") && gameData.blackUsername() != null) || (joinGameRequest.playerColor().equals("WHITE") && gameData.whiteUsername() != null)) {
            throw new AlreadyTakenException("Color not available");
        }
        String username = authData.username();
        dataAccess.joinGame(username, joinGameRequest.gameID(), joinGameRequest.playerColor());
    }

    public void clear() {
        dataAccess.clear();
    }
}
