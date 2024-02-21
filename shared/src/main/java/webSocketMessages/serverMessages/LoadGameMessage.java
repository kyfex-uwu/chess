package webSocketMessages.serverMessages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage{
    public final ChessGame game;
    public final int gameID;
    @Deprecated
    public LoadGameMessage(ChessGame game) {
        this(game, -1);
        System.out.println("something went wrong, you better be testing");
    }
    public LoadGameMessage(ChessGame game, int gameID){
        super(ServerMessageType.LOAD_GAME);
        this.game=game;
        this.gameID=gameID;
    }
}
