package chess;

import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    final private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }


    public void removePiece(ChessPosition position) {
        board[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return null;
        }
        return board[row - 1][col - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int i = 1; i <= 8; i++) {
            addPiece(new ChessPosition(2, i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
        var teams = List.of(ChessGame.TeamColor.WHITE, ChessGame.TeamColor.BLACK);
        for (ChessGame.TeamColor team : teams) {
            int row = (team == ChessGame.TeamColor.WHITE ? 1 : 8);
            addPiece(new ChessPosition(row, 1), new ChessPiece(team, ChessPiece.PieceType.ROOK));
            addPiece(new ChessPosition(row, 2), new ChessPiece(team, ChessPiece.PieceType.KNIGHT));
            addPiece(new ChessPosition(row, 3), new ChessPiece(team, ChessPiece.PieceType.BISHOP));
            addPiece(new ChessPosition(row, 4), new ChessPiece(team, ChessPiece.PieceType.QUEEN));
            addPiece(new ChessPosition(row, 5), new ChessPiece(team, ChessPiece.PieceType.KING));
            addPiece(new ChessPosition(row, 6), new ChessPiece(team, ChessPiece.PieceType.BISHOP));
            addPiece(new ChessPosition(row, 7), new ChessPiece(team, ChessPiece.PieceType.KNIGHT));
            addPiece(new ChessPosition(row, 8), new ChessPiece(team, ChessPiece.PieceType.ROOK));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        var boardString = new StringBuilder();
        boardString.append("\n");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                var piece = board[i][j];
                var pieceType = piece == null ? "   " : switch (piece.getPieceType()) { // can we make this different?
                    case KING   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? "\u001b[38;5;15m" + " ♚ "  : "\u001B[38;5;0M" + " ♚ " ;
                    case QUEEN  -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? "\u001b[38;5;15m" + " ♛ "  : "\u001B[38;5;0M" + " ♛ " ;
                    case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? "\u001b[38;5;15m" + " ♝ "  : "\u001B[38;5;0M" + " ♝ " ;
                    case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? "\u001b[38;5;15m" + " ♞ "  : "\u001B[38;5;0M" + " ♞ ";
                    case ROOK   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? "\u001b[38;5;15m" + " ♜ "  : "\u001B[38;5;0M" + " ♜ " ;
                    case PAWN   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? "\u001b[38;5;15m" + " ♟ "  : "\u001B[38;5;0M" + " ♟ ";
                };
                boardString.append(pieceType);
            }
            boardString.append("\n");
        }
        return boardString.toString();
    }
}
