import chess.*;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Array;
import java.util.*;

import datamodel.*;
import serverfacade.ServerFacade;
import ui.EscapeSequences;


public class Main {

    static ServerFacade facade = new ServerFacade(8080);

    public static void main(String[] args) throws Exception {
        System.out.println("♕ 240 Chess Client ♕");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            var state = facade.getAuthToken() == null ? "LOGGED_OUT" : "LOGGED_IN";
            System.out.printf("[%s] >>> ", state);
            var input_words = scanner.nextLine().split(" ");
            switch (input_words[0].toLowerCase()) {
                case "test": {
                    display();
                    break;
                }
                case "clear": { // FOR TESTING ONLY, REMOVE BEFORE COMPLETION
                    facade.clear();
                    break;
                } case "help": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("""
                                \tregister <USERNAME> <EMAIL> <PASSWORD> | Register a new user
                                \tlogin <USERNAME> <PASSWORD> | Login to an existing user
                                \tquit | Quit the chess program
                                \thelp | See possible commands""");
                    } else {
                        System.out.println("""
                                \tcreate <GAME_NAME> | Create a new game
                                \tlist | List existing games
                                \tjoin <ID> <WHITE|BLACK> | Join a game
                                \tobserve <ID> | Observe a game
                                \tlogout | Logout when you are done
                                \tquit | Quit the chess program
                                \thelp | See possible commands""");
                    }
                    break;
                } case "quit": {
                    return;
                } case "login": {
                    facade.login(input_words);
                    //PROBLEM: if you quit while logged in, you will be required to log in again next time, but the authdata isn't deleted
                    break;
                } case "register": {
                    facade.register(input_words);
                    break;
                } case "logout": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("Logout utility not available while logged out");
                        break;
                    }
                    facade.logout();
                    break;
                } case "create": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("Create game utility not available while logged out");
                        break;
                    }
                    facade.create(input_words);
                    break;
                } case "list": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("List games utility not available while logged out");
                        break;
                    }
                    facade.list();
                    break;
                } case "join": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("Join game utility not available while logged out");
                        break;
                    }
                    facade.join(input_words);
                    break;
                }
            }
        }
    }

    public static void display() {
        // idea for phase 6: make a map where each key has a value that is a color and piece.
        // that way you just update the piece part and update the board
        var board = new String[10][10];
        var pieceMap = new HashMap<String, List<String>>(); // key: 0:0, value: [black, whitePawn]
        String[] horizRow = {"   ", " a ", " b ", " c ", " d ", " e ", " f ", " g ", " h ", "   "};
        String[] vertRow = {"   ", " 8 ", " 7 ", " 6 ", " 5 ", " 4 ", " 3 ", " 2 ", " 1 ", "   "};
        String[] royalRowWhite = {
                "   ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♖ ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♘ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♗ ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♕ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♔ ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♗ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♘ ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♖ ",
                "   "};
        String[] pawnRowWhite = {
                "   ",
                EscapeSequences.SET_BG_COLOR_WHITE +" ♙ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♙ ",
                EscapeSequences.SET_BG_COLOR_WHITE +" ♙ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♙ ",
                EscapeSequences.SET_BG_COLOR_WHITE +" ♙ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♙ ",
                EscapeSequences.SET_BG_COLOR_WHITE +" ♙ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♙ ",
                "   "};
        String[] royalRowBlack = {
                "   ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♜ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♞ ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♝ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♛ ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♚ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♝ ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♞ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♜ ",
                "   "};
        String[] pawnRowBlack  = {
                "   ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♟ ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♟ ",
                EscapeSequences.SET_BG_COLOR_BLACK + " ♟ ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♟ ",
                EscapeSequences.SET_BG_COLOR_BLACK +  " ♟ ",
                EscapeSequences.SET_BG_COLOR_WHITE + " ♟ ",
                EscapeSequences.SET_BG_COLOR_BLACK +  " ♟ ",
                EscapeSequences.SET_BG_COLOR_WHITE+ " ♟ ",
                "   "};
        String[] emptyWhiteFirstRow  = {
                "   ",
                EscapeSequences.SET_BG_COLOR_WHITE + "   ",
                EscapeSequences.SET_BG_COLOR_BLACK + "   ",
                EscapeSequences.SET_BG_COLOR_WHITE + "   ",
                EscapeSequences.SET_BG_COLOR_BLACK + "   ",
                EscapeSequences.SET_BG_COLOR_WHITE + "   ",
                EscapeSequences.SET_BG_COLOR_BLACK + "   ",
                EscapeSequences.SET_BG_COLOR_WHITE + "   ",
                EscapeSequences.SET_BG_COLOR_BLACK + "   ",
                "   "};
        String[] emptyBlackFirstRow  = {
                "   ",
                EscapeSequences.SET_BG_COLOR_BLACK + "   ",
                EscapeSequences.SET_BG_COLOR_WHITE + "   ",
                EscapeSequences.SET_BG_COLOR_BLACK + "   ",
                EscapeSequences.SET_BG_COLOR_WHITE + "   ",
                EscapeSequences.SET_BG_COLOR_BLACK + "   ",
                EscapeSequences.SET_BG_COLOR_WHITE + "   ",
                EscapeSequences.SET_BG_COLOR_BLACK + "   ",
                EscapeSequences.SET_BG_COLOR_WHITE + "   ",
                "   "};
        for (int j = 0; j < 10; j++) {
            board[0][j] = EscapeSequences.SET_BG_COLOR_DARK_GREY + horizRow[j];
            board[1][j] = royalRowBlack[j];
            board[2][j] = pawnRowBlack[j];
            board[3][j] = emptyWhiteFirstRow[j];
            board[4][j] = emptyBlackFirstRow[j];
            board[5][j] = emptyWhiteFirstRow[j];
            board[6][j] = emptyBlackFirstRow[j];
            board[7][j] = pawnRowWhite[j];
            board[8][j] = royalRowWhite[j];
            board[9][j] = EscapeSequences.SET_BG_COLOR_DARK_GREY + horizRow[j];
        }
        for (int i = 0; i < 10; i++) {
            board[i][0] = EscapeSequences.SET_BG_COLOR_DARK_GREY + vertRow[i];
            board[i][9] = EscapeSequences.SET_BG_COLOR_DARK_GREY + vertRow[i];
            // this got overwritten in the other loop, so I made it its own loop
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                System.out.print(board[i][j]);
            }
            System.out.print(EscapeSequences.RESET_BG_COLOR + "\n");
        }
    }
}