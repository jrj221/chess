package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.SQLException;
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
                    var found_username = rs.getString("username");
                    var found_email = rs.getString("email");
                    var found_password = rs.getString("password");
                    return new UserData(found_username, found_email, found_password);
                }
            }
            throw new DataAccessException("User not found");
        } catch (SQLException ex) {
            throw new SQLException("SQL Exception");
        }
    }

    @Override
    public void createUser(UserData user) throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO users (username, email, password) " +
                    "VALUES (?, ?, ?)");
            statement.setString(1, user.username());
            statement.setString(2, user.email());
            String hashedPassword = generateHashedPassword(user.password());
            statement.setString(3, hashedPassword);
            statement.executeUpdate();
        } catch (SQLException ex) { // how can i differentiate this from any other SQLException?
            throw new DataAccessException("Username taken");
        } catch (Exception ex) {
            throw new SQLException("SQL Exception");
        }
    }

    @Override
    public String generateHashedPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public void clear() {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("DROP TABLE auth, games, users");
            statement.executeUpdate();
        } catch (Exception ex) {
            //
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
        } catch (SQLException ex) {
            throw new SQLException("SQL Exception");
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
        } catch (SQLException ex) {
            throw new SQLException("SQL Exception");
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
            throw new DataAccessException("invalid authToken");
            // wouldn't happen since it's handled by service, but I need a fail test
        } catch (SQLException ex) {
            throw new SQLException("SQL Exception");
        }
    }

    @Override
    public int createGameData(String gameName) throws Exception {
        try (var connection = DatabaseManager.getConnection()) {
            if (gameName == null) {
                throw new DataAccessException("gameName cannot be null");
            }
            var statement = connection.prepareStatement("INSERT INTO games " +
                    "(gameID, gameName) " +
                    "VALUES (?,?)");
            int gameID = 1;
            while (true) {
                if (getGame(gameID) != null) { // need to implement getGame()
                    gameID++;
                }
                else {
                    break; // usable gameID
                }
            }
            statement.setInt(1, gameID);
            statement.setString(2, gameName);
            statement.executeUpdate();
            return gameID;
        } catch (SQLException ex) {
            throw new SQLException("SQL Exception");
        }
    }

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
        } catch (SQLException ex) {
            throw new SQLException("SQL Exception");
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
        } catch (SQLException ex) {
            throw new SQLException("SQL Exception");
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
                throw new DataAccessException("invalid playerColor");
            }
        } catch (DataAccessException ex) {
            throw new DataAccessException("invalid GameID");
        } catch (SQLException ex) {
            throw new SQLException("SQL Exception");
        }
    }
}
