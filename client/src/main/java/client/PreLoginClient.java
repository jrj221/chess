package client;

import com.google.gson.Gson;

import java.util.*;

import ui.EscapeSequences;
import serverfacade.ServerFacade;


public class PreLoginClient implements Client {

    static ServerFacade facade = new ServerFacade(8080);


    public void printPrompt() {
        System.out.print(EscapeSequences.SET_TEXT_COLOR_YELLOW + "[LOGGED_OUT] " +
                EscapeSequences.RESET_TEXT_COLOR + ">>> ");
    }


    public String eval(String command)  throws Exception {
        var inputWords = command.toLowerCase().split(" ");
        return switch (inputWords[0]) {
            case "clear" -> clear(); // FOR TESTING ONLY, REMOVE BEFORE COMPLETION
            case "help", "h" -> help();
            case "quit", "q", "exit" -> "quit"; // best way to quit?
            case "register" -> register(inputWords);
            case "login" -> login(inputWords);
            default -> String.format("%s is not a valid command", inputWords[0]);
        };
    }

    private String clear() throws Exception {
        facade.clear();
        return "Database cleared";
    }

    private String help() {
        return """
                \tregister <USERNAME> <EMAIL> <PASSWORD> | Register a new user
                \tlogin <USERNAME> <PASSWORD> | Login to an existing user
                \tquit|q|exit | Quit the chess program
                \thelp|h | See possible commands""";
    }

    private String register(String[] inputWords) throws Exception {
        if (inputWords.length == 4) {
            var body = Map.of(
                    "username", inputWords[1],
                    "email", inputWords[2],
                    "password", inputWords[3]);
            var jsonBody = new Gson().toJson(body);

            facade.register(jsonBody); // returns a string on success
            return "Account successfully registered. You are now logged in.";
        }
        throw new Exception("Invalid command.\nRegistration takes the form \"register USERNAME, EMAIL, PASSWORD\"");
    }

    private String login(String[] inputWords) throws Exception {
        if (inputWords.length == 3) {
            var body = Map.of("username", inputWords[1],
                            "password", inputWords[2]);
            var jsonBody = new Gson().toJson(body);
            facade.login(jsonBody);
            return "Login successful!";
        }
        throw new Exception("Invalid command.\nLogging in takes the form \"login USERNAME, PASSWORD\"");
    }

}