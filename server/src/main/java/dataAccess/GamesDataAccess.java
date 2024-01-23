package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashMap;

public class GamesDataAccess {
    public static void clear(){
        games.clear();
    }
    public static void createGame(int id, String name) throws DataAccessException{
        //if game with id already exists, throw error
        if(games.get(id)!=null) throw new DataAccessException("game already exists with id");
        games.put(id, new GameData(id, name));
    }
    public static void joinGame(int id, ChessGame.TeamColor color, String username) throws DataAccessException{
        var game = games.get(id);
        if(game==null) throw new DataAccessException("game does not exist");

        if(color== ChessGame.TeamColor.WHITE) game.whiteUsername=username;
        else game.blackUsername=username;
    }
    public static GameData getGame(int id) throws DataAccessException{
        if(!games.containsKey(id)) return null;
        return games.get(id);
    }
    public static GameData[] getGames() throws DataAccessException{
        return games.values().toArray(new GameData[0]);
    }

    //--

    private static HashMap<Integer, GameData> games = new HashMap<>();
}
