package webSocketMessages.userCommands;

public class IdentifyCommand extends UserGameCommand{
    public IdentifyCommand(String authToken) {
        super(authToken);
        this.commandType=CommandType.IDENTIFY;
    }
}
