package chess;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static chess.ChessRenderer.CLColor.*;

/**
 * Everything needed to render a chess game.
 * Use renderGame(ChessGame game, RenderData renderData) to draw a game, and
 * RenderData.from to get the renderData
 */
public class ChessRenderer {
    /**
     * Optional data to render a chess game
     */
    public static class RenderData{
        private final Collection<ChessPiece.Offset> highlightedPositions;
        private final ChessPiece.Offset originPosition;
        private RenderData(Collection<ChessPiece.Offset> highlightedPositions,
                           ChessPiece.Offset originPosition){
            this.highlightedPositions=highlightedPositions;
            this.originPosition=originPosition;
        }

        /**
         * Creates a {@code RenderData} instance with moves to highlight and an origin space that can make those moves
         * @param movesToShow
         * @param positionToShow
         * @return RenderData instance
         */
        public static RenderData from(Collection<ChessMove> movesToShow, ChessPosition positionToShow){
            return new RenderData(movesToShow.stream().map(move->new ChessPiece.Offset(move.getEndPosition().getColumn()-1,
                    move.getEndPosition().getRow()-1)).toList(),
                    positionToShow!=null?new ChessPiece.Offset(positionToShow.getColumn()-1, positionToShow.getRow()-1):
                            new ChessPiece.Offset(-1,-1));
        }

        /**
         * Creates an empty {@code RenderData} instance
         * @return RenderData instance
         */
        public static RenderData from(){
            return new RenderData(Collections.emptySet(), new ChessPiece.Offset(-1,-1));
        }
    }
    private static final Map<ChessPiece.PieceType, String> blackPieces = Map.ofEntries(
            Map.entry(ChessPiece.PieceType.KING,
                    """
                           \s + \s
                           ( | )
                           \\_I_/"""),
            Map.entry(ChessPiece.PieceType.QUEEN,
                    """
                            .'I'.
                            \\ | /
                            -----"""),
            Map.entry(ChessPiece.PieceType.BISHOP,
                    """
                            \s o \s
                            \s(+)\s
                            \s/ \\\s"""),
            Map.entry(ChessPiece.PieceType.KNIGHT,
                    """
                            \s/o^\s
                            \sL/ |
                            \s/__|"""),
            Map.entry(ChessPiece.PieceType.ROOK,
                    """
                            \s...\s
                            \s| |\s
                            \s---\s"""),
            Map.entry(ChessPiece.PieceType.PAWN,
                    """
                            \s   \s
                            \s o \s
                            \s A \s""")
    );
    private static final Map<ChessPiece.PieceType, String> whitePieces = Map.ofEntries(
            Map.entry(ChessPiece.PieceType.KING,
                    """
                           \s + \s
                           (X|X)
                           \\#I#/"""),
            Map.entry(ChessPiece.PieceType.QUEEN,
                    """
                            .iIi.
                            \\###/
                            ====="""),
            Map.entry(ChessPiece.PieceType.BISHOP,
                    """
                            \s * \s
                            \s(#)\s
                            \s/=\\\s"""),
            Map.entry(ChessPiece.PieceType.KNIGHT,
                    """
                            \s/*^\s
                            \sL/%|
                            \s/%%|"""),
            Map.entry(ChessPiece.PieceType.ROOK,
                    """
                            \s---\s
                            \s|#|\s
                            \s===\s"""),
            Map.entry(ChessPiece.PieceType.PAWN,
                    """
                            \s   \s
                            \s * \s
                            \s A \s""")
    );
    private static final String highlight="""
            +=   =+
            |     |
            +=   =+""";
    public enum CLColor{
        WHITE(97,107),
        GRAY(37,47),
        DARK_GRAY(90,100),
        BLACK(30,40),
        CLEAR(0,0),

        HIGHLIGHT(93,43),
        HIGHLIGHT2(92,42);

        public final int fg;
        public final int bg;
        CLColor(int fg, int bg){
            this.fg=fg;
            this.bg=bg;
        }

        /**
         * Sets the terminal color
         * @param fg foreground (text) color
         * @param bg background color
         */
        public static void setColor(CLColor fg, CLColor bg){
            if(!canShowColor) return;
            currColor=new int[]{fg.fg,bg.bg};
            System.out.print("\u001b["+currColor[0]+";"+currColor[1]+"m");
        }
    }

    private static int[] currColor = {0,0};
    public static boolean canShowColor=false;

    /**
     * Renders the specified game
     * @see RenderData
     * @param game The game to render
     * @param data Any additional data
     */
    public static void renderGame(ChessGame game, RenderData data){
        for(int y=0;y<=8;y++){
            setColor(CLEAR, CLEAR);
            if(!canShowColor) System.out.println("   "+"+-------".repeat(8)+"+");
            if(y==8){
                System.out.print("   ");
                for(int x=0;x<8;x++){
                    System.out.print((canShowColor?"   ":"    ")+(char)(x+97)+"   ");
                }
                System.out.print("\n\n   ");

                var color = game.getTeamTurn().whiteOrBlack(WHITE,BLACK);
                setColor(color,color==WHITE?DARK_GRAY:GRAY);

                if(game.isInCheckmate(game.getTeamTurn())){
                    System.out.print("Checkmate! "+game.getTeamTurn().opposite()+" wins");
                }else if(game.isInStalemate(game.getTeamTurn())){
                    System.out.print("Stalemate");
                }else{
                    System.out.print(game.getTeamTurn() + "'s move");
                    if(game.isInCheck(game.getTeamTurn()))
                        System.out.print(" (Check)");
                }
                setColor(CLEAR, CLEAR);
                System.out.println();
                break;
            }

            for(int i=0;i<3;i++) {
                for (int x = 0; x < 8; x++) {
                    var currPiece = game.getBoard().getPiece(new ChessPosition(8-y,x+1));
                    boolean isHighlighted = data.highlightedPositions.contains(new ChessPiece.Offset(x,7-y));
                    boolean isOrigin = data.originPosition.equals(new ChessPiece.Offset(x,7-y));
                    if (x == 0) {
                        if(i==1) System.out.print(" "+(8-y)+" ");
                        else System.out.print("   ");
                    }

                    if(!canShowColor) System.out.print("|");

                    setColor(currPiece==null?
                                    CLEAR:
                                    currPiece.getTeamColor().whiteOrBlack(WHITE,BLACK),
                            x%2==y%2?GRAY:DARK_GRAY);
                    String strToPrint;
                    if(currPiece!=null) {
                        strToPrint = " " +
                                (currPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? whitePieces : blackPieces)
                                        .get(currPiece.getPieceType()).substring(i * 6, i * 6 + 5) + " ";
                    } else {
                        strToPrint="       ";
                    }
                    if(isHighlighted||isOrigin){
                        var highlightColor=isOrigin?HIGHLIGHT:HIGHLIGHT2;
                        var overlayLine = highlight.substring(i*8,i*8+7)+" ";
                        var chars = strToPrint.toCharArray();
                        strToPrint="";
                        for(int j=0;j<chars.length;j++){
                            var overlayChar = overlayLine.charAt(j);
                            if(canShowColor) {
                                if (overlayChar == '=' || overlayChar == '|') {
                                    strToPrint+="\u001b["+highlightColor.fg+";"+currColor[1]+"m";
                                } else if (overlayChar == '#') {
                                    strToPrint+="\u001b["+highlightColor.fg+";"+highlightColor.bg+"m";
                                } else {// space
                                    strToPrint+="\u001b["+currColor[0]+";"+currColor[1]+"m";
                                }
                            }
                            strToPrint+=overlayChar==' '?chars[j]:overlayChar;
                        }
                    }
                    System.out.print(strToPrint);
                    setColor(CLEAR, CLEAR);

                    if(x==7)
                        System.out.println(canShowColor?"":"|");
                }
            }
        }
    }
}