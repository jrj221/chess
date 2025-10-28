package server;

import com.google.gson.Gson;
import dataaccess.*;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.post("user", ctx -> register(ctx));
        server.post("session", ctx -> login(ctx));
        server.delete("session", ctx -> logout(ctx));
        server.get("game", ctx -> listGames(ctx));
        server.post("game", ctx -> createGame(ctx));
        server.put("game", ctx -> joinGame(ctx));
        server.delete("db", ctx -> clear(ctx));
        // Register your endpoints and exception handlers here.


    }
    // start with the simplest method (functions) and later on rework into
    // classes or interfaces to see if it needs to change
    private void register(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, RegisterRequest.class);
            var authData = userService.register(user);
            ctx.result(serializer.toJson(authData)); // response
        } catch (AlreadyTakenException ex) {
            // when userService throws an exception>
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).result(message);
        } catch (SQLException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        } catch (Exception ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        }
    }

    private void login(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, LoginRequest.class);
            var authData = userService.login(user);
            ctx.result(serializer.toJson(authData));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (Exception ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        }
    }

    private void logout(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var logoutRequest = new LogoutRequest(ctx.header("authorization"));
            userService.logout(logoutRequest);
            ctx.result();
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (Exception ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        }
    }

    private void listGames(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var listGamesRequest = new ListGamesRequest(ctx.header("authorization"));
            ArrayList<GameData> allGames = gameService.listGames(listGamesRequest);
            ctx.result(serializer.toJson(Map.of("games", allGames)));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (Exception ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        }
    }

    private void createGame(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var createGameRequest = serializer.fromJson(requestJson, CreateGameRequest.class);
            int gameID = gameService.createGame(createGameRequest, ctx.header("authorization"));
            ctx.result(serializer.toJson(Map.of("gameID", gameID)));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (Exception ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        }
    }

    private void joinGame(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var joinGameRequest = serializer.fromJson(requestJson, JoinGameRequest.class);
            gameService.joinGame(joinGameRequest, ctx.header("authorization"));
        } catch (NoExistingGameException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        } catch (AlreadyTakenException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).result(message);
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (Exception ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        }
        ctx.result();
    }

    private void clear(Context ctx) {
        userService.clear();
        ctx.result();
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
