package dataaccess;

import datamodel.*;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MemoryDataAccessTest {

    @Test
    void createAndGetUser() {
        DataAccess db = new MemoryDataAccess();
        var user = new UserData("joe", "joe@email.com", "password");
        db.createUser(user);
        var foundUser = db.getUser(user.username());
        assertEquals(user.email(), foundUser.email());
        assertTrue(BCrypt.checkpw(user.password(), foundUser.password()));
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
        DataAccess db = new MemoryDataAccess();
        var myGameID = db.createGameData("myGame");
        assertEquals("myGame", db.getGame(myGameID).gameName());
        assertNull(db.getGame(myGameID).whiteUsername());
        assertNull(db.getGame(myGameID).blackUsername());
        assertNull(db.getGame(myGameID).game());
    }

    @Test
    void getGame() {
        DataAccess db = new MemoryDataAccess();
        var game1ID = db.createGameData("game1");
        assertNotNull(db.getGame(game1ID));
        assertEquals("game1", db.getGame(game1ID).gameName());
    }

    @Test
    void getAllGames() {
        DataAccess db = new MemoryDataAccess();
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
    void joinGame() {
        DataAccess db = new MemoryDataAccess();
        var game1ID = db.createGameData("game1");
        db.joinGame("joe", game1ID, "WHITE");
        var gameData = db.getGame(game1ID);
        assertEquals("joe", gameData.whiteUsername());
    }

    @Test
    void clear() {
        DataAccess db = new MemoryDataAccess();
        db.createUser(new UserData("joe", "joe@email.com", "password"));
        AuthData authData = db.createAuth("joe");
        int myGameID = db.createGameData("myGame");
        db.clear();
        assertNull(db.getUser("joe"));
        assertNull(db.getAuth(authData.authToken()));
        assertNull(db.getGame(myGameID));
    }
}