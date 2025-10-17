package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.LoginRequest;
import datamodel.RegisterRequest;
import datamodel.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void registerSuccessful() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var user = new RegisterRequest("joe", "joe@email.com", "password");
        var userService = new UserService(db);
        var authData = userService.register(user);
        assertNotNull(authData);
        assertEquals(user.username(), authData.username());
        assertTrue(!authData.authToken().isEmpty()); // non empty string
    }

    @Test
    void registerUserTaken() throws Exception { // why are these throwing exceptions
        DataAccess db = new MemoryDataAccess();
        var user = new RegisterRequest("joe", "joe@email.com", "password");
        var userService = new UserService(db);
        userService.register(user);
        assertThrows(Exception.class, () -> userService.register(user));
    }

    @Test
    void loginSuccessful() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var registerUser = new RegisterRequest("joe", "joe@email.com", "password");
        var loginUser = new LoginRequest("joe", "password");
        userService.register(registerUser);
        var authData = userService.login(loginUser);
        assertNotNull(authData);
        assertEquals(loginUser.username(), authData.username());
        assertTrue(!authData.authToken().isEmpty()); // non empty string
    }

    @Test
    void loginNoExistingUser() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var loginUser = new LoginRequest("joe", "password");
        assertThrows(Exception.class, () -> userService.login(loginUser));
    }

    @Test
    void loginWrongPassword() throws Exception {
        DataAccess db = new MemoryDataAccess();
        var userService = new UserService(db);
        var loginUser = new LoginRequest("joe", "BADpassword");
        var registerUser = new RegisterRequest("joe", "joe@email.com", "password");
        userService.register(registerUser);
        assertThrows(Exception.class, () -> userService.login(loginUser));
    }

    @Test
    void clear() {
    }
}