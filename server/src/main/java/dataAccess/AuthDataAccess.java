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
        if(username==null||username.isEmpty()) throw new DataAccessException("invalid username");

        AtomicReference<UserData> toReturn = new AtomicReference<>();
        DatabaseManager.execQuery("SELECT * FROM users WHERE username=?", query->{
            query.setString(1,username);
        }, resultSet->{
            if(!resultSet.next()) return;
            toReturn.set(new UserData(resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getInt(5)));
        });
        return toReturn.get();
    }
    public static void createUser(UserData userData) throws DataAccessException{
        if(!userData.isValid()) throw new DataAccessException("invalid data");

        DatabaseManager.execStatement(
                "INSERT INTO users (username, password, email, pfp) VALUES (?, ?, ?, ?)", query->{
                    query.setString(1, userData.username());
                    query.setString(2, encoder.encode(userData.password()));
                    query.setString(3, userData.email());
                    query.setString(4, userData.pfp());
                });
    }
    public static void createToken(AuthData authData) throws DataAccessException{
        if(!authData.isValid()) throw new DataAccessException("invalid data");

        DatabaseManager.execStatement(
                "INSERT INTO auth (token, username) VALUES (?, ?)", query->{
                    query.setString(1, authData.authToken());
                    query.setString(2, authData.username());
                });
    }
    public static void deleteToken(String token) throws DataAccessException{
        if(token==null||token.isEmpty()) throw new DataAccessException("invalid token");

        DatabaseManager.execStatement(
                "DELETE FROM auth WHERE token=?", query->{
                    query.setString(1, token);
                });
    }
    public static UserData userFromToken(String token) throws DataAccessException{
        if(token==null||token.isEmpty()) throw new DataAccessException("invalid token");

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
                        resultSet2.getString(3),
                        resultSet2.getString(4),
                        resultSet2.getInt(5)
                ));
            });
        });

        return toReturn.get();
    }

    //--

    public static void updateAchievements(int achievements, String username) throws DataAccessException{
        if(username==null||username.isEmpty()) throw new DataAccessException("invalid username");

        DatabaseManager.execStatement("UPDATE users SET achievements=? WHERE username=?\n", query->{
            query.setInt(1, achievements);
            query.setString(2, username);
        });
    }
}
