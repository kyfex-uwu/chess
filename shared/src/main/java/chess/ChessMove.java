package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    private final ChessPosition start;
    private final ChessPosition end;
    private final ChessPiece.PieceType promotionPiece;


    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.start=startPosition;
        this.end=endPosition;
        this.promotionPiece=promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return this.start;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return this.end;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return this.promotionPiece;
    }

    public ChessMove withPromotionPiece(ChessPiece.PieceType type){
        return new ChessMove(this.start, this.end, type);
    }

    public void apply(ChessBoard board){
        var startPiece=board.getPiece(this.start);
        board.addPiece(this.end, this.promotionPiece==null ?
                startPiece :
                new ChessPiece(startPiece.getTeamColor(),this.promotionPiece));
        board.addPiece(this.start, null);
    }

    public String toString(){
        return this.start+" -> "+this.end+(this.promotionPiece!=null?":"+this.promotionPiece:"");
    }
    public boolean equals(Object other){
        return other instanceof ChessMove otherMove &&
                otherMove.start.equals(this.start) && otherMove.end.equals(this.end) &&
                otherMove.promotionPiece==this.promotionPiece;
    }
    public int hashCode(){
        return this.start.hashCode()*64+this.end.hashCode()+
                (this.promotionPiece==null?0:(this.promotionPiece.ordinal()+1)*64*64);
    }
}
