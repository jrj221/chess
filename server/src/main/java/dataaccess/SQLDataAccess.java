package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.UUID;

public class SQLDataAccess implements DataAccess {

    @Override
    public UserData getUser(String username) {
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
            return null; //no user found
        } catch (Exception ex) {
            return null; //what do I do here??
        }
    }

    @Override
    public void createUser(UserData user) {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO users (username, email, password) " +
                    "VALUES (?, ?, ?)");
            statement.setString(1, user.username());
            statement.setString(2, user.email());
            statement.setString(3, user.password());
            statement.executeUpdate();
        } catch (Exception ex) {
            //
        }
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
    public AuthData createAuth(String username) {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO auth (username, authToken) VALUES (?, ?)");
            var authData = new AuthData(username, generateAuthToken());
            statement.setString(1, authData.username());
            statement.setString(2, authData.authToken());
            statement.executeUpdate();
            return authData;
        } catch (Exception ex) {
            //
        }
        return null; // what do here outside of logic loop
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public AuthData getAuth(String authToken) {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("SELECT * from auth");
            var rs = statement.executeQuery();
            while (rs.next()) {
                if (authToken.equals(rs.getString("authToken"))) {
                    var username = rs.getString("username");
                    return new AuthData(username, authToken);
                }
            }
        } catch (Exception ex) {
            //
        }
        return null; // no auth found
    }

    @Override
    public void deleteAuth(String authToken) {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = connection.prepareStatement("DELETE FROM auth WHERE authToken = ?");
            statement.setString(1, authToken);
            statement.executeUpdate();
        } catch (Exception ex) {
            //
        }
    }

    @Override
    public int createGameData(String gameName) {
        try (var connection = DatabaseManager.getConnection()) {
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
        } catch (Exception ex) {
            //
        }
        return -1; // idk error
    }

    @Override
    public GameData getGame(int gameID) {
        return null;
    }

    @Override
    public ArrayList<GameData> getAllGames() {
        return null;
    }

    @Override
    public void joinGame(String username, int gameID, String playerColor) {

    }
}
