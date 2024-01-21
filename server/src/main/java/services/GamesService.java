package services;

import chess.ChessGame;
import model.GameData;
import server.Server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

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
        games.put(id, new GameData(id, name));//new ChessGame() ??
        return id;
    }
    public static Optional<Server.FailedResponse> joinGame(int gameID, String color, String username){
        var game = games.get(gameID);
        if(game==null) return Optional.of(Server.FailedResponse.BAD_REQ);

        ChessGame.TeamColor teamColor;
        switch(color){
            case "WHITE" -> teamColor=ChessGame.TeamColor.WHITE;
            case "BLACK" -> teamColor=ChessGame.TeamColor.BLACK;
            default -> {
                return Optional.of(Server.FailedResponse.BAD_REQ);
            }
        }

        if(teamColor.whiteOrBlack(game.whiteUsername, game.blackUsername)==null){
            if(teamColor== ChessGame.TeamColor.WHITE)
                game.whiteUsername=username;
            else
                game.blackUsername=username;
            return Optional.empty();
        }else{
            return Optional.of(Server.FailedResponse.ALREADY_TAKEN);
        }
    }
    public static GameData[] getGames(){
        return games.values().toArray(new GameData[0]);
    }
}
