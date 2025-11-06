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
        while (true) {
            System.out.printf("[%s] >>> ", state);
            var input_words = scanner.nextLine().split(" ");
            switch (input_words[0]) {
                case "help":
                    System.out.println("\tregister <USERNAME> <EMAIL> <PASSWORD> | Register a new user\n" +
                            "\tlogin <USERNAME> <PASSWORD> | Login to an existing user\n" +
                            "\tquit | Quit the chess program\n" +
                            "\thelp | See possible commands");
                    break;
                case "quit":
                    return;
                case "login":
                    if (input_words.length != 4) {
                        System.out.println("login requires 3 arguments\n\tUSERNAME\n\tEMAIL\n\tPASSWORD");
                        break;
                    }
                    var username = input_words[1];
                    var email = input_words[2];
                    var password = input_words[3];
                    var body = Map.of("username", username, "email", email, "password", password);
                    var jsonBody = new Gson().toJson(body);
                    try (HttpClient client = HttpClient.newHttpClient()) {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(new URI("http://localhost:8080/session"))
                                .header("Content-Type", "application.json")
                                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                                .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        System.out.println(response.body());
                    }
                    break;
                case "register":
                    break;
            }
        }
    }
}