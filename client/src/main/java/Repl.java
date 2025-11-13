import client.*;
import client.PostLoginClient;

import java.util.Scanner;

public class Repl {

    public static void main(String[] args) {
        var command = "";
        Client client = new PreLoginClient();

        System.out.println("♕ 240 Chess client.Client ♕");
        while (!command.equals("quit")) {
            Scanner scanner = new Scanner(System.in);
            client.printPrompt();
            command = scanner.nextLine(); // READ
            try {
                String result = client.eval(command); // EVAL
                System.out.println(result); // PRINT
                if (result.equals("Account successfully registered. You are now logged in.")
                    || result.equals("Login successful!")) {
                    client = new PostLoginClient();
                } else if (result.equals("Logout successful!")) {
                    client = new PreLoginClient();
                }

            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
        } // LOOP
    }
}
