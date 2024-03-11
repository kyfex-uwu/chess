package chess.specialmoves;

import chess.*;

/**
 * Represents a castling move
 */
public class CastleMove extends ChessMove {
    static{
        Json.specialMoveDeserializers.put("castle", data->
                new CastleMove(
                        ChessGame.TeamColor.valueOf(data[1]),
                        Side.valueOf(data[2])));
    }

    public enum Side{
        KINGSIDE(8, 1),
        QUEENSIDE(1, -1);

        public final int x;
        public final int direc;
        Side(int castleX, int direc){
            this.x=castleX;
            this.direc=direc;
        }
    }
    private final Side side;
    private final ChessGame.TeamColor color;
    public CastleMove(ChessGame.TeamColor color, Side side) {
        super(new ChessPosition(color.row,5),
                new ChessPosition(color.row,5+side.direc*2), null);
        this.side=side;
        this.color=color;
    }

    public void apply(ChessBoard board){
    }

    public String toString(){
        return  "scastle "+this.color+" "+this.side;
    }
}
