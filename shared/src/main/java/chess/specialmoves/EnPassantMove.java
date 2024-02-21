package chess.specialmoves;

import chess.*;
import com.google.gson.JsonPrimitive;

import static chess.Json.deserializeChessPosition;

/**
 * Represents a castling move
 */
public class EnPassantMove extends ChessMove {
    static{
        Json.specialMoveDeserializers.put("enpass", data->
                new EnPassantMove(
                        deserializeChessPosition(new JsonPrimitive(data[1])),
                        deserializeChessPosition(new JsonPrimitive(data[2])),
                        data.length==4? ChessPiece.PieceType.getType(data[3].charAt(0)):null));
    }
    public EnPassantMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        super(startPosition, endPosition, promotionPiece);
    }

    public ChessMove withPromotionPiece(ChessPiece.PieceType type){
        return new EnPassantMove(this.getStartPosition(), this.getEndPosition(), type);
    }

    @Override
    public void apply(ChessBoard board) {
        var startPiece=board.getPiece(this.getStartPosition());

        board.addPiece(this.getEndPosition(), this.getPromotionPiece()==null ?
                startPiece :
                new ChessPiece(startPiece.getTeamColor(),this.getPromotionPiece()));
        board.addPiece(this.getStartPosition(), null);
        board.addPiece(this.getEndPosition().addOffset(new ChessPiece.Offset(0,
                -startPiece.getTeamColor().advDir)), null);
    }

    public String toString(){
        return "senpass "+super.toString();
    }
}
