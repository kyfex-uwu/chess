package server;

import chess.InvalidMoveException;
import chess.Json;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import services.GamesService;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.serverMessages.SuccessMessage;
import webSocketMessages.userCommands.MakeMoveCommand;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.IOException;
import java.util.Map;

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
        var values=Json.GSON.fromJson(message, Map.class);
        var type = UserGameCommand.CommandType.valueOf(values.get("commandType").toString());
        var messageObj = Json.GSON.fromJson(message, type.clazz);

        var idObj = values.get("_messageID");
        if(idObj!=null){
            int id = Integer.parseInt(idObj.toString());
            if(messageObj instanceof MakeMoveCommand makeMoveObj){
                try {
                    var game = GamesService.getGameById(makeMoveObj.gameID);
                    if(game==null){
                        sendWithId(user, new ErrorMessage("game not found"), id);
                        return;
                    }

                    try {
                        game.game.makeMove(makeMoveObj.move);
                        GamesService.updateGame(makeMoveObj.gameID, game.game);
                    }catch(InvalidMoveException e){
                        sendWithId(user, new ErrorMessage("invalid move"), id);
                    }

                    sendWithId(user, new SuccessMessage(true), id);
                    send(user, new LoadGameMessage(game.game));
                }catch(Exception e){
                    sendWithId(user, new ErrorMessage("something went wrong"), id);
                }
            }
        }else{

        }
    }

    @OnWebSocketError
    public void onError(Session user, Throwable error) throws IOException {

    }

    private static void send(Session user, ServerMessage message){
        try{ user.getRemote().sendString(Json.GSON.toJsonTree(message).toString()); }catch(Exception e){ }
    }
    private static void sendWithId(Session user, ServerMessage message, int id){
        var toSend = Json.GSON.toJsonTree(message);
        toSend.getAsJsonObject().addProperty("_messageID", String.valueOf(id));
        try{ user.getRemote().sendString(toSend.toString()); }catch(Exception e){ }
    }
}