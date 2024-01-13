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
    private final boolean[] blackDoubleMoved =new boolean[8];
    private final boolean[] whiteDoubleMoved =new boolean[8];
    private final boolean[] blackCanCastle=new boolean[]{true, true};
    private final boolean[] whiteCanCastle=new boolean[]{true, true};

    public ChessBoard() {}
    private ChessBoard(ChessPiece[] pieces, boolean[] BEP, boolean[] WEP, boolean[] BCC, boolean[] WCC){
        for(int i=0;i<pieces.length;i++){
            this.pieces[i]=pieces[i]==null?null:pieces[i].clone();
        }
        System.arraycopy(BEP, 0, this.blackDoubleMoved, 0, 8);
        System.arraycopy(WEP, 0, this.whiteDoubleMoved, 0, 8);
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

        Arrays.fill(this.whiteDoubleMoved, false);
        Arrays.fill(this.blackDoubleMoved, false);
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

            for(var move : this.pieces[i].pieceMoves(this,
                    new ChessPosition((int) Math.floor(i/8f)+1, i%8+1), false)){
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

            if(this.validMovesOf(new ChessPosition((int) Math.floor(i/8f)+1, i%8+1)).size()>0)
                return false;
        }
        return true;
    }

    public boolean isInCheckMate(ChessGame.TeamColor color){
        return this.isInCheck(color)&&!this.hasValidMoves(color);
    }

    //--

    public boolean canEnPassantTo(ChessGame.TeamColor color, ChessPosition pos){
        if(!pos.isValid()) return false;
        return color.whiteOrBlack(this.blackDoubleMoved,this.whiteDoubleMoved)[pos.getColumn()-1];
    }
    public void clearDidDoubleMove(ChessGame.TeamColor color){
        if(color== ChessGame.TeamColor.WHITE)
            Arrays.fill(this.whiteDoubleMoved, false);
        else if(color== ChessGame.TeamColor.BLACK)
            Arrays.fill(this.blackDoubleMoved, false);
    }

    public void setDoubleMoved(int col, ChessGame.TeamColor color){
        color.whiteOrBlack(this.whiteDoubleMoved,this.blackDoubleMoved)[col-1]=true;
    }

    public static boolean checkPiece(ChessBoard board, ChessPiece piece, ChessPosition pos){
        var toCheck=board.pieces[pos.toIndex()];
        if(toCheck==null) return false;
        return toCheck.getPieceType() == piece.getPieceType() && toCheck.getTeamColor() == piece.getTeamColor();
    }
    public boolean canCastle(ChessGame.TeamColor color, CastleMove.Side side){
        if(!checkPiece(this, new ChessPiece(color, ChessPiece.PieceType.KING),
                new ChessPosition(color.row,5))||
                !checkPiece(this, new ChessPiece(color, ChessPiece.PieceType.ROOK),
                    new ChessPosition(color.row,side.x))) return false;

        if(color.whiteOrBlack(this.whiteCanCastle,this.blackCanCastle)
                [side==CastleMove.Side.QUEENSIDE?0:1]){
            if(this.isInCheck(color)) return false;
            for(int i=side.x-side.direc;i!=5;i-=side.direc){
                var pos=new ChessPosition(color.row, i);
                if(this.getPiece(pos)!=null) return false;
                var futureBoard = this.clone();
                futureBoard.addPiece(pos, new ChessPiece(color, ChessPiece.PieceType.KING));
                if(futureBoard.isInCheck(color)) return false;
            }
            return true;
        }
        return false;
    }
    public void removeCastlePrivileges(ChessGame.TeamColor color, CastleMove.Side side){
        if(color== ChessGame.TeamColor.WHITE) this.whiteCanCastle[side==CastleMove.Side.QUEENSIDE?0:1]=false;
        else if(color== ChessGame.TeamColor.BLACK) this.blackCanCastle[side==CastleMove.Side.QUEENSIDE?0:1]=false;
    }

    //--

    public ChessBoard clone(){
        return new ChessBoard(this.pieces, this.blackDoubleMoved, this.whiteDoubleMoved,
                this.blackCanCastle, this.whiteCanCastle);
    }

    //--

    public boolean equals(Object other){
        if(!(other instanceof ChessBoard otherBoard)) return false;
        return Arrays.equals(this.pieces, otherBoard.pieces)&&
                Arrays.equals(this.whiteDoubleMoved, otherBoard.whiteDoubleMoved)&&
                Arrays.equals(this.blackDoubleMoved, otherBoard.blackDoubleMoved);
    }
}
