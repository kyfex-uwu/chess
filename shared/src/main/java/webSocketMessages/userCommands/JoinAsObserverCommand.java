package webSocketMessages.userCommands;

public class JoinAsObserverCommand extends CommandForGame{
    public JoinAsObserverCommand(String authToken, int gameID) {
        super(authToken, gameID);
        this.commandType=CommandType.JOIN_OBSERVER;
    }
}
