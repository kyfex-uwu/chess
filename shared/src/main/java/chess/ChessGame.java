package chess;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor currTeam;
    private ChessBoard board;
    public final ArrayList<ChessMove> history = new ArrayList<>();

    public ChessGame() {
        this.currTeam=TeamColor.WHITE;
        this.board=new ChessBoard();
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.currTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currTeam=team;
        this.board.clearDidDoubleMove(team);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE(1, 1),
        BLACK(8, -1);
        public final int row;
        public final int advDir;
        TeamColor(int row, int advDir){
            this.row=row;
            this.advDir=advDir;
        }

        /**
         * @return opposite color
         */
        public TeamColor opposite(){
            return this==WHITE?BLACK:WHITE;
        }

        /**
         * Returns {@code white} if this is WHITE, and {@code black} if this is BLACK
         * @param white To return if WHITE
         * @param black To return if BLACK
         * @return Either {@code white} or {@code black}
         */
        public <T> T whiteOrBlack(T white, T black){
            if(this==WHITE) return white;
            return black;
        }
    }

    /**
     * @see ChessBoard#validMovesOf(ChessPosition)
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        return this.board.validMovesOf(startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        var piece = this.board.getPiece(move.getStartPosition());
        if(piece.getTeamColor()==this.currTeam&&this.validMoves(move.getStartPosition()).contains(move)){
            move.apply(this.board);
            this.history.add(move);
        }else{
            //System.out.println(move+" is invalid");
            throw new InvalidMoveException();
        }
        this.setTeamTurn(this.currTeam.opposite());
    }

    /**
     * @see ChessBoard#isInCheck(TeamColor)
     */
    public boolean isInCheck(TeamColor teamColor) {
        return this.board.isInCheck(teamColor);
    }

    /**
     * @see ChessBoard#isInCheckMate(TeamColor)
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return this.board.isInCheckMate(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     * (returns if the team has no valid moves and they are not in check)
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !this.board.hasValidMoves(teamColor)&&!this.isInCheck(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board=board;
        // :(
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    //--

    public static ChessGame deserialize(String str) throws JsonParseException {
        try {
            var obj = JsonParser.parseString(str).getAsJsonObject();
            var toReturn = new ChessGame();

            toReturn.currTeam = obj.get("currTeam").getAsString().equals("WHITE")?TeamColor.WHITE:TeamColor.BLACK;
            toReturn.board = Json.deserializeChessBoard(obj.get("board").getAsJsonObject());

            return toReturn;
        }catch(Exception e){
            throw new JsonParseException("Could not parse");
        }
    }
}
