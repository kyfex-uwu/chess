package ui;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Properties;

public class PlayData {
    public static AuthData currAuth = null;
    public static UserData selfData = null;
    public static boolean loggedIn(){ return currAuth!=null&&selfData!=null; }
    public static GameData game = null;

    public static void init() {
        try {
            FileInputStream propsInput = new FileInputStream(Main.configFileName);
            Properties prop = new Properties();
            prop.load(propsInput);

            var token = prop.getProperty("token");
            var username = prop.getProperty("username");
            if(token==null||token.isEmpty()||username==null||username.isEmpty()) return;

            currAuth = new AuthData(token, username);
            Online.request(Online.ReqMethod.GET, "user/"+username,
                            (String)null, UserData.class)
                    .ifSuccess(userData -> {
                        selfData = userData;
                    }).ifError(error -> {
                        currAuth=null;
                    });
        }catch(Exception ignored){ }
    }
    public static void save(){
        try {
            Properties prop = new Properties();
            try {
                FileInputStream propsInput = new FileInputStream(Main.configFileName);
                prop.load(propsInput);
            }catch(Exception ignored){} //todo: replace this with a check


            try (Writer inputStream = new FileWriter(Main.configFileName)) {
                prop.setProperty("token", currAuth==null?null:currAuth.authToken());
                prop.setProperty("username", currAuth==null?null:currAuth.username());

                prop.store(inputStream, Main.confingVer);
            }
        }catch(Exception ignored){ }
    }
}
