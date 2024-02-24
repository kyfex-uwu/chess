package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import chess.Serialization;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import services.AuthService;
import services.GamesService;
import webSocketMessages.serverMessages.*;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static chess.ChessGame.TESTING;

@WebSocket
public class WebsocketHandler {
    private static class SessionData{
        public final String username;
        public GameData currGameData;
        public SessionData(String username){ this.username = username; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SessionData that = (SessionData) o;
            return Objects.equals(username, that.username) && Objects.equals(currGameData, that.currGameData);
        }
        @Override
        public int hashCode() {
            return Objects.hash(username, currGameData);
        }
    }
    private static final Map<SessionData, Session> connectedUsers = new HashMap<>();
    private static final Map<Session, SessionData> reverseUsers = new HashMap<>();
    private static void addUser(SessionData data, Session session){
        connectedUsers.put(data, session);
        reverseUsers.put(session, data);
    }
    private static Session getSession(String username){
        for(var entry : connectedUsers.entrySet())
            if(entry.getKey().username.equals(username)&&entry.getValue().isOpen())
                return entry.getValue();
        return null;
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
        var sessionData = reverseUsers.get(session);
        if(sessionData==null){
            try {
                sessionData=new SessionData(AuthService.getUserFromToken(messageObj.getAuthString()).username());
                addUser(sessionData, session);
            }catch(Exception ignored){
                send(session, new ErrorMessage("Could not identify you"));
                return;
            }
        }
        if(idObj==null&& TESTING) idObj=testingId;

        if(idObj!=null){
            int id = Integer.parseInt(idObj.toString());
            if(messageObj instanceof MakeMoveCommand makeMoveObj){
                try {
                    var gameData = GamesService.getGameById(makeMoveObj.gameID, true);
                    if(gameData==null){
                        sendWithId(session, new ErrorMessage("game not found"), id);
                        return;
                    }
                    if(!gameData.equals(sessionData.currGameData)){
                        sendWithId(session, new ErrorMessage("not joined"), id);
                        return;
                    }

                    try {
                        gameData.game.makeMove(makeMoveObj.move);
                        GamesService.updateGame(makeMoveObj.gameID, gameData.game);
                    }catch(InvalidMoveException e){
                        sendWithId(session, new ErrorMessage("invalid move"), id);
                        return;
                    }

                    sendWithId(session, new SuccessMessage(true), id);
                    sendToGame(gameData, new LoadGameMessage(gameData.game, gameData.gameID), "");
                }catch(Exception e){
                    sendWithId(session, new ErrorMessage("something went wrong"), id);
                }
            }
        }
        if(idObj==testingId&& TESTING) idObj=null;
        if(idObj==null){
            if(messageObj instanceof JoinAsPlayerCommand joinPCommand){
                try {
                    if(joinPCommand.playerColor==null){
                        send(session, new ErrorMessage("No color specified"));
                        return;
                    }
                    var gameData = GamesService.getGameById(joinPCommand.gameID, true);
                    if(gameData==null){
                        send(session, new ErrorMessage("Game not found"));
                        return;
                    }
                    if(!sessionData.username.equals(joinPCommand.playerColor
                            .whiteOrBlack(gameData.whiteUsername,gameData.blackUsername))){
                        send(session, new ErrorMessage("Color does not match"));
                        return;
                    }

                    sendToGame(gameData,
                            new NotificationMessage(sessionData+" joined the game as "+joinPCommand.playerColor.name),
                            sessionData.username);
                    sessionData.currGameData =gameData;
                    send(session, new LoadGameMessage(gameData.game, gameData.gameID));
                }catch(Exception ignored){ }
            }else if(messageObj instanceof JoinAsObserverCommand joinOCommand){
                try {
                    var gameData = GamesService.getGameById(joinOCommand.gameID, true);
                    if(gameData==null){
                        send(session, new ErrorMessage("Game not found"));
                        return;
                    }

                    sendToGame(gameData,
                            new NotificationMessage(sessionData+" started watching the game"),
                            sessionData.username);
                    sessionData.currGameData =gameData;
                    send(session, new LoadGameMessage(gameData.game, gameData.gameID));
                }catch(Exception ignored){ }
            }else if(messageObj instanceof ResignCommand resignCommand){
                try{
                    var gameData = GamesService.getGameById(resignCommand.gameID, true);
                    if(gameData==null){
                        send(session, new ErrorMessage("Game not found"));
                        return;
                    }
                    if(!gameData.game.equals(sessionData.currGameData.game)){
                        send(session, new ErrorMessage("not joined"));
                        return;
                    }
                    if(gameData.game.winner!=ChessGame.WinType.NONE||
                            (!sessionData.username.equals(gameData.whiteUsername)&&
                            !sessionData.username.equals(gameData.blackUsername))){
                        send(session, new ErrorMessage("Not able to resign"));
                        return;
                    }

                    gameData.game.winner=sessionData.username.equals(gameData.blackUsername)?
                            ChessGame.WinType.WHITE:ChessGame.WinType.BLACK;
                    GamesService.updateGame(resignCommand.gameID, gameData.game);

                    sendToGame(gameData, new NotificationMessage(sessionData+" resigned"), "");
                    if(!TESTING) sendToGame(gameData, new LoadGameMessage(gameData.game, gameData.gameID), sessionData.username);
                }catch(Exception ignored){}
            }else if(messageObj instanceof LeaveGameCommand leaveGameCommand){
                try{
                    var gameData = GamesService.getGameById(leaveGameCommand.gameID, true);
                    if(gameData==null){
                        send(session, new ErrorMessage("Game not found"));
                        return;
                    }
                    if(!gameData.equals(sessionData.currGameData)){
                        send(session, new ErrorMessage("not joined"));
                        return;
                    }

                    onLeave(gameData, sessionData);
                }catch(Exception ignored){}
            }
        }
    }

    private static void sendToGame(GameData gameData, ServerMessage message, String toExclude){
        //new Exception().printStackTrace();
        if(!toExclude.equals(gameData.whiteUsername)) sendIfInGame(gameData, message, gameData.whiteUsername);
        if(!toExclude.equals(gameData.blackUsername)) sendIfInGame(gameData, message, gameData.blackUsername);
        for(var watcherName : gameData.watchers)
            if(!toExclude.equals(watcherName)) sendIfInGame(gameData, message, watcherName);
    }
    private static void sendIfInGame(GameData gameData, ServerMessage message, String sendTo){
        var session = getSession(sendTo);
        var data = reverseUsers.get(session);
        if(data!=null&&gameData.game.equals(data.currGameData.game)){
            System.out.println("sent to "+sendTo+" "+message);
            send(session, message);
        }
    }
    private static void onLeave(GameData gameData, SessionData leavingUser){
        leavingUser.currGameData =null;
        sendToGame(gameData, new NotificationMessage(leavingUser+" left"), leavingUser.username);
    }

    @OnWebSocketError
    public void onError(Session user, Throwable error) throws IOException {
        onClose(user, -1, "error");
    }

    private static void send(Session user, ServerMessage message){
        sendWithId(user, message, null);
    }
    private static void sendWithId(Session user, ServerMessage message, Integer id){
        if(user==null||!user.isOpen()) return;

        System.out.println("sent for real! "+message);
        var toSend = Serialization.GSON.toJsonTree(message);
        if(id!=null) toSend.getAsJsonObject().addProperty("_messageID", String.valueOf(id));
        try{ user.getRemote().sendString(toSend.toString()); }catch(Exception e){ }
    }
}