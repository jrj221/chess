package client;

import com.google.gson.Gson;

import java.util.*;

import datamodel.RegisterResponse;
import serverfacade.ServerFacade;


public class PreLoginClient implements Client {

    public void printPrompt() {
        System.out.print("[LOGGED_OUT] >>> ");
    }

    static ServerFacade facade = new ServerFacade(8080);

    public String eval(String command)  throws Exception {
        var inputWords = command.toLowerCase().split(" ");
        return switch (inputWords[0]) {
            case "clear" -> clear(); // FOR TESTING ONLY, REMOVE BEFORE COMPLETION
            case "help" -> help();
            case "quit" -> "quit"; // best way to quit?
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
                \tquit | Quit the chess program
                \thelp | See possible commands""";
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
        throw new Exception("You are missing one of three fields: " +
                "USERNAME, EMAIL, or PASSWORD. " +
                "Please ensure you include all necessariy fields.");
    }

    private String login(String[] inputWords) throws Exception {
        if (inputWords.length == 3) {
            facade.login(inputWords);
            return "Login successful!";
        }
        throw new Exception("You are missing one of two fields: USERNAME or PASSWORD. " +
                "Please ensure you include all necessariy fields.");
    }

}