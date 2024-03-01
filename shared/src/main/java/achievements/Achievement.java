package achievements;

import chess.ChessMove;
import model.GameData;

import java.util.ArrayList;
import java.util.List;

public abstract class Achievement {

    public final String id;
    protected Achievement(String id){
        this.id=id;
    }

    public abstract boolean hasAchieved(String username, GameData gameData, ChessMove.ReversibleChessMove<?> lastMove);

    //--

    public static final List<Achievement> achievements = List.of(new Achievement[]{
            new GoldenGameAchievement(),

            new WinStreakAchievement(3),
            new WinStreakAchievement(20),
            new WinStreakAchievement(100),

            new KnightPromotionAchievement(),
    });

    public static int getNewAchievements(String username, GameData gameData, ChessMove.ReversibleChessMove<?> lastMove){
        int newAchievements=0;
        for(int i=0;i<achievements.size();i++){
            if(achievements.get(i).hasAchieved(username, gameData, lastMove))
                newAchievements+=Math.pow(2,i);
        }
        return newAchievements;
    }
    public static List<String> intToAchievementIDs(int achievementMap){
        var toReturn = new ArrayList<String>();
        for(int i=0;i<achievements.size();i++){
            if((achievementMap&0b1)==0b1)
                toReturn.add(achievements.get(i).id);
            achievementMap>>=1;//shifting one to the right
        }
        return toReturn;
    }
}
