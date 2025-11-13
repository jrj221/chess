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


    public void observe(String jsonBody) throws Exception {
        // next phase we will change this to actually grab a specific game
        if (jsonBody.length() == 2) {
            throw new BadRequestException("Bad request");
            // this will get refined, for now this simulates not having a gameID
        }
        return;
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
    public String display(String team) {
        var board = new String[10][10];
        var pieceMap = initBoard(); // key: 0:0, value: [black, whitePawn] // initializes the map

        // Build intial board
        pieceMap.forEach((position, piece) -> {
            var i = Integer.parseInt(position.split(":")[0]);
            var j = Integer.parseInt(position.split(":")[1]);
            board[i][j] = piece.get(0) + piece.get(1);
        });

        // Display board (convert to string)
        var boardString = new StringBuilder();
        if (team.equals("WHITE")) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    boardString.append(board[i][j]);
                }
                boardString.append(EscapeSequences.RESET_BG_COLOR + "\n");
            }
        } else {
            for (int i = 9; i > -1 ; i--) {
                for (int j = 9; j > -1; j--) {
                    boardString.append(board[i][j]);
                }
                boardString.append(EscapeSequences.RESET_BG_COLOR + "\n");
            }
        }
        return boardString.toString();
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
