package chess;

import java.util.Collection;
import java.util.HashSet;

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
        ChessPosition endPosition = new ChessPosition(myPosition.getRow() + 3, myPosition.getColumn() - 2);
        if (type.equals(ChessPiece.PieceType.BISHOP)) {
            if (ChessMove.isLegal(myPosition.getRow() + 3, myPosition.getColumn() - 2)) {
                ChessMove move = new ChessMove(myPosition, endPosition, null);
                moves.add(move);
            }
        }
        return moves;
    }


}
