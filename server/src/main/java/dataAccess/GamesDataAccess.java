package dataAccess;

import chess.ChessBoard;
import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static server.Server.GSON;
import static services.AuthService.encoder;

public class GamesDataAccess {
    public static void clear() throws DataAccessException{
        DatabaseManager.execStatement("DELETE FROM games");
    }
    public static void createGame(int id, String name) throws DataAccessException{
        DatabaseManager.execStatement(
                "INSERT INTO games (gameID, name, game) VALUES (?, ?, ?)", query->{
                    query.setInt(1, id);
                    query.setString(2, name);
                    query.setString(3, GSON.toJsonTree(new ChessGame()).toString());
                });
    }
    public static void joinGame(int id, ChessGame.TeamColor color, String username) throws DataAccessException{
        DatabaseManager.execStatement(
                "UPDATE games SET "+color.whiteOrBlack("white","black")+"=? WHERE gameID=?", query->{
                    query.setString(1, username);
                    query.setInt(2, id);
                });
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
                            GSON.fromJson(resultSet.getString(5), ChessGame.class)
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
}
