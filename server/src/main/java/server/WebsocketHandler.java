package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import chess.Serialization;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import passoffTests.obfuscatedTestClasses.TestServerFacade;
import services.AuthService;
import services.GamesService;
import webSocketMessages.serverMessages.*;
import webSocketMessages.userCommands.*;

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

    private static final String testingId="1";
    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        var values= Serialization.GSON.fromJson(message, Map.class);
        var type = UserGameCommand.CommandType.valueOf(values.get("commandType").toString());
        var messageObj = Serialization.GSON.fromJson(message, type.clazz);

        var idObj = values.get("_messageID");
        var username = reverseUsers.get(session);
        if(username==null){
            try {
                username=AuthService.getUserFromToken(messageObj.getAuthString()).username();
                addUser(username, session);
            }catch(Exception ignored){
                send(session, new ErrorMessage("Could not identify you"));
                return;
            }
        }
        if(idObj==null&& ChessGame.TESTING) idObj=testingId;

        if(idObj!=null){
            int id = Integer.parseInt(idObj.toString());
            if(messageObj instanceof MakeMoveCommand makeMoveObj){
                try {
                    var game = GamesService.getGameById(makeMoveObj.gameID, true);
                    if(game==null){
                        sendWithId(session, new ErrorMessage("game not found"), id);
                        return;
                    }

                    try {
                        game.game.makeMove(makeMoveObj.move);
                        GamesService.updateGame(makeMoveObj.gameID, game.game);
                    }catch(InvalidMoveException e){
                        sendWithId(session, new ErrorMessage("invalid move"), id);
                        return;
                    }

                    sendWithId(session, new SuccessMessage(true), id);
                    var toSend = new LoadGameMessage(game.game, game.gameID);
                    send(connectedUsers.get(game.whiteUsername),toSend);
                    send(connectedUsers.get(game.blackUsername),toSend);
                    for(var watcherName : game.watchers)
                        send(connectedUsers.get(watcherName), toSend);
                }catch(Exception e){
                    sendWithId(session, new ErrorMessage("something went wrong"), id);
                }
            }
        }
        if(idObj==testingId&& ChessGame.TESTING) idObj=null;
        if(idObj==null){
            if(messageObj instanceof JoinAsPlayerCommand joinPCommand){
                try {
                    if(joinPCommand.playerColor==null){
                        send(session, new ErrorMessage("No color specified"));
                        return;
                    }
                    var game = GamesService.getGameById(joinPCommand.gameID, true);
                    if(game==null){
                        send(session, new ErrorMessage("Game not found"));
                        return;
                    }
                    if(!username.equals(joinPCommand.playerColor.whiteOrBlack(game.whiteUsername,game.blackUsername))){
                        send(session, new ErrorMessage("Color does not match"));
                        return;
                    }

                    send(session, new LoadGameMessage(game.game, game.gameID));

                    var toSend = new NotificationMessage(username+" joined the game as "+joinPCommand.playerColor.name);
                    if(!username.equals(game.whiteUsername)) send(connectedUsers.get(game.whiteUsername),toSend);
                    if(!username.equals(game.blackUsername)) send(connectedUsers.get(game.blackUsername),toSend);
                    for(var watcherName : game.watchers)
                        if(!username.equals(watcherName)) send(connectedUsers.get(watcherName), toSend);
                }catch(Exception ignored){ }
            }else if(messageObj instanceof JoinAsObserverCommand joinOCommand){
                try {
                    var game = GamesService.getGameById(joinOCommand.gameID, true);
                    if(game==null){
                        send(session, new ErrorMessage("Game not found"));
                        return;
                    }

                    send(session, new LoadGameMessage(game.game, game.gameID));

                    var toSend = new NotificationMessage(username+" started watching the game");
                    if(!username.equals(game.whiteUsername)) send(connectedUsers.get(game.whiteUsername),toSend);
                    if(!username.equals(game.blackUsername)) send(connectedUsers.get(game.blackUsername),toSend);
                    for(var watcherName : game.watchers)
                        if(!username.equals(watcherName)) send(connectedUsers.get(watcherName), toSend);
                }catch(Exception ignored){ }
            }else if(messageObj instanceof ResignCommand resignCommand){
                try{
                    var game = GamesService.getGameById(resignCommand.gameID, true);
                    if(game==null){
                        send(session, new ErrorMessage("Game not found"));
                        return;
                    }
                    if(!username.equals(game.whiteUsername)&&!username.equals(game.blackUsername)){
                        send(session, new ErrorMessage("Not able to resign"));
                        return;
                    }
                    game.game.winner=username.equals(game.blackUsername)?
                            ChessGame.WinType.WHITE:ChessGame.WinType.BLACK;

                    var resign = new NotificationMessage(username+" resigned");
                    var gameState = new LoadGameMessage(game.game, game.gameID);
                    if(!username.equals(game.whiteUsername)){
                        send(connectedUsers.get(game.whiteUsername),resign);
                        send(connectedUsers.get(game.whiteUsername),gameState);
                    }
                    if(!username.equals(game.blackUsername)){
                        send(connectedUsers.get(game.whiteUsername),resign);
                        send(connectedUsers.get(game.whiteUsername),gameState);
                    }
                    for(var watcherName : game.watchers)
                        if(!username.equals(watcherName)){
                            send(connectedUsers.get(game.whiteUsername),resign);
                            send(connectedUsers.get(game.whiteUsername),gameState);
                        }
                }catch(Exception ignored){}
            }
        }
    }

    @OnWebSocketError
    public void onError(Session user, Throwable error) throws IOException {
        onClose(user, -1, "error");
    }

    private static void send(Session user, ServerMessage message){
        if(user==null||!user.isOpen()) return;
        try{ user.getRemote().sendString(Serialization.GSON.toJsonTree(message).toString()); }catch(Exception e){ }
    }
    private static void sendWithId(Session user, ServerMessage message, int id){
        if(user==null||!user.isOpen()) return;

        var toSend = Serialization.GSON.toJsonTree(message);
        toSend.getAsJsonObject().addProperty("_messageID", String.valueOf(id));
        try{ user.getRemote().sendString(toSend.toString()); }catch(Exception e){ }
    }
}