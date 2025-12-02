package server;

import com.google.gson.Gson;
import dataaccess.*;
import datamodel.*;
import io.javalin.http.Context;
import service.GameService;
import service.UserService;

import java.util.ArrayList;
import java.util.Map;

public class HttpHandler {
    private final UserService userService;
    private final GameService gameService;

    public HttpHandler(DataAccess dataAccess) {
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
    }

    public void register(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, RegisterRequest.class);
            var authData = userService.register(user);
            ctx.result(serializer.toJson(authData));
        } catch (AlreadyTakenException ex) {
            // when userService throws an exception
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).result(message);
        } catch (BadRequestException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    public void login(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, LoginRequest.class);
            var authData = userService.login(user);
            ctx.result(serializer.toJson(authData));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (BadRequestException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    public void logout(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var logoutRequest = new LogoutRequest(ctx.header("authorization"));
            userService.logout(logoutRequest);
            ctx.result();
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (BadRequestException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    public void listGames(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var listGamesRequest = new ListGamesRequest(ctx.header("authorization"));
            ArrayList<GameData> allGames = gameService.listGames(listGamesRequest);
            ctx.result(serializer.toJson(Map.of("games", allGames)));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (BadRequestException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    public void createGame(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var createGameRequest = serializer.fromJson(requestJson, CreateGameRequest.class);
            int gameID = gameService.createGame(createGameRequest, ctx.header("authorization"));
            ctx.result(serializer.toJson(Map.of("gameID", gameID)));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (BadRequestException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    public void joinGame(Context ctx) {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var joinGameRequest = serializer.fromJson(requestJson, JoinGameRequest.class);
            gameService.joinGame(joinGameRequest, ctx.header("authorization"));
        } catch (UnauthorizedException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).result(message);
        } catch (BadRequestException | NoExistingGameException ex) { // bad request
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).result(message);
        } catch (AlreadyTakenException ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).result(message);
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
        ctx.result();
    }

    public void clear(Context ctx) {
        try {
            userService.clear();
            ctx.result();
        } catch (Exception ex) {
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }
}
