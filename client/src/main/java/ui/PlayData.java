package ui;

import model.AuthData;
import model.GameData;
import model.UserData;

public class PlayData {
    public static AuthData currAuth = null;
    public static UserData selfData = null;
    public static boolean loggedIn(){ return currAuth!=null&&selfData!=null; }
    public static GameData game = null;
}
