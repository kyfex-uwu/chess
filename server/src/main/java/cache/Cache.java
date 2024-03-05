package cache;

import dataAccess.DataAccessException;
import model.GameData;
import services.GamesService;

import java.util.HashMap;
import java.util.Set;

public class Cache {
    private static HashMap<Integer, GameDataView> gamesByID = new HashMap<>();
    public static GameDataView getGame(int id) throws DataAccessException {//filtering out watchers has to be done manually
        if(gamesByID.containsKey(id)){
            return gamesByID.get(id);
        } else{
            var toReturn = new GameDataView(id);
            gamesByID.put(id, toReturn);
            return toReturn;
        }
    }
}
