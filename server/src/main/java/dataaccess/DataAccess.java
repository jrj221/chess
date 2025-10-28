package dataaccess;

import datamodel.*;

import java.util.ArrayList;

public interface DataAccess {
    UserData getUser(String username) throws Exception;
    void createUser(UserData user) throws Exception;
    void clear();
    AuthData createAuth(String username) throws Exception;
    AuthData getAuth(String authToken) throws Exception;
    void deleteAuth(String authToken) throws Exception;
    int createGameData(String gameName) throws Exception;
    GameData getGame(int gameID) throws Exception;
    ArrayList<GameData> getAllGames() throws Exception;
    void joinGame(String username, int gameID, String playerColor) throws Exception;
    String generateHashedPassword(String password) throws Exception;
}
