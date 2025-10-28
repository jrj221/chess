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
    public UserData getUser(String username) throws Exception{
        var user = users.get(username);
        if (user == null) {
            throw new DataAccessException("User not found");
        } else {
            return user;
        }
    }

    @Override
    public void createUser(UserData user) throws Exception {
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
    public AuthData createAuth(String username) throws Exception {
        String authToken = generateAuthToken();
        var authData = new AuthData(username, authToken);
        auth.put(authToken, authData);
        return authData;
    }

    @Override
    public AuthData getAuth(String authToken)  throws Exception{
        var found_auth = auth.get(authToken);
        if (found_auth == null) {
            throw new DataAccessException("Auth not found");
        } else {
            return found_auth;
        }
    }

    @Override
    public void deleteAuth(String authToken) throws Exception {
        auth.remove(authToken);
    }

    @Override
    public int createGameData(String gameName) throws Exception {
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
    public GameData getGame(int gameID) throws Exception {
        var game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        } else {
            return game;
        }
    }

    @Override
    public ArrayList<GameData> getAllGames() throws Exception {
        var allGames = new ArrayList<GameData>();
        var keys = games.keySet();
        for (var key : keys) {
            allGames.add(games.get(key));
        }
        return allGames;
    }

    @Override
    public void joinGame(String username, int gameID, String playerColor) throws Exception {
        var gameData = games.get(gameID);
        if (playerColor.equals("WHITE")) {
            gameData = new GameData(gameID, username, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else if (playerColor.equals("BLACK")) {
            gameData = new GameData(gameID, gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
        } else {
            throw new DataAccessException("invalid playerColor");
        }
        games.put(gameID, gameData);
    }

    @Override
    public String generateHashedPassword(String password) throws Exception {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
