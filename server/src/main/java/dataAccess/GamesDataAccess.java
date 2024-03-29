package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static chess.Json.GSON;

public class GamesDataAccess {
    public static void clear() throws DataAccessException{
        DatabaseManager.execStatement("DELETE FROM games");
    }
    public static void createGame(int id, String name) throws DataAccessException{
        if(name==null||name.isEmpty()) throw new DataAccessException("invalid name");
        DatabaseManager.execStatement(
                "INSERT INTO games (gameID, name, game) VALUES (?, ?, ?)", query->{
                    query.setInt(1, id);
                    query.setString(2, name);
                    query.setString(3, GSON.toJsonTree(new ChessGame()).toString());
                });
    }
    public static void joinGame(int id, ChessGame.TeamColor color, String username) throws DataAccessException{
        if(username==null||username.isEmpty()) throw new DataAccessException("invalid username");
        if(color==null) throw new DataAccessException("invalid color");

        var result = DatabaseManager.execStatement(
                "UPDATE games SET " + color.whiteOrBlack("white", "black") + "=? WHERE gameID=?", query -> {
                    query.setString(1, username);
                    query.setInt(2, id);
                });
        if(result==0) throw new DataAccessException("no game found");
    }
    public static GameData getGame(int id) throws DataAccessException{
        AtomicReference<GameData> toReturn = new AtomicReference<>();
        DatabaseManager.execQuery(
                "SELECT * FROM games WHERE gameID=?", query->{
                    query.setInt(1, id);
                },resultSet -> {
                    if(!resultSet.next()) return;
                    toReturn.set(new GameData(
                            resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            ChessGame.deserialize(resultSet.getString(5))
                    ));
                });
        return toReturn.get();
    }
    public static GameData[] getGames() throws DataAccessException{
        ArrayList<GameData> games = new ArrayList<>();
        DatabaseManager.execQuery(
                "SELECT * FROM games", resultSet -> {
                    while(resultSet.next()){
                        var currGame = new GameData(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4),
                                ChessGame.deserialize(resultSet.getString(5))
                        );
                        games.add(currGame);
                    }
                });
        return games.toArray(new GameData[0]);
    }

    //--

    public static GameData[] getGamesWithUser(String username) throws DataAccessException{
        if(username==null||username.isEmpty()) throw new DataAccessException("invalid username");
        ArrayList<GameData> games = new ArrayList<>();
        DatabaseManager.execQuery(
                "SELECT * FROM games WHERE white=? OR black=?", query->{
                    query.setString(1, username);
                    query.setString(2, username);
                },resultSet -> {
                    while(resultSet.next()){
                        var currGame = new GameData(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4),
                                ChessGame.deserialize(resultSet.getString(5))
                        );
                        games.add(currGame);
                    }
                });
        return games.toArray(new GameData[0]);
    }
}
