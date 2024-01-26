package server;

import chess.ChessBoard;
import com.google.gson.*;
import dataAccess.DataAccessException;
import model.*;
import services.AuthService;
import services.GamesService;
import spark.ExceptionHandler;
import spark.Spark;

public class Server {
    public static final String jsonEmpty = "{}";
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ChessBoard.class, new ChessBoard.ChessBoardSerializer())
            .create();

    public enum FailedResponse {
        ALREADY_TAKEN(403, "already taken"),
        NOT_AUTH(401, "not authorized"),
        BAD_REQ(400,"bad request"),
        SERVER_ERROR(500, "something went wrong, please try again");
        public final int status;
        public final String message;
        FailedResponse(int status, String message){
            this.status=status;
            this.message=message;
        }
    }

    private record ErrorMessage(String message){
        public static String error(String message){
            return GSON.toJson(new ErrorMessage("Error: "+message));
        }
    }
    public static class InvalidRequestException extends Exception{}

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        
        Spark.staticFiles.location("web");

        //delete db
        Spark.delete("/db", (req, res) -> {
            AuthService.clear();
            GamesService.clear();

            res.status(200);
            return jsonEmpty;
        });
        //register
        Spark.post("/user", (req, res) -> {
            var data = GSON.fromJson(req.body(), UserData.class);

            if(!AuthService.registerUser(data)){
                res.status(FailedResponse.ALREADY_TAKEN.status);
                return ErrorMessage.error(FailedResponse.ALREADY_TAKEN.message);
            }

            var token = AuthService.login(new LoginData(data.username(), data.password()));
            if(token.isEmpty()){
                res.status(FailedResponse.SERVER_ERROR.status);
                return ErrorMessage.error(FailedResponse.SERVER_ERROR.message);
            }

            res.status(200);
            return GSON.toJson(new AuthData(token.get(), data.username()));
        });
        //login
        Spark.post("/session", (req, res) -> {
            var data = GSON.fromJson(req.body(), LoginData.class);

            var token = AuthService.login(data);
            if(token.isEmpty()){
                res.status(FailedResponse.NOT_AUTH.status);
                return ErrorMessage.error(FailedResponse.NOT_AUTH.message);
            }

            res.status(200);
            return GSON.toJson(new AuthData(token.get(), data.username()));
        });
        //logout
        Spark.delete("/session", (req, res) -> {
            var hRes=AuthService.validateHeader(req);
            if(hRes.isPresent()){
                res.status(hRes.get().status);
                return ErrorMessage.error(hRes.get().message);
            }
            AuthService.logout(req.headers("Authorization"));

            res.status(200);
            return jsonEmpty;
        });

        //list games
        Spark.get("/game", (req, res) -> {
            var hRes= AuthService.validateHeader(req);
            if(hRes.isPresent()){
                res.status(hRes.get().status);
                return ErrorMessage.error(hRes.get().message);
            }

            JsonArray array = new JsonArray();
            for(var data : GamesService.getGames()) {
                array.add(GSON.toJsonTree(data));
            }

            var toReturn = new JsonObject();
            toReturn.add("games", array);//remove chessgame field (? what)
            res.status(200);
            return toReturn;
        });
        //create game
        Spark.post("/game", (req, res) -> {
            var hRes= AuthService.validateHeader(req);
            if(hRes.isPresent()){
                res.status(hRes.get().status);
                return ErrorMessage.error(hRes.get().message);
            }

            var body = JsonParser.parseString(req.body());
            String gameName;
            try{
                gameName=((JsonObject)body).get("gameName").getAsString();
            }catch(Exception e){
                res.status(FailedResponse.BAD_REQ.status);
                return ErrorMessage.error(FailedResponse.BAD_REQ.message);
            }
            var id = GamesService.createGame(gameName);

            var toReturn = new JsonObject();
            toReturn.addProperty("gameID", id);
            res.status(200);
            return toReturn;
        });
        //join game
        Spark.put("/game", (req, res) -> {
            var hRes= AuthService.validateHeader(req);
            if(hRes.isPresent()){
                res.status(hRes.get().status);
                return ErrorMessage.error(hRes.get().message);
            }
            var data = GSON.fromJson(req.body(), JoinGameData.class);
            if (!data.isValid()) throw new InvalidRequestException();

            var username = AuthService.getUserFromToken(req.headers("authorization")).username();

            if(data.playerColor()!=null){//joining
                var gameJoin = GamesService.joinGame(data.gameID(), data.playerColor(), username);
                if (gameJoin.isPresent()) {
                    res.status(gameJoin.get().status);
                    return ErrorMessage.error(gameJoin.get().message);
                }
            }else{//watching

            }

            res.status(200);
            return jsonEmpty;
        });

        ExceptionHandler<Exception> e400handler = (e, request, response) -> {
            if(logErrors) e.printStackTrace();

            response.status(FailedResponse.BAD_REQ.status);
            response.body(ErrorMessage.error(FailedResponse.BAD_REQ.message));
        };
        Spark.exception(JsonSyntaxException.class, e400handler);
        Spark.exception(InvalidRequestException.class, e400handler);

        Spark.exception(DataAccessException.class, (e, request, response)->{
            if(logErrors) e.printStackTrace();

            response.status(FailedResponse.SERVER_ERROR.status);
            response.body(ErrorMessage.error("Error: "+e.getMessage()));
        });

        Spark.awaitInitialization();
        return Spark.port();
    }
    private static final boolean logErrors=false;

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}