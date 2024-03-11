package chess.specialmoves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

/**
 * Represents a double pawn move (a pawn moving two spaces forward as its first move)
 */
public class DoublePawnMove extends ChessMove {
    public DoublePawnMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        super(startPosition, endPosition, promotionPiece);
    }

    @Override
    public void apply(ChessBoard board) {
    }
}
