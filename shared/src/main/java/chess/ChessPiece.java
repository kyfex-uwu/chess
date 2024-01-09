package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

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
        KING(moves->{
            for(int y=-1;y<=1;y++){
                for(int x=-1;x<=1;x++){
                    if(y==0&&x==0) continue;
                    moves.add(new Offset(x, y));
                }
            }
        }),
        QUEEN(moves->{
            for(int i=-7;i<=7;i++){
                if(i==0) continue;
                moves.add(new Offset(i,i));
                moves.add(new Offset(-i,i));
                moves.add(new Offset(i,0));
                moves.add(new Offset(0,i));
            }
        }),
        BISHOP(moves->{
            for(int i=-7;i<=7;i++){
                if(i==0) continue;
                moves.add(new Offset(i,i));
                moves.add(new Offset(-i,i));
            }
        }),
        KNIGHT(moves->{
            moves.add(new Offset(-1,-2));
            moves.add(new Offset(1,-2));
            moves.add(new Offset(-1,2));
            moves.add(new Offset(-1,2));

            moves.add(new Offset(-2,-1));
            moves.add(new Offset(-2,1));
            moves.add(new Offset(2,-1));
            moves.add(new Offset(2,1));
        }),
        ROOK(moves->{
            for(int i=-7;i<=7;i++){
                if(i==0) continue;
                moves.add(new Offset(i,0));
                moves.add(new Offset(0,i));
            }
        }),
        PAWN(moves->{});//pawns are handled specially
        public final Collection<Offset> moves;
        PieceType(Consumer<Collection<Offset>> moveFunc){
            var collection = new ArrayList<Offset>();
            moveFunc.accept(collection);
            this.moves=Collections.unmodifiableCollection(collection);
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

    private void addPawnMove(ChessPosition start, Offset endOffs, ChessBoard board, boolean pieceAtDest,
                             ArrayList<ChessMove> moveList){
        var move = new ChessMove(start, start.addOffset(endOffs), null);
        if(!move.getEndPosition().isValid()) return;
        if((board.getPiece(move.getEndPosition())==null)!=pieceAtDest) return;
        //if (piece is present) != (need to capture)

        if(move.getEndPosition().getRow()==(this.color==ChessGame.TeamColor.WHITE?8:1)) {
            moveList.add(move.withPromotionPiece(PieceType.BISHOP));
            moveList.add(move.withPromotionPiece(PieceType.ROOK));
            moveList.add(move.withPromotionPiece(PieceType.KNIGHT));
            moveList.add(move.withPromotionPiece(PieceType.QUEEN));
        }else{
            moveList.add(move.withPromotionPiece(null));
        }
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
            var yOffs = this.color==ChessGame.TeamColor.WHITE?1:-1;

            if(myPosition.getRow()==(this.color==ChessGame.TeamColor.WHITE?2:7)){
                this.addPawnMove(myPosition, new Offset(0,yOffs*2), board, false, toReturn);
            }else if(myPosition.getRow()==(this.color==ChessGame.TeamColor.WHITE?5:4)){
                if(board.enPassantable(this.color, myPosition.addOffset(new Offset(1,0)))){
                    this.addPawnMove(myPosition, new Offset(1,yOffs), board, false, toReturn);
                }
                if(board.enPassantable(this.color, myPosition.addOffset(new Offset(-1,0)))){
                    this.addPawnMove(myPosition, new Offset(-1,yOffs), board, false, toReturn);
                }
            }

            this.addPawnMove(myPosition, new Offset(0,yOffs), board, false, toReturn);
            this.addPawnMove(myPosition, new Offset(1,yOffs), board, true, toReturn);
            this.addPawnMove(myPosition, new Offset(-1,yOffs), board, true, toReturn);
            return toReturn;
        }

        var toReturn = new ArrayList<ChessMove>();
        for(var offset : this.type.moves){
            var newPos = myPosition.addOffset(offset);
            if(newPos.isValid()){
                toReturn.add(new ChessMove(myPosition, newPos, null));
            }
        }
        return toReturn;
    }
}
