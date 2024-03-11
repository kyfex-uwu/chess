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

    @Override
    public void apply(ChessBoard board) {
    }

    public String toString(){
        return "senpass "+super.toString();
    }
}
