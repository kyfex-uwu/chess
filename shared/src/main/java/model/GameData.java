package model;

import chess.ChessGame;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameData implements Data{
    public final int gameID;
    public String whiteUsername;
    public String blackUsername;
    public final String gameName;
    public final ChessGame game;
    public final Set<String> watchers = new HashSet<>();
    public GameData(int gameID, String gameName){
        this.gameID=gameID;
        this.gameName=gameName;
        this.game = new ChessGame();
    }
    public GameData(int gameID, String gameName, String whiteUsername, String blackUsername, String watchers, ChessGame game){
        this(gameID,gameName,whiteUsername,blackUsername,List.of(watchers.split(",")),game);
    }
    public GameData(int gameID, String gameName, String whiteUsername, String blackUsername,
                    Collection<String> watchers, ChessGame game){
        this.gameID=gameID;
        this.whiteUsername=whiteUsername;
        this.blackUsername=blackUsername;
        this.watchers.addAll(watchers);
        this.gameName=gameName;
        this.game = game;
    }
    public boolean isValid() {
        return Data.isValid(gameName);
    }
}
