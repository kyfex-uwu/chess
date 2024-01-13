package chess.specialmoves;

import chess.*;

public class DoublePawnMove extends ChessMove {
    public DoublePawnMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        super(startPosition, endPosition, promotionPiece);
    }

    @Override
    public void apply(ChessBoard board) {
        var startPiece=board.getPiece(this.getStartPosition());

        board.addPiece(this.getEndPosition(), this.getPromotionPiece()==null ?
                startPiece :
                new ChessPiece(startPiece.getTeamColor(),this.getPromotionPiece()));
        board.addPiece(this.getStartPosition(), null);

        board.setDoubleMoved(this.getStartPosition().getColumn(), startPiece.getTeamColor());
    }
}
