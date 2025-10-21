package dataaccess;

import datamodel.*;

public interface DataAccess {
    UserData getUser(String username);
    void createUser(UserData user);
    void clear();
    AuthData createAuth(String username);
    AuthData getAuth(String authToken);
    void deleteAuth(String authToken);
    int createGameData(String gameName);
}
