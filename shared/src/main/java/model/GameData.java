package model;

import chess.ChessGame;

public class GameData implements Data{
    public final int gameID;
    public String whiteUsername;
    public String blackUsername;
    public final String gameName;
    public final ChessGame game;
    public GameData(int gameID, String gameName){
        this.gameID=gameID;
        this.gameName=gameName;
        this.game = new ChessGame();
    }
    public GameData(int gameID, String gameName, String whiteUsername, String blackUsername, ChessGame game){
        this.gameID=gameID;
        this.whiteUsername=whiteUsername;
        this.blackUsername=blackUsername;
        this.gameName=gameName;
        this.game = game;
    }
    public boolean isValid() {
        return Data.isValid(gameName);
    }
}
