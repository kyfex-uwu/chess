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

    /**
     * @return the integer 1-64 corresponding to this position <br>
     * a1 -> 0, a2 -> 1, b1 -> 8, h8 -> 63
     */

    public int toIndex(){ return (this.column-1)+(this.row-1)*8; }

    /**
     * Returns a new {@code ChessPosition} with the offset applied
     * @param offset the offset to apply to the position
     * @return the new offset position
     */
    public ChessPosition addOffset(ChessPiece.Offset offset){
        return new ChessPosition(this.row+offset.y(),this.column+offset.x());
    }

    /**
     * @return if this position is on the chessboard
     */
    public boolean isValid(){
        return this.row>=1&&this.row<=8&&
                this.column>=1&&this.column<=8;
    }

    public String toString(){
        return ""+(char)(this.column+96)+this.row;
    }
    public boolean equals(Object other){
        return other instanceof ChessPosition otherPos &&
                otherPos.row==this.row&&otherPos.column==this.column;
    }
    public int hashCode(){
        return this.toIndex();
    }
}
