package chess;

import chess.specialmoves.CastleMove;

import java.util.Arrays;
import java.util.Collection;

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
    private final boolean[] blackCanCastle=new boolean[]{true, true};
    private final boolean[] whiteCanCastle=new boolean[]{true, true};

    public ChessBoard() {}
    private ChessBoard(ChessPiece[] pieces, boolean[] BEP, boolean[] WEP, boolean[] BCC, boolean[] WCC){
        for(int i=0;i<pieces.length;i++){
            this.pieces[i]=pieces[i]==null?null:pieces[i].clone();
        }
        System.arraycopy(BEP, 0, this.blackEnPassantable, 0, 8);
        System.arraycopy(WEP, 0, this.whiteEnPassantable, 0, 8);
        System.arraycopy(BCC, 0, this.blackCanCastle, 0, 2);
        System.arraycopy(WCC, 0, this.whiteCanCastle, 0, 2);
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

        Arrays.fill(this.whiteEnPassantable, false);
        Arrays.fill(this.blackEnPassantable, false);
        Arrays.fill(this.whiteCanCastle, true);
        Arrays.fill(this.blackCanCastle, true);
    }

    //--

    public Collection<ChessMove> validMovesOf(ChessPosition startPosition) {
        var piece=this.getPiece(startPosition);
        if(piece==null) return null;
        return piece.pieceMoves(this, startPosition).stream().filter(move->{
            var futureBoard = this.clone();
            move.apply(futureBoard);
            return !futureBoard.isInCheck(piece.getTeamColor());
        }).toList();
    }

    public boolean isInCheck(ChessGame.TeamColor color){
        for(int i=0;i<64;i++){
            if(this.pieces[i]==null||this.pieces[i].getTeamColor()==color) continue;

            for(var move : this.validMovesOf(new ChessPosition(i%8+1, (int) Math.floor(i/8f)+1))){
                var destPiece = this.getPiece(move.getEndPosition());
                if(destPiece!=null&&destPiece.getPieceType()==ChessPiece.PieceType.KING
                        &&destPiece.getTeamColor()==color)
                    return true;
            }
        }
        return false;
    }

    public boolean hasValidMoves(ChessGame.TeamColor color){
        for(int i=0;i<64;i++){
            if(this.pieces[i]==null||this.pieces[i].getTeamColor()!=color) continue;

            if(this.validMovesOf(new ChessPosition(i%8+1, (int) Math.floor(i/8f)+1)).size()>0)
                return false;
        }
        return true;
    }

    public boolean isInCheckMate(ChessGame.TeamColor color){
        return this.isInCheck(color)&&!this.hasValidMoves(color);
    }

    //--

    public boolean isEnPassantable(ChessGame.TeamColor color, ChessPosition pos){
        if(!pos.isValid()) return false;
        return (color==ChessGame.TeamColor.WHITE?this.whiteEnPassantable:this.blackEnPassantable)[pos.getColumn()];
    }
    public void clearEnPassantables(ChessGame.TeamColor color){
        if(color== ChessGame.TeamColor.WHITE)
            Arrays.fill(this.whiteEnPassantable, false);
        else if(color== ChessGame.TeamColor.BLACK)
            Arrays.fill(this.blackEnPassantable, false);
    }

    public void setEnPassantable(int col, ChessGame.TeamColor color){
        (color== ChessGame.TeamColor.WHITE?
                this.whiteEnPassantable:
                this.blackEnPassantable)[col]=true;
    }

    public boolean canCastle(ChessGame.TeamColor color, CastleMove.Side side){
        if((color== ChessGame.TeamColor.WHITE&&this.whiteCanCastle[side==CastleMove.Side.QUEENSIDE?0:1])||
                (color== ChessGame.TeamColor.BLACK&&this.blackCanCastle[side==CastleMove.Side.QUEENSIDE?0:1])){

            if(this.isInCheck(color)) return false;
            for(int i=side.x-side.direc;i!=5;i-=side.direc){
                var pos=new ChessPosition(i,color==ChessGame.TeamColor.WHITE?1:8);
                if(this.getPiece(pos)!=null) return false;
                var futureBoard = this.clone();
                futureBoard.addPiece(pos, new ChessPiece(color, ChessPiece.PieceType.KING));
                if(futureBoard.isInCheck(color)) return false;
            }
        }
        return false;
    }
    public void removeCastlePrivileges(ChessGame.TeamColor color, CastleMove.Side side){
        if(color== ChessGame.TeamColor.WHITE) this.whiteCanCastle[side==CastleMove.Side.QUEENSIDE?0:1]=false;
        else if(color== ChessGame.TeamColor.BLACK) this.blackCanCastle[side==CastleMove.Side.QUEENSIDE?0:1]=false;
    }

    //--

    public ChessBoard clone(){
        return new ChessBoard(this.pieces, this.blackEnPassantable, this.whiteEnPassantable,
                this.blackCanCastle, this.whiteCanCastle);
    }

    //--

    public boolean equals(Object other){
        if(!(other instanceof ChessBoard otherBoard)) return false;
        return Arrays.equals(this.pieces, otherBoard.pieces)&&
                Arrays.equals(this.whiteEnPassantable, otherBoard.whiteEnPassantable)&&
                Arrays.equals(this.blackEnPassantable, otherBoard.blackEnPassantable);
    }
}
