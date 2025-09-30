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
        return (type == otherChessPiece.getPieceType() && pieceColor == otherChessPiece.getTeamColor());
        // return true if same piece type
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return String.format("%s %s", pieceColor, type);
    }

    public static HashSet<ChessPosition> getEndPositions(boolean recursion, ChessBoard board, ChessPosition myPosition, HashSet<ChessPosition> directions) {
        var endPositions = new HashSet<ChessPosition>();
        if (recursion) {
            for (ChessPosition direction : directions) {
                for (int i = 1; i <= 8; i++) {
                    var endPosition = new ChessPosition(myPosition.getRow() + (i * direction.getRow()), myPosition.getColumn() + (i * direction.getColumn()));
                    var legality = ChessMove.isLegal(board, endPosition, pieceColor);
                    if (legality == 2) { // available spot
                        endPositions.add(endPosition);
                    }
                    else if (legality == 3) { // enemy occupied, but don't recurse further
                        endPositions.add(endPosition);
                        break;
                    }
                    else { // path blocked by edge or teammate
                        break;
                    }
                }
            }
        }
        else { // no recursion
            for (ChessPosition direction : directions) {
                var endPosition = new ChessPosition(myPosition.getRow() + direction.getRow(), myPosition.getColumn() + direction.getColumn());
                var legality = ChessMove.isLegal(board, endPosition, pieceColor);
                if (legality == 2 || legality == 3) { // available or enemy capture
                    endPositions.add(endPosition);
                }
            }
        }

        return endPositions;
    }

    public static HashSet<ChessPosition> getPawnEndPositions(ChessBoard board, ChessPosition myPosition) {
        var endPositions = new HashSet<ChessPosition>();
        if (pieceColor == ChessGame.TeamColor.WHITE) {
            var forward = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
            if (board.getPiece(forward) == null) {endPositions.add(forward);}
            var twiceForward = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
            if (myPosition.getRow() == 2 && board.getPiece(twiceForward) == null && board.getPiece(forward) == null) {endPositions.add(twiceForward);}
            var leftDiagonal = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()-1);
            var rightDiagonal = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()+1);
            if (!ChessMove.outOfBounds(leftDiagonal) && board.getPiece(leftDiagonal) != null && board.getPiece(leftDiagonal).getTeamColor() != pieceColor) {endPositions.add(leftDiagonal);}
            if (!ChessMove.outOfBounds(rightDiagonal) && board.getPiece(rightDiagonal) != null && board.getPiece(rightDiagonal).getTeamColor() != pieceColor) {endPositions.add(rightDiagonal);}
        }
        if (pieceColor == ChessGame.TeamColor.BLACK) {
            var downward = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
            if (board.getPiece(downward) == null) {endPositions.add(downward);}
            var twiceDownward = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());
            if (myPosition.getRow() == 7 && board.getPiece(twiceDownward) == null && board.getPiece(downward) == null) {endPositions.add(twiceDownward);}
            var leftDiagonal = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()-1);
            var rightDiagonal = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()+1);
            if (!ChessMove.outOfBounds(leftDiagonal) && board.getPiece(leftDiagonal) != null && board.getPiece(leftDiagonal).getTeamColor() != pieceColor) {endPositions.add(leftDiagonal);}
            if (!ChessMove.outOfBounds(rightDiagonal) && board.getPiece(rightDiagonal) != null && board.getPiece(rightDiagonal).getTeamColor() != pieceColor) {endPositions.add(rightDiagonal);}
        }
        return endPositions;
    }
    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     * @return Collection of valid moves
     */
    public static Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new HashSet<ChessMove>();
        var directions = new HashSet<ChessPosition>();
        var endPositions = new HashSet<ChessPosition>();
        if (board.getPiece(myPosition).getPieceType() == PieceType.BISHOP) {
            directions.add(new ChessPosition(1,1));
            directions.add(new ChessPosition(1,-1));
            directions.add(new ChessPosition(-1,1));
            directions.add(new ChessPosition(-1,-1));
            endPositions = getEndPositions(true, board, myPosition, directions);
        }
        if (board.getPiece(myPosition).getPieceType() == PieceType.ROOK) {
            directions.add(new ChessPosition(1,0));
            directions.add(new ChessPosition(0,-1));
            directions.add(new ChessPosition(-1,0));
            directions.add(new ChessPosition(0,1));
            endPositions = getEndPositions(true, board, myPosition, directions);
        }
        if (board.getPiece(myPosition).getPieceType() == PieceType.QUEEN) {
            directions.add(new ChessPosition(1,1));
            directions.add(new ChessPosition(1,-1));
            directions.add(new ChessPosition(-1,1));
            directions.add(new ChessPosition(-1,-1));
            directions.add(new ChessPosition(1,0));
            directions.add(new ChessPosition(0,-1));
            directions.add(new ChessPosition(-1,0));
            directions.add(new ChessPosition(0,1));
            endPositions = getEndPositions(true, board, myPosition, directions);
        }
        if (board.getPiece(myPosition).getPieceType() == PieceType.KING) {
            directions.add(new ChessPosition(1,1));
            directions.add(new ChessPosition(1,-1));
            directions.add(new ChessPosition(-1,1));
            directions.add(new ChessPosition(-1,-1));
            directions.add(new ChessPosition(1,0));
            directions.add(new ChessPosition(0,-1));
            directions.add(new ChessPosition(-1,0));
            directions.add(new ChessPosition(0,1));
            endPositions = getEndPositions(false, board, myPosition, directions);
        }
        if (board.getPiece(myPosition).getPieceType() == PieceType.KNIGHT) {
            directions.add(new ChessPosition(2,1));
            directions.add(new ChessPosition(2,-1));
            directions.add(new ChessPosition(-2,1));
            directions.add(new ChessPosition(-2,-1));
            directions.add(new ChessPosition(1,2));
            directions.add(new ChessPosition(1,-2));
            directions.add(new ChessPosition(-1,2));
            directions.add(new ChessPosition(-1,-2));
            endPositions = getEndPositions(false, board, myPosition, directions);
        }
        if (board.getPiece(myPosition).getPieceType() == PieceType.PAWN) {
            endPositions = getPawnEndPositions(board, myPosition);
            for (ChessPosition endPosition : endPositions) {
                if (endPosition.getRow() == 1 || endPosition.getRow() == 8) {
                    moves.add(new ChessMove(myPosition, endPosition, PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, endPosition, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, endPosition, PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, endPosition, PieceType.QUEEN));
                }
                else {
                    moves.add(new ChessMove(myPosition, endPosition, null));
                }
            }
            return moves;
        }
        for (ChessPosition endPosition : endPositions) {
            moves.add(new ChessMove(myPosition, endPosition, null));
        }
        return moves;
    }


}
