package chess;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    ChessBoard board;
    ChessGame.TeamColor currentTurn = TeamColor.WHITE;
    boolean isGameOver = false;

    public ChessGame() {
        this.board = new ChessBoard();
        board.resetBoard(); //creating a game object will start with a default board
    }


    public void endGame() {
        isGameOver = true;
    }

    public boolean getIsGameOver() {
        return isGameOver;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
        //switches turn
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves (legal moves that do not leave the king in danger) for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public HashSet<ChessMove> validMoves(ChessPosition startPosition) {
        if (board.getPiece(startPosition) == null) {
            return null;
        }
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        var validMoves = new HashSet<ChessMove>();
        for (ChessMove possibleMove : possibleMoves) { // test move, add if safe
            board.removePiece(startPosition);
            var currPiece = board.getPiece(possibleMove.getEndPosition()); // test move might eliminate an enemy
            board.addPiece(possibleMove.getEndPosition(), piece);
            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(possibleMove);
            }
            board.removePiece(possibleMove.getEndPosition());
            board.addPiece(possibleMove.getEndPosition(), currPiece);
            board.addPiece(startPosition, piece);
        }
        return validMoves;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        var piece = board.getPiece(startPosition);
        if (piece != null && piece.getTeamColor() != currentTurn) { // not your turn
            throw new InvalidMoveException("Not your turn");
        } else if (piece == null) { // no piece there
            throw new InvalidMoveException("No piece found");
        }
        var validMoves = validMoves(startPosition);
        for (ChessMove validMove : validMoves) {
            if (validMove.equals(move)) { // if valid, make move
                board.removePiece(startPosition);
                board.addPiece(endPosition, piece);
                if (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getPromotionPiece() != null) {
                    board.addPiece(endPosition, new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
                }
                setTeamTurn(piece.getTeamColor() == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK);
                return;
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getPromotionPiece() == null
            && (move.endPosition.getRow() == 1 || move.endPosition.getRow() == 8)) {
            throw new InvalidMoveException("Move requires pawn promotion piece");
        }
        throw new InvalidMoveException("Invalid move"); // not valid move
    }


    public HashSet<ChessPosition> getTeamPositions(ChessGame.TeamColor myTeamColor) {
        var teamPositions = new HashSet<ChessPosition>();
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                var piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null && piece.getTeamColor() == myTeamColor) {
                    teamPositions.add(new ChessPosition(i, j));
                }
            }
        }
        return teamPositions;
    }

    public ChessPosition getKingPosition(ChessGame.TeamColor teamColor) {
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                var piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING &&
                        piece.getTeamColor() == teamColor) {
                    return new ChessPosition(i, j);
                }
            }
        }
        return null; // won't trigger since there's always a king
    }


    /**Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // find king and also all enemy pieces
        ChessPosition kingPosition = getKingPosition(teamColor);
        TeamColor enemyColor = teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
        HashSet<ChessPosition> enemyPositions = getTeamPositions(enemyColor);

        for (ChessPosition enemyPosition : enemyPositions) {
            var enemyPiece = board.getPiece(enemyPosition);
            var enemyMoves = enemyPiece.pieceMoves(board, enemyPosition);
            for (ChessMove enemyMove : enemyMoves) {
                if (enemyMove.endPosition.equals(kingPosition)) {
                    return true; // in check
                }
            }
        }
        return false; // not in check
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        var allyPositions = getTeamPositions(teamColor);

        for (ChessPosition allyPosition : allyPositions) {
            var allyPiece = board.getPiece(allyPosition);
            var allyMoves = allyPiece.pieceMoves(board, allyPosition);
            for (ChessMove allyMove : allyMoves) {
                var startPosition = allyMove.getStartPosition();
                board.removePiece(startPosition);
                var currPiece = board.getPiece(allyMove.getEndPosition()); // test move might eliminate an enemy
                board.addPiece(allyMove.getEndPosition(), allyPiece);
                if (!isInCheck(teamColor)) {
                    return false; // there exists a move where you aren't in check
                }
                board.addPiece(allyMove.getEndPosition(), currPiece);
                board.addPiece(startPosition, allyPiece);
            }
        }
        return true; // no move found that doesn't put you in check
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false; // no stalemate if in danger
        }
        var allyPositions = getTeamPositions(teamColor);

        for (ChessPosition allyPosition : allyPositions) {
            var allyMoves = validMoves(allyPosition);
                if (!allyMoves.isEmpty()) {
                    return false; // possible moves, no stalemate
                }
        }
        return true; // no moves, no danger = stalemate
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }


    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTurn == chessGame.currentTurn;
    }


    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }
}
