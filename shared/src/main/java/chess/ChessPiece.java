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

    public List<ChessPosition> getEndPositions(boolean recursion, List<List<Integer>> directions, ChessBoard board, ChessPiece piece, ChessPosition myPosition) {
        var endPositions = new ArrayList<ChessPosition>();

        if (!recursion) {
            for (List<Integer> direction : directions) {
                var endPosition = new ChessPosition(myPosition.getRow() + direction.get(0), myPosition.getColumn() + direction.get(1));
                List<Boolean> isLegal = ChessMove.isLegal(board, endPosition, piece);
                if (isLegal.get(0) || isLegal.get(1)) { // legal move
                    endPositions.add(endPosition);
                }
            }
            return endPositions;
        }

        for (List<Integer> direction : directions) {
            for (int i = 1; i < 8; i++) {
                var endPosition = new ChessPosition(myPosition.getRow() + (i * direction.get(0)), myPosition.getColumn() + (i * direction.get(1)));
                List<Boolean> isLegal = ChessMove.isLegal(board, endPosition, piece);
                if (isLegal.get(0)) { // legal move
                    endPositions.add(endPosition);
                    continue;
                }
                if (isLegal.get(1)) { // enemy present
                    endPositions.add(endPosition);
                }
                break;
            }
        }
        return endPositions;
    }

    public List<ChessPosition> getPawnEndPositions(List<List<Integer>> directions, ChessBoard board, ChessPiece piece, ChessPosition myPosition) {
        var endPositions = new ArrayList<ChessPosition>();
        if (pieceColor == ChessGame.TeamColor.WHITE) {
            var upPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
            if (board.getPiece(upPosition) == null) {endPositions.add(upPosition);}
            if (myPosition.getRow() == 2) {
                var upTwicePosition = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
                if (board.getPiece(upTwicePosition) == null && board.getPiece(upPosition) == null) {endPositions.add(upTwicePosition);}
                //both forward spaces unoccupied
            }
            var upLeftPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
            // do i need to check both null and enemy? will attempting to check teamColor of null be bad?
            if (board.getPiece(upLeftPosition) != null && board.getPiece(upLeftPosition).getTeamColor() != pieceColor) {endPositions.add(upLeftPosition);}
            var upRightPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
            if (board.getPiece(upRightPosition) != null && board.getPiece(upRightPosition).getTeamColor() != pieceColor) {endPositions.add(upRightPosition);}
        }
        if (pieceColor == ChessGame.TeamColor.BLACK) {
            var downPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
            if (board.getPiece(downPosition) == null) {endPositions.add(downPosition);}
            if (myPosition.getRow() == 7) {
                var downTwicePosition = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());
                if (board.getPiece(downTwicePosition) == null && board.getPiece(downPosition) == null) {endPositions.add(downTwicePosition);}
                // both downward spaces unoccupied
            }
            var downLeftPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
            // do i need to check both null and enemy? will attempting to check teamColor of null be bad?
            if (board.getPiece(downLeftPosition) != null && board.getPiece(downLeftPosition).getTeamColor() != pieceColor) {endPositions.add(downLeftPosition);}
            var downRightPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
            if (board.getPiece(downRightPosition) != null && board.getPiece(downRightPosition).getTeamColor() != pieceColor) {endPositions.add(downRightPosition);}
        }
        return endPositions;
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
        List<ChessPosition> endPositions = null;

        if (piece.getPieceType() == PieceType.BISHOP) {
            List<List<Integer>> directions = Arrays.asList(Arrays.asList(1, 1),Arrays.asList(-1, 1),Arrays.asList(1, -1),Arrays.asList(-1, -1));
            endPositions = getEndPositions(true, directions, board, piece, myPosition);
        }
        if (piece.getPieceType() == PieceType.QUEEN) {
            List<List<Integer>> directions = Arrays.asList(Arrays.asList(0, -1), Arrays.asList(-1, 0), Arrays.asList(0, 1), Arrays.asList(1, 0), Arrays.asList(1, 1),Arrays.asList(-1, 1),Arrays.asList(1, -1),Arrays.asList(-1, -1));
            endPositions = getEndPositions(true, directions, board, piece, myPosition);
        }
        if (piece.getPieceType() == PieceType.ROOK) {
            List<List<Integer>> directions = Arrays.asList(Arrays.asList(0, -1), Arrays.asList(-1, 0), Arrays.asList(0, 1), Arrays.asList(1, 0));
            endPositions = getEndPositions(true, directions, board, piece, myPosition);
        }
        if (piece.getPieceType() == PieceType.KNIGHT) {
            List<List<Integer>> directions = Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, -2), Arrays.asList(-1, 2), Arrays.asList(-1, -2), Arrays.asList(2, 1), Arrays.asList(2, -1), Arrays.asList(-2, 1), Arrays.asList(-2, -1));
            endPositions = getEndPositions(false, directions, board, piece, myPosition);
        }
        if (piece.getPieceType() == PieceType.KING) {
            List<List<Integer>> directions = Arrays.asList(Arrays.asList(1, 1), Arrays.asList(1, -1), Arrays.asList(-1, 1), Arrays.asList(-1, -1), Arrays.asList(-1, 0), Arrays.asList(1, 0), Arrays.asList(0, -1), Arrays.asList(0, 1));
            endPositions = getEndPositions(false, directions, board, piece, myPosition);
        }
        if (piece.getPieceType() == PieceType.PAWN) {
            List<List<Integer>> directions = Arrays.asList(Arrays.asList(1, 0), Arrays.asList(2, 0), Arrays.asList(1, 1), Arrays.asList(1, -1));
            endPositions = getPawnEndPositions(directions, board, piece, myPosition);
        }

        endPositions.sort(Comparator.comparing(ChessPosition::toString));
        for (var endPosition : endPositions) {
            var move = new ChessMove(myPosition, endPosition, null);
            moves.add(move);
        }
        return moves;
    }


}
