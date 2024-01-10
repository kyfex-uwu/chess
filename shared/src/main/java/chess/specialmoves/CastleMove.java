package chess.specialmoves;

import chess.*;

public class CastleMove extends ChessMove {
    public enum Side{
        KINGSIDE(8, 1),
        QUEENSIDE(1, -1);

        public final int x;
        public final int direc;
        Side(int castleX, int direc){
            this.x=castleX;
            this.direc=direc;
        }
    }
    private final Side side;
    private final ChessGame.TeamColor color;
    public CastleMove(ChessGame.TeamColor color, Side side) {
        super(new ChessPosition(color==ChessGame.TeamColor.WHITE?1:8,5),
                new ChessPosition(color==ChessGame.TeamColor.WHITE?1:8,5+side.direc*2), null);
        this.side=side;
        this.color=color;
    }

    public void apply(ChessBoard board){
        var y=this.color== ChessGame.TeamColor.WHITE?1:8;
        board.addPiece(new ChessPosition(this.side.x,y), null);
        board.addPiece(new ChessPosition(5,y), null);
        board.addPiece(new ChessPosition(5+this.side.direc*2,y),
                new ChessPiece(this.color, ChessPiece.PieceType.KING));
        board.addPiece(new ChessPosition(5+this.side.direc,y),
                new ChessPiece(this.color, ChessPiece.PieceType.ROOK));

        board.removeCastlePrivileges(this.color, this.side);
    }

    public String toString(){
        return this.side.toString()+" "+this.color;
    }
    public int hashCode(){
        return 40960+this.side.ordinal()+this.color.ordinal()*2;
    }
}
