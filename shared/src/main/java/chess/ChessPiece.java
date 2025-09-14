package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    public final ChessGame.TeamColor pieceColor;
    public final ChessPiece.PieceType type;


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessPiece otherChessPiece = (ChessPiece) obj;
        return type == otherChessPiece.getPieceType();
        // return true if same piece type
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();
        // hardcode moves.add() things to test
        var piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {
            var endPositions = new ArrayList<ChessPosition>();
//            for (int i = 1; i < 8; i++) {
//                endPositions.add(new ChessPosition(myPosition.getRow() + i, myPosition.getColumn() + i));
//                endPositions.add(new ChessPosition(myPosition.getRow() - i, myPosition.getColumn() + i));
//                endPositions.add(new ChessPosition(myPosition.getRow() + i, myPosition.getColumn() - i));
//                endPositions.add(new ChessPosition(myPosition.getRow() - i, myPosition.getColumn() - i));
//            } // my method seems more efficient (no secondary loop)
            // but test won't recognize valid moves if they aren't in a specific order

            for (int col = 1; col < 9; col++) {
                for (int row = 1; row < 9; row++) {
                    int diff_row = myPosition.getRow() - row;
                    int diff_col = myPosition.getColumn() - col;
                    if (Math.abs(diff_row) == Math.abs(diff_col)) {
                        endPositions.add(new ChessPosition(row, col));
                    }
                }
            }

            for (var endPosition : endPositions) {
                if (myPosition.equals(endPosition)) {
                    continue; // refactor to isLegal if this is a problem for pieces beyond Bishop
                }
                if (ChessMove.isLegal(endPosition)) {
                    var move = new ChessMove(myPosition, endPosition, null);
                    moves.add(move);
                }
            }
        }


        return moves;
    }


}
