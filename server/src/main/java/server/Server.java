package server;

import chess.Json;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dataAccess.DataAccessException;
import model.AuthData;
import model.JoinGameData;
import model.LoginData;
import model.UserData;
import services.AuthService;
import services.GamesService;
import spark.ExceptionHandler;
import spark.Spark;

import static chess.ChessGame.TESTING;

public class Server {
    //todo: golden board when you checkmate without losing a piece

    public enum FailedResponse {
        ALREADY_TAKEN(403, "already taken"),
        NOT_AUTH(401, "not authorized"),
        BAD_REQ(400,"bad request"),
        NOT_FOUND(404,"not found"),
        SERVER_ERROR(500, "something went wrong :(");
        public final int status;
        public final String message;
        FailedResponse(int status, String message){
            this.status=status;
            this.message=message;
        }
    }

    private record ErrorMessage(String message){
        public static String error(String message){
            return Json.GSON.toJson(new ErrorMessage("Error: "+message));
        }
    }
    public static class InvalidRequestException extends Exception{}

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.webSocket("/connect", WebsocketHandler.class);

        //delete db
        Spark.delete("/db", (req, res) -> {
            AuthService.clear();
            GamesService.clear();

            res.status(200);
            return Json.jsonEmpty;
        });
        //register
        Spark.post("/user", (req, res) -> {
            var data = Json.GSON.fromJson(req.body(), UserData.class);

            if(TESTING&&data.pfp()==null)
                data = new UserData(data.username(), data.password(), data.email());

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
            return Json.GSON.toJson(new AuthData(token.get(), data.username()));
        });
        //login
        Spark.post("/session", (req, res) -> {
            var data = Json.GSON.fromJson(req.body(), LoginData.class);

            var token = AuthService.login(data);
            if(token.isEmpty()){
                res.status(FailedResponse.NOT_AUTH.status);
                return ErrorMessage.error(FailedResponse.NOT_AUTH.message);
            }

            res.status(200);
            return Json.GSON.toJson(new AuthData(token.get(), data.username()));
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
            return Json.jsonEmpty;
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
                array.add(Json.GSON.toJsonTree(data));
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
                if(!TESTING&&!gameName.matches("[\\w ]{3,32}")) throw new Exception("wrong size");
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
            var data = Json.GSON.fromJson(req.body(), JoinGameData.class);
            if (!data.isValid()) throw new InvalidRequestException();

            var username = AuthService.getUserFromToken(req.headers("authorization")).username();

            if(!data.playerColor().isEmpty()){//joining
                var gameJoin = GamesService.joinGame(data.gameID(), data.playerColor(), username);
                if (gameJoin.isPresent()) {
                    res.status(gameJoin.get().status);
                    return ErrorMessage.error(gameJoin.get().message);
                }
            }else{
                var gameWatch = GamesService.watchGame(data.gameID(), username);
                if (gameWatch.isPresent()) {
                    res.status(gameWatch.get().status);
                    return ErrorMessage.error(gameWatch.get().message);
                }
            }

            //watch

            res.status(200);
            return Json.jsonEmpty;
        });

        //--

        //get specified user data
        Spark.get("/user/:username", (req, res) -> {
            var hRes=AuthService.validateHeader(req);
            if(hRes.isPresent()){
                res.status(hRes.get().status);
                return ErrorMessage.error(hRes.get().message);
            }

            var user = AuthService.getUserFromName(req.params(":username"));
            if(user==null){
                res.status(FailedResponse.NOT_FOUND.status);
                return ErrorMessage.error(FailedResponse.NOT_FOUND.message);
            }

            res.status(200);

            return Json.GSON.toJson(new UserData(user.username(),"","",user.pfp()));
        });

        //get games with user
        Spark.get("/game/:username", (req, res) -> {
            var hRes= AuthService.validateHeader(req);
            if(hRes.isPresent()){
                res.status(hRes.get().status);
                return ErrorMessage.error(hRes.get().message);
            }

            JsonArray array = new JsonArray();
            for(var data : GamesService.getGamesWithUser(req.params(":username"))) {
                array.add(Json.GSON.toJsonTree(data));
            }

            var toReturn = new JsonObject();
            toReturn.add("games", array);
            res.status(200);
            return toReturn;
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
    private static final boolean logErrors=true;

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}