package cache;

import chess.ChessGame;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import services.GamesService;

import java.util.HashSet;
import java.util.Set;

public class GameDataView{
    private GameData data;
    public final int gameID;
    public final String gameName;
    public GameDataView(int gameID) throws DataAccessException {
        this.gameID=gameID;
        this.init();
        this.gameName=this.data.gameName;
    }
    private void init() throws DataAccessException {
        this.data=GamesService.getGameById(this.gameID, true);
    }
    public String getWhiteUsername(){

    }
    public void setWhiteUsername(){
        
    }
    public String getBlackUsername(){

    }
    public ChessGame getGame(){

    }
    public Set<String> getWatchers(){

    }
}
