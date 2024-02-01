package chess;

import chess.specialmoves.CastleMove;
import chess.specialmoves.EnPassantMove;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Json {
    public static final String jsonEmpty = "{}";
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ChessBoard.class, new ChessBoardSerializer())
            .registerTypeAdapter(ChessBoard.class, new ChessBoardDeserializer())
            .registerTypeAdapter(ChessMove.class, new ChessMoveSerializer())
            .registerTypeAdapter(ChessPosition.class, new ChessPosSerializer())
            .create();

    //-- ChessBoard

    private static String miscMoveDataToString(boolean[] BDM, boolean[] WDM, boolean[] BC, boolean[] WC){
        boolean[] allArray = new boolean[BDM.length+WDM.length+BC.length+WC.length];
        System.arraycopy(BDM, 0, allArray, 0, 8);
        System.arraycopy(WDM, 0, allArray, 8, 8);
        System.arraycopy(BC, 0, allArray, 16, 2);
        System.arraycopy(WC, 0, allArray, 18, 2);

        StringBuilder toReturn= new StringBuilder();
        for(int i=0;i<allArray.length;i+=4){
            int n=0;
            for(int j=0;j<4;j++){
                n=n*2+(j+i<allArray.length?(allArray[j+i]?1:0):0);
            }
            toReturn.append(Integer.toString(n, 16));
        }
        return toReturn.toString();
    }
    private static class ChessBoardSerializer implements JsonSerializer<ChessBoard> {
        @Override
        public JsonElement serialize(ChessBoard chessBoard, Type type, JsonSerializationContext jsonSerializationContext) {
            var toReturn = new JsonObject();
            toReturn.addProperty("pieces", Arrays.stream(chessBoard.pieces).map(piece->
                    String.valueOf(piece == null ? ' ' : piece.toCompressedString()))
                    .collect(Collectors.joining()));
            toReturn.addProperty("miscMovedData",
                    miscMoveDataToString(chessBoard.blackDoubleMoved,
                            chessBoard.whiteDoubleMoved,
                            chessBoard.blackCanCastle,
                            chessBoard.whiteCanCastle));

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
                    toReturn.pieces[i] = new ChessPiece(
                            Character.isLowerCase(pieces[i])? ChessGame.TeamColor.WHITE: ChessGame.TeamColor.BLACK,
                            ChessPiece.PieceType.getType(pieces[i]));
                }

                var miscData = obj.get("miscMovedData").getAsString().toCharArray();
                var miscDataToBools = new boolean[miscData.length*4];
                for(int i=0;i< miscData.length;i++){
                    int num = Integer.valueOf(String.valueOf(miscData[i]),16);
                    for(int j=0;j<4;j++){
                        miscDataToBools[i*4+j]=num%2==1;
                        num/=2;
                    }
                }
                for(int i=0;i<miscDataToBools.length;i++){
                    var val = miscDataToBools[i];
                    if(i<8){
                        toReturn.blackDoubleMoved[i]=val;
                    }else if(i<16){
                        toReturn.whiteDoubleMoved[i-8]=val;
                    }else if(i<18){
                        toReturn.blackCanCastle[i-16]=val;
                    }else if(i<20){
                        toReturn.whiteCanCastle[i-18]=val;
                    }else{
                        break;
                    }
                }

                return toReturn;
            }catch(Exception e){
                throw new JsonParseException(e);
            }
        }
    }

    //-- ChessMove
    public static final HashMap<String, Function<String[], ChessMove>> specialMoveDeserializers = new HashMap<>();
    public static ChessMove deserializeChessMove(JsonPrimitive obj) throws JsonParseException {
        try{
            var components = obj.getAsString().split(" ");
            if(components[0].startsWith("s")){
                return specialMoveDeserializers.get(components[0].substring(1)).apply(components);
            }

            return new ChessMove(
                    deserializeChessPosition(new JsonPrimitive(components[0])),
                    deserializeChessPosition(new JsonPrimitive(components[1])),
                    components.length==3?ChessPiece.PieceType.getType(components[2].charAt(0)):null);
        }catch(Exception e){
            throw new JsonParseException(e);
        }
    }
    private static class ChessMoveSerializer implements JsonSerializer<ChessMove>{
        @Override
        public JsonElement serialize(ChessMove chessMove, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(chessMove.toString());
        }
    }

    //-- ChessPosition
    public static ChessPosition deserializeChessPosition(JsonPrimitive obj) throws JsonParseException {
        try{
            var str=obj.getAsString();
            return new ChessPosition('a'-str.charAt(0)+1, '1'-str.charAt(2)+1);
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
}
