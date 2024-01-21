package server;

import chess.ChessBoard;
import com.google.gson.*;
import model.*;
import services.AuthService;
import services.GamesService;
import spark.Spark;

public class Server {
    private static final String jsonEmpty = "{}";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ChessBoard.class, new ChessBoard.ChessBoardSerializer())
            .create();

    public enum FailedResponse {
        ALREADY_TAKEN(403, "already taken"),
        NOT_AUTH(401, "not authorized"),
        BAD_REQ(400,"bad request");
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
    private class InvalidRequestException extends Exception{}

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        
        Spark.staticFiles.location( "web");

        Spark.get("/", (req, res) -> {
            res.status(200);
            return "CS 240 Chess Server Web API";
        });

        //delete db
        Spark.delete("/db", (req, res) -> {
            AuthService.clear();
            GamesService.clear();

            res.status(200);
            return jsonEmpty;
        });
        //register
        Spark.post("/user", (req, res) -> {
            try {
                var data = GSON.fromJson(req.body(), UserData.class);
                if(!data.isValid()) throw new InvalidRequestException();

                if(!AuthService.registerUser(data)) {
                    res.status(403);
                    return ErrorMessage.error("already taken");
                }

                var token = AuthService.login(new LoginData(data.username(), data.password()));
                if(token.isEmpty()){
                    res.status(500);
                    return ErrorMessage.error("login failed");
                }

                res.status(200);
                return GSON.toJson(new AuthData(token.get(), data.username()));
            }catch(JsonSyntaxException | InvalidRequestException e){
                res.status(400);
                return ErrorMessage.error("bad request");
            }
        });
        //login
        Spark.post("/session", (req, res) -> {
            try {
                var data = GSON.fromJson(req.body(), LoginData.class);
                if(!data.isValid()) throw new InvalidRequestException();

                var token = AuthService.login(data);
                if(token.isEmpty()){
                    res.status(401);
                    return ErrorMessage.error("unauthorized");
                }

                res.status(200);
                return GSON.toJson(new AuthData(token.get(), data.username()));
            }catch(JsonSyntaxException | InvalidRequestException e){
                res.status(400);
                return ErrorMessage.error("bad request");
            }
        });
        //logout
        Spark.delete("/session", (req, res) -> {
            var hRes= AuthService.validateHeader(req);
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
            toReturn.add("games", array);//remove chessgame field
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
            int id;
            try {
                id = GamesService.createGame(((JsonObject)body).get("gameName").getAsString());
            }catch(Exception e){
                res.status(400);
                return ErrorMessage.error("bad request");
            }

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

            try {
                var data = GSON.fromJson(req.body(), JoinGameData.class);
                if (!data.isValid()) throw new InvalidRequestException();

                var username = AuthService.getUserFromToken(req.headers("authorization"));
                if (username.isEmpty()) {
                    res.status(500);
                    return ErrorMessage.error("server error");
                }

                if(data.playerColor()!=null){//joining
                    var gameJoin = GamesService.joinGame(data.gameID(), data.playerColor(), username.get());
                    if (gameJoin.isPresent()) {
                        res.status(gameJoin.get().status);
                        return ErrorMessage.error(gameJoin.get().message);
                    }
                }else{//watching

                }

                res.status(200);
                return jsonEmpty;
            }catch(JsonSyntaxException | InvalidRequestException e){
                res.status(400);
                return ErrorMessage.error("bad request");
            }
        });

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }
}