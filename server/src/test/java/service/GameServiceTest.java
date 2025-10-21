package service;

import dataaccess.*;
import datamodel.*;
import org.junit.jupiter.api.Test;

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
        assertDoesNotThrow(() -> userService.createGame(createGameRequest));
    }

    @Test
    void createGameBadAuthToken() throws Exception{
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        userService.register(registerRequest);
        var loginRequest = new LoginRequest("joe", "password");
        var authData = new AuthData("joe", "badAuth");
        var createGameRequest = new CreateGameRequest(authData.authToken(), "testGame");
        assertThrows(Exception.class, () -> userService.createGame(createGameRequest));
    }
}
