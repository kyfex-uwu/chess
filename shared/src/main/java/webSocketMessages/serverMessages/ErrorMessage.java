package webSocketMessages.serverMessages;

public class ErrorMessage extends ServerMessage{
    public final String errorMessage;
    public ErrorMessage(String errorMessage) {
        super(ServerMessageType.ERROR);
        this.errorMessage="Server error: "+errorMessage;
    }
}
