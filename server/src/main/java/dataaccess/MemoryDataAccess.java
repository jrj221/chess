package dataaccess;

import datamodel.*;

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
        users.put(user.username(), user);
    }

    @Override
    public void clear() {
        users.clear();
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
        int gameID = 124;
        games.put(gameID, new GameData(gameID, null, null, gameName, null));
        return gameID;
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

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
