package client;

import chess.*;
import serverfacade.ServerFacade;
import ui.EscapeSequences;
import websocket.WebsocketFacade;
import websocket.commands.*;


import java.util.*;

import static ui.EscapeSequences.*;

public class GameplayClient implements Client, ServerMessageHandler {

    static ServerFacade facade = new ServerFacade(8080);
    WebsocketFacade websocketFacade = new WebsocketFacade(8080, this); // websocket session
    private String teamColor;
    private int gameID;
    private String authToken;
    private String username;
    private ChessGame game;
    private String state;
    private boolean resignPromtedOnce = false;


    public GameplayClient(String stringGameID, String teamColor, String joinedOrObserved) throws Exception {
        gameID = Integer.parseInt(stringGameID.replace("!\n", ""));
        this.teamColor = teamColor;
        authToken = facade.getAuthToken();
        username = facade.getUsername();
        state = joinedOrObserved;
        websocketFacade.send(new ConnectCommand(username, authToken, gameID, teamColor, state));
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
            case "redraw" -> redraw(null, null);
            case "leave" -> leave();
            case "highlight" -> highlight(inputWords);
            case "move" -> makeMove(inputWords);
            case "resign" -> resign();
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
        System.out.println(redraw(null, null)); // print game
        printPrompt();
    }


    @Override
    public void sendError(String errorMessage) {
        System.out.println(); // newline to get off of the prompt line
        System.out.println(SET_TEXT_ITALIC + SET_TEXT_COLOR_RED
                + "ERROR: " + errorMessage + RESET_TEXT_COLOR + RESET_TEXT_ITALIC);
        printPrompt();
    }


    @Override
    public void notify(String message) {
        System.out.println(); // newline to get off of the prompt line
        System.out.println(SET_TEXT_ITALIC + SET_TEXT_COLOR_MAGENTA
                + "NOTIFICATION: " + message + RESET_TEXT_COLOR + RESET_TEXT_ITALIC);
        printPrompt();
    }

    private String leave() throws Exception {
        websocketFacade.send(new LeaveCommand(username, authToken, gameID));
        return "Successfully left the game!";
    }

    private String resign() {
        return "resign";
    }

    private String makeMove(String[] inputWords) throws Exception {
        if (state.equals("observer")) {
            throw new Exception("Observers cannot make moves");
        }
        if (inputWords.length == 3 || inputWords.length == 4) {
            var start = inputWords[1];
            var end = inputWords[2];
            ChessPiece.PieceType promotionPiece = null; // default

            if (inputWords.length == 4) {
                var promotionPieceString = inputWords[3];
                if (!isValidPromotionPiece(promotionPieceString)) {
                    throw new Exception("Promotion piece is invalid.\n" +
                            "Must be ROOK, KNIGHT, BISHOP, or QUEEN");
                }
                promotionPiece = switch (promotionPieceString.toUpperCase()) {
                    case "KNIGHT" -> ChessPiece.PieceType.KNIGHT;
                    case "QUEEN" -> ChessPiece.PieceType.QUEEN;
                    case "ROOK" -> ChessPiece.PieceType.ROOK;
                    case "BISHOP" -> ChessPiece.PieceType.BISHOP;
                    default -> null; // shouldn't trigger but it needs a default
                };
            }
            if (!isValidPosition(start) || !isValidPosition(end)) {
                throw new Exception("Either your START or END position is invalid.\n" +
                        "Positions should take the form \"A3\"");
            }
            var convertedStart = convertPosition(start); //11
            var convertedEnd = convertPosition(end); //31
            var startPosition = new ChessPosition(convertedStart.charAt(0) - '0', convertedStart.charAt(1) - '0');
            var endPosition = new ChessPosition(convertedEnd.charAt(0) - '0', convertedEnd.charAt(1) - '0');

            var moveString = String.format("%s to %s", start, end);
            var move = new ChessMove(startPosition, endPosition, promotionPiece);
            websocketFacade.send(new MakeMoveCommand(move, moveString, username, authToken, gameID, teamColor));
            return String.format("Attempted move from %s to %s", start, end);
            // not the best way to handle this since it could fail
        }
        throw new Exception("""
                Invalid command.
                Making a move takes the form "move START END PROMOTION_PIECE"
                (note: PROMOTION_PIECE is only necessary if you are moving a pawn to the final row)""");
    }


    private boolean isValidPromotionPiece(String promotionPiece) {
        return switch (promotionPiece.toUpperCase()) {
            case "KNIGHT", "QUEEN", "ROOK", "BISHOP" -> true;
            default -> false;
        };
    }


    private String highlight(String[] inputWords) throws Exception {
        if (inputWords.length == 2) {
            String positionString = inputWords[1];
            if (!isValidPosition(positionString)) {
                throw new Exception("Invalid position.\nA position takes the form \"A1\"");
            }

            String convertedPosition = convertPosition(positionString);
            String pieceToHighlight = String.format("%c:%c", convertedPosition.charAt(0), convertedPosition.charAt(1));
            HashSet<ChessMove> moves = getChessMoves(convertedPosition, positionString);
            List<String> positionsToHighlight = convertMoves(moves);
            System.out.println(redraw(pieceToHighlight, positionsToHighlight));

            return String.format("Highlighted legal moves for piece at %s", positionString);
        }
        throw new Exception("Invalid command.\n" +
                "Highlighting legal moves takes the form \"highlight POSITION\"");

    }


    /**
     * Gets the moves available for a certain position
     *
     * @param convertedPosition String position like "13" (was A3)
     * @param positionString Original position string like "A3"
     * @return HashSet of legal ChessMoves from the given position
     */
    private HashSet<ChessMove> getChessMoves(String convertedPosition, String positionString) throws Exception {
        int start = convertedPosition.charAt(0) - '0';
        int end = convertedPosition.charAt(1) - '0';

        ChessPosition position = new ChessPosition(start, end);
        var board = game.getBoard();
        ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            throw new Exception(String.format("No piece at %s to highlight moves for", positionString));
        }

        return piece.pieceMoves(board, position);
    }


    /**
     * Verifies valid syntax by checking that length is 2, if it has a letter A-H, and if it has a number 1-8.
     * @param position String position like "A3"
     * @return true or false depending on if it is in correct syntax
     */
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


    /**
     * Converts a position like "A3" to "31". We flip them since a ChessPosition is row:column, not column:row
     * @param position String position like "A3"
     * @return String position like "31"
     */
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
        // the repl has them give positions as A3 which is col:row, but ChessPositions are row:col, so we need to swap
        return String.format("%c%c", position.charAt(1), number);
    }


    /**
     * Converts a HashSet of ChessMoves into a list of legal end positions in string form
     * ex. ChessMove(1:1, 3:1) -> "3:1"
     * @param moves A HashSet of ChessMoves
     * @return A list of legal end positions in string form
     */
    private List<String> convertMoves(HashSet<ChessMove> moves) {
        List<String> stringMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            var endPosition = move.getEndPosition();
            int row = endPosition.getRow();
            int col = endPosition.getColumn();
            stringMoves.add(String.format("%d:%d", row, col));
        }
        return stringMoves;
    }


    /**
     * Redraws the board (and optionally highlights legal moves)
     *
     * @param highlightedPiece (optional) piece which you want to highlight moves for.
     *                         Use null if you don't want to do this
     * @param highlightedPositions (optional) positions to highlight when redrawing.
     *                             Use null if you don't want to do this
     * @return Stringified board (optionally with highlighted squares)
     */
    private String redraw(String highlightedPiece, List<String> highlightedPositions) {
        return parseChessGame(game, highlightedPiece, highlightedPositions);
    }


    /**
     * Parses a ChessGame into a string representing the board
     * @param game A ChessGame to parse
     * @param highlightedPiece (optional) piece which you want to highlight moves for.
     *                         Use null if you don't want to do this
     * @param highlightedPositions (optional) positions to highlight when redrawing.
     *                             Use null if you don't want to do this
     * @return Stringified board (optionally with highlighted squares)
     */
    private String parseChessGame(ChessGame game, String highlightedPiece, List<String> highlightedPositions) {
        ChessBoard board = game.getBoard();
        var pieceMap = new HashMap<String, String>();
        var emptyBoard = initEmptyBoard();

        // Add main board colors
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                var pieceType = piece == null ? "   " : switch (piece.getPieceType()) { // can we make this different?
                    case KING   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? SET_TEXT_COLOR_BLUE + " ♚ "  : SET_TEXT_COLOR_RED + " ♚ " ;
                    case QUEEN  -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? SET_TEXT_COLOR_BLUE + " ♛ "  : SET_TEXT_COLOR_RED + " ♛ " ;
                    case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? SET_TEXT_COLOR_BLUE + " ♝ "  : SET_TEXT_COLOR_RED + " ♝ " ;
                    case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? SET_TEXT_COLOR_BLUE + " ♞ "  : SET_TEXT_COLOR_RED + " ♞ ";
                    case ROOK   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? SET_TEXT_COLOR_BLUE + " ♜ "  : SET_TEXT_COLOR_RED + " ♜ " ;
                    case PAWN   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? SET_TEXT_COLOR_BLUE + " ♟ "  : SET_TEXT_COLOR_RED + " ♟ ";
                };
                pieceType += RESET_TEXT_COLOR;
                var position = String.format("%d:%d", i, j);
                var color = emptyBoard.get(position);
                if (highlightedPositions != null && highlightedPositions.contains(position)) {
                    color = color.equals(SET_BG_COLOR_WHITE) ? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN;
                }
                if (position.equals(highlightedPiece)) {
                    color = SET_BG_COLOR_YELLOW;
                }
                pieceMap.put(position, color + pieceType);
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
        boardString.append(String.format("Team %s is now playing", game.getTeamTurn()));
        return boardString.toString();
    }


    /**
     * Gets a map representing the colors of squares on an empty board
     * @return A map of Position->Color where Color is the EscapeSequence needed to paint the square at Position
     */
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
