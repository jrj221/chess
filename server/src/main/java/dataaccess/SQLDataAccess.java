package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;

import java.sql.Connection;
import java.util.ArrayList;

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

    }

    @Override
    public void clear() {

    }

    @Override
    public AuthData createAuth(String username) {
        return null;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) {

    }

    @Override
    public int createGameData(String gameName) {
        return 0;
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
