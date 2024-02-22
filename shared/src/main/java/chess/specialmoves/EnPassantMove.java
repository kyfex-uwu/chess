package chess.specialmoves;

import chess.*;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;

import static chess.Serialization.deserializeChessPosition;

/**
 * Represents a castling move
 */
public class EnPassantMove extends ChessMove {
    public static void initSpecialMove(){
        Serialization.specialMoveDeserializers.put("enpass", data->
                new EnPassantMove(
                        deserializeChessPosition(new JsonPrimitive(data[1])),
                        deserializeChessPosition(new JsonPrimitive(data[2])),
                        data.length==4? ChessPiece.PieceType.getType(data[3].charAt(0)):null));
        Serialization.specialReversibleMoveDeserializers.put("enpass", (data, board)->
                new ReversibleEnPassantMove(board, (EnPassantMove) Serialization.deserializeMove(data[0]),
                        Serialization.pieceFromChar(data[1].charAt(0)), data[2]));
    }
    public EnPassantMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        super(startPosition, endPosition, promotionPiece);
    }

    public ChessMove withPromotionPiece(ChessPiece.PieceType type){
        return new EnPassantMove(this.getStartPosition(), this.getEndPosition(), type);
    }

    @Override
    public ReversibleChessMove<EnPassantMove> apply(ChessBoard board) {
        var toReturn = new ReversibleEnPassantMove(board, this);
        var startPiece=board.getPiece(this.getStartPosition());

        board.addPiece(this.getEndPosition(), this.getPromotionPiece()==null ?
                startPiece :
                new ChessPiece(startPiece.getTeamColor(),this.getPromotionPiece()));
        board.addPiece(this.getStartPosition(), null);
        board.addPiece(this.getEndPosition().addOffset(new ChessPiece.Offset(0,
                -startPiece.getTeamColor().advDir)), null);
        return toReturn;
    }

    public String toString(){
        return "senpass "+super.toString();
    }

    //--

    private static class ReversibleEnPassantMove extends ReversibleChessMove<EnPassantMove>{
        //forwards
        public ReversibleEnPassantMove(ChessBoard board, EnPassantMove move) {
            super(move, board.getPiece(move.getEndPosition().addOffset(
                    new ChessPiece.Offset(0,-board.getPiece(move.getStartPosition()).getTeamColor().advDir))),
                    board.miscMoveDataToString(), getDefaultConsumer(board, move,
                            board.getPiece(move.getEndPosition().addOffset(new ChessPiece.Offset(0,
                                    -board.getPiece(move.getStartPosition()).getTeamColor().advDir))),
                            true));
        }
        private static Consumer<ChessBoard> getDefaultConsumer(ChessBoard boardP, EnPassantMove move,
                                                               ChessPiece takenPiece, boolean forward){
            var startPiece = boardP.getPiece(forward?move.getStartPosition():move.getEndPosition());
            var takenPos = move.getEndPosition().addOffset(new ChessPiece.Offset(
                    0,-startPiece.getTeamColor().advDir));
            var moveData = boardP.miscMoveDataToString();
            return board -> {
                board.addPiece(move.getStartPosition(), startPiece);
                board.addPiece(move.getEndPosition(), null);
                board.addPiece(takenPos, takenPiece);
                board.applyMoveData(moveData);
            };
        }
        //backwards
        public ReversibleEnPassantMove(ChessBoard board, EnPassantMove move, ChessPiece takenPiece, String moveDataState) {
            super(move, takenPiece, moveDataState, getDefaultConsumer(board, move, takenPiece, false));
        }

        @Override
        public String toString(){
            return super.toString();
        }
    }
}
