package dataaccess;

import datamodel.*;

import java.util.HashMap;
import java.util.UUID;
// if a DataAccess method fails, it should throw a DataAccessException

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();

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
        return new AuthData(username, generateAuthToken());
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
