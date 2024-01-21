package model;

public record JoinGameData(String playerColor, int gameID) implements Data {
    public boolean isValid() {
        return gameID!=0 && (playerColor == null || playerColor.equals("WHITE") || playerColor.equals("BLACK"));
    }
}
