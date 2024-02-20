package webSocketMessages.userCommands;

import chess.ChessGame;

public class JoinAsPlayerCommand extends CommandForGame{
    public final ChessGame.TeamColor playerColor;
    public JoinAsPlayerCommand(String authToken, int gameID, ChessGame.TeamColor playerColor) {
        super(authToken, gameID);
        this.commandType=CommandType.JOIN_PLAYER;
        this.playerColor=playerColor;
    }
}
