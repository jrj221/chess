package serverfacade;

import com.google.gson.Gson;
import datamodel.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;

public class ServerFacade {
    Integer port;
    String authToken = null;

    public ServerFacade(Integer port) {
        this.port = port;
    }


    public String getAuthToken() {
        return authToken;
    }


    public void setAuthToken(String string) { authToken = string; } // only used for testing


    public void register(String[] input_words) throws Exception {
        if (input_words.length != 4) {
            System.out.println("Registering requires 3 arguments: USERNAME, EMAIL, and PASSWORD");
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        // a lot of similar stuff with login, should it be refactored somehow?
        var username = input_words[1];
        var email = input_words[2];
        var password = input_words[3];
        var body = Map.of("username", username, "email", email, "password", password);
        var jsonBody = new Gson().toJson(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/user")) // we need to grab the port being used
                .header("Content-Type", "application.json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200:
                var responseJson = new Gson().fromJson(response.body(), RegisterResponse.class);
                System.out.println("Account successfully registered. You are now logged in.");
                authToken = responseJson.authToken();
                return;
            case 400:
                // will never happen since I've already ensured that all fields are given
                System.out.println("You are missing one of three fields: USERNAME, EMAIL, or PASSWORD. " +
                        "Please ensure you include all necessariy fields.");
                return;
            case 403:
                System.out.println("Username already taken, try a different one");
                return;
            case 500:
                System.out.println("Internal error, please try again"); // SQL errors?
        }
    }


    public void login(String[] input_words) throws Exception {
        if (input_words.length != 3) {
            System.out.println("logging in requires 2 arguments: USERNAME and PASSWORD");
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        var username = input_words[1];
        var password = input_words[2];
        var body = Map.of("username", username, "password", password);
        var jsonBody = new Gson().toJson(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/session")) // we need to grab the port being used
                .header("Content-Type", "application.json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200: {
                var responseJson = new Gson().fromJson(response.body(), LoginResponse.class); // refer to GPT if the type casting is being weird
                System.out.println("Successfully logged in");
                authToken = responseJson.authToken();
                return;
            } case 400: {
                // will never happen since I've already ensured that all fields are given
                System.out.println("You are missing one of two fields: USERNAME or PASSWORD. " +
                        "Please ensure you include all necessariy fields.");
                return;
            } case 401: {
                System.out.println("Incorrect username or password");
                return;
            } case 500: {
                System.out.println("Internal error, please try again"); // SQL errors?
            }
        }
    }


    public void logout() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/session"))
                .DELETE()
                .header("authorization", authToken)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200:  {
                authToken = null;
                System.out.println("Successfully logged out");
                return;
            } case 400: {
                // will never happen since I've already ensured that all fields are given
                System.out.println("authToken cannot be null");
                return;
            } case 401: {
                // shouldn't ever happen since how woudl you even get a bad authToken?
                System.out.println("bad authToken");
                return;
            } case 500: {
                System.out.println("Internal error, please try again"); // SQL errors?
                // return basically
            }
        }
    }


    public void create(String[] input_words) throws Exception {
        if (input_words.length != 2) {
            System.out.println("Creating a game requires 1 argument: GAME_NAME");
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        var gameName = input_words[1];
        var body = Map.of("gameName", gameName);
        var jsonBody = new Gson().toJson(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/game")) // we need to grab the port being used
                .header("Content-Type", "application.json")
                .header("authorization", authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200: {
                var responseJson = new Gson().fromJson(response.body(), CreateGameResponse.class);
                var gameID = responseJson.gameID();
                System.out.printf("Game %s succesfully created. Use game ID %d to join or observe it\n",
                        gameName, gameID);
                return;
            } case 400: {
                // will never happen since I've already ensured that all fields are given
                System.out.println("authToken cannot be null");
                return;
            } case 401: {
                // shouldn't ever happen since how would you even get a bad authToken?
                System.out.println("bad authToken");
                return;
            } case 500: {
                System.out.println("Internal error, please try again"); // SQL errors?
                // return basically
            }
        }
    }


    public void list() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI("http://localhost:" + port + "/game"))
            .header("authorization", authToken)
            .GET()
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200: {
                var responseJson = new Gson().fromJson(response.body(), ListGamesResponse.class);
                ArrayList<GameData> games = responseJson.games();
                if (games.isEmpty()) {
                    System.out.println("No games yet. Create one using \"create <GAME_NAME>\"");
                }
                for (int i = 0; i < games.size(); i++) {
                    var game = games.get(i);
                    System.out.printf("""
                            %d.
                            \tGame Name: %s
                            \tGame ID: %d
                            \tWhite Player: %s
                            \tBlack Player: %s
                            """, i+1, game.gameName(), game.gameID(),
                            game.whiteUsername() == null ? "No player" : game.whiteUsername(),
                            game.blackUsername() == null ? "No player" : game.blackUsername());
                }
                return;
            } case 401: {
                // shouldn't ever happen since how would you even get a bad authToken?
                System.out.println("bad authToken");
                return;
            } case 500: {
                System.out.println("Internal error, please try again"); // SQL errors?
                // return basically
            }
        }
    }


    public void join(String[] input_words) throws Exception {
        if (input_words.length != 3) {
            System.out.println("Creating a game requires 2 arguments: GAME_ID and TEAM_COLOR");
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        var gameID = input_words[1];
        var playerColor = input_words[2];
        var body = Map.of("gameID", gameID, "playerColor", playerColor);
        var jsonBody = new Gson().toJson(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/game"))
                .header("Content-Type", "application.json")
                .header("authorization", authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200: {
                System.out.printf("Successfully joined team %s in game %s!\n", playerColor, gameID);
                return;
            } case 400: {
                // will never happen since I've already ensured that all fields are given
                System.out.println("authToken cannot be null");
                return;
            } case 401: {
                // shouldn't ever happen since how would you even get a bad authToken?
                System.out.println("bad authToken");
                return;
            } case 403: {
                System.out.printf("Team %s is not available. Please choose a different team.\n", playerColor);
                return;
            } case 500: {
                System.out.println("Internal error, please try again"); // SQL errors?
                // return basically
            }
        }
    }


    public void clear() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/db"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        setAuthToken(null);
    }
}
