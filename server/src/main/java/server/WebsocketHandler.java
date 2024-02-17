package server;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;

@WebSocket
public class WebsocketHandler {
    @OnWebSocketConnect
    public void onConnect(Session user) throws IOException {
        System.out.println("connected!");
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) throws IOException {

    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws IOException {

    }

    @OnWebSocketError
    public void onError(Session user, Throwable error) throws IOException {

    }
}