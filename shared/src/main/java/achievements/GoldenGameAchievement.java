package achievements;

import chess.ChessMove;
import model.GameData;

public class GoldenGameAchievement extends Achievement{
    protected GoldenGameAchievement(){
        super("golden");
    }
    @Override
    public boolean hasAchieved(String username, GameData gameData, ChessMove.ReversibleChessMove<?> lastMove) {
        var winnerColor = gameData.game.winner.color;
        if(winnerColor==null) return false;
        return winnerColor.whiteOrBlack(gameData.whiteUsername,gameData.blackUsername).equals(username)&&
                gameData.game.history.stream()
                        .filter(move->move.piece.getTeamColor()==winnerColor.opposite())
                        .filter(move->move.takenPiece!=null)
                        .toList().size()==0;
    }
}
