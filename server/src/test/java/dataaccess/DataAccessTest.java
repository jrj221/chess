package dataaccess;

import datamodel.UserData;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

class DataAccessTest {

    @Test
    void getUser() {
    }

    @Test
    void createUser() {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "joe@email.com", "password");
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }

    @Test
    void clear() {
        DataAccess db = new MemoryDataAccess();
        db.createUser(new UserData("joe", "joe@email.com", "password"));
        db.clear();
        assertNull(db.getUser("joe"));
    }
}