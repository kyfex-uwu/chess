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
    public static void clear() throws DataAccessException {
        GamesDataAccess.clear();
    }

    public static int createGame(String name) throws DataAccessException, Server.InvalidRequestException {
        if(name==null||name.isEmpty()) throw new Server.InvalidRequestException();

        int id;
        do {
            id = (int) Math.floor(Math.random() * 1544804416);//[0, 34^6)
        }while(GamesDataAccess.getGame(id)!=null);

        GamesDataAccess.createGame(id, name);
        return id;
    }
    public static Optional<Server.FailedResponse> joinGame(int gameID, String color, String username)
            throws DataAccessException{
        if(username==null||username.isEmpty()||
                color==null||color.isEmpty()) return Optional.of(Server.FailedResponse.BAD_REQ);
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

    //--

    public static GameData[] getGamesWithUser(String username) throws DataAccessException, Server.InvalidRequestException {
        if(username==null||username.isEmpty()) throw new Server.InvalidRequestException();
        return GamesDataAccess.getGamesWithUser(username);
    }
}
