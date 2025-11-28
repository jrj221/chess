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
                pieceMap.put(position, emptyBoard.get(position) + pieceType);
            }
        }

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

        var boardArray = new String[10][10];
        // Build intial board
        pieceMap.forEach((position, piece) -> {
            var i = Integer.parseInt(position.split(":")[0]);
            var j = Integer.parseInt(position.split(":")[1]);
            boardArray[i][j] = piece;
        });
        var boardString = new StringBuilder();
        for (int i = 9; i > -1; i--) {
            for (int j = 0; j < 10; j++) {
                boardString.append(boardArray[i][j]);
            }
            boardString.append(EscapeSequences.RESET_BG_COLOR + "\n");
        }
        return boardString.toString();
    }

    //used to grab colors
    private static HashMap<String, String> initEmptyBoard() {
        String[] emptyBlackFirstRow = {
                EscapeSequences.SET_BG_COLOR_DARK_GREY,
                EscapeSequences.SET_BG_COLOR_BLACK,
                EscapeSequences.SET_BG_COLOR_WHITE,
                EscapeSequences.SET_BG_COLOR_BLACK,
                EscapeSequences.SET_BG_COLOR_WHITE,
                EscapeSequences.SET_BG_COLOR_BLACK,
                EscapeSequences.SET_BG_COLOR_WHITE,
                EscapeSequences.SET_BG_COLOR_BLACK,
                EscapeSequences.SET_BG_COLOR_WHITE,
                EscapeSequences.SET_BG_COLOR_DARK_GREY
        };
        String[] emptyWhiteFirstRow = {
                EscapeSequences.SET_BG_COLOR_DARK_GREY,
                EscapeSequences.SET_BG_COLOR_WHITE,
                EscapeSequences.SET_BG_COLOR_BLACK,
                EscapeSequences.SET_BG_COLOR_WHITE,
                EscapeSequences.SET_BG_COLOR_BLACK,
                EscapeSequences.SET_BG_COLOR_WHITE,
                EscapeSequences.SET_BG_COLOR_BLACK,
                EscapeSequences.SET_BG_COLOR_WHITE,
                EscapeSequences.SET_BG_COLOR_BLACK,
                EscapeSequences.SET_BG_COLOR_DARK_GREY
        };
        var pieceMap = new HashMap<String, String>();
        for (int j = 0; j < 10; j++) {
            pieceMap.put(String.format("0:%d", j), EscapeSequences.SET_BG_COLOR_DARK_GREY); // top row
            pieceMap.put(String.format("1:%d", j), emptyBlackFirstRow[j]);
            pieceMap.put(String.format("2:%d", j), emptyWhiteFirstRow[j]);
            pieceMap.put(String.format("3:%d", j), emptyBlackFirstRow[j]);
            pieceMap.put(String.format("4:%d", j), emptyWhiteFirstRow[j]);
            pieceMap.put(String.format("5:%d", j), emptyBlackFirstRow[j]);
            pieceMap.put(String.format("6:%d", j), emptyWhiteFirstRow[j]);
            pieceMap.put(String.format("7:%d", j), emptyBlackFirstRow[j]);
            pieceMap.put(String.format("8:%d", j), emptyWhiteFirstRow[j]);
            pieceMap.put(String.format("9:%d", j), EscapeSequences.SET_BG_COLOR_DARK_GREY);
        }
        return pieceMap;
    }

}
