package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SQLDataAccessTest {

    @BeforeEach
    void clearAll() {
        DataAccess db = new SQLDataAccess();
        db.clear();
    }

    @Test
    void createAndGetUserSuccessful() throws Exception{ // not sure how you would test separately
        DataAccess db = new SQLDataAccess();
        var user = new UserData("joe", "joe@email.com", "password");
        db.createUser(user);
        var foundUser = db.getUser(user.username());
        assertEquals(user.email(), foundUser.email());
        assertTrue(BCrypt.checkpw(user.password(), foundUser.password()));
    }

    @Test
    void createUsernameTaken() throws Exception {
        DataAccess db = new SQLDataAccess();
        var user = new UserData("joe", "joe@email.com", "password");
        db.createUser(user);
        var newUser = new UserData("joe", "joe@email.com", "password");
        assertThrows(DataAccessException.class, () -> db.createUser(newUser));
    }

    @Test
    void getUserUserNotFound() throws Exception {
        DataAccess db = new SQLDataAccess();
        assertThrows(DataAccessException.class, () -> db.getUser("joe"));
    }

    @Test
    void createAuth() throws Exception {
        DataAccess db = new SQLDataAccess();
        String username = "joe";
        AuthData authData = db.createAuth(username);
        assertEquals(username, authData.username());
        assertNotNull(authData.authToken());
    }

    @Test
    void createAuthNullUsername() throws Exception {
        DataAccess db = new SQLDataAccess();
        assertThrows(DataAccessException.class, () -> db.createAuth(null));
    }

    @Test
    void getAuth() throws Exception {
        DataAccess db = new SQLDataAccess();
        String username = "joe";
        AuthData authData = db.createAuth(username);
        assertEquals(authData, db.getAuth(authData.authToken()));
    }

    @Test
    void getAuthAuthNotFound() {
        DataAccess db = new SQLDataAccess();
        assertThrows(DataAccessException.class, () -> db.getAuth("nonsenseToken"));
    }

    @Test
    void deleteAuth() throws Exception {
        DataAccess db = new SQLDataAccess();
        String username = "joe";
        AuthData authData = db.createAuth(username);
        db.deleteAuth(authData.authToken());
        assertNull(db.getAuth(authData.authToken()));
    }

    @Test
    void deleteAuthBadAuthToken() throws Exception {
        DataAccess db = new SQLDataAccess();
        assertThrows(DataAccessException.class, () -> db.deleteAuth("badToken"));
    }

    @Test
    void getGame() throws Exception {
        DataAccess db = new SQLDataAccess();
        var game1ID = db.createGameData("game1");
        assertNotNull(db.getGame(game1ID));
        assertEquals("game1", db.getGame(game1ID).gameName());
    }

    @Test
    void getGameGameNotFoudn() throws Exception {
        DataAccess db = new SQLDataAccess();
        assertThrows(DataAccessException.class, () -> db.getGame(1));
    }

    @Test
    void getAllGames() throws Exception {
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
    void joinGame() throws Exception {
        DataAccess db = new SQLDataAccess();
        var game1ID = db.createGameData("game1");
        db.joinGame("joe", game1ID, "WHITE");
        var gameData = db.getGame(game1ID);
        assertEquals("joe", gameData.whiteUsername());
    }

    @Test
    void createGameData() throws Exception {
        DataAccess db = new SQLDataAccess();
        var myGameID = db.createGameData("myGame");
        assertEquals("myGame", db.getGame(myGameID).gameName());
        assertNull(db.getGame(myGameID).whiteUsername());
        assertNull(db.getGame(myGameID).blackUsername());
        assertNull(db.getGame(myGameID).game());
    }

    @Test
    void createGameDataNullGameName() throws Exception {
        DataAccess db = new SQLDataAccess();
        assertThrows(DataAccessException.class, () -> db.createGameData(null));
    }
}
