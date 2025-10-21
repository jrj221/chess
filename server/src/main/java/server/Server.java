package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;

import java.util.ArrayList;
import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;

    public Server() {
        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", ctx -> register(ctx));
        server.post("session", ctx -> login(ctx));
        server.delete("session", ctx -> logout(ctx));
        server.get("game", ctx -> listGames(ctx));
        server.post("game", ctx -> createGame(ctx));
        // Register your endpoints and exception handlers here.


    }
    // start with the simplest method (functions) and later on rework into
    // classes or interfaces to see if it needs to change
    private void register(Context ctx)  {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, RegisterRequest.class);
            var authData = userService.register(user);
            ctx.result(serializer.toJson(authData)); // response
        } catch (Exception ex) {
            // when userService throws an exception>
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).result(message);
        }
    }

    private void login(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, LoginRequest.class);
            var authData = userService.login(user);
            ctx.result(serializer.toJson(authData));
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        }
    }

    private void logout(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var logoutRequest = serializer.fromJson(requestJson, LogoutRequest.class);
            userService.logout(logoutRequest);
            ctx.result();
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        }
    }

    private void listGames(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var listGamesRequest = serializer.fromJson(requestJson, ListGamesRequest.class);
            ArrayList<GameData> allGames = userService.listGames(listGamesRequest);
            ctx.result(serializer.toJson(allGames));
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        }
    }

    private void createGame(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var createGameRequest = serializer.fromJson(requestJson, CreateGameRequest.class);
            int gameID = userService.createGame(createGameRequest);
            ctx.result(serializer.toJson(gameID));
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        }
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
