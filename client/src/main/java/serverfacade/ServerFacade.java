package serverfacade;

import com.google.gson.Gson;
import datamodel.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerFacade {
    Integer port;
    static String authToken = ""; // static shares it across instances so switching clients doesn't affect it

    public ServerFacade(Integer port) {
        this.port = port;
    }


    public String getAuthToken() {
        return authToken;
    }



    public void setAuthToken(String string) { authToken = string; } // only used for testing


    public void register(String jsonBody) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        // a lot of similar stuff with login, should it be refactored somehow?
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/user")) // we need to grab the port being used
                .header("Content-Type", "application.json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200:
                var responseJson = new Gson().fromJson(response.body(), RegisterResponse.class);
                authToken = responseJson.authToken();
                return;
            case 400:
                throw new BadRequestException("You are missing one of three fields: " +
                        "USERNAME, EMAIL, or PASSWORD. " +
                        "Please ensure you include all necessariy fields.");
            case 403:
                throw new AlreadyTakenException("Username already taken, try a different one");
            case 500:
                throw new Exception("Internal error, please try again"); // SQL errors?
        }
    }


    public void login(String jsonBody) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/session")) // we need to grab the port being used
                .header("Content-Type", "application.json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200: {
                var responseJson = new Gson().fromJson(response.body(), LoginResponse.class); // refer to GPT if the type casting is being weird
                authToken = responseJson.authToken();
                return;
            } case 400: {
                throw new BadRequestException("You are missing one of two fields: USERNAME or PASSWORD. " +
                        "Please ensure you include all necessary fields.");
            } case 401: {
                throw new UnauthorizedException("Incorrect username or password");
            } case 500: {
                throw new Exception("Internal error, please try again"); // SQL errors?
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
                authToken = "";
                return;
            } case 400: {
                throw new BadRequestException("Bad request");
            } case 401: {
                throw new UnauthorizedException("LOGOUT Unauthorized");
            } case 500: {
                throw new Exception("Internal error, please try again"); // SQL errors?
            }
        }
    }


    public void create(String jsonBody) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/game")) // we need to grab the port being used
                .header("Content-Type", "application.json")
                .header("authorization", authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200: {
                return;
            } case 400: {
                throw new BadRequestException("Bad request");
            } case 401: {
                throw new UnauthorizedException("CREATE Unauthorized");
            } case 500: {
                throw new Exception("Internal error, please try again"); // SQL errors?
            }
        }
    }


    public ArrayList<GameData> list() throws Exception {
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
                    throw new Exception("No games yet. Create one using \"create <GAME_NAME>\"");
                }
                return games;
            } case 401: {
                throw new UnauthorizedException("LIST Unauthorized");
            } case 500: {
                throw new Exception("Internal error, please try again"); // SQL errors?
            }
        }
        return null; // Intellij wanted a return statement
    }


    public void play(String jsonBody) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/game"))
                .header("Content-Type", "application.json")
                .header("authorization", authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200: {
                return;
            } case 400: {
                var responseJson = new Gson().fromJson(response.body(), ErrorResponse.class);
                if (responseJson.message().equals("Error: No existing game")) {
                    throw new NoExistingGameException("No existing game");
                }
                throw new BadRequestException("Bad request");
            } case 401: {
                throw new UnauthorizedException("PLAY Unauthorized");
            } case 403: {
                throw new AlreadyTakenException("Team is not available. Please choose a different team.");
            } case 500: {
                throw new Exception("Internal error, please try again"); // SQL errors?
            }
        }
    }


    public void clear() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/db"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        setAuthToken("");
    }
}
