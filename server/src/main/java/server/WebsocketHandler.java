package server;

import chess.Json;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.serverMessages.SuccessMessage;
import webSocketMessages.userCommands.MakeMoveCommand;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@WebSocket
public class WebsocketHandler {
    @OnWebSocketConnect
    public void onConnect(Session user) throws IOException {

    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) throws IOException {

    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws IOException {
        System.out.println(message);
        var values=Json.GSON.fromJson(message, Map.class);
        var type = UserGameCommand.CommandType.valueOf(values.get("commandType").toString());
        var messageObj = Json.GSON.fromJson(message, type.clazz);

        Optional.of(values.get("_messageID").toString()).ifPresentOrElse(id -> {
            if(messageObj instanceof MakeMoveCommand makeMoveObj){

                sendWithId(user, new SuccessMessage(true), Integer.parseInt(id));
            }
        }, ()->{
            //process
        });
    }

    @OnWebSocketError
    public void onError(Session user, Throwable error) throws IOException {

    }

    private static void sendWithId(Session user, ServerMessage message, int id){
        var toSend = Json.GSON.toJsonTree(message);
        toSend.getAsJsonObject().addProperty("_messageID", String.valueOf(id));
        try{ user.getRemote().sendString(toSend.toString()); }catch(Exception ignored){}
    }
}