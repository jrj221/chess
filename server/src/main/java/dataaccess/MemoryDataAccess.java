package dataaccess;

import chess.ChessGame;
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
        try {
            if (getUser(user.username()) != null) {
                throw new DataAccessException("Username already taken");
            }
        } catch (DataAccessException ex) {
            if (ex.getMessage().equals("User not found")) {
                var hashedPass = generateHashedPassword(user.password());
                var hashedUser = new UserData(user.username(), user.email(), hashedPass);
                users.put(user.username(), hashedUser);
            }
            else if (ex.getMessage().equals("Username already taken")) {
                throw new DataAccessException("Username already taken");
            }
        }
    }

    @Override
    public void clear() throws Exception {
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
        var foundAuth = auth.get(authToken);
        if (foundAuth == null) {
            throw new DataAccessException("Auth not found");
        } else {
            return foundAuth;
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
        games.put(gameID, new GameData(gameID, null, null, gameName,
                null));
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
        if (gameData == null) {
            throw new NoExistingGameException("Invalid gameID");
        }
        if (playerColor.equals("WHITE")) {
            if (games.get(gameID).whiteUsername() != null) {
                throw new AlreadyTakenException("Team taken");
            }
            gameData = new GameData(gameID, username, gameData.blackUsername(),
                    gameData.gameName(), gameData.game());
        } else if (playerColor.equals("BLACK")) {
            if (games.get(gameID).blackUsername() != null) {
                throw new AlreadyTakenException("Team taken");
            }
            gameData = new GameData(gameID, gameData.whiteUsername(), username,
                    gameData.gameName(), gameData.game());
        } else {
            throw new DataAccessException("Invalid playerColor");
        }
        games.put(gameID, gameData);
    }

    @Override
    public String generateHashedPassword(String password) throws Exception {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public Integer countGames() throws Exception {
        return games.size();
    }

    @Override
    public void updateGame(int gameID, ChessGame game) throws Exception {
        var gameData = games.get(gameID);
        gameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), game);
        games.put(gameID, gameData);
    }


    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
