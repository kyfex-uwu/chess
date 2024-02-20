package webSocketMessages.serverMessages;

public class SuccessMessage extends ServerMessage{
    public final boolean success;
    public SuccessMessage(boolean success) {
        super(ServerMessageType.SUCCESS);
        this.success=success;
    }
}
