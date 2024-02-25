package dataAccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;

import static services.AuthService.encoder;

public class AuthDataAccess {
    public static UserData withPassHashed(UserData data){
        return new UserData(data.username(), encoder.encode(data.password()), data.email());
    }
    public static void clear(){
        data.clear();
        tokens.clear();
    }
    public static UserData getUser(String username) throws DataAccessException{
        if(data.containsKey(username)) return data.get(username);
        return null;
    }
    public static void createUser(UserData userData) throws DataAccessException{
        if(!data.containsKey(userData.username()))
            data.put(userData.username(), withPassHashed(userData));
        else
            throw new DataAccessException("name already taken");
    }
    public static void createToken(AuthData data) throws DataAccessException{
        tokens.put(data.authToken(), data.username());
    }
    public static void deleteToken(String token) throws DataAccessException{
        tokens.remove(token);
    }
    public static UserData userFromToken(String token) throws DataAccessException{
        if(tokens.containsKey(token)&&data.containsKey(tokens.get(token)))
            return data.get(tokens.get(token));
        return null;
    }

    //--

    private static final HashMap<String, UserData> data = new HashMap<>();//username, data
    public static final HashMap<String, String> tokens = new HashMap<>();//token, username
}
