package dataAccess;

import model.AuthData;
import model.UserData;

import java.util.concurrent.atomic.AtomicReference;

import static services.AuthService.encoder;

public class AuthDataAccess {
    public static void clear() throws DataAccessException {
        DatabaseManager.execStatement("DELETE FROM auth");
        DatabaseManager.execStatement("DELETE FROM users");
    }
    public static UserData getUser(String username) throws DataAccessException{
        AtomicReference<UserData> toReturn = new AtomicReference<>();
        DatabaseManager.execQuery("SELECT * FROM users WHERE username=?", query->{
            query.setString(1,username);
        }, resultSet->{
            if(!resultSet.next()) return;
            toReturn.set(new UserData(resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getString(3)));
        });
        return toReturn.get();
    }
    public static void createUser(UserData userData) throws DataAccessException{
        DatabaseManager.execStatement(
                "INSERT INTO users (username, password, email) VALUES (?, ?, ?)", query->{
                    query.setString(1, userData.username());
                    query.setString(2, encoder.encode(userData.password()));
                    query.setString(3, userData.email());
                });
    }
    public static void createToken(AuthData authData) throws DataAccessException{
        DatabaseManager.execStatement(
                "INSERT INTO auth (token, username) VALUES (?, ?)", query->{
                    query.setString(1, authData.authToken());
                    query.setString(2, authData.username());
                });
    }
    public static void deleteToken(String token) throws DataAccessException{
        DatabaseManager.execStatement(
                "DELETE FROM auth WHERE token=?", query->{
                    query.setString(1, token);
                });
    }
    public static UserData userFromToken(String token) throws DataAccessException{
        AtomicReference<UserData> toReturn = new AtomicReference<>();

        DatabaseManager.execQuery("SELECT username from auth WHERE token=?", query->{
            query.setString(1, token);
        }, resultSet -> {
            if(!resultSet.next()) return;

            DatabaseManager.execQuery("SELECT * FROM users WHERE username=?", query2->{
                query2.setString(1, resultSet.getString(1));
            }, resultSet2 -> {
                if(!resultSet2.next()) return;
                toReturn.set(new UserData(
                        resultSet2.getString(1),
                        resultSet2.getString(2),
                        resultSet2.getString(3)
                ));
            });
        });

        return toReturn.get();
    }
}
