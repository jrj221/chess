package service;

import java.util.Objects;
import java.util.UUID;
import dataaccess.*;
import datamodel.*; // AuthData and UserData and the like
import io.javalin.http.UnauthorizedResponse;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) { // constructor
        this.dataAccess = dataAccess;
        //var authData = new AuthData(user.username(), generateAuthToken());
    }

    public AuthData register(RegisterRequest user) throws Exception {
        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("Already exists");
        }
        dataAccess.createUser(new UserData(user.username(), user.email(), user.password()));
        return dataAccess.createAuth(user.username());
    }

    public AuthData login(LoginRequest user) throws Exception {
        UserData userData = dataAccess.getUser(user.username());
        if (userData == null) {
            throw new Exception("Unauthorized Login"); // no existing user
        }
        if (!Objects.equals(user.password(), userData.password())) { // bad match
            throw new Exception("Unauthorized Login"); // wrong password
        }
        return dataAccess.createAuth(user.username());
    }


}
