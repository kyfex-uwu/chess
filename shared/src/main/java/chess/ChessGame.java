package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    public static boolean TESTING = true;
    static{
        if(TESTING) System.out.println("=".repeat(50)+"\nTESTING\n"+"=".repeat(50));
    }
    private TeamColor currTeam;
    private ChessBoard board;
    public WinType winner = WinType.NONE;
    public final ArrayList<ChessMove.ReversibleChessMove<?>> history = new ArrayList<>();

    public ChessGame() {
        this.currTeam=TeamColor.WHITE;
        this.board=new ChessBoard();
        this.board.resetBoard();
    }
    ChessGame(TeamColor currTeam, ChessBoard board, ArrayList<ChessMove.ReversibleChessMove<?>> history){
        this.currTeam=currTeam;
        this.board=board;
        this.history.addAll(history);
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
        WHITE(1, 1, "White"),
        BLACK(8, -1, "Black");
        public final int row;
        public final int advDir;
        public final String name;
        TeamColor(int row, int advDir, String name){
            this.row=row;
            this.advDir=advDir;
            this.name=name;
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
    public enum WinType{
        WHITE(TeamColor.WHITE),
        BLACK(TeamColor.BLACK),
        DRAW(null),
        NONE(null);
        public final TeamColor color;
        WinType(TeamColor color){ this.color = color; }
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
            var toAdd = move.apply(this.board);

            toAdd.piece=piece;
            if(this.isInCheckmate(piece.getTeamColor().opposite())) {
                toAdd.checkType = ChessMove.ReversibleChessMove.CheckType.MATE;
                this.winner = this.currTeam.whiteOrBlack(WinType.WHITE, WinType.BLACK);
            }else if(this.isInCheck(piece.getTeamColor().opposite()))
                toAdd.checkType= ChessMove.ReversibleChessMove.CheckType.CHECK;
            else if(this.isInStalemate(piece.getTeamColor().opposite()))
                this.winner = WinType.DRAW;

            this.history.add(toAdd);
        }else{
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
}
