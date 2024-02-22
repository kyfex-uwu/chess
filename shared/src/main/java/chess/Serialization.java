package chess;

import chess.specialmoves.CastleMove;
import chess.specialmoves.DoublePawnMove;
import chess.specialmoves.EnPassantMove;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Serialization {
    public static final String jsonEmpty = "{}";
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ChessBoard.class, new ChessBoardSerializer())
            .registerTypeAdapter(ChessBoard.class, new ChessBoardDeserializer())
            .registerTypeAdapter(ChessGame.class, new ChessGameSerializer())
            .registerTypeAdapter(ChessGame.class, new ChessGameDeserializer())
            .registerTypeAdapter(ChessMove.class, new ChessMoveSerializer())
            .registerTypeAdapter(ChessPosition.class, new ChessPosSerializer())
            .create();

    public static ChessPiece pieceFromChar(char piece){
        return new ChessPiece(
                Character.isLowerCase(piece)? ChessGame.TeamColor.WHITE: ChessGame.TeamColor.BLACK,
                ChessPiece.PieceType.getType(piece));
    }

    //-- ChessBoard

    private static class ChessBoardSerializer implements JsonSerializer<ChessBoard> {
        @Override
        public JsonElement serialize(ChessBoard chessBoard, Type type, JsonSerializationContext jsonSerializationContext) {
            var toReturn = new JsonObject();
            toReturn.addProperty("pieces", Arrays.stream(chessBoard.pieces).map(piece->
                    String.valueOf(piece == null ? ' ' : piece.toCompressedString()))
                    .collect(Collectors.joining()));
            toReturn.addProperty("miscMovedData",
                    chessBoard.miscMoveDataToString());

            return toReturn;
        }
    }
    private static class ChessBoardDeserializer implements JsonDeserializer<ChessBoard>{

        @Override
        public ChessBoard deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                var obj = jsonElement.getAsJsonObject();
                var toReturn = new ChessBoard();

                var pieces = obj.get("pieces").getAsString().toCharArray();
                for (int i = 0; i < 64; i++) {
                    if(pieces[i]==' ') continue;
                    toReturn.pieces[i] = pieceFromChar(pieces[i]);
                }

                toReturn.applyMoveData(obj.get("miscMovedData").getAsString());

                return toReturn;
            }catch(Exception e){
                throw new JsonParseException(e);
            }
        }
    }

    //-- ChessGame
    public static final HashMap<String, BiFunction<String[], ChessBoard, ChessMove.ReversibleChessMove<?>>>
            specialReversibleMoveDeserializers = new HashMap<>();

    private static class ChessGameSerializer implements JsonSerializer<ChessGame>{

        @Override
        public JsonElement serialize(ChessGame chessGame, Type type, JsonSerializationContext jsonSerializationContext) {
            var toReturn = new JsonObject();
            toReturn.addProperty("currTeam", chessGame.currTeam.name());
            toReturn.add("board", GSON.toJsonTree(chessGame.board));

            var history = new JsonArray();
            for(var reversibleMove : chessGame.history)
                history.add(reversibleMove.toString());
            toReturn.add("history", history);
            return toReturn;
        }
    }
    private static class ChessGameDeserializer implements JsonDeserializer<ChessGame>{

        @Override
        public ChessGame deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                var obj = jsonElement.getAsJsonObject();

                var board = GSON.fromJson(obj.get("board"), ChessBoard.class);

                var history = new ArrayList<ChessMove.ReversibleChessMove<?>>();
                var historyObj = obj.get("history").getAsJsonArray();
                for(int i=historyObj.size()-1;i>=0;i--){
                    var reversibleMoveArgs = historyObj.get(i).getAsString().split(":");
                    ChessMove.ReversibleChessMove<?> reversibleChessMove;
                    if(reversibleMoveArgs[0].startsWith("s")){
                        reversibleChessMove = specialReversibleMoveDeserializers.get(reversibleMoveArgs[0]
                                .split(" ")[0].substring(1)).apply(reversibleMoveArgs, board);
                    }else{
                        reversibleChessMove=new ChessMove.ReversibleChessMove<>(board,
                                deserializeMove(reversibleMoveArgs[0]),
                                reversibleMoveArgs[1].length()>0?pieceFromChar(reversibleMoveArgs[1].charAt(0)):null,
                                reversibleMoveArgs[2]);
                    }
                    reversibleChessMove.onReverse.accept(board);
                    history.add(reversibleChessMove);
                }

                return new ChessGame(
                        ChessGame.TeamColor.valueOf(obj.get("currTeam").getAsString()),
                        GSON.fromJson(obj.get("board"), ChessBoard.class),
                        history);
            }catch (Exception e){
                throw new JsonParseException("could not parse", e);
            }
        }
    }

    //-- ChessMove
    public static final HashMap<String, Function<String[], ChessMove>> specialMoveDeserializers = new HashMap<>();
    public static ChessMove deserializeMove(String move){
        var components = move.split(" ");
        if(components[0].startsWith("s")){
            return specialMoveDeserializers.get(components[0].substring(1)).apply(components);
        }

        return new ChessMove(
                deserializeChessPosition(new JsonPrimitive(components[0])),
                deserializeChessPosition(new JsonPrimitive(components[1])),
                components.length==3?ChessPiece.PieceType.getType(components[2].charAt(0)):null);
    }
    private static class ChessMoveSerializer implements JsonSerializer<ChessMove>{
        @Override
        public JsonElement serialize(ChessMove chessMove, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(chessMove.toString());
        }
    }

    //-- ChessPosition
    public static ChessPosition deserializeChessPosition(String str) throws ParseException{
        try{
            return new ChessPosition(str.charAt(1)-'1'+1, str.charAt(0)-'a'+1);
        }catch(Exception e){
            throw new ParseException(str+" could not parse", 0);
        }
    }
    public static ChessPosition deserializeChessPosition(JsonPrimitive obj) throws JsonParseException {
        try{
            return deserializeChessPosition(obj.getAsString());
        }catch(Exception e){
            throw new JsonParseException(e);
        }
    }
    private static class ChessPosSerializer implements JsonSerializer<ChessPosition>{
        @Override
        public JsonElement serialize(ChessPosition chessPos, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(chessPos.toString());
        }
    }

    //--

    static{
        CastleMove.initSpecialMove();
        DoublePawnMove.initSpecialMove();
        EnPassantMove.initSpecialMove();
    }
}
