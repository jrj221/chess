package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    public final int row;
    public final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * #1 is the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * #1 is the leftmost col
     */
    public int getColumn() {
        return col;
    }

    @Override
    public String toString() {
        return String.format("%d:%d", row, col);
    }

    //TODO edit equals and hashcode

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
