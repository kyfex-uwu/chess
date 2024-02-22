package chess.specialmoves;

import chess.*;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;

import static chess.Serialization.deserializeChessPosition;

/**
 * Represents a double pawn move (a pawn moving two spaces forward as its first move)
 */
public class DoublePawnMove extends ChessMove {
    public static void initSpecialMove(){
        Serialization.specialMoveDeserializers.put("dpawn", data->
                new DoublePawnMove(
                        deserializeChessPosition(new JsonPrimitive(data[1])),
                        deserializeChessPosition(new JsonPrimitive(data[2])),
                        data.length==4?ChessPiece.PieceType.getType(data[3].charAt(0)):null));
        Serialization.specialReversibleMoveDeserializers.put("dpawn", (data, board)->
                new ReversibleDoublePawnMove(board, (DoublePawnMove) Serialization.deserializeMove(data[0]),
                        data[2]));
    }
    public DoublePawnMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        super(startPosition, endPosition, promotionPiece);
    }

    public ChessMove withPromotionPiece(ChessPiece.PieceType type){
        return new DoublePawnMove(this.getStartPosition(), this.getEndPosition(), type);
    }

    @Override
    public ReversibleChessMove<DoublePawnMove> apply(ChessBoard board) {
        var toReturn = new ReversibleDoublePawnMove(board, this);
        var startPiece=board.getPiece(this.getStartPosition());

        board.addPiece(this.getEndPosition(), this.getPromotionPiece()==null ?
                startPiece :
                new ChessPiece(startPiece.getTeamColor(),this.getPromotionPiece()));
        board.addPiece(this.getStartPosition(), null);

        board.setDoubleMoved(this.getStartPosition().getColumn(), startPiece.getTeamColor());
        return toReturn;
    }

    public String toString(){
        return "sdpawn "+super.toString();
    }

    private static class ReversibleDoublePawnMove extends ReversibleChessMove<DoublePawnMove>{
        //forwards
        public ReversibleDoublePawnMove(ChessBoard board, DoublePawnMove move) {
            super(board, move);
        }
        //backwards
        public ReversibleDoublePawnMove(ChessBoard board, DoublePawnMove move, String moveDataState){
            super(board, move, null, moveDataState);
        }

        @Override
        public String toString(){
            return super.toString();
        }
    }
}
