package service;

import dataaccess.*;
import datamodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    static DataAccess db;
    static UserService userService;

    @BeforeEach
    void setup() {
        db = new MemoryDataAccess();
        userService = new UserService(db);
    }

    @Test
    void registerSuccessful() throws Exception {
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        var authData = userService.register(registerRequest);
        assertNotNull(authData);
        assertEquals(registerRequest.username(), authData.username());
        assertTrue(!authData.authToken().isEmpty()); // non empty string
    }

    @Test
    void registerUserTaken() throws Exception { // why are these throwing exceptions
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        userService.register(registerRequest);
        assertThrows(Exception.class, () -> userService.register(registerRequest));
    }

    @Test
    void loginSuccessful() throws Exception {
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        var loginRequest = new LoginRequest("joe", "password");
        userService.register(registerRequest);
        var authData = userService.login(loginRequest);
        assertNotNull(authData);
        assertEquals(loginRequest.username(), authData.username());
        assertTrue(!authData.authToken().isEmpty()); // non empty string
    }

    @Test
    void loginNoExistingUser() throws Exception {
        var loginRequest = new LoginRequest("joe", "password");
        assertThrows(Exception.class, () -> userService.login(loginRequest));
    }

    @Test
    void loginWrongPassword() throws Exception {
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        userService.register(registerRequest);
        var loginRequest = new LoginRequest("joe", "BADpassword");
        assertThrows(Exception.class, () -> userService.login(loginRequest));
    }

    @Test
    void logoutSuccessful() throws Exception {
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        var authData = userService.register(registerRequest);
        var loginRequest = new LoginRequest("joe", "password");
        userService.login(loginRequest);
        var logoutRequest = new LogoutRequest(authData.authToken());
        assertDoesNotThrow(() -> userService.logout(logoutRequest));
    }

    @Test
    void logoutBadAuthToken() throws Exception {
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        userService.register(registerRequest);
        var loginRequest = new LoginRequest("joe", "password");
        userService.login(loginRequest);
        String badAuthToken = "bad";
        var logoutRequest = new LogoutRequest(badAuthToken);
        assertThrows(Exception.class, () -> userService.logout(logoutRequest));
    }

    @Test
    void clear() throws Exception {
        var gameService = new GameService(db);
        // Create UserData and AuthData
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        AuthData authData = userService.register(registerRequest);
        // Create GameData
        var createGameRequest = new CreateGameRequest("testGame");
        gameService.createGame(createGameRequest, authData.authToken());

        userService.clear();
        AuthData newAuthData = assertDoesNotThrow(() -> userService.register(registerRequest)); // no user or auth in order for this to work
        assertTrue(gameService.listGames(new ListGamesRequest(newAuthData.authToken())).isEmpty());
    }
}