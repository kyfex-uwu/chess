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
import java.util.*;

import static chess.ChessGame.TESTING;

@WebSocket
public class WebsocketHandler {
    private static class SessionData{
        public final Session session;
        public final String username;
        public Integer gameID;
        public SessionData(Session session, String username){
            this.session = session;
            this.username=username;
        }
    }
    private static final Set<SessionData> connectedUsers = new HashSet<>();
    private static SessionData getBySession(Session session){
        return connectedUsers.stream().filter(data->data.session.equals(session))
                .findFirst().orElse(null);
    }
    private static SessionData getByUsername(String username){
        return connectedUsers.stream().filter(data->data.username.equals(username))
                .findFirst().orElse(null);
    }

    @OnWebSocketConnect
    public void onConnect(Session user) throws IOException {

    }

    @OnWebSocketError
    public void onError(Session user, Throwable error) throws IOException {
        onClose(user, -1, "error");
    }
    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) throws IOException {
        var toRemove = getBySession(user);
        if(toRemove!=null){
            try {
                onLeave(GamesService.getGameById(toRemove.gameID, true), toRemove);
            }catch(Exception ignored){}
            connectedUsers.remove(toRemove);
        }
    }

    private static final String testingId="1";
    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        var values= Serialization.GSON.fromJson(message, Map.class);
        var type = UserGameCommand.CommandType.valueOf(values.get("commandType").toString());
        var messageObj = Serialization.GSON.fromJson(message, type.clazz);

        var idObj = values.get("_messageID");
        var sessionData = getBySession(session);
        if(sessionData==null){
            try {
                sessionData=new SessionData(session,AuthService.getUserFromToken(messageObj.getAuthString()).username());
                connectedUsers.add(sessionData);
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
                    if(makeMoveObj.gameID!=sessionData.gameID){
                        sendWithId(session, new ErrorMessage("not joined"), id);
                        return;
                    }
                    if(!gameData.game.getTeamTurn().whiteOrBlack(
                            gameData.whiteUsername,gameData.blackUsername).equals(sessionData.username)){
                        sendWithId(session, new ErrorMessage("not your turn"), id);
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
                    sessionData.gameID=joinPCommand.gameID;
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
                    sessionData.gameID=joinOCommand.gameID;
                    send(session, new LoadGameMessage(gameData.game, gameData.gameID));
                }catch(Exception ignored){ }
            }else if(messageObj instanceof ResignCommand resignCommand){
                try{
                    var gameData = GamesService.getGameById(resignCommand.gameID, true);
                    if(gameData==null){
                        send(session, new ErrorMessage("Game not found"));
                        return;
                    }
                    if(gameData.gameID!=resignCommand.gameID){
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
                    if(gameData.gameID!= leaveGameCommand.gameID){
                        send(session, new ErrorMessage("not joined"));
                        return;
                    }

                    onLeave(gameData, sessionData);
                }catch(Exception ignored){}
            }
        }
    }
    private static void onLeave(GameData gameData, SessionData leavingUser){
        if(leavingUser==null) return;

        leavingUser.gameID=null;
        sendToGame(gameData, new NotificationMessage(leavingUser+" left"), leavingUser.username);
    }

    private static void sendToGame(GameData gameData, ServerMessage message, String toExclude){
        if(gameData==null) return;
        if(toExclude==null) toExclude="";

        if(!toExclude.equals(gameData.whiteUsername)) sendIfInGame(gameData, message, gameData.whiteUsername);
        if(!toExclude.equals(gameData.blackUsername)) sendIfInGame(gameData, message, gameData.blackUsername);
        for(var watcherName : gameData.watchers)
            if(!toExclude.equals(watcherName)) sendIfInGame(gameData, message, watcherName);
    }
    private static void sendIfInGame(GameData gameData, ServerMessage message, String sendTo){
        if(gameData==null) return;

        var user = getByUsername(sendTo);
        if(user!=null&&user.gameID!=null&&gameData.gameID==user.gameID){
            send(user.session, message);
        }
    }

    private static void send(Session user, ServerMessage message){
        sendWithId(user, message, null);
    }
    private static void sendWithId(Session user, ServerMessage message, Integer id){
        if(TESTING&&message instanceof SuccessMessage) return;
        if(user==null||!user.isOpen()) return;
        //System.out.print(message+", ");
        //try{ System.out.println(getBySession(user).username); }catch(Exception e){ System.out.println();}

        var toSend = Serialization.GSON.toJsonTree(message);
        if(id!=null) toSend.getAsJsonObject().addProperty("_messageID", String.valueOf(id));
        try{ user.getRemote().sendString(toSend.toString()); }catch(Exception e){ }
    }
}