package serverfacade;

import com.google.gson.Gson;
import datamodel.*;
import ui.EscapeSequences;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    Integer port;
    String authToken;

    public ServerFacade(Integer port) {
        this.port = port;
    }


    public String getAuthToken() {
        return authToken;
    }


    public void setAuthToken(String string) { authToken = string; } // only used for testing


    public void register(String[] inputWords) throws Exception {
        if (inputWords.length != 4) {
            System.out.println("Registering requires 3 arguments: USERNAME, EMAIL, and PASSWORD");
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        // a lot of similar stuff with login, should it be refactored somehow?
        var username = inputWords[1];
        var email = inputWords[2];
        var password = inputWords[3];
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


    public void login(String[] inputWords) throws Exception {
        if (inputWords.length != 3) {
            System.out.println("logging in requires 2 arguments: USERNAME and PASSWORD");
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        var username = inputWords[1];
        var password = inputWords[2];
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
                authToken = "";
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


    public void create(String[] inputWords) throws Exception {
        if (inputWords.length != 2) {
            System.out.println("Creating a game requires 1 argument: GAME_NAME");
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        var gameName = inputWords[1];
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
                            \tWhite Player: %s
                            \tBlack Player: %s
                            """, i+1, game.gameName(),
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


    public void join(String[] inputWords) throws Exception {
        if (inputWords.length != 3) {
            System.out.println("Joining a game requires 2 arguments: GAME_ID and TEAM_COLOR");
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        var gameID = inputWords[1];
        var playerColor = inputWords[2];
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
                display(playerColor);
                return;
            } case 400: {
                var responseJson = new Gson().fromJson(response.body(), ErrorResponse.class);
                if (responseJson.message().equals("Error: No existing game")) {
                    System.out.println("No existing game.");
                    return;
                }
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


    public void observe(String[] inputWords) throws Exception {
        if (inputWords.length != 2) {
            System.out.println("Observing a game requires 1 argument: GAME_ID");
            return;
        }
        // next phase we will change this to actually grab a specific game
        display("WHITE");
    }


    private static HashMap<String, List<String>> initBoard() {
        String[] horizRow = {"   ", " h ", " g ", " f ", " e ", " d ", " c ", " b ", " a ", "   "};
        String[] vertRow = {"   ", " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ", "   "};
        String[][] royalRowWhite = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♖ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♘ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♗ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♕ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♔ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♗ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♘ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♖ "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] pawnRowWhite = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] royalRowBlack = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♜ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♞ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♝ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♛ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♚ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♝ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♞ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♜ "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] pawnRowBlack = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] emptyWhiteFirstRow = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] emptyBlackFirstRow = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        var pieceMap = new HashMap<String, List<String>>();
        for (int j = 0; j < 10; j++) {
            pieceMap.put(String.format("0:%d", 9-j), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, horizRow[j])); // top row
            pieceMap.put(String.format("1:%d", 9-j), List.of(royalRowBlack[j][0], royalRowBlack[j][1]));
            pieceMap.put(String.format("2:%d", 9-j), List.of(pawnRowBlack[j][0], pawnRowBlack[j][1]));
            pieceMap.put(String.format("3:%d", 9-j), List.of(emptyWhiteFirstRow[j][0], emptyWhiteFirstRow[j][1]));
            pieceMap.put(String.format("4:%d", 9-j), List.of(emptyBlackFirstRow[j][0], emptyBlackFirstRow[j][1]));
            pieceMap.put(String.format("5:%d", 9-j), List.of(emptyWhiteFirstRow[j][0], emptyWhiteFirstRow[j][1]));
            pieceMap.put(String.format("6:%d", 9-j), List.of(emptyBlackFirstRow[j][0], emptyBlackFirstRow[j][1]));
            pieceMap.put(String.format("7:%d", 9-j), List.of(pawnRowWhite[j][0], pawnRowWhite[j][1]));
            pieceMap.put(String.format("8:%d", 9-j), List.of(royalRowWhite[j][0], royalRowWhite[j][1]));
            pieceMap.put(String.format("9:%d", 9-j), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, horizRow[j]));
        }

        for (int i = 0; i < 10; i++) {
            pieceMap.put(String.format("%d:0", 9-i), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, vertRow[i]));
            pieceMap.put(String.format("%d:9", 9-i), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, vertRow[i]));
        }
        return pieceMap;
    }


    /// Displays the chess board (orientation based on team)
    private static void display(String team) {
        var board = new String[10][10];
        var pieceMap = initBoard(); // key: 0:0, value: [black, whitePawn] // initializes the map

        // Build intial board
        pieceMap.forEach((position, piece) -> {
            var i = Integer.parseInt(position.split(":")[0]);
            var j = Integer.parseInt(position.split(":")[1]);
            board[i][j] = piece.get(0) + piece.get(1);
        });

        // Display board
        if (team.equals("WHITE")) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    System.out.print(board[i][j]);
                }
                System.out.print(EscapeSequences.RESET_BG_COLOR + "\n");
            }
        } else {
            for (int i = 9; i > -1 ; i--) {
                for (int j = 9; j > -1; j--) {
                    System.out.print(board[i][j]);
                }
                System.out.print(EscapeSequences.RESET_BG_COLOR + "\n");
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
        setAuthToken("");
    }
}
