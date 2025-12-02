package client;

import com.google.gson.Gson;
import datamodel.BadRequestException;
import datamodel.GameData;
import datamodel.NoExistingGameException;
import serverfacade.ServerFacade;
import static ui.EscapeSequences.*;
import java.util.ArrayList;
import java.util.Map;

public class PostLoginClient implements Client {

    public void printPrompt() {
        System.out.print(SET_TEXT_COLOR_GREEN + "[LOGGED_IN] " +
                RESET_TEXT_COLOR + ">>> ");
    }

    static ServerFacade facade = new ServerFacade(8080);


    public String eval(String command) throws Exception {
        var inputWords = command.split(" ");
        return switch (inputWords[0].toLowerCase()) {
            case "help", "h" -> help();
            case "clear" -> clear();
            case "quit", "exit", "q" -> quit();
            case "logout" -> logout();
            case "create" -> create(inputWords);
            case "list" -> list();
            case "play", "join" -> play(inputWords);
            case "observe" -> observe(inputWords);
            default -> String.format("%s is not a valid command", inputWords[0]);
        };
    }


    private String create(String[] inputWords) throws Exception {
        if (inputWords.length == 2) {
            var body = Map.of("gameName", inputWords[1]);
            var jsonBody = new Gson().toJson(body);
            facade.create(jsonBody); // returns a string on success
            return String.format("Game %s successfully created!", inputWords[1]);
        }
        throw new Exception("Invalid command.\nCreating a game takes the form \"create GAME_NAME\"");
    }

    // make a map in client that maps game indices to their actual gameIDs

    private String list() throws Exception {
        ArrayList<GameData> games = facade.list();
        StringBuilder list = new StringBuilder(); // like a string-list hybrid
        for (int i = 0; i < games.size(); i++) {
            var game = games.get(i);
            list.append(String.format("""
                    %d.
                    \tGame Name: %s
                    \tWhite Player: %s
                    \tBlack Player: %s
                    """, i+1, game.gameName(),
                    game.whiteUsername() == null ? "No player" : game.whiteUsername(),
                    game.blackUsername() == null ? "No player" : game.blackUsername())
            );
        }
        return list.toString();
    }


    private String play(String[] inputWords) throws Exception {
        if (inputWords.length == 3) {
            var gameIndex = inputWords[1];
            if (Character.isLetter(gameIndex.charAt(0))) {
                return "Invalid game index provided. Please use numerals (e.g. \"1\" instead of \"one\")";
            }
            // is this the best practice?
            ArrayList<GameData> games = facade.list();
            var playerColor = inputWords[2].toUpperCase();
            if (Integer.parseInt(gameIndex) > games.size()) {
                throw new NoExistingGameException("No existing game");
            }
            if (Integer.parseInt(gameIndex) < 0) {
                throw new BadRequestException("No existing game");
            }
            var gameID = games.get(Integer.parseInt(gameIndex) - 1).gameID();
            var body = Map.of(
                    "gameID", gameID,
                    "playerColor", playerColor);
            var jsonBody = new Gson().toJson(body);
            facade.play(jsonBody);
            return String.format("Successfully joined team %s in game %s!\n", playerColor, gameID);
        }
        throw new Exception("Invalid command.\nJoining a game takes the form \"play|join GAME_ID, PLAYER_COLOR\"");
    }


    private String observe(String[] inputWords) throws Exception {
        if (inputWords.length == 2) {
            if (Character.isLetter(inputWords[1].charAt(0))) {
                return "Invalid game index provided. Please use numerals (e.g. \"1\" instead of \"one\"";
            };
            var gameIndex = Integer.parseInt(inputWords[1]);
            ArrayList<GameData> games = facade.list();
            if (gameIndex > games.size()) {
                throw new NoExistingGameException("No existing game");
            }
            if (gameIndex < 0) {
                throw new BadRequestException("No existing game");
            }
            return String.format("Successfully observing in game %s!\n", inputWords[1]);
        }
        throw new Exception("Invalid command.\nObserving a game takes the form \"observe GAME_ID\"");
    }


    private String logout() throws Exception {
        facade.logout();
        return "Logout successful!";
    }


    private String clear() throws Exception {
        facade.clear();
        return "Database cleared";
    }


    private String help() {
        return """
                \tcreate <GAME_NAME> | Create a new game
                \tlist | List existing games
                \tplay|join <ID> <WHITE|BLACK> | Join a game
                \tobserve <ID> | Observe a game
                \tlogout | Logout when you are done
                \tquit|q|exit | Quit the chess program
                \thelp|h | See possible commands""";
    }


    private String quit() throws Exception {
        facade.logout();
        return "quit"; // to exit repl loop
    }

}
