package chess;

import java.util.List;
import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    public final ChessPosition startPosition;
    public final ChessPosition endPosition;
    public final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /*@return ChessPosition of starting location*/
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /*@return ChessPosition of ending location*/
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    public static int isLegal(ChessBoard board, ChessPosition endPosition, ChessGame.TeamColor pieceColor) {
        if (outOfBounds(endPosition)) {
            return 1; // completely out of bounds
        }
        else if (board.getPiece(endPosition) == null) {
            return 2; // available space
        }
        else if (board.getPiece(endPosition) != null && board.getPiece(endPosition).getTeamColor() != pieceColor) {
            return 3; // enemy occupied
        }
        else {
            return 4; // teammate occupied
        }
    }

    public static boolean outOfBounds(ChessPosition endPosition) {
        int row = endPosition.getRow();
        int col = endPosition.getColumn();
        return row < 1 || row > 8 || col < 1 || col > 8;
    }

    @Override
    public String toString() {
        String startString = toLetter(startPosition.getColumn()) + startPosition.getRow();
        String endString = toLetter(endPosition.getColumn()) + endPosition.getRow();
        return "from " + startString + " to " + endString;
    }

    private String toLetter(int col) {
        return switch (col) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> "D";
            case 5 -> "E";
            case 6 -> "F";
            case 7 -> "G";
            case 8 -> "H";
            default -> null; // won't happen but it wanted one
        };
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(startPosition, chessMove.startPosition)
                && Objects.equals(endPosition, chessMove.endPosition)
                && promotionPiece == chessMove.promotionPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }
}
