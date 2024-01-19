package services;

import model.AuthData;
import model.LoginData;
import model.UserData;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;

public class AuthService {
    private static final ArrayList<UserData> userData = new ArrayList<>();
    private static final ArrayList<String> tokens = new ArrayList<>();

    public static void clear(){
        userData.clear();
        tokens.clear();
    }

    public static boolean validateToken(String token){
        return tokens.contains(token);
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
        if(userData.stream().anyMatch(data->data.username().equals(dataToAdd.username()))) return false;

        userData.add(dataToAdd);
        return true;
    }

    public static Optional<String> login(LoginData loginData){
        if(userData.stream().anyMatch(data->data.matchesLoginData(loginData))){
            var token = generateNewToken();
            tokens.add(token);
            return Optional.of(token);
        }
        return Optional.empty();
    }
    public static void logout(String token){
        tokens.remove(token);
    }
}
