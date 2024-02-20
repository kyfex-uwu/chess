package webSocketMessages.userCommands;

public class ResignCommand extends CommandForGame{
    public ResignCommand(String authToken, int gameID) {
        super(authToken, gameID);
        this.commandType=CommandType.RESIGN;
    }
}
