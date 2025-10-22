package service;

import java.util.ArrayList;
import java.util.Objects;

import dataaccess.*;
import datamodel.*; // AuthData and UserData and the like


public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) { // constructor
        this.dataAccess = dataAccess;
    }

    public AuthData register(RegisterRequest registerRequest) throws Exception {
        if (registerRequest.username() == null || registerRequest.email() == null|| registerRequest.password() == null) {
            throw new Exception("Bad request");
        }
        if (dataAccess.getUser(registerRequest.username()) != null) {
            throw new AlreadyTakenException("Already exists");
        }
        dataAccess.createUser(new UserData(registerRequest.username(), registerRequest.email(), registerRequest.password()));
        return dataAccess.createAuth(registerRequest.username());
    }

    public AuthData login(LoginRequest loginRequest) throws Exception {
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw new Exception("Bad request");
        }
        UserData userData = dataAccess.getUser(loginRequest.username());
        if (userData == null) {
            throw new UnauthorizedException("Unauthorized Login"); // no existing user
        }
        if (!Objects.equals(loginRequest.password(), userData.password())) { // bad match
            throw new UnauthorizedException("Unauthorized Login"); // wrong password
        }
        return dataAccess.createAuth(loginRequest.username());
    }

    public void logout(LogoutRequest logoutRequest) throws Exception {
        if (logoutRequest.authToken() == null) {
            throw new Exception("Bad request");
        }
        var authData = dataAccess.getAuth(logoutRequest.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Unauthorized Logout");
        }
        dataAccess.deleteAuth(logoutRequest.authToken());
    }

    public void clear() {
        dataAccess.clear();
    }
}
