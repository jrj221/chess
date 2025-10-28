package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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

    @Test
    void createAuth() {
        DataAccess db = new SQLDataAccess();
        String username = "joe";
        AuthData authData = db.createAuth(username);
        assertEquals(username, authData.username());
        assertNotNull(authData.authToken());
    }

    @Test
    void getAuth() {
        DataAccess db = new SQLDataAccess();
        String username = "joe";
        AuthData authData = db.createAuth(username);
        assertEquals(authData, db.getAuth(authData.authToken()));
    }

    @Test
    void deleteAuth() {
        DataAccess db = new SQLDataAccess();
        String username = "joe";
        AuthData authData = db.createAuth(username);
        db.deleteAuth(authData.authToken());
        assertNull(db.getAuth(authData.authToken()));
    }

    @Test
    void getGame() {
        DataAccess db = new SQLDataAccess();
        var game1ID = db.createGameData("game1");
        assertNotNull(db.getGame(game1ID));
        assertEquals("game1", db.getGame(game1ID).gameName());
    }

    @Test
    void getAllGames() {
        DataAccess db = new SQLDataAccess();
        var game1 = db.createGameData("game1");
        var game2 = db.createGameData("game2");
        var game3 = db.createGameData("game3");
        var myGames = new ArrayList<GameData>();
        myGames.add(db.getGame(game1));
        myGames.add(db.getGame(game2));
        myGames.add(db.getGame(game3));
        ArrayList<GameData> allGames = db.getAllGames();
        assertEquals(myGames, allGames);
    }

    @Test
    void createGameData() {
        DataAccess db = new SQLDataAccess();
        var myGameID = db.createGameData("myGame");
        assertEquals("myGame", db.getGame(myGameID).gameName());
        assertNull(db.getGame(myGameID).whiteUsername());
        assertNull(db.getGame(myGameID).blackUsername());
        assertNull(db.getGame(myGameID).game());
    }
}
