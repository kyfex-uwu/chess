package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game)
    implements Data{
    public boolean isValid() {
        return Data.isValid(gameName);
    }
}
