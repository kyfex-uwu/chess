package chess;

import chess.specialmoves.CastleMove;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[] pieces = new ChessPiece[8*8];

    private final boolean[] blackDoubleMoved =new boolean[8];
    private final boolean[] whiteDoubleMoved =new boolean[8];
    private final boolean[] blackCanCastle=new boolean[]{true, true};
    private final boolean[] whiteCanCastle=new boolean[]{true, true};

    public ChessBoard() {}
    private ChessBoard(ChessPiece[] pieces, boolean[] BEP, boolean[] WEP, boolean[] BCC, boolean[] WCC){
        for(int i=0;i<pieces.length;i++){
            this.pieces[i]=pieces[i]==null?null:pieces[i].clone();
        }
        System.arraycopy(BEP, 0, this.blackDoubleMoved, 0, 8);
        System.arraycopy(WEP, 0, this.whiteDoubleMoved, 0, 8);
        System.arraycopy(BCC, 0, this.blackCanCastle, 0, 2);
        System.arraycopy(WCC, 0, this.whiteCanCastle, 0, 2);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        if(!position.isValid()) return;
        this.pieces[position.toIndex()]=piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if(!position.isValid()) return null;
        return this.pieces[position.toIndex()];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        Arrays.fill(this.pieces, null);

        for(int i=0;i<2;i++){
            int offs=i==0?0:56;
            ChessGame.TeamColor color = i==0? ChessGame.TeamColor.WHITE: ChessGame.TeamColor.BLACK;

            this.pieces[0+offs]=new ChessPiece(color, ChessPiece.PieceType.ROOK);
            this.pieces[7+offs]=new ChessPiece(color, ChessPiece.PieceType.ROOK);
            this.pieces[1+offs]=new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
            this.pieces[6+offs]=new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
            this.pieces[2+offs]=new ChessPiece(color, ChessPiece.PieceType.BISHOP);
            this.pieces[5+offs]=new ChessPiece(color, ChessPiece.PieceType.BISHOP);
            this.pieces[3+offs]=new ChessPiece(color, ChessPiece.PieceType.QUEEN);
            this.pieces[4+offs]=new ChessPiece(color, ChessPiece.PieceType.KING);
        }

        for(int i=0;i<8;i++){
            this.pieces[8+i]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            this.pieces[48+i]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }

        Arrays.fill(this.whiteDoubleMoved, false);
        Arrays.fill(this.blackDoubleMoved, false);
        Arrays.fill(this.whiteCanCastle, true);
        Arrays.fill(this.blackCanCastle, true);
    }

    //--

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMovesOf(ChessPosition startPosition) {
        var piece=this.getPiece(startPosition);
        if(piece==null) return null;
        return piece.pieceMoves(this, startPosition).stream().filter(move->{
            var futureBoard = this.clone();
            move.apply(futureBoard);
            return !futureBoard.isInCheck(piece.getTeamColor());
        }).collect(Collectors.toSet());
    }

    /**
     * Determines if the given team is in check
     *
     * @param color which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(ChessGame.TeamColor color){
        for(int i=0;i<64;i++){
            if(this.pieces[i]==null||this.pieces[i].getTeamColor()==color) continue;

            for(var move : this.pieces[i].pieceMoves(this,
                    new ChessPosition((int) Math.floor(i/8f)+1, i%8+1), false)){
                var destPiece = this.getPiece(move.getEndPosition());
                if(destPiece!=null&&destPiece.getPieceType()==ChessPiece.PieceType.KING
                        &&destPiece.getTeamColor()==color)
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns if the given team has any valid moves
     * @param color team to check
     * @return if the team has any valid moves
     */
    public boolean hasValidMoves(ChessGame.TeamColor color){
        for(int i=0;i<64;i++){
            if(this.pieces[i]==null||this.pieces[i].getTeamColor()!=color) continue;

            if(this.validMovesOf(new ChessPosition((int) Math.floor(i/8f)+1, i%8+1)).size()>0)
                return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     * (returns if the team is in check, and if they do not have any valid moves)
     *
     * @param color which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckMate(ChessGame.TeamColor color){
        return this.isInCheck(color)&&!this.hasValidMoves(color);
    }

    //--

    /**
     * Returns if a pawn of color {@code color} can <b>finish</b> their en passant on this position
     * @param color color of pawn performing the en passant
     * @param pos position the pawn will end it's en passant on
     * @return if the pawn can perform this en passant
     */
    public boolean canEnPassantTo(ChessGame.TeamColor color, ChessPosition pos){
        if(!pos.isValid()||pos.getRow()!=color.whiteOrBlack(6,3)) return false;

        var capturingPiece = this.getPiece(pos.addOffset(new ChessPiece.Offset(0,-color.advDir)));
        return color.whiteOrBlack(this.blackDoubleMoved,this.whiteDoubleMoved)[pos.getColumn()-1]&&
                (capturingPiece!=null&&capturingPiece.getPieceType()== ChessPiece.PieceType.PAWN&&
                        capturingPiece.getTeamColor()==color.opposite());
    }

    /**
     * Call {@code clearDidDoubleMove(color)} right before team {@code color} moves<br>
     * Clears the double move flag of the team specified
     * @param color the team to clear
     */
    public void clearDidDoubleMove(ChessGame.TeamColor color){
        if(color== ChessGame.TeamColor.WHITE)
            Arrays.fill(this.whiteDoubleMoved, false);
        else if(color== ChessGame.TeamColor.BLACK)
            Arrays.fill(this.blackDoubleMoved, false);
    }

    /**
     * Call {@code clearDidDoubleMove(color)} right after team {@code color} moves, if the move is a double move<br>
     * Sets the double move flag of the team specified
     * @param col the column the pawn double moved on
     * @param color the team to set
     */
    public void setDoubleMoved(int col, ChessGame.TeamColor color){
        color.whiteOrBlack(this.whiteDoubleMoved,this.blackDoubleMoved)[col-1]=true;
    }

    /**
     * Returns if the board contains a piece of the specified type and color at the specified position
     * @param board the board to check
     * @param piece the piece that should be at the position
     * @param pos the position to check
     * @return if the specified piece is at the position
     */
    public static boolean checkPiece(ChessBoard board, ChessPiece piece, ChessPosition pos){
        var toCheck=board.pieces[pos.toIndex()];
        if(toCheck==null) return false;
        return toCheck.getPieceType() == piece.getPieceType() && toCheck.getTeamColor() == piece.getTeamColor();
    }

    /**
     * Returns if the specified team can castle on that side
     * @param color the team requesting to castle
     * @param side the side to castle on
     * @return if that team can castle on that side
     */
    public boolean canCastle(ChessGame.TeamColor color, CastleMove.Side side){
        if(!checkPiece(this, new ChessPiece(color, ChessPiece.PieceType.KING),
                new ChessPosition(color.row,5))||
                !checkPiece(this, new ChessPiece(color, ChessPiece.PieceType.ROOK),
                    new ChessPosition(color.row,side.x))) return false;

        if(color.whiteOrBlack(this.whiteCanCastle,this.blackCanCastle)
                [side==CastleMove.Side.QUEENSIDE?0:1]){
            if(this.isInCheck(color)) return false;
            for(int i=side.x-side.direc;i!=5;i-=side.direc){
                var pos=new ChessPosition(color.row, i);
                if(this.getPiece(pos)!=null) return false;
                var futureBoard = this.clone();
                futureBoard.addPiece(pos, new ChessPiece(color, ChessPiece.PieceType.KING));
                if(futureBoard.isInCheck(color)) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Call {@code removeCastlePrivileges(color, side)} after team {@code color} castles
     * Removes both castle privileges from a team
     *
     * @param color team to remove castle privileges from
     */
    public void removeCastlePrivileges(ChessGame.TeamColor color){
        Arrays.fill(color.whiteOrBlack(this.whiteCanCastle, this.blackCanCastle), false);
    }
    /**
     * Call {@code removeCastlePrivileges(color, side)} after team {@code color} castles
     * Removes castle privileges from a team on the specified side
     *
     * @param color team to remove castle privileges from
     */
    public void removeCastlePrivileges(ChessGame.TeamColor color, CastleMove.Side side){
        color.whiteOrBlack(this.whiteCanCastle, this.blackCanCastle)[side== CastleMove.Side.QUEENSIDE?0:1]=false;
    }

    //--

    public ChessBoard clone(){
        return new ChessBoard(this.pieces, this.blackDoubleMoved, this.whiteDoubleMoved,
                this.blackCanCastle, this.whiteCanCastle);
    }

    //--

    public boolean equals(Object other){
        if(!(other instanceof ChessBoard otherBoard)) return false;
        return Arrays.equals(this.pieces, otherBoard.pieces)&&
                Arrays.equals(this.whiteDoubleMoved, otherBoard.whiteDoubleMoved)&&
                Arrays.equals(this.blackDoubleMoved, otherBoard.blackDoubleMoved);
    }

    public static class ChessBoardSerializer implements JsonSerializer<ChessBoard> {
        @Override
        public JsonElement serialize(ChessBoard chessBoard, Type type, JsonSerializationContext jsonSerializationContext) {
            var toReturn = new JsonObject();
            toReturn.addProperty("pieces", Arrays.stream(chessBoard.pieces).map(piece->
                    String.valueOf(piece == null ? ' ' : piece.toCompressedString()))
                    .collect(Collectors.joining())+"-");

            return null;
        }
    }
    public static class ChessBoardDeserializer implements JsonDeserializer<ChessBoard> {
        @Override
        public ChessBoard deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }
    }
}
