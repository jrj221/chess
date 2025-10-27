package dataaccess;

import datamodel.UserData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SQLDataAccessTest {

    @Test
    void getUser() {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("joe", "joe@email.com", "password");
        db.createUser(user);
        assertEquals(user, db.getUser("joe"));
    }

    @Test
    void createUser() {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("joe", "joe@email.com", "password");
        db.createUser(user);
        assertEquals(user, db.getUser(user.username()));
    }
}
