package dataaccess;

import datamodel.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
// if a DataAccess method fails, it should throw a DataAccessException

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, AuthData> auth = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap<>();

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createUser(UserData user) {
        var hashedPass = generateHashedPassword(user.password());
        var hashedUser = new UserData(user.username(), user.email(), hashedPass);
        users.put(user.username(), hashedUser);
    }

    @Override
    public void clear() {
        users.clear();
        auth.clear();
        games.clear();
    }

    @Override
    public AuthData createAuth(String username) {
        String authToken = generateAuthToken();
        var authData = new AuthData(username, authToken);
        auth.put(authToken, authData);
        return authData;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return auth.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        auth.remove(authToken);
    }

    @Override
    public int createGameData(String gameName) {
        int gameID = 1;
        while (true) {
            if (games.get(gameID) != null) {
                gameID++;
            }
            else {
                break; // usable gameID
            }
        }
        games.put(gameID, new GameData(gameID, null, null, gameName, null));
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public ArrayList<GameData> getAllGames() {
        var allGames = new ArrayList<GameData>();
        var keys = games.keySet();
        for (var key : keys) {
            allGames.add(games.get(key));
        }
        return allGames;
    }

    @Override
    public void joinGame(String username, int gameID, String playerColor) {
        var gameData = games.get(gameID);
        if (playerColor.equals("WHITE")) {
            gameData = new GameData(gameID, username, gameData.blackUsername(), gameData.gameName(), gameData.game());
        }
        else {
            gameData = new GameData(gameID, gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
        }
        games.put(gameID, gameData);
    }

    @Override
    public String generateHashedPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
