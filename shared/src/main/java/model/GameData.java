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
    public boolean isValid() {
        return Data.isValid(gameName);
    }
}
