package ui;

import chess.Serialization;
import env.Environment;
import model.GameData;
import ui.rendering.scene.GameScene;
import ui.rendering.scene.PlayMenuScene;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.UserGameCommand;

import javax.websocket.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class WebsocketManager {
    public static WebsocketManager inst;

    public static boolean init(){
        if(!PlayData.loggedIn()) return false;

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
            inst.session.getBasicRemote().sendText(Serialization.GSON.toJson(command));
            return true;
        }catch(Exception e){ return false; }
    }
    private static int currMessageID=0;
    private static final HashMap<Integer, ServerMessageHolder> waitingMessages = new HashMap<>();
    private static class ServerMessageHolder{
        public ServerMessage message=new ErrorMessage("Server took too long to respond");
        public CountDownLatch lock = new CountDownLatch(1);
    }
    private static final long maxWaitTime = 5000;
    public static ServerMessage sendMessageWithResponse(UserGameCommand command){
        if(inst==null) return new ServerMessage(ServerMessage.ServerMessageType.CLIENT_ERROR);
        try {
            var toSend = Serialization.GSON.toJsonTree(command).getAsJsonObject();
            currMessageID++;
            toSend.addProperty("_messageID", String.valueOf(currMessageID));
            inst.session.getBasicRemote().sendText(toSend.toString());
            var message = new ServerMessageHolder();
            waitingMessages.put(currMessageID, message);
            new Thread(()->{
                try {
                    Thread.sleep(maxWaitTime);
                }catch(Exception ignored){}
                message.lock.countDown();
            }).start();
            message.lock.await();
            return message.message;
        }catch(Exception e){ return new ServerMessage(ServerMessage.ServerMessageType.CLIENT_ERROR); }
    }
    private static void handleMessage(String message){
        var values = Serialization.GSON.fromJson(message, Map.class);
        var type = ServerMessage.ServerMessageType.valueOf(
                values.get("serverMessageType").toString());
        var messageObj = Serialization.GSON.fromJson(message, type.clazz);

        var idObj = values.get("_messageID");
        if(idObj!=null){
            int id = Integer.parseInt(idObj.toString());
            var waitingMessage = waitingMessages.get(id);
            waitingMessage.message = messageObj;
            waitingMessage.lock.countDown();
        }else{
            var scene = Main.getScene();
            if(messageObj instanceof LoadGameMessage loadGameMessage){
                if(scene instanceof GameScene gameScene){
                    gameScene.data = new GameData(gameScene.data.gameID, gameScene.data.gameName,
                            gameScene.data.whiteUsername, gameScene.data.blackUsername, gameScene.data.watchers,
                            loadGameMessage.game);
                    Main.getScene().onLine(new String[0]);
                }else if(scene instanceof PlayMenuScene playMenuScene){
                    playMenuScene.myGames.stream().filter(gameData -> gameData.gameID==loadGameMessage.gameID)
                            .findFirst().ifPresent(gameData -> {
                                gameData.game = loadGameMessage.game;
                                Main.getScene().onLine(new String[0]);
                            });
                }
            }else if(messageObj instanceof NotificationMessage notificationMessage){
                if(scene instanceof GameScene gameScene){
                    gameScene.setNotification(notificationMessage.message);
                    Main.getScene().onLine(new String[0]);
                }
            }
        }
    }

    //--

    private final Session session;
    private WebsocketManager() throws Exception{
        this.session = ContainerProvider.getWebSocketContainer()
                .connectToServer(this, URI.create(Environment.wsScheme + Environment.baseUrl + "connect"));
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String s) {
                handleMessage(s);
            }
        });
    }
}
