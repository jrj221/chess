package service;

import dataaccess.*;
import datamodel.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
public class GameServiceTest {

    @Test
    void createGameSuccessful() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        userService.register(registerRequest);
        var loginRequest = new LoginRequest("joe", "password");
        AuthData authData = userService.login(loginRequest);
        var createGameRequest = new CreateGameRequest(authData.authToken(), "testGame");
        int gameID = userService.createGame(createGameRequest);
        assertEquals("testGame", db.getGame(gameID).gameName());
    }

    @Test
    void createGameBadAuthToken() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        userService.register(registerRequest);
        var authData = new AuthData("joe", "badAuth");
        var createGameRequest = new CreateGameRequest(authData.authToken(), "testGame");
        assertThrows(Exception.class, () -> userService.createGame(createGameRequest));
    }


    @Test
    void listGamesSuccessful() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        AuthData authData = userService.register(registerRequest);
        var listGamesRequest = new ListGamesRequest(authData.authToken());

        var game1 = db.createGameData("game1");
        var game2 = db.createGameData("game2");
        var game3 = db.createGameData("game3");
        var myGames = new ArrayList<GameData>();
        myGames.add(db.getGame(game1));
        myGames.add(db.getGame(game2));
        myGames.add(db.getGame(game3));
        ArrayList<GameData> allGames = userService.listGames(listGamesRequest);
        assertEquals(myGames, allGames);
    }

    @Test
    void listGamesBadAuthToken() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        AuthData authData = userService.register(registerRequest);
        var listGamesRequest = new ListGamesRequest("badToken");
        assertThrows(Exception.class, () -> userService.listGames(listGamesRequest));
    }
}
