package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[] pieces = new ChessPiece[8*8];
    private final boolean[] blackEnPassantable=new boolean[8];
    private final boolean[] whiteEnPassantable=new boolean[8];

    public ChessBoard() {
        this.resetBoard();
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        if(!position.isValid()) return;
        this.pieces[position.toIndex()]=piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if(!position.isValid()) return null;
        return this.pieces[position.toIndex()];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        Arrays.fill(this.pieces, null);

        for(int i=0;i<2;i++){
            int offs=i==0?0:56;
            ChessGame.TeamColor color = i==0? ChessGame.TeamColor.WHITE: ChessGame.TeamColor.BLACK;

            this.pieces[0+offs]=new ChessPiece(color, ChessPiece.PieceType.ROOK);
            this.pieces[7+offs]=new ChessPiece(color, ChessPiece.PieceType.ROOK);
            this.pieces[1+offs]=new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
            this.pieces[6+offs]=new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
            this.pieces[2+offs]=new ChessPiece(color, ChessPiece.PieceType.BISHOP);
            this.pieces[5+offs]=new ChessPiece(color, ChessPiece.PieceType.BISHOP);
            this.pieces[3+offs]=new ChessPiece(color, ChessPiece.PieceType.QUEEN);
            this.pieces[4+offs]=new ChessPiece(color, ChessPiece.PieceType.KING);
        }

        for(int i=0;i<8;i++){
            this.pieces[8+i]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            this.pieces[48+i]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }

        this.clearEnPassantables();
    }

    public boolean isEnPassantable(ChessGame.TeamColor color, ChessPosition pos){
        if(!pos.isValid()) return false;
        return (color==ChessGame.TeamColor.WHITE?this.whiteEnPassantable:this.blackEnPassantable)[pos.getColumn()];
    }
    public void clearEnPassantables(){
        Arrays.fill(this.blackEnPassantable, false);
        Arrays.fill(this.whiteEnPassantable, false);
    }
}
