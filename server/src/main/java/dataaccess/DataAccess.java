package dataaccess;

import datamodel.*;

import java.util.ArrayList;

public interface DataAccess {
    UserData getUser(String username);
    void createUser(UserData user);
    void clear();
    AuthData createAuth(String username);
    AuthData getAuth(String authToken);
    void deleteAuth(String authToken);
    int createGameData(String gameName);
    GameData getGame(int gameID);
    ArrayList<GameData> getAllGames();
    void joinGame(String username, int gameID, String playerColor);
}
