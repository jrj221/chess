package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

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

    public void createTeam(ChessGame.TeamColor color) {
        var rook = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        var knight = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        var bishop = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        var queen = new ChessPiece(color, ChessPiece.PieceType.QUEEN);
        var king = new ChessPiece(color, ChessPiece.PieceType.KING);
        var pawn = new ChessPiece(color, ChessPiece.PieceType.PAWN);
        var pieces = new ArrayList<>(Arrays.asList(rook, knight, bishop, queen, king, bishop, knight, rook));
        if (color == ChessGame.TeamColor.WHITE) {
            int i = 1;
            for (ChessPiece piece : pieces) {
                addPiece(new ChessPosition(1, i), piece);
                addPiece(new ChessPosition(2, i), pawn);
                i++;
            }
        }
        if (color == ChessGame.TeamColor.BLACK) {
            int i = 1;
            for (ChessPiece piece : pieces) {
                addPiece(new ChessPosition(8, i), piece);
                addPiece(new ChessPosition(7, i), pawn);
                i++;
            }
        }
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        createTeam(ChessGame.TeamColor.WHITE);
        createTeam(ChessGame.TeamColor.BLACK);
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
}
