package dataaccess;

import datamodel.*;

public interface DataAccess {
    UserData getUser(String username);
    void createUser(UserData user);
    void clear();
}
