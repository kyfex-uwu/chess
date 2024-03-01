package achievements;

import chess.ChessMove;
import model.GameData;
import model.UserData;

public class WinStreakAchievement extends Achievement{
    private final int streak;
    protected WinStreakAchievement(int streak){
        super("streak"+streak);
        this.streak=streak;
    }
    public interface WinStreakRunnable{
        boolean hasAchieved(WinStreakAchievement thisAchievement, String username,
                            GameData gameData, ChessMove.ReversibleChessMove<?> lastMove);
    }
    public static WinStreakRunnable condition = (a,b,c,d)->false;
    @Override
    public boolean hasAchieved(String username, GameData gameData, ChessMove.ReversibleChessMove<?> lastMove) {
        return condition.hasAchieved(this, username, gameData, lastMove);
    }
}
