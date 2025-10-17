package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void registerHappy() throws Exception{
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "joe@email.com", "password");
        var userService = new UserService(db);
        var authData = userService.register(user);
        assertNotNull(authData);
        assertEquals(user.username(), authData.username());
        assertTrue(!authData.authToken().isEmpty()); // non empty string
    }

    @Test
    void clear() {
    }
}