package service;

import chess.*;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.NoExistingGameException;
import datamodel.GameData;
import ui.EscapeSequences;

import java.util.HashMap;
import java.util.List;

public class GameplayService {
    private final DataAccess dataAccess;

    public GameplayService(DataAccess dataAccess) { // constructor
        this.dataAccess = dataAccess;
    }

    public String redraw() throws Exception {
        try {
            int gameID = 1;
            GameData gameData = dataAccess.getGame(gameID);
            ChessGame game = gameData.game();
            String board = parseChessGame(game);
            return board;
        } catch (DataAccessException ex) {
            throw new NoExistingGameException("No existing game");
        }
    }

    private String parseChessGame(ChessGame game) {
        ChessBoard board = game.getBoard();
        var pieceMap = new HashMap<String, String>();
        var emptyBoard = initEmptyBoard();
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                var pieceType = piece == null ? "   " : switch (piece.getPieceType()) {
                    case KING -> " ♔ ";
                    case QUEEN -> " ♕ ";
                    case BISHOP -> " ♗ ";
                    case KNIGHT -> " ♘ ";
                    case ROOK -> " ♖ ";
                    case PAWN -> " ♙ ";
                };
                // need to figure out square color
                var position = String.format("%d:%d", i, j);
                pieceMap.put(position, emptyBoard.get(position).get(0) + pieceType);
            }
        }

        String[] vert = {"   ", " a ", " b ", " c ", " d ", " e ", " f ", " g ", " h ", "   "};
        String[] horiz = {"   ", " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ", "   "};
        for (int i = 0; i < vert.length; i++) {
            var position = String.format("0:%d", i);
            pieceMap.put(position, emptyBoard.get(position).get(0) + vert[i]);
            position = String.format("9:%d", i);
            pieceMap.put(position, emptyBoard.get(position).get(0) + vert[i]);

            position = String.format("%d:0", i);
            pieceMap.put(position, emptyBoard.get(position).get(0) + horiz[i]);
            position = String.format("%d:9", i);
            pieceMap.put(position, emptyBoard.get(position).get(0) + horiz[i]);
        }

        var boardArray = new String[10][10];
        // Build intial board
        pieceMap.forEach((position, piece) -> {
            var i = Integer.parseInt(position.split(":")[0]);
            var j = Integer.parseInt(position.split(":")[1]);
            boardArray[i][j] = piece;
        });
        var boardString = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                boardString.append(boardArray[i][j]);
            }
            boardString.append(EscapeSequences.RESET_BG_COLOR + "\n");
        }
        return boardString.toString();
    }

    private static HashMap<String, List<String>> initEmptyBoard() {
        String[] horizRow = {"   ", " h ", " g ", " f ", " e ", " d ", " c ", " b ", " a ", "   "};
        String[] vertRow = {"   ", " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ", "   "};
        String[][] royalRowWhite = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] pawnRowWhite = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] royalRowBlack = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] pawnRowBlack = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] emptyWhiteFirstRow = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] emptyBlackFirstRow = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        var pieceMap = new HashMap<String, List<String>>();
        for (int j = 0; j < 10; j++) {
            pieceMap.put(String.format("0:%d", 9-j), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, horizRow[j])); // top row
            pieceMap.put(String.format("1:%d", 9-j), List.of(royalRowBlack[j][0], royalRowBlack[j][1]));
            pieceMap.put(String.format("2:%d", 9-j), List.of(pawnRowBlack[j][0], pawnRowBlack[j][1]));
            pieceMap.put(String.format("3:%d", 9-j), List.of(emptyWhiteFirstRow[j][0], emptyWhiteFirstRow[j][1]));
            pieceMap.put(String.format("4:%d", 9-j), List.of(emptyBlackFirstRow[j][0], emptyBlackFirstRow[j][1]));
            pieceMap.put(String.format("5:%d", 9-j), List.of(emptyWhiteFirstRow[j][0], emptyWhiteFirstRow[j][1]));
            pieceMap.put(String.format("6:%d", 9-j), List.of(emptyBlackFirstRow[j][0], emptyBlackFirstRow[j][1]));
            pieceMap.put(String.format("7:%d", 9-j), List.of(pawnRowWhite[j][0], pawnRowWhite[j][1]));
            pieceMap.put(String.format("8:%d", 9-j), List.of(royalRowWhite[j][0], royalRowWhite[j][1]));
            pieceMap.put(String.format("9:%d", 9-j), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, horizRow[j]));
        }

        for (int i = 0; i < 10; i++) {
            pieceMap.put(String.format("%d:0", 9-i), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, vertRow[i]));
            pieceMap.put(String.format("%d:9", 9-i), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, vertRow[i]));
        }
        return pieceMap;
    }

    private static HashMap<String, List<String>> initBoard() {
        String[] horizRow = {"   ", " h ", " g ", " f ", " e ", " d ", " c ", " b ", " a ", "   "};
        String[] vertRow = {"   ", " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ", "   "};
        String[][] royalRowWhite = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♖ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♘ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♗ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♔ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♕ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♗ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♘ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♖ "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] pawnRowWhite = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♙ "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] royalRowBlack = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♜ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♞ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♝ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♚ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♛ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♝ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♞ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♜ "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] pawnRowBlack = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_WHITE, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_BLACK, " ♟ "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] emptyWhiteFirstRow = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        String[][] emptyBlackFirstRow = {
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_WHITE, "   "},
                {EscapeSequences.SET_BG_COLOR_BLACK, "   "},
                {EscapeSequences.SET_BG_COLOR_DARK_GREY, "   "}
        };
        var pieceMap = new HashMap<String, List<String>>();
        for (int j = 0; j < 10; j++) {
            pieceMap.put(String.format("0:%d", 9-j), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, horizRow[j])); // top row
            pieceMap.put(String.format("1:%d", 9-j), List.of(royalRowBlack[j][0], royalRowBlack[j][1]));
            pieceMap.put(String.format("2:%d", 9-j), List.of(pawnRowBlack[j][0], pawnRowBlack[j][1]));
            pieceMap.put(String.format("3:%d", 9-j), List.of(emptyWhiteFirstRow[j][0], emptyWhiteFirstRow[j][1]));
            pieceMap.put(String.format("4:%d", 9-j), List.of(emptyBlackFirstRow[j][0], emptyBlackFirstRow[j][1]));
            pieceMap.put(String.format("5:%d", 9-j), List.of(emptyWhiteFirstRow[j][0], emptyWhiteFirstRow[j][1]));
            pieceMap.put(String.format("6:%d", 9-j), List.of(emptyBlackFirstRow[j][0], emptyBlackFirstRow[j][1]));
            pieceMap.put(String.format("7:%d", 9-j), List.of(pawnRowWhite[j][0], pawnRowWhite[j][1]));
            pieceMap.put(String.format("8:%d", 9-j), List.of(royalRowWhite[j][0], royalRowWhite[j][1]));
            pieceMap.put(String.format("9:%d", 9-j), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, horizRow[j]));
        }

        for (int i = 0; i < 10; i++) {
            pieceMap.put(String.format("%d:0", 9-i), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, vertRow[i]));
            pieceMap.put(String.format("%d:9", 9-i), List.of(EscapeSequences.SET_BG_COLOR_DARK_GREY, vertRow[i]));
        }
        return pieceMap;
    }


    /// Displays the chess board (orientation based on team)
    public String display(String team) {
        var board = new String[10][10];
        var pieceMap = initBoard(); // key: 0:0, value: [black, whitePawn] // initializes the map

        // Build intial board
        pieceMap.forEach((position, piece) -> {
            var i = Integer.parseInt(position.split(":")[0]);
            var j = Integer.parseInt(position.split(":")[1]);
            board[i][j] = piece.get(0) + piece.get(1);
        });

        // Display board (convert to string)
        var boardString = new StringBuilder();
        if (team.equals("WHITE")) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    boardString.append(board[i][j]);
                }
                boardString.append(EscapeSequences.RESET_BG_COLOR + "\n");
            }
        } else {
            for (int i = 9; i > -1 ; i--) {
                for (int j = 9; j > -1; j--) {
                    boardString.append(board[i][j]);
                }
                boardString.append(EscapeSequences.RESET_BG_COLOR + "\n");
            }
        }
        return boardString.toString();
    }
}
