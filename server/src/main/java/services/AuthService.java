package services;

import model.LoginData;
import model.UserData;
import server.Server;
import spark.Request;

import java.security.SecureRandom;
import java.util.*;

public class AuthService {
    private static final HashMap<String, UserData> userData = new HashMap<>();//username, data
    private static final HashMap<String, String> tokens = new HashMap<>();//token, username

    public static void clear(){
        userData.clear();
        tokens.clear();
    }

    public static boolean validateToken(String token){
        return tokens.containsKey(token);
    }

    // https://stackoverflow.com/a/56628391/14000178
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
    private static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public static boolean registerUser(UserData dataToAdd){
        if(userData.containsKey(dataToAdd.username())) return false;

        userData.put(dataToAdd.username(), dataToAdd);
        return true;
    }

    public static Optional<String> login(LoginData loginData){
        if(userData.values().stream().anyMatch(data->data.matchesLoginData(loginData))){
            var token = generateNewToken();
            tokens.put(token, loginData.username());
            return Optional.of(token);
        }
        return Optional.empty();
    }
    public static void logout(String token){
        tokens.remove(token);
    }

    public static Optional<String> getUserFromToken(String token){
        if(tokens.containsKey(token)) return Optional.of(tokens.get(token));
        return Optional.empty();
    }

    public static Optional<Server.FailedResponse> validateHeader(Request req){
        var authHeader = req.headers("authorization");
        if(authHeader==null||authHeader.isEmpty()) return Optional.of(Server.FailedResponse.BAD_REQ);
        if(!validateToken(authHeader)) return Optional.of(Server.FailedResponse.NOT_AUTH);
        return Optional.empty();
    }
}
