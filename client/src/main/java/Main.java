import chess.*;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("♕ 240 Chess Client ♕");
        var state = "LOGGED_OUT";
        Scanner scanner = new Scanner(System.in);
        String auth = null;
        while (true) {
            System.out.printf("[%s] >>> ", state);
            var input_words = scanner.nextLine().split(" ");
            switch (input_words[0]) {
                case "clear": { // FOR TESTING ONLY, REMOVE BEFORE COMPLETION
                    clear();
                    break;
                } case "help": {
                    System.out.println("""
                            \tregister <USERNAME> <EMAIL> <PASSWORD> | Register a new user
                            \tlogin <USERNAME> <PASSWORD> | Login to an existing user
                            \tquit | Quit the chess program
                            \thelp | See possible commands""");
                    break;
                } case "quit": {
                    return;
                } case "login": {
                    login(input_words);
                    break;
                } case "register": {
                    register(input_words);
                    break;
                }
            }
        }
    }

    public static void clear() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/db"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }

    public static void login(String[] input_words) throws Exception {
        if (input_words.length != 4) {
            System.out.println("logging in requires 3 arguments: USERNAME, EMAIL, and PASSWORD");
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        var username = input_words[1];
        var email = input_words[2];
        var password = input_words[3];
        var body = Map.of("username", username, "email", email, "password", password);
        var jsonBody = new Gson().toJson(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/session")) // we need to grab the port being used
                .header("Content-Type", "application.json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
    }

    public static void register(String[] input_words) throws Exception {
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
                .uri(new URI("http://localhost:8080/user")) // we need to grab the port being used
                .header("Content-Type", "application.json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        switch (response.statusCode()) {
            case 200:
                System.out.println("Account successfully registered. Login to access more utilities.");
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
                // return basically
        }
    }
}