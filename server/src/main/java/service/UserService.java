package service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

import dataaccess.*;
import datamodel.*; // AuthData and UserData and the like
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;


public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) { // constructor
        this.dataAccess = dataAccess;
    }

    public AuthData register(RegisterRequest registerRequest) throws Exception {
        try {
            if (registerRequest.username() == null || registerRequest.email() == null|| registerRequest.password() == null) {
                throw new Exception("Bad request");
            }
            dataAccess.createUser(new UserData(registerRequest.username(), registerRequest.email(), registerRequest.password()));
            return dataAccess.createAuth(registerRequest.username());
        } catch (DataAccessException ex) {
            throw new AlreadyTakenException("Already exists");
        }

    }

    public AuthData login(LoginRequest loginRequest) throws Exception {
        try {
            if (loginRequest.username() == null || loginRequest.password() == null) {
                throw new Exception("Bad request");
            }
            UserData userData = dataAccess.getUser(loginRequest.username());
            if (!BCrypt.checkpw(loginRequest.password(), userData.password())) { // bad match
                throw new UnauthorizedException("Unauthorized Login"); // wrong password
            }
            return dataAccess.createAuth(loginRequest.username());
        }
        catch (DataAccessException ex) {
            throw new UnauthorizedException("Unauthorized Login"); // no existing user
        }
    }

    public void logout(LogoutRequest logoutRequest) throws Exception {
        try {
            if (logoutRequest.authToken() == null) {
                throw new Exception("Bad request");
            }
            var authData = dataAccess.getAuth(logoutRequest.authToken());
            dataAccess.deleteAuth(logoutRequest.authToken());
        } catch (DataAccessException ex) {
            throw new UnauthorizedException("Unauthorized Logout");
        }
    }

    public void clear() {
        dataAccess.clear();
    }
}
