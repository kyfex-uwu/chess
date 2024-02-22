package chess.specialmoves;

import chess.*;

import java.util.function.Consumer;

/**
 * Represents a castling move
 */
public class CastleMove extends ChessMove {
    public static void initSpecialMove(){
        Serialization.specialMoveDeserializers.put("castle", data->
                new CastleMove(
                        ChessGame.TeamColor.valueOf(data[1]),
                        Side.valueOf(data[2])));
        Serialization.specialReversibleMoveDeserializers.put("castle", (data, board)->
                new ReversibleCastleMove(board, new CastleMove(
                        ChessGame.TeamColor.values()[Character.getNumericValue(data[1].charAt(0))],
                        Side.values()[Character.getNumericValue(data[1].charAt(1))]
                ),data[2]));
    }

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
        super(new ChessPosition(color.row,5),
                new ChessPosition(color.row,5+side.direc*2), null);
        this.side=side;
        this.color=color;
    }

    public ReversibleChessMove<CastleMove> apply(ChessBoard board){
        var toReturn = new ReversibleCastleMove(board, this);
        board.addPiece(new ChessPosition(this.color.row,this.side.x), null);
        board.addPiece(new ChessPosition(this.color.row,5), null);
        board.addPiece(new ChessPosition(this.color.row,5+this.side.direc*2),
                new ChessPiece(this.color, ChessPiece.PieceType.KING));
        board.addPiece(new ChessPosition(this.color.row,5+this.side.direc),
                new ChessPiece(this.color, ChessPiece.PieceType.ROOK));

        board.removeCastlePrivileges(this.color);
        return toReturn;
    }

    public String toString(){
        return  "scastle "+this.color+" "+this.side;
    }

    private static class ReversibleCastleMove extends ReversibleChessMove<CastleMove>{
        //forwards
        public ReversibleCastleMove(ChessBoard board, CastleMove move) {
            super(move, null, board.miscMoveDataToString(),
                    getDefaultConsumer(board,move));
        }
        private static Consumer<ChessBoard> getDefaultConsumer(ChessBoard boardP, CastleMove move){
            var moveData = boardP.miscMoveDataToString();
            return board -> {
                board.addPiece(new ChessPosition(move.color.row,move.side.x),
                        new ChessPiece(move.color, ChessPiece.PieceType.ROOK));
                board.addPiece(new ChessPosition(move.color.row,5),
                        new ChessPiece(move.color, ChessPiece.PieceType.KING));
                board.addPiece(new ChessPosition(move.color.row,5+move.side.direc*2), null);
                board.addPiece(new ChessPosition(move.color.row,5+move.side.direc), null);

                board.applyMoveData(moveData);
            };
        }
        //backwards
        public ReversibleCastleMove(ChessBoard board, CastleMove move,
                                       String moveDataState){
            super(move, null, moveDataState, getDefaultConsumer(board, move));
        }

        @Override
        public String toString(){
            return "scastle:"+this.move.color.ordinal()+this.move.side.ordinal()+":"+this.moveDataState;
        }
    }
}
