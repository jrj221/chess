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
                if (!result.equals("quit")) {System.out.println(result);} // PRINT
                if (result.equals("Account successfully registered. You are now logged in.")
                    || result.equals("Login successful!")) {
                    client = new PostLoginClient();
                } else if (result.equals("Logout successful!") || result.equals("Database cleared")) {
                    client = new PreLoginClient();
                } else if (result.split(" ").length > 1 && (result.split(" ")[1].equals("observing") ||
                        result.split(" ")[1].equals("joined"))) {
                    client = new GameplayClient(result.split(" ")[3]);
                }

            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
        } // LOOP
    }
}
