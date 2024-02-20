package webSocketMessages.userCommands;

public class LeaveGameCommand extends CommandForGame{
    public LeaveGameCommand(String authToken, int gameID) {
        super(authToken, gameID);
        this.commandType=CommandType.LEAVE;
    }
}
