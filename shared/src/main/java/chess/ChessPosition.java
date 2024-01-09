package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row;
    private final int column;

    public ChessPosition(int row, int col) {
        this.row=row;
        this.column=col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return this.column;
    }

    ChessPosition addOffset(ChessPiece.Offset offset){
        return new ChessPosition(this.row+offset.x(),this.column+offset.y());
    }

    public boolean isValid(){
        return this.row>=1&&this.row<=8&&
                this.column>=1&&this.column<=8;
    }
}
