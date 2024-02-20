package webSocketMessages.userCommands;

public abstract class CommandForGame extends UserGameCommand{
    public final int gameID;
    public CommandForGame(String authToken, int gameID) {
        super(authToken);
        this.gameID=gameID;
    }
}
