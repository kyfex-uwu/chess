package chess;

import java.util.Collection;
import java.util.Map;

public class ChessRenderer {
    public static class RenderData{
        private final Collection<ChessPiece.Offset> highlightedPositions;
        private final ChessPiece.Offset originPosition;
        private RenderData(Collection<ChessPiece.Offset> highlightedPositions,
                           ChessPiece.Offset originPosition){
            this.highlightedPositions=highlightedPositions;
            this.originPosition=originPosition;
        }
        public static RenderData from(Collection<ChessMove> movesToShow, ChessPosition positionToShow){
            return new RenderData(movesToShow.stream().map(move->new ChessPiece.Offset(move.getEndPosition().getColumn()-1,
                    move.getEndPosition().getRow()-1)).toList(),
                    positionToShow!=null?new ChessPiece.Offset(positionToShow.getColumn()-1, positionToShow.getRow()-1):
                            new ChessPiece.Offset(-1,-1));
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
            #=   =#
            |     |
            #=   =#""";
    private enum CLColor{
        WHITE(97,107),
        GRAY(37,47),
        DARK_GRAY(90,100),
        BLACK(30,40),
        CLEAR(0,0);

        public final int fg;
        public final int bg;
        CLColor(int fg, int bg){
            this.fg=fg;
            this.bg=bg;
        }
        static CLColor getComplimentary(CLColor color){
            return switch (color){
                case WHITE -> DARK_GRAY;
                case GRAY -> BLACK;
                case DARK_GRAY -> WHITE;
                case BLACK -> GRAY;
                case CLEAR -> CLEAR;
            };
        }
    }
    private static void setColor(CLColor fg, CLColor bg){
        if(!canShowColor) return;
        currColor=new int[]{fg.fg,bg.bg};
        System.out.print("\u001b["+currColor[0]+";"+currColor[1]+"m");
    }
    private static void setComplimentaryColor(CLColor color){
        setColor(color,CLColor.getComplimentary(color));
    }
    private static int[] currColor = {0,0};
    public static boolean canShowColor=false;
    public static void renderGame(ChessGame game, RenderData data){
        for(int y=0;y<=8;y++){
            setColor(CLColor.CLEAR, CLColor.CLEAR);
            if(!canShowColor) System.out.println("   "+"+-------".repeat(8)+"+");
            if(y==8){
                System.out.print("   ");
                for(int x=0;x<8;x++){
                    System.out.print((canShowColor?"   ":"    ")+(char)(x+97)+"   ");
                }
                System.out.print("\n\n   ");
                setComplimentaryColor(game.getTeamTurn().whiteOrBlack(CLColor.WHITE,CLColor.BLACK));
                if(game.isInCheckmate(game.getTeamTurn())){
                    System.out.print("Checkmate! ")
                }else {
                    System.out.print(game.getTeamTurn() + "'s move");
                }
                setColor(CLColor.CLEAR, CLColor.CLEAR);
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
                                    CLColor.CLEAR:
                                    currPiece.getTeamColor().whiteOrBlack(CLColor.WHITE,CLColor.GRAY),
                            x%2==y%2?CLColor.DARK_GRAY:CLColor.BLACK);
                    String strToPrint;
                    if(currPiece!=null) {
                        strToPrint = " " +
                                (currPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? whitePieces : blackPieces)
                                        .get(currPiece.getPieceType()).substring(i * 6, i * 6 + 5) + " ";
                    } else {
                        strToPrint="       ";
                    }
                    if(isHighlighted||isOrigin){
                        var colors=isOrigin?new int[]{93,43}:new int[]{92,42};
                        var overlayLine = highlight.substring(i*8,i*8+7)+" ";
                        var chars = strToPrint.toCharArray();
                        strToPrint="";
                        for(int j=0;j<chars.length;j++){
                            var overlayChar = overlayLine.charAt(j);
                            if(canShowColor) {
                                if (overlayChar == '=' || overlayChar == '|') {
                                    strToPrint+="\u001b["+colors[0]+";"+currColor[1]+"m";
                                } else if (overlayChar == '#') {
                                    strToPrint+="\u001b["+colors[0]+";"+colors[1]+"m";
                                } else {// space
                                    strToPrint+="\u001b["+currColor[0]+";"+currColor[1]+"m";
                                }
                            }
                            strToPrint+=overlayChar==' '?chars[j]:overlayChar;
                        }
                    }
                    System.out.print(strToPrint);
                    setColor(CLColor.CLEAR, CLColor.CLEAR);

                    if(x==7)
                        System.out.println(canShowColor?"":"|");
                }
            }
        }
    }
}
