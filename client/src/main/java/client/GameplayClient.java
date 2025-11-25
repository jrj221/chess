package client;

import serverfacade.ServerFacade;
import ui.EscapeSequences;

public class GameplayClient implements Client{

    static ServerFacade facade = new ServerFacade(8080);

    public void printPrompt() {
        System.out.print(EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[IN_GAME] " +
                EscapeSequences.RESET_TEXT_COLOR + ">>> ");
    }


    public String eval(String command) throws Exception {
        var inputWords = command.toLowerCase().split(" ");
        return switch (inputWords[0]) {
            case "clear" -> clear(); // FOR TESTING ONLY, REMOVE BEFORE COMPLETION
            case "help", "h" -> help();
            case "quit", "q", "exit" -> "quit"; // best way to quit?
            default -> String.format("%s is not a valid command", inputWords[0]);
        };
    }

    private String clear() throws Exception {
        facade.clear();
        return "Database cleared";
    }

    private String help() {
        return """
            \tredraw | Redraw the chess board
            \tleave | Leave the game
            \tmove <START POS> <END POS> | Make a move
            \tresign | Forfeit the game
            \thighlight <POS> | Highlight possible moves for the piece at the given position
            \tquit|q|exit | Quit the chess program
            \thelp|h | See possible commands""";
    }
}
