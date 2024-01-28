package rendering.renderables;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import rendering.Color;
import rendering.Pixel;
import rendering.Renderable;
import rendering.Sprite;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Everything needed to render a chess game.
 * Use renderGame(ChessGame game, RenderData renderData) to draw a game, and
 * RenderData.from to get the renderData
 */
public class ChessRenderer implements Renderable {

    /**
     * Optional data to render a chess game
     */
    public static class RenderData{
        private final Collection<ChessPosition> highlightedPositions;
        private final ChessPosition highlightedOrigin;
        private final ChessMove lastMove;
        private final boolean isBig;
        private final boolean facingWhite;
        private RenderData(Collection<ChessPosition> highlightedPositions,
                           ChessPosition highlightedOrigin, ChessMove lastMove,
                           boolean isBig, boolean facingWhite){
            this.highlightedPositions=highlightedPositions;
            this.highlightedOrigin = highlightedOrigin;
            this.lastMove = lastMove;
            this.isBig=isBig;
            this.facingWhite=facingWhite;
        }

        /**
         * Builder class to generate RenderData
         */
        public static class Builder{
            private Collection<ChessPosition> highlightedPositions = Collections.emptySet();
            private ChessPosition highlightedOrigin = new ChessPosition(0,0);
            private ChessMove lastMove;
            private boolean isBig=true;
            private boolean facingWhite=true;

            /**
             * Sets the positions to be highlighted
             * @param movesToShow moves this piece can make
             * @param positionToShow the piece making the moves
             * @return this, for chaining
             */
            public Builder setPositions(Collection<ChessMove> movesToShow, ChessPosition positionToShow){
                this.highlightedPositions = movesToShow.stream().map(ChessMove::getEndPosition).toList();
                this.highlightedOrigin =positionToShow;
                return this;
            }

            public Builder setLastMove(ChessMove move){
                this.lastMove=move;
                return this;
            }

            /**
             * Sets if the board should be rendered with white on bottom (true) or black on botton (false)
             * @param isFacing if white is on bottom
             * @return this, for chaining
             */
            public Builder facingWhite(boolean isFacing){ this.facingWhite=isFacing; return this;}

            /**
             * Sets if the board should be rendered big (height of 3) or small (height of 2)
             * @param isBig if big
             * @return this, for chaining
             */
            public Builder isBig(boolean isBig){ this.isBig=isBig; return this;}

            /**
             * @return a RenderData
             */
            public RenderData build(){
                return new RenderData(this.highlightedPositions, this.highlightedOrigin, this.lastMove,
                        this.isBig, this.facingWhite);
            }
        }
    }
    private enum HighlightType{
        MOVE(new Color(1,5,1), new Color(0,3,0)),
        ORIGIN(new Color(5,5,1), new Color(3,3,0)),
        TAKE(new Color(5,1,1), new Color(3,0,0)),
        PAST_MOVE(new Color(2,2, 5), new Color(1,1, 3)),
        MOVE_DEST(new Color(1,4,4), new Color(0,3,3));

        public final Color bright;
        public final Color dark;
        HighlightType(Color bright, Color dark){
            this.bright=bright;
            this.dark=dark;
        }
    }

    private enum ChessColor{
        BLACK(new Color(0)),
        WHITE(new Color(23)),
        BOARD_BLACK(new Color(10)),
        BOARD_WHITE(new Color(15));

        public final Color color;
        ChessColor(Color color){
            this.color=color;
        }
    }
    private static final Color markerColor = new Color(0);
    private static final Map<ChessPiece.PieceType, Sprite> bigBlackPieces = Map.ofEntries(
            Map.entry(ChessPiece.PieceType.KING,
                    Sprite.Builder.fromStr("""
                           \s + \s
                           ( | )
                           \\_I_/""").withFGColor(ChessColor.BLACK.color).build()),
            Map.entry(ChessPiece.PieceType.QUEEN,
                    Sprite.Builder.fromStr("""
                            .'I'.
                            \\ | /
                            -----""").withFGColor(ChessColor.BLACK.color).build()),
            Map.entry(ChessPiece.PieceType.BISHOP,
                    Sprite.Builder.fromStr("""
                            \s o \s
                            \s(+)\s
                            \s/ \\\s""").withFGColor(ChessColor.BLACK.color).build()),
            Map.entry(ChessPiece.PieceType.KNIGHT,
                    Sprite.Builder.fromStr("""
                            \s/o^\s
                            \sL/ |
                            \s/__|""").withFGColor(ChessColor.BLACK.color).build()),
            Map.entry(ChessPiece.PieceType.ROOK,
                    Sprite.Builder.fromStr("""
                            \s...\s
                            \s| |\s
                            \s---\s""").withFGColor(ChessColor.BLACK.color).build()),
            Map.entry(ChessPiece.PieceType.PAWN,
                    Sprite.Builder.fromStr("""
                            \s   \s
                            \s o \s
                            \s A \s""").withFGColor(ChessColor.BLACK.color).build())
    );
    private static final Map<ChessPiece.PieceType, Sprite> bigWhitePieces = Map.ofEntries(
            Map.entry(ChessPiece.PieceType.KING,
                    Sprite.Builder.fromStr("""
                           \s + \s
                           (X|X)
                           \\#I#/""").withFGColor(ChessColor.WHITE.color).build()),
            Map.entry(ChessPiece.PieceType.QUEEN,
                    Sprite.Builder.fromStr("""
                            .iIi.
                            \\###/
                            =====""").withFGColor(ChessColor.WHITE.color).build()),
            Map.entry(ChessPiece.PieceType.BISHOP,
                    Sprite.Builder.fromStr("""
                            \s * \s
                            \s(#)\s
                            \s/=\\\s""").withFGColor(ChessColor.WHITE.color).build()),
            Map.entry(ChessPiece.PieceType.KNIGHT,
                    Sprite.Builder.fromStr("""
                            \s/*^\s
                            \sL/ |
                            \s/%%|""").withFGColor(ChessColor.WHITE.color).build()),
            Map.entry(ChessPiece.PieceType.ROOK,
                    Sprite.Builder.fromStr("""
                            \s---\s
                            \s|#|\s
                            \s===\s""").withFGColor(ChessColor.WHITE.color).build()),
            Map.entry(ChessPiece.PieceType.PAWN,
                    Sprite.Builder.fromStr("""
                            \s   \s
                            \s * \s
                            \s A \s""").withFGColor(ChessColor.WHITE.color).build())
    );
    private static final Sprite bigHighlight;
    static {
        var builder = Sprite.Builder.fromStr("""
                +=   =+
                |     |
                +=   =+""").withFGColor(markerColor);

        for(var pixel : builder.getPixels())
            if(pixel.character==0) pixel.clearColor();
        builder.pixels[0][0].bg = markerColor;
        builder.pixels[2][0].bg = markerColor;
        builder.pixels[0][6].bg = markerColor;
        builder.pixels[2][6].bg = markerColor;

        bigHighlight = builder.build();
    }
    private static final Map<ChessPiece.PieceType, Sprite> smallBlackPieces = Map.ofEntries(
            Map.entry(ChessPiece.PieceType.KING,
                    Sprite.Builder.fromStr("""
                           \s+\s
                           (|)""").withFGColor(ChessColor.BLACK.color).build()),
            Map.entry(ChessPiece.PieceType.QUEEN,
                    Sprite.Builder.fromStr("""
                            .!.
                            \\|/""").withFGColor(ChessColor.BLACK.color).build()),
            Map.entry(ChessPiece.PieceType.BISHOP,
                    Sprite.Builder.fromStr("""
                            \so\s
                            /+\\""").withFGColor(ChessColor.BLACK.color).build()),
            Map.entry(ChessPiece.PieceType.KNIGHT,
                    Sprite.Builder.fromStr("""
                            /o^
                            "/|""").withFGColor(ChessColor.BLACK.color).build()),
            Map.entry(ChessPiece.PieceType.ROOK,
                    Sprite.Builder.fromStr("""
                            -.-
                            |_|""").withFGColor(ChessColor.BLACK.color).build()),
            Map.entry(ChessPiece.PieceType.PAWN,
                    Sprite.Builder.fromStr("""
                            \so\s
                            \sA\s""").withFGColor(ChessColor.BLACK.color).build())
    );
    private static final Map<ChessPiece.PieceType, Sprite> smallWhitePieces = Map.ofEntries(
            Map.entry(ChessPiece.PieceType.KING,
                    Sprite.Builder.fromStr("""
                           \s*\s
                           (#)""").withFGColor(ChessColor.WHITE.color).build()),
            Map.entry(ChessPiece.PieceType.QUEEN,
                    Sprite.Builder.fromStr("""
                            .I.
                            \\X/""").withFGColor(ChessColor.WHITE.color).build()),
            Map.entry(ChessPiece.PieceType.BISHOP,
                    Sprite.Builder.fromStr("""
                            \s*\s
                            /#\\""").withFGColor(ChessColor.WHITE.color).build()),
            Map.entry(ChessPiece.PieceType.KNIGHT,
                    Sprite.Builder.fromStr("""
                            /*^
                            "/|""").withFGColor(ChessColor.WHITE.color).build()),
            Map.entry(ChessPiece.PieceType.ROOK,
                    Sprite.Builder.fromStr("""
                            =-=
                            |#|""").withFGColor(ChessColor.WHITE.color).build()),
            Map.entry(ChessPiece.PieceType.PAWN,
                    Sprite.Builder.fromStr("""
                            \s*\s
                            \sA\s""").withFGColor(ChessColor.WHITE.color).build())
    );
    private static final Sprite smallHighlight;
    static {
        var builder = Sprite.Builder.fromStr("""
                /   \\
                \\   /""").withFGColor(markerColor);

        for(var pixel : builder.getPixels())
            if(pixel.character==0) pixel.clearColor();
        builder.pixels[0][4].bg = markerColor;
        builder.pixels[1][0].bg = markerColor;

        smallHighlight = builder.build();
    }

    private static Sprite highlightWithColor(Sprite sprite, HighlightType type){
        var builder = Sprite.Builder.fromSprite(sprite);
        for(var pixel : builder.getPixels()){
            if(pixel.fg==markerColor) pixel.fg=type.bright;
            if(pixel.bg==markerColor) pixel.bg=type.dark;
        }
        return builder.build();
    }

    //--

    private final ChessGame game;
    private final RenderData data;
    public ChessRenderer(ChessGame game, RenderData data){
        this.game=game;
        this.data=data;
    }

    private int getRow(int row){ return this.data.facingWhite?8-row:row-1; }
    private int getCol(int col){ return this.data.facingWhite?col-1:8-col; }
    @Override
    public void render(Pixel[][] screen) {
        int spaceWidth = this.data.isBig?7:5;
        int spaceHeight = this.data.isBig?3:2;
        int startingX = (screen[0].length-(spaceWidth*8+2))/2;
        int startingY = (screen.length-(spaceHeight*8+2))/2;

        for(int y=0;y<8;y++){
            for(int x=0;x<8;x++) {
                var pieceAt = this.game.getBoard().getPiece(new ChessPosition(8-y,x+1));
                Sprite sprite=pieceAt!=null?
                        pieceAt.getTeamColor().whiteOrBlack(
                                this.data.isBig?bigWhitePieces:smallWhitePieces,
                                this.data.isBig?bigBlackPieces:smallBlackPieces).get(pieceAt.getPieceType()):
                        Sprite.NULL;

                int transfX=this.data.facingWhite?x:7-x;
                int transfY=this.data.facingWhite?y:7-y;
                sprite.draw(4+startingX+transfX*spaceWidth, startingY+transfY*spaceHeight, screen);

                for(int y2=0;y2<spaceHeight;y2++){
                    for(int x2=0;x2<spaceWidth;x2++){
                        Renderable.overlayPixel(startingX+3+transfX*spaceWidth+x2, startingY+transfY*spaceHeight+y2,
                                new Pixel((char)0, null, (((transfX%2==transfY%2)==this.data.facingWhite)?
                                        ChessColor.BOARD_WHITE:ChessColor.BOARD_BLACK).color), screen);

                    }
                }
            }
        }

        var highlight = this.data.isBig?bigHighlight:smallHighlight;
        if(this.data.lastMove!=null){
            highlightWithColor(highlight, HighlightType.MOVE_DEST).draw(
                    3+startingX+(this.getCol(this.data.lastMove.getEndPosition().getColumn()))*spaceWidth,
                    startingY+(this.getRow(this.data.lastMove.getEndPosition().getRow()))*spaceHeight,
                    screen);
            highlightWithColor(highlight, HighlightType.PAST_MOVE).draw(
                    3+startingX+(this.getCol(this.data.lastMove.getStartPosition().getColumn()))*spaceWidth,
                    startingY+(this.getRow(this.data.lastMove.getStartPosition().getRow()))*spaceHeight,
                    screen);
        }
        for(var pos : this.data.highlightedPositions)
            highlightWithColor(highlight, this.game.getBoard().getPiece(pos)==null?
                    HighlightType.MOVE:HighlightType.TAKE).draw(
                    3+startingX+(this.getCol(pos.getColumn()))*spaceWidth,
                    startingY+(this.getRow(pos.getRow()))*spaceHeight,
                    screen);
        if(this.data.highlightedOrigin.isValid())
            highlightWithColor(highlight, HighlightType.ORIGIN).draw(
                    3+startingX+(this.getCol(this.data.highlightedOrigin.getColumn()))*spaceWidth,
                    startingY+(this.getRow(this.data.highlightedOrigin.getRow()))*spaceHeight,
                    screen);

        for(int i=0;i<8;i++){
            Renderable.overlayPixel(startingX+1, startingY+1+i*spaceHeight,
                    new Pixel(Character.forDigit((this.data.facingWhite?8-i:i+1),10), null), screen);
            Renderable.overlayPixel(startingX+3+spaceWidth/2+i*spaceWidth, startingY+spaceHeight*8,
                    new Pixel((char)(this.data.facingWhite?('a'+i):('h'-i)), null), screen);
        }

        String toPrint;
        if(game.isInCheckmate(game.getTeamTurn())){
            toPrint="Checkmate! "+game.getTeamTurn().opposite()+" wins";
        }else if(game.isInStalemate(game.getTeamTurn())){
            toPrint="Stalemate";
        }else{
            toPrint=game.getTeamTurn() + "'s move";
            if(game.isInCheck(game.getTeamTurn()))
                toPrint+=" (Check)";
        }

        Sprite.Builder.fromStr(toPrint)
                .withFGColor(game.getTeamTurn().whiteOrBlack(ChessColor.WHITE, ChessColor.BLACK).color)
                .withBGColor(game.getTeamTurn().whiteOrBlack(ChessColor.BOARD_WHITE, ChessColor.BOARD_BLACK).color)
                .build().draw(startingX+2, startingY+spaceHeight*8+2, screen);
    }

    @Override
    public int getOrder() { return 0; }
}
