import client.*;
import client.PostLoginClient;

import java.util.Scanner;

public class Repl {

    public static void main(String[] args) {
        var result = "";
        Client client = new PreLoginClient();

        System.out.println("♕ 240 Chess client.Client ♕");
        while (!result.equals("quit")) {
            Scanner scanner = new Scanner(System.in);
            client.printPrompt();
            String command = scanner.nextLine(); // READ
            try {
                result = client.eval(command); // EVAL
                var parts = result.split(" ");
                if (!result.equals("quit")) {System.out.println(result);} // PRINT
                if (result.equals("Account successfully registered. You are now logged in.")
                    || result.equals("Login successful!") || result.equals("Successfully left the game!")) {
                    client = new PostLoginClient();
                } else if (result.equals("Logout successful!") || result.equals("Database cleared")) {
                    client = new PreLoginClient();
                } else if (parts.length > 1 && parts[1].equals("joined")) {
                    client = new GameplayClient(parts[6], parts[3], "player");
                } else if (parts.length > 1 && parts[1].equals("observing")) {
                    client = new GameplayClient(parts[4], "WHITE", "observer");
                }

            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
        } // LOOP
    }
}
