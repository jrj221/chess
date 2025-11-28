package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.UUID;

public class SQLDataAccess implements DataAccess {

    @Override
    public UserData getUser(String username) throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM users");
            var rs = statement.executeQuery();
            while (rs.next()) {
                if (rs.getString("username").equals(username)) {
                    var foundUsername = rs.getString("username");
                    var foundEmail = rs.getString("email");
                    var foundPassword = rs.getString("password");
                    return new UserData(foundUsername, foundEmail, foundPassword);
                }
            }
            throw new DataAccessException("User not found");
        }
    }

    @Override
    public void createUser(UserData user) throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            try {
                getUser(user.username()); // if user doesn't exist, it throws, and we catch and resume below
                throw new DataAccessException("Username taken");
            } catch (DataAccessException ex) {
                    if (ex.getMessage().equals("Username taken")) { // distinguishes logic flow
                        throw new DataAccessException("Username taken");
                    }
                    var statement = connection.prepareStatement("INSERT INTO users " +
                            "(username, email, password) " +
                            "VALUES (?, ?, ?)");
                    statement.setString(1, user.username());
                    statement.setString(2, user.email());
                    String hashedPassword = generateHashedPassword(user.password());
                    statement.setString(3, hashedPassword);
                    statement.executeUpdate();
            }
        }
    }

    @Override
    public String generateHashedPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public Integer countGames() throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM games");
            var rs = statement.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
            }
            return count;
        }
    }

    @Override
    public void clear() throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("DROP TABLE auth, games, users");
            statement.executeUpdate();
        }
    }

    @Override
    public AuthData createAuth(String username) throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO auth (username, authToken) VALUES (?, ?)");
            var authData = new AuthData(username, generateAuthToken());
            if (authData.username() == null) {
                throw new DataAccessException("You can't provide a null username");
                // this will never happen because my service checks against this, but I needed a fail test
            }
            statement.setString(1, authData.username());
            statement.setString(2, authData.authToken());
            statement.executeUpdate();
            return authData;
        }
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public AuthData getAuth(String authToken) throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("SELECT * from auth");
            var rs = statement.executeQuery();
            while (rs.next()) {
                if (authToken.equals(rs.getString("authToken"))) {
                    var username = rs.getString("username");
                    return new AuthData(username, authToken);
                }
            }
            throw new DataAccessException("Auth not found");
        }
    }

    @Override
    public void deleteAuth(String authToken) throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            getAuth(authToken);
            var statement = connection.prepareStatement("DELETE FROM auth WHERE authToken = ?");
            statement.setString(1, authToken);
            statement.executeUpdate();
        } catch (DataAccessException ex) {
            throw new DataAccessException("Invalid authToken");
            // wouldn't happen since it's handled by service, but I need a fail test
        }
    }

    @Override
    public int createGameData(String gameName) throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            if (gameName == null) {
                throw new DataAccessException("gameName cannot be null");
            }
            var statement = connection.prepareStatement("INSERT INTO games " +
                    "(gameID, gameName, game) " +
                    "VALUES (?,?, ?)");
            int gameID = countGames() + 1;
            statement.setInt(1, gameID);
            statement.setString(2, gameName);
            var game = new Gson().toJson(new ChessGame()); // default chess board? if it works
            statement.setString(3, game);
            statement.executeUpdate();
            return gameID;
        }
    }

//    public void updateGame() throws Exception {
//        try (var connection = DatabaseManager.getConnection()) {
//            //
//        }
//    }

    @Override
    public GameData getGame(int gameID) throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("SELECT * from games");
            var rs = statement.executeQuery();
            while (rs.next()) {
                if (rs.getInt("gameID") == gameID) {
                    var whiteUsername = rs.getString("whiteUsername");
                    var blackUsername = rs.getString("blackUsername");
                    var gameName = rs.getString("gameName");
                    var gameJson = rs.getString("game");
                    var serializer = new Gson();
                    var game = serializer.fromJson(gameJson, ChessGame.class);
                    return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
                }
            }
            throw new DataAccessException("Game not found");
        }
    }

    @Override
    public ArrayList<GameData> getAllGames() throws Exception {
        var allGames = new ArrayList<GameData>();
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("SELECT * from games");
            var rs = statement.executeQuery();
            while (rs.next()) {
                var gameID = rs.getInt("gameID");
                var whiteUsername = rs.getString("whiteUsername");
                var blackUsername = rs.getString("blackUsername");
                var gameName = rs.getString("gameName");
                var gameJson = rs.getString("game");
                var serializer = new Gson();
                var game = serializer.fromJson(gameJson, ChessGame.class);
                var gameData = new GameData(gameID, whiteUsername, blackUsername, gameName, game);
                allGames.add(gameData);
            }
            return allGames;
        }
    }

    @Override
    public void joinGame(String username, int gameID, String playerColor) throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            getGame(gameID); // throws DataAccessException if game not found
            if (playerColor.equals("WHITE")) {
                var statement = connection.prepareStatement("UPDATE games SET whiteUsername = ? WHERE gameID = ?");
                statement.setString(1, username);
                statement.setInt(2, gameID);
                statement.executeUpdate();
            } else if (playerColor.equals("BLACK")) {
                var statement = connection.prepareStatement("UPDATE games SET blackUsername = ? WHERE gameID = ?");
                statement.setString(1, username);
                statement.setInt(2, gameID);
                statement.executeUpdate();
            } else {
                throw new DataAccessException("Invalid playerColor");
            }
        } catch (DataAccessException ex) {
            if (ex.getMessage().equals("Invalid playerColor")) {
                throw new DataAccessException("Invalid playerColor");
            }
            throw new DataAccessException("Invalid GameID");
        }
    }
}
