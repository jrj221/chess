package service;

import dataaccess.*;
import datamodel.*; // AuthData and UserData and the like
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) { // constructor
        this.dataAccess = dataAccess;
    }

    public AuthData register(RegisterRequest registerRequest) throws Exception {
        try {
            if (registerRequest.username() == null || registerRequest.email() == null|| registerRequest.password() == null) {
                throw new BadRequestException("Bad request");
            }
            dataAccess.createUser(new UserData(registerRequest.username(), registerRequest.email(), registerRequest.password()));
            return dataAccess.createAuth(registerRequest.username());
        } catch (DataAccessException ex) {
            if (ex.getMessage().equals("Username taken")) {
                throw new AlreadyTakenException("Already exists");
            } else {
                throw new Exception("Internal Error");
            }
        }

    }

    public AuthData login(LoginRequest loginRequest) throws Exception {
        try {
            if (loginRequest.username() == null || loginRequest.password() == null) {
                throw new BadRequestException("Bad request");
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
                throw new BadRequestException("Bad request");
            }
            dataAccess.getAuth(logoutRequest.authToken()); // throws error if bad authToken
            dataAccess.deleteAuth(logoutRequest.authToken());
        } catch (DataAccessException ex) {
            throw new UnauthorizedException("Unauthorized Logout");
        }
    }

    public void clear() throws Exception {
        dataAccess.clear();
    }
}
