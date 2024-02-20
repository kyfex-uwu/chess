package server;

import chess.InvalidMoveException;
import chess.Json;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import services.AuthService;
import services.GamesService;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.serverMessages.SuccessMessage;
import webSocketMessages.userCommands.IdentifyCommand;
import webSocketMessages.userCommands.MakeMoveCommand;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebSocket
public class WebsocketHandler {
    private static final Map<String, Session> connectedUsers = new HashMap<>();
    private static final Map<Session, String> reverseUsers = new HashMap<>();
    private static void addUser(String username, Session session){
        connectedUsers.put(username, session);
        reverseUsers.put(session, username);
    }

    @OnWebSocketConnect
    public void onConnect(Session user) throws IOException {

    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) throws IOException {
        var username = reverseUsers.remove(user);
        if(username!=null){
            connectedUsers.remove(username);
        }
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
                    var game = GamesService.getGameById(makeMoveObj.gameID, true);
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
                    var toSend = new LoadGameMessage(game.game);
                    send(connectedUsers.get(game.whiteUsername),toSend);
                    send(connectedUsers.get(game.blackUsername),toSend);
                    for(var username : game.watchers)
                        send(connectedUsers.get(username), toSend);
                }catch(Exception e){
                    sendWithId(user, new ErrorMessage("something went wrong"), id);
                }
            }
        }else{
            if(messageObj instanceof IdentifyCommand identifyCommand){
                try {
                    addUser(AuthService.getUserFromToken(identifyCommand.getAuthString()).username(),user);
                }catch(Exception ignored){}
            }
        }
    }

    @OnWebSocketError
    public void onError(Session user, Throwable error) throws IOException {
        onClose(user, -1, "error");
    }

    private static void send(Session user, ServerMessage message){
        if(user==null) return;

        try{ user.getRemote().sendString(Json.GSON.toJsonTree(message).toString()); }catch(Exception e){ }
    }
    private static void sendWithId(Session user, ServerMessage message, int id){
        var toSend = Json.GSON.toJsonTree(message);
        toSend.getAsJsonObject().addProperty("_messageID", String.valueOf(id));
        try{ user.getRemote().sendString(toSend.toString()); }catch(Exception e){ }
    }
}