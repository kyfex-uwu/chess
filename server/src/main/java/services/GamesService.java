package services;

import chess.ChessGame;
import dataAccess.AuthDataAccess;
import dataAccess.DataAccessException;
import dataAccess.GamesDataAccess;
import model.GameData;
import server.Server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class GamesService {
    //todo: cache
    public static void clear(){
        GamesDataAccess.clear();
    }

    public static int createGame(String name) throws DataAccessException {
        int id;
        do {
            id = (int) Math.floor(Math.random() * 1544804416);//34^6
        }while(GamesDataAccess.getGame(id)!=null);

        GamesDataAccess.createGame(id, name);
        return id;
    }
    public static Optional<Server.FailedResponse> joinGame(int gameID, String color, String username)
            throws DataAccessException{
        var game = GamesDataAccess.getGame(gameID);
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
            GamesDataAccess.joinGame(gameID, teamColor, username);
            return Optional.empty();
        }else{
            return Optional.of(Server.FailedResponse.ALREADY_TAKEN);
        }
    }
    public static GameData[] getGames() throws DataAccessException {
        return GamesDataAccess.getGames();
    }
}
