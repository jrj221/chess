import chess.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("♕ 240 Chess Client ♕");
        var state = "LOGGED_OUT";
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.printf("[%s] >>> ", state);
            var input = scanner.nextLine();
            switch (input) {
                case "help":
                    System.out.println("\tregister <USERNAME> <EMAIL> <PASSWORD> | Register a new user\n" +
                            "\tlogin <USERNAME> <PASSWORD> | Login to an existing user\n" +
                            "\tquit | Quit the chess program\n" +
                            "\thelp | See possible commands");
                    break;
                case "quit":
                    return;
                case "login":
                    break;
                case "register":
                    break;
            }
        }
    }
}