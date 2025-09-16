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

    public static List<Boolean> isLegal (ChessBoard board, ChessPosition endPosition, ChessPiece piece) {
        // returns bool List(isLegal, enemyOccupied)
        // check out of bounds
        int row = endPosition.getRow();
        int col = endPosition.getColumn();
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return List.of(false, false); // out of bounds
        }
        // check if occupied
        ChessPiece otherPiece = board.getPiece(endPosition);
        if (otherPiece != null) {
            if (otherPiece.getTeamColor() != piece.getTeamColor()) {
                return List.of(false, true); // add the endpoint, but don't keep searching the path
            }
            return List.of(false, false); // our team, path not legal
        }
        return List.of(true, false); // path clear
    }


    @Override
    public String toString() {
        //return String.format("Start: %s, End: %s", startPosition, endPosition);
        return String.format("End: %s", endPosition);

    }

    //TODO edit equals and hashcode
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(startPosition, chessMove.startPosition) && Objects.equals(endPosition, chessMove.endPosition) && promotionPiece == chessMove.promotionPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }
}
