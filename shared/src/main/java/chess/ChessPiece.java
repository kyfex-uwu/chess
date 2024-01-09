package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final PieceType type;
    private final ChessGame.TeamColor color;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.color=pieceColor;
        this.type=type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING(()->{
            var toReturn = new ArrayList<Offset>();
            for(int y=-1;y<=1;y++){
                for(int x=-1;x<=1;x++){
                    if(y==0&&x==0) continue;
                    toReturn.add(new Offset(x, y));
                }
            }
            return toReturn;
        }),
        QUEEN(()->{
            var toReturn = new ArrayList<Offset>();
            for(int i=-7;i<=7;i++){
                if(i==0) continue;
                toReturn.add(new Offset(i,i));
                toReturn.add(new Offset(-i,i));
                toReturn.add(new Offset(i,0));
                toReturn.add(new Offset(0,i));
            }
            return toReturn;
        }),
        BISHOP(()->{
            var toReturn = new ArrayList<Offset>();
            for(int i=-7;i<=7;i++){
                if(i==0) continue;
                toReturn.add(new Offset(i,i));
                toReturn.add(new Offset(-i,i));
            }
            return toReturn;
        }),
        KNIGHT(()->{
            var toReturn = new ArrayList<Offset>();
            toReturn.add(new Offset(-1,-2));
            toReturn.add(new Offset(1,-2));
            toReturn.add(new Offset(-1,2));
            toReturn.add(new Offset(-1,2));

            toReturn.add(new Offset(-2,-1));
            toReturn.add(new Offset(-2,1));
            toReturn.add(new Offset(2,-1));
            toReturn.add(new Offset(2,1));
            return toReturn;
        }),
        ROOK(()->{
            var toReturn = new ArrayList<Offset>();
            for(int i=-7;i<=7;i++){
                if(i==0) continue;
                toReturn.add(new Offset(i,0));
                toReturn.add(new Offset(0,i));
            }
            return toReturn;
        }),
        PAWN(ArrayList::new);//pawns are handled specially
        public final Collection<Offset> moves;
        PieceType(Supplier<Collection<Offset>> moveFunc){
            this.moves=Collections.unmodifiableCollection(moveFunc.get());
        }
    }

    record Offset(int x, int y) {}

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if(this.type==PieceType.PAWN){
            var toReturn = new ArrayList<ChessMove>();
            /*
            if(this.color==ChessGame.TeamColor.WHITE){
                if(myPosition.getRow()==2){}
                else if(myPosition.getRow()==5){}

                if(board.getPiece(myPosition.addOffset(new Offset(0,1)))!=null)
                    toReturn.add(new ChessMove(myPosition, myPosition.addOffset(new Offset(0,1)), null)));
            }
            */
            return toReturn;
        }
        var toReturn = new ArrayList<ChessMove>();
        for(var offset : this.type.moves){
            var newPos = myPosition.addOffset(offset);
            if(newPos.getRow()>=1&&newPos.getRow()<=8&&
                newPos.getColumn()>=1&&newPos.getColumn()<=8){
                toReturn.add(new ChessMove(myPosition, newPos, null));
            }
        }
        return toReturn;
    }
}
