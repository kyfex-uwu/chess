package webSocketMessages.userCommands;

import chess.ChessMove;

public class MakeMoveCommand extends CommandForGame{
    public final ChessMove move;
    public MakeMoveCommand(String authToken, int gameID, ChessMove move) {
        super(authToken, gameID);
        this.commandType=CommandType.MAKE_MOVE;
        this.move=move;
    }
}
