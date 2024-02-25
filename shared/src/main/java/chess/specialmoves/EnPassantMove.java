package chess.specialmoves;

import chess.*;

/**
 * Represents a castling move
 */
public class EnPassantMove extends ChessMove {
    public EnPassantMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        super(startPosition, endPosition, promotionPiece);
    }

    @Override
    public void apply(ChessBoard board) {

    }
}
