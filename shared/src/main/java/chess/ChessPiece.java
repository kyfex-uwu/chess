package chess;

import chess.specialmoves.CastleMove;
import chess.specialmoves.DoublePawnMove;
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

    /**
     * An x,y pair that can modify {@code ChessPosition}s
     * @param x (column offset, left and right)
     * @param y (row offset, up and down)
     */
    public record Offset(int x, int y) {
        /**
         * Returns a new {@code Offset} with the x and y multiplied by {@code mult}
         * @param mult offset scalar
         * @return new {@code Offset}
         */
        public Offset mult(int mult){
            return new Offset(this.x*mult, this.y*mult);
        }
        public boolean equals(Object other){
            if(!(other instanceof Offset otherOffset)) return false;
            return otherOffset.x==this.x&&otherOffset.y==this.y;
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

    /**
     * Adds a pawn move from start to end, accounting for en passant and a double move
     * @param start start position
     * @param endOffs end <b>offset</b>, not position
     * @param board chessboard to add to
     * @param pieceAtDest if there should be a piece at this destination (if not, don't add the move)
     * @param moveList the list to add the move to, if it is valid
     */
    private void addPawnMove(ChessPosition start, Offset endOffs, ChessBoard board, boolean pieceAtDest,
                             Collection<ChessMove> moveList){
        ChessMove move;
        if(endOffs.x!=0&&endOffs.y!=0&&board.getPiece(start.addOffset(endOffs))==null){
            move = new EnPassantMove(start, start.addOffset(endOffs), null);
        }else if(Math.abs(endOffs.y)==2){
            move = new DoublePawnMove(start, start.addOffset(endOffs), null);
        }else{
            move=new ChessMove(start, start.addOffset(endOffs), null);
        }

        if(!move.getEndPosition().isValid()) return;
        var destPiece=board.getPiece(move.getEndPosition());
        if((destPiece==null)==pieceAtDest) return;
        if(destPiece!=null&&destPiece.color==this.color) return;

        if(move.getEndPosition().getRow()==(this.color.opposite().row)) {
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
     * You probably want to use pieceMoves(ChessBoard board, ChessPosition myPosition),
     * this only exists for check recursion reasons
     * @see #pieceMoves(ChessBoard, ChessPosition)
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, boolean canCastle) {
        if(this.type==PieceType.PAWN){
            var toReturn = new HashSet<ChessMove>();

            if(myPosition.getRow()==this.color.row+this.color.advDir&&
                    board.getPiece(myPosition.addOffset(new Offset(0,this.color.advDir)))==null){
                this.addPawnMove(myPosition, new Offset(0,this.color.advDir*2), board, false, toReturn);
            }else if(myPosition.getRow()==this.color.row+this.color.advDir*4){
                if(board.canEnPassantTo(this.color, myPosition.addOffset(new Offset(1,0)))){
                    this.addPawnMove(myPosition, new Offset(1,this.color.advDir), board, false, toReturn);
                }
                if(board.canEnPassantTo(this.color, myPosition.addOffset(new Offset(-1,0)))){
                    this.addPawnMove(myPosition, new Offset(-1,this.color.advDir), board, false, toReturn);
                }
            }

            this.addPawnMove(myPosition, new Offset(0,this.color.advDir), board, false, toReturn);
            this.addPawnMove(myPosition, new Offset(1,this.color.advDir), board, true, toReturn);
            this.addPawnMove(myPosition, new Offset(-1,this.color.advDir), board, true, toReturn);
            return toReturn;
        }

        var toReturn = new HashSet<ChessMove>();
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

    /**
     * @see #pieceMoves(ChessBoard, ChessPosition, boolean)
     */
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
