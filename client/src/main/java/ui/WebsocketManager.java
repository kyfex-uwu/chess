package ui;

import org.glassfish.tyrus.core.MessageHandlerManager;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

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

    public static boolean sendMessage(String string){
        if(inst==null) return false;
        try {
            inst.session.getBasicRemote().sendText(string);
            return true;
        }catch(Exception e){ return false; }
    }
    private static void handleMessage(String string){

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
