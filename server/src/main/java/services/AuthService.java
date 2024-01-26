package services;

import dataAccess.AuthDataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.LoginData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import server.Server;
import spark.Request;

import java.security.SecureRandom;
import java.util.*;

public class AuthService {
    // https://stackoverflow.com/a/56628391/14000178
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
    private static String generateNewToken() {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
    public static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    //public static HashMap<Integer, HashSet<Void>> watchers = new HashMap<>();
    public static void clear() throws DataAccessException {
        AuthDataAccess.clear();
    }
    public static boolean validateToken(String token) throws DataAccessException {
        return AuthDataAccess.userFromToken(token)!=null;
    }

    public static boolean registerUser(UserData dataToAdd) throws DataAccessException, Server.InvalidRequestException {
        if(!dataToAdd.isValid()) throw new Server.InvalidRequestException();

        if(AuthDataAccess.getUser(dataToAdd.username())!=null) return false;
        AuthDataAccess.createUser(dataToAdd);
        return true;
    }

    public static Optional<String> login(LoginData loginData) throws DataAccessException, Server.InvalidRequestException {
        if(!loginData.isValid()) throw new Server.InvalidRequestException();

        var user = AuthDataAccess.getUser(loginData.username());
        if(user!=null&&encoder.matches(loginData.password(), user.password())){
            //AuthDataAccess.deleteToken(AuthDataAccess.getToken(loginData.username()));
            var token = generateNewToken();
            AuthDataAccess.createToken(new AuthData(token, loginData.username()));
            return Optional.of(token);
        }
        return Optional.empty();
    }
    public static void logout(String token) throws DataAccessException {
        AuthDataAccess.deleteToken(token);
    }

    public static UserData getUserFromToken(String token) throws DataAccessException {
        return AuthDataAccess.userFromToken(token);
    }

    public static Optional<Server.FailedResponse> validateHeader(Request req) throws DataAccessException {
        var authHeader = req.headers("authorization");
        if(authHeader==null||authHeader.isEmpty()) return Optional.of(Server.FailedResponse.BAD_REQ);
        if(!validateToken(authHeader)) return Optional.of(Server.FailedResponse.NOT_AUTH);
        return Optional.empty();
    }
}
