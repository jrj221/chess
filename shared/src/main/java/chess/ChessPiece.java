package chess;

import java.util.*;

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

    @Override
    public String toString() {
        return String.format("%s %s", pieceColor, type);
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

            for (int i = 1; i < 8; i++) { //up-right
                var endPosition = new ChessPosition(myPosition.getRow() + i, myPosition.getColumn() + i);
                if (ChessMove.outOfBounds(endPosition)) {
                    break; //path is out of bounds
                }
                if (ChessMove.isOccupied(board, endPosition)) {
                    var ex = board.getPiece(endPosition).getTeamColor();
                    if (board.getPiece(endPosition).getTeamColor() != pieceColor) {
                        endPositions.add(endPosition); // there's got to be a cleaner way of doing this
                    }
                    break; //occupied, path not clear
                }
                endPositions.add(endPosition);
            }

            for (int i = 1; i < 8; i++) { //up-left
                var endPosition = new ChessPosition(myPosition.getRow() + i, myPosition.getColumn() - i);
                if (ChessMove.outOfBounds(endPosition)) {
                    break; //path is out of bounds
                }
                if (ChessMove.isOccupied(board, endPosition)) {
                    if (board.getPiece(endPosition).getTeamColor() != pieceColor) {
                        endPositions.add(endPosition); // there's got to be a cleaner way of doing this
                    }
                    break; //occupied, path not clear
                }
                endPositions.add(endPosition);
            }

            for (int i = 1; i < 8; i++) { //down-right
                var endPosition = new ChessPosition(myPosition.getRow() - i, myPosition.getColumn() + i);
                if (ChessMove.outOfBounds(endPosition)) {
                    break; //path is out of bounds
                }
                if (ChessMove.isOccupied(board, endPosition)) {
                    if (board.getPiece(endPosition).getTeamColor() != pieceColor) {
                        endPositions.add(endPosition); // there's got to be a cleaner way of doing this
                    }
                    break; //occupied, path not clear
                }
                endPositions.add(endPosition);
            }

            for (int i = 1; i < 8; i++) { //down-left
                var endPosition = new ChessPosition(myPosition.getRow() - i, myPosition.getColumn() - i);
                if (ChessMove.outOfBounds(endPosition)) {
                    break; //path is out of bounds
                }
                if (ChessMove.isOccupied(board, endPosition)) {
                    if (board.getPiece(endPosition).getTeamColor() != pieceColor) {
                        endPositions.add(endPosition); // there's got to be a cleaner way of doing this
                    }
                    break; //occupied, path not clear
                }
                endPositions.add(endPosition);
            }
            endPositions.sort(Comparator.comparing(ChessPosition::toString));

//            for (int col = 1; col < 9; col++) {
//                for (int row = 1; row < 9; row++) {
//                    int diff_row = myPosition.getRow() - row;
//                    int diff_col = myPosition.getColumn() - col;
//                    if (Math.abs(diff_row) == Math.abs(diff_col)) {
//                        endPositions.add(new ChessPosition(row, col));
//                    }
//                }
//            }

            for (var endPosition : endPositions) {
                var move = new ChessMove(myPosition, endPosition, null);
                moves.add(move);
            }
        }


        return moves;
    }


}
