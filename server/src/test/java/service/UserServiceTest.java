package service;

import dataaccess.*;
import datamodel.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void registerSuccessful() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        var userService = new UserService(db);
        var authData = userService.register(registerRequest);
        assertNotNull(authData);
        assertEquals(registerRequest.username(), authData.username());
        assertTrue(!authData.authToken().isEmpty()); // non empty string
    }

    @Test
    void registerUserTaken() throws Exception { // why are these throwing exceptions
        DataAccess db = new MemoryDataAccess();
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        var userService = new UserService(db);
        userService.register(registerRequest);
        assertThrows(Exception.class, () -> userService.register(registerRequest));
    }

    @Test
    void loginSuccessful() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
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
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var loginRequest = new LoginRequest("joe", "password");
        assertThrows(Exception.class, () -> userService.login(loginRequest));
    }

    @Test
    void loginWrongPassword() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        userService.register(registerRequest);
        var loginRequest = new LoginRequest("joe", "BADpassword");
        assertThrows(Exception.class, () -> userService.login(loginRequest));
    }

    @Test
    void logoutSuccessful() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        var authData = userService.register(registerRequest);
        var loginRequest = new LoginRequest("joe", "password");
        userService.login(loginRequest);
        var logoutRequest = new LogoutRequest(authData.authToken());
        assertDoesNotThrow(() -> userService.logout(logoutRequest));
    }

    @Test
    void logoutBadAuthToken() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var registerRequest = new RegisterRequest("joe", "joe@email.com", "password");
        userService.register(registerRequest);
        var loginRequest = new LoginRequest("joe", "password");
        userService.login(loginRequest);
        String badAuthToken = "bad";
        var logoutRequest = new LogoutRequest(badAuthToken);
        assertThrows(Exception.class, () -> userService.logout(logoutRequest));
    }


    @Test
    void clear() {
    }
}