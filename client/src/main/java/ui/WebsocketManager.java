package ui;

import chess.Json;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.UserGameCommand;

import javax.websocket.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class WebsocketManager {
    private static final String scheme = "ws://";
    public static WebsocketManager inst;

    public static boolean init(){
        inst = null;
        try {
            inst = new WebsocketManager();
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public static boolean sendMessage(UserGameCommand command){
        if(inst==null) return false;
        try {
            inst.session.getBasicRemote().sendText(Json.GSON.toJson(command));
            return true;
        }catch(Exception e){ return false; }
    }
    private static int currMessageID=0;
    private static final HashMap<Integer, ServerMessageHolder> waitingMessages = new HashMap<>();
    private static class ServerMessageHolder{
        public ServerMessage message=null;
        public CountDownLatch lock = new CountDownLatch(1);
    }
    public static ServerMessage sendMessageWithResponse(UserGameCommand command){
        if(inst==null) return new ServerMessage(ServerMessage.ServerMessageType.CLIENT_ERROR);
        try {
            var toSend = Json.GSON.toJsonTree(command).getAsJsonObject();
            currMessageID++;
            toSend.addProperty("_messageID", String.valueOf(currMessageID));
            inst.session.getBasicRemote().sendText(toSend.toString());
            var message = new ServerMessageHolder();
            waitingMessages.put(currMessageID, message);
            message.lock.await();
            return message.message;
        }catch(Exception e){ return new ServerMessage(ServerMessage.ServerMessageType.CLIENT_ERROR); }
    }
    private static void handleMessage(String message){
        var values = Json.GSON.fromJson(message, Map.class);
        var type = ServerMessage.ServerMessageType.valueOf(
                values.get("serverMessageType").toString());
        var messageObj = Json.GSON.fromJson(message, type.clazz);

        Optional.of(values.get("_messageID").toString()).ifPresentOrElse(id -> {
            var waitingMessage = waitingMessages.get(Integer.valueOf(id));
            waitingMessage.message = messageObj;
            waitingMessage.lock.countDown();
        }, ()->{
            //process
        });
    }

    //--

    private final Session session;
    private WebsocketManager() throws Exception{
        this.session = ContainerProvider.getWebSocketContainer()
                .connectToServer(this, URI.create(scheme + Online.baseUrl + "connect"));
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String s) {
                handleMessage(s);
            }
        });
    }
}
