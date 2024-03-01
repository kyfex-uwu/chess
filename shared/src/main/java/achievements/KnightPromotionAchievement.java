package achievements;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import model.GameData;
import model.UserData;

public class KnightPromotionAchievement extends Achievement{
    protected KnightPromotionAchievement() {
        super("knightprom");
    }

    @Override
    public boolean hasAchieved(String username, GameData gameData, ChessMove.ReversibleChessMove<?> lastMove) {
        return lastMove.move.getPromotionPiece()==ChessPiece.PieceType.KNIGHT;
    }
}
