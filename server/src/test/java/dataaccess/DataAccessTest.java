package dataaccess;

import datamodel.*;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    @Test
    void getUser() {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "joe@email.com", "password");
        db.createUser(user);
        assertEquals(user, db.getUser("joe"));
    }

    @Test
    void createUser() {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "joe@email.com", "password");
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }

    @Test
    void createAuth() {
        DataAccess db = new MemoryDataAccess();
        String username = "joe";
        AuthData authData = db.createAuth(username);
        assertEquals(username, authData.username());
        assertNotNull(authData.authToken());
    }

    @Test
    void getAuth() {
        DataAccess db = new MemoryDataAccess();
        String username = "joe";
        AuthData authData = db.createAuth(username);
        assertEquals(authData, db.getAuth(authData.authToken()));
    }

    @Test
    void deleteAuth() {
        DataAccess db = new MemoryDataAccess();
        String username = "joe";
        AuthData authData = db.createAuth(username);
        db.deleteAuth(authData.authToken());
        assertNull(db.getAuth(authData.authToken()));
    }

    @Test
    void createGameData() {
        //Implement
    }


    @Test
    void clear() {
        DataAccess db = new MemoryDataAccess();
        db.createUser(new UserData("joe", "joe@email.com", "password"));
        db.clear();
        assertNull(db.getUser("joe"));
    }
}