package services;

import chess.ChessGame;
import model.GameData;

import java.util.HashMap;
import java.util.HashSet;

public class GamesService {
    private static final HashMap<Integer, GameData> games = new HashMap<>();
    private static final HashSet<Integer> gameIDs = new HashSet<>();

    public static void clear(){
        games.clear();
        gameIDs.clear();
    }

    public static int createGame(String name){
        int id;
        do{
            id= (int) Math.floor(Math.random()*1544804416);//34^6
        }while(gameIDs.contains(id));

        gameIDs.add(id);
        games.put(id, new GameData(id, null, null, name, null));//new ChessGame() ??
        return id;
    }
    public static GameData[] getGames(){
        return games.values().toArray(new GameData[0]);
    }
}
