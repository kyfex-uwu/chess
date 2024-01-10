package chess;

import chess.specialmoves.CastleMove;
import chess.specialmoves.EnPassantMove;

import java.util.*;
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
                    moves.add(List.of(new Offset(x, y)));
                }
            }
        }),
        QUEEN(new Offset[]{
                new Offset(-1,-1),new Offset(0,-1),new Offset(1,-1),
                new Offset(-1,0),/*new Offset(0,0),*/new Offset(1,0),
                new Offset(-1,1),new Offset(0,1),new Offset(1,1),
        }),
        BISHOP(new Offset[]{
                new Offset(-1,-1),new Offset(1,-1),
                new Offset(-1,1),new Offset(1,1),
        }),
        KNIGHT(moves->{
            moves.add(List.of(new Offset(-1,-2)));
            moves.add(List.of(new Offset(1,-2)));
            moves.add(List.of(new Offset(-1,2)));
            moves.add(List.of(new Offset(1,2)));

            moves.add(List.of(new Offset(-2,-1)));
            moves.add(List.of(new Offset(2,-1)));
            moves.add(List.of(new Offset(-2,1)));
            moves.add(List.of(new Offset(2,1)));
        }),
        ROOK(new Offset[]{
                new Offset(0,-1),new Offset(0,1),
                new Offset(-1,0),new Offset(1,0),
        }),
        PAWN(new Offset[]{});//pawns are handled specially
        public final Collection<List<Offset>> moves;
        PieceType(Consumer<Collection<List<Offset>>> moveFunc){
            var collection = new ArrayList<List<Offset>>();
            moveFunc.accept(collection);
            this.moves=Collections.unmodifiableCollection(collection);
        }
        PieceType(Offset[] offsets){
            var collection = new ArrayList<List<Offset>>();
            for (Offset offset : offsets) {
                List<Offset> list = new ArrayList<>();
                for (int j = 1; j < 8; j++) {
                    list.add(offset.mult(j));
                }
                collection.add(list);
            }
            this.moves=Collections.unmodifiableCollection(collection);
        }
    }

    public record Offset(int x, int y) {
        public Offset mult(int mult){
            return new Offset(this.x*mult, this.y*mult);
        }
    }

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
        var move = (endOffs.x!=0&&endOffs.y!=0&&board.getPiece(start.addOffset(endOffs))==null)?
                new ChessMove(start, start.addOffset(endOffs), null):
                new EnPassantMove(start, start.addOffset(endOffs), null);
        if(!move.getEndPosition().isValid()) return;
        var destPiece=board.getPiece(move.getEndPosition());
        if((destPiece==null)==pieceAtDest) return;
        if(destPiece!=null&&destPiece.color==this.color) return;

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
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, boolean canCastle) {
        if(this.type==PieceType.PAWN){
            var toReturn = new ArrayList<ChessMove>();
            var yOffs = this.color==ChessGame.TeamColor.WHITE?1:-1;

            if(myPosition.getRow()==(this.color==ChessGame.TeamColor.WHITE?2:7)&&
                    board.getPiece(myPosition.addOffset(new Offset(0,yOffs)))==null){
                this.addPawnMove(myPosition, new Offset(0,yOffs*2), board, false, toReturn);
            }else if(myPosition.getRow()==(this.color==ChessGame.TeamColor.WHITE?5:4)){
                if(board.canEnPassant(this.color, myPosition.addOffset(new Offset(1,0)))){
                    this.addPawnMove(myPosition, new Offset(1,yOffs), board, false, toReturn);
                }
                if(board.canEnPassant(this.color, myPosition.addOffset(new Offset(-1,0)))){
                    this.addPawnMove(myPosition, new Offset(-1,yOffs), board, false, toReturn);
                }
            }

            this.addPawnMove(myPosition, new Offset(0,yOffs), board, false, toReturn);
            this.addPawnMove(myPosition, new Offset(1,yOffs), board, true, toReturn);
            this.addPawnMove(myPosition, new Offset(-1,yOffs), board, true, toReturn);
            return toReturn;
        }

        var toReturn = new ArrayList<ChessMove>();
        for(var chain : this.type.moves){
            for(var offs : chain){
                var newPos = myPosition.addOffset(offs);
                if(!newPos.isValid()) break;
                var pieceAt=board.getPiece(newPos);
                if(pieceAt==null||pieceAt.color!=this.color){
                    toReturn.add(new ChessMove(myPosition, newPos, null));
                }else{
                    break;
                }
                if(pieceAt!=null) break;
            }
        }

        if(this.type==PieceType.KING&&canCastle){
            if(board.canCastle(this.color, CastleMove.Side.KINGSIDE))
                toReturn.add(new CastleMove(this.color, CastleMove.Side.KINGSIDE));
            if(board.canCastle(this.color, CastleMove.Side.QUEENSIDE))
                toReturn.add(new CastleMove(this.color, CastleMove.Side.QUEENSIDE));
        }
        return toReturn;
    }
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return this.pieceMoves(board, myPosition, true);
    }

    public String toString(){
        return this.color+"-"+this.type;
    }
    public boolean equals(Object other){
        if(!(other instanceof ChessPiece otherPiece)) return false;
        return this.color==otherPiece.color&&this.type==otherPiece.type;
    }
    public ChessPiece clone(){
        return new ChessPiece(this.color, this.type);
    }
}
