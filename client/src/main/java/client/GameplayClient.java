package client;

import chess.*;
import serverfacade.ServerFacade;
import ui.EscapeSequences;
import websocket.WebsocketFacade;
import websocket.commands.*;


import java.util.HashMap;

import static ui.EscapeSequences.*;

public class GameplayClient implements Client, ServerMessageHandler {

    static ServerFacade facade = new ServerFacade(8080);
    WebsocketFacade websocketFacade = new WebsocketFacade(8080, this); // websocket session
    private String teamColor;
    private int gameID;
    private String authToken;
    private String username;
    private ChessGame game;


    public GameplayClient(String stringGameID, String teamColor) throws Exception {
        gameID = Integer.parseInt(stringGameID.replace("!\n", ""));
        this.teamColor = teamColor;
        authToken = facade.getAuthToken();
        username = facade.getUsername();
        websocketFacade.send(new ConnectCommand(username, authToken, gameID, teamColor));
    }

    public void printPrompt() {
        System.out.print(SET_TEXT_COLOR_MAGENTA + "[IN_GAME] " +
                RESET_TEXT_COLOR + ">>> ");
    }

    public String eval(String command) throws Exception {
        var inputWords = command.toLowerCase().split(" ");
        return switch (inputWords[0]) {
            case "clear" -> clear(); // FOR TESTING ONLY, REMOVE BEFORE COMPLETION
            case "help", "h" -> help();
            case "quit", "q", "exit" -> "quit";
            case "redraw" -> redraw();
            case "leave" -> leave();
            case "highlight" -> highlight(inputWords);
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

    @Override
    public void loadGame(ChessGame game) {
        this.game = game; // update game
        System.out.println(); // newline to get off of the prompt line
        System.out.println(redraw()); // print game
        printPrompt();
    }


    @Override
    public void sendError(String errorMessage) {
    }


    @Override
    public void notify(String message) {
        System.out.println(); // newline to get off of the prompt line
        System.out.println(SET_TEXT_COLOR_RED + message + RESET_TEXT_COLOR);
        printPrompt();
    }

    private String leave() throws Exception {
        websocketFacade.send(new LeaveCommand(username, authToken, gameID));
        return "Successfully left the game!";
    }

    private String highlight(String[] inputWords) throws Exception {
        if (inputWords.length == 2) {
            String positionString = inputWords[1];
            if (!isValidPosition(positionString)) {
                throw new Exception("Invalid position.\nA position takes the form \"A1\"");
            }
            String convertedPosition = convertPosition(positionString);
            System.out.println(convertedPosition);
            var board = game.getBoard();
            return String.format("Highlighted legal moves for piece at %s", positionString);
        }
        throw new Exception("Invalid command.\n" +
                "Highlighting legal moves takes the form \"highlight POSITION\"");

    }

    private boolean isValidPosition(String position) {
        // check length
        if (position.length() != 2) {return false;}

        // check letter
        boolean isValidLetter = false;
        for (char character : "ABCDEFGH".toCharArray()) {
            if (Character.toUpperCase(position.charAt(0)) == character) {
                isValidLetter = true;
                break;
            }
        }
        if (!isValidLetter) {return false;}

        // check number
        boolean isValidNumber = false;
        for (char character : "12345678".toCharArray()) {
            if (position.charAt(1) == character) {
                isValidNumber= true;
                break;
            }
        }
        if (!isValidNumber) {return false;}

        // passed all tests
        return true;
    }


    private String convertPosition(String position) throws Exception {
        char letter = position.charAt(0);
        char number = switch (letter) {
            case 'a' -> '1';
            case 'b' -> '2';
            case 'c' -> '3';
            case 'd' -> '4';
            case 'e' -> '5';
            case 'f' -> '6';
            case 'g' -> '7';
            case 'h' -> '8';
            default -> throw new Exception("Invalid letter in position");
            // this will never happen but it wants a default branch
        };
        return String.format("%c%c", number, position.charAt(1));
    }


    private String redraw() {
        return parseChessGame(game);
    }


    private String parseChessGame(ChessGame game) {
        ChessBoard board = game.getBoard();
        var pieceMap = new HashMap<String, String>();
        var emptyBoard = initEmptyBoard();

        // Add main board colors
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                var pieceType = piece == null ? "   " : switch (piece.getPieceType()) {
                    case KING   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? " ♔ " : " ♚ ";
                    case QUEEN  -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? " ♕ " : " ♛ ";
                    case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? " ♗ " : " ♝ ";
                    case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? " ♘ " : " ♞ ";
                    case ROOK   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? " ♖ " : " ♜ ";
                    case PAWN   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? " ♙ " : " ♟ ";
                };
                // need to figure out square color
                var position = String.format("%d:%d", i, j);
                pieceMap.put(position, emptyBoard.get(position) + pieceType);
            }
        }

        // Add edges
        String[] vert = {"   ", " a ", " b ", " c ", " d ", " e ", " f ", " g ", " h ", "   "};
        String[] horiz = {"   ", " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ", "   "};
        for (int i = 0; i < vert.length; i++) {
            var position = String.format("0:%d", i);
            pieceMap.put(position, emptyBoard.get(position) + vert[i]);
            position = String.format("9:%d", i);
            pieceMap.put(position, emptyBoard.get(position) + vert[i]);

            position = String.format("%d:0", i);
            pieceMap.put(position, emptyBoard.get(position) + horiz[i]);
            position = String.format("%d:9", i);
            pieceMap.put(position, emptyBoard.get(position) + horiz[i]);
        }

        var boardString = new StringBuilder();
        if (teamColor.equals("WHITE")) {
            for (int i = 9; i > -1; i--) {
                for (int j = 0; j < 10; j++) {
                    var position = String.format("%d:%d", i, j);
                    boardString.append(pieceMap.get(position));
                }
                boardString.append(RESET_BG_COLOR + "\n");
            }
        } else {
            for (int i = 0; i < 10; i++) {
                for (int j = 9; j > -1; j--) {
                    var position = String.format("%d:%d", i, j);
                    boardString.append(pieceMap.get(position));
                }
                boardString.append(RESET_BG_COLOR + "\n");
            }
        }
        return boardString.toString();
    }


    //used to grab colors for parseChessGame
    private static HashMap<String, String> initEmptyBoard() {
        String[] emptyBlackFirstRow = {
                SET_BG_COLOR_DARK_GREY,
                SET_BG_COLOR_BLACK,
                SET_BG_COLOR_WHITE,
                SET_BG_COLOR_BLACK,
                SET_BG_COLOR_WHITE,
                SET_BG_COLOR_BLACK,
                SET_BG_COLOR_WHITE,
                SET_BG_COLOR_BLACK,
                SET_BG_COLOR_WHITE,
                SET_BG_COLOR_DARK_GREY
        };
        String[] emptyWhiteFirstRow = {
                SET_BG_COLOR_DARK_GREY,
                SET_BG_COLOR_WHITE,
                SET_BG_COLOR_BLACK,
                SET_BG_COLOR_WHITE,
                SET_BG_COLOR_BLACK,
                SET_BG_COLOR_WHITE,
                SET_BG_COLOR_BLACK,
                SET_BG_COLOR_WHITE,
                SET_BG_COLOR_BLACK,
                SET_BG_COLOR_DARK_GREY
        };
        var pieceMap = new HashMap<String, String>();
        for (int j = 0; j < 10; j++) {
            pieceMap.put(String.format("0:%d", j), SET_BG_COLOR_DARK_GREY); // top row
            pieceMap.put(String.format("1:%d", j), emptyBlackFirstRow[j]);
            pieceMap.put(String.format("2:%d", j), emptyWhiteFirstRow[j]);
            pieceMap.put(String.format("3:%d", j), emptyBlackFirstRow[j]);
            pieceMap.put(String.format("4:%d", j), emptyWhiteFirstRow[j]);
            pieceMap.put(String.format("5:%d", j), emptyBlackFirstRow[j]);
            pieceMap.put(String.format("6:%d", j), emptyWhiteFirstRow[j]);
            pieceMap.put(String.format("7:%d", j), emptyBlackFirstRow[j]);
            pieceMap.put(String.format("8:%d", j), emptyWhiteFirstRow[j]);
            pieceMap.put(String.format("9:%d", j), SET_BG_COLOR_DARK_GREY);
        }
        return pieceMap;
    }
}
