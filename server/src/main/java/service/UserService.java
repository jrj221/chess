package service;

import dataaccess.*;
import datamodel.*; // AuthData and UserData and the like
import io.javalin.http.UnauthorizedResponse;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) { // constructor
        this.dataAccess = dataAccess;
        //var authData = new AuthData(user.username(), generateAuthToken());
    }

    public AuthData register(UserData user) throws Exception {
        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("Already exists");
        }
        dataAccess.createUser(user);
        return new AuthData(user.username(), generateAuthToken());
    }

    private String generateAuthToken() {
        return "xyz"; //hardcoded for now
    }


}
