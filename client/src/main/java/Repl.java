import java.util.Scanner;

public class Repl {

    public static void main(String[] args) {
        var command = "";
        var client = new PreLoginClient();

        System.out.println("♕ 240 Chess Client ♕");
        while (!command.equals("quit")) {
            Scanner scanner = new Scanner(System.in);
            client.printPrompt();
            command = scanner.nextLine(); // READ
            try {
                String result = client.eval(command); // EVAL
                System.out.println(result); // PRINT

            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
        } // LOOP
    }
}
