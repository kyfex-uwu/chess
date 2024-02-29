package chess;

import chess.specialmoves.CastleMove;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

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

    /**
     * Returns a copy of this move, but with the promotion piece set to {@code type}
     * @param type piece to promote to (can be null)
     * @return new {@code ChessMove}
     */
    public ChessMove withPromotionPiece(ChessPiece.PieceType type){
        return new ChessMove(this.start, this.end, type);
    }


    /**
     * Applies this move to the chessboard. Does not check for validity
     * @param board board to make the move on
     */
    public ReversibleChessMove<? extends ChessMove> apply(ChessBoard board){
        var toReturn = new ReversibleChessMove<>(board, this);

        var startPiece=board.getPiece(this.start);
        board.addPiece(this.end, this.promotionPiece==null ?
                startPiece :
                new ChessPiece(startPiece.getTeamColor(),this.promotionPiece));
        board.addPiece(this.start, null);

        if(startPiece.getPieceType()== ChessPiece.PieceType.KING){
            board.removeCastlePrivileges(startPiece.getTeamColor());
        }else if(startPiece.getPieceType()== ChessPiece.PieceType.ROOK){
            if(this.getStartPosition().getColumn()==1)
                board.removeCastlePrivileges(startPiece.getTeamColor(), CastleMove.Side.QUEENSIDE);
            if(this.getStartPosition().getColumn()==8)
                board.removeCastlePrivileges(startPiece.getTeamColor(), CastleMove.Side.KINGSIDE);
        }

        return toReturn;
    }

    public String toString(){
        return this.start+" "+this.end+(this.promotionPiece!=null?" "+this.promotionPiece.identifier:"");
    }
    public boolean equals(Object other){
        return other instanceof ChessMove otherMove &&
                otherMove.start.equals(this.start) && otherMove.end.equals(this.end) &&
                otherMove.promotionPiece==this.promotionPiece;
    }
    public int hashCode(){
        return Objects.hash(this.start, this.end, this.promotionPiece);
    }

    //--

    public static class ReversibleChessMove<T extends ChessMove>{
        public final T move;
        public final ChessPiece takenPiece;
        public final String moveDataState;
        public final Consumer<ChessBoard> onReverse;
        protected ReversibleChessMove(T move, ChessPiece takenPiece,
                                      String moveDataState, Consumer<ChessBoard> onReverse){
            this.move=move;
            this.takenPiece=takenPiece;
            this.moveDataState=moveDataState;
            this.onReverse=onReverse;
        }

        //for use during a game
        public ReversibleChessMove(ChessBoard board, T move){
            this(move, board.getPiece(move.getEndPosition()), board.miscMoveDataToString(),
                    getDefaultConsumer(move,
                            board.getPiece(move.getStartPosition()),board.getPiece(move.getEndPosition()),
                            board.miscMoveDataToString()));
        }
        private static Consumer<ChessBoard> getDefaultConsumer(ChessMove move,
                                                               ChessPiece startPiece, ChessPiece endPiece,
                                                               String moveData){
            return board -> {
                board.addPiece(move.start, startPiece);
                board.addPiece(move.end, endPiece);
                board.applyMoveData(moveData);
            };
        }

        //for deserializing history
        public ReversibleChessMove(ChessBoard board, T move, ChessPiece takenPiece, String moveDataState){
            this(move, takenPiece, moveDataState,
                    getDefaultConsumer(move,board.getPiece(move.getEndPosition()),takenPiece,moveDataState));
        }

        public ChessPiece piece;
        public CheckType checkType=CheckType.NONE;
        public enum CheckType{
            CHECK("+"), MATE("#"), NONE("");
            public final String str;
            CheckType(String str){ this.str=str; }
        }

        public String toString(){
            return this.move.toString()+":"+
                    (this.takenPiece==null?"":this.takenPiece.toCompressedString())+":"+
                    this.moveDataState;
        }
        public String toAlgNotation(){
            return (this.piece.getPieceType()!=ChessPiece.PieceType.PAWN?
                    Character.toUpperCase(this.piece.getPieceType().identifier):(this.takenPiece!=null?
                        Character.toString((int)'a'-1+this.move.getStartPosition().getColumn()):""))+
                    //disambugation here
                    (this.takenPiece==null?"":"x")+
                    this.move.getEndPosition()+
                    (this.move.getPromotionPiece()==null?
                            "":("="+Character.toUpperCase(this.move.getPromotionPiece().identifier)))+
                    this.checkType.str;
        }
    }
}
