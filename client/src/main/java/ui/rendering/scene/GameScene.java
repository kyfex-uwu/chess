package ui.rendering.scene;

import chess.*;
import chess.specialmoves.EnPassantMove;
import model.GameData;
import model.UserData;
import ui.*;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.ChessRenderer;
import ui.rendering.renderable.PFPMaker;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.SuccessMessage;
import webSocketMessages.userCommands.MakeMoveCommand;

import java.util.*;

import static ui.rendering.renderable.ChessRenderer.*;

public class GameScene extends Scene{
    private UserData whiteUser;
    private UserData blackUser;
    private final Sprite whitePFP;
    private final Sprite blackPFP;
    private boolean facingWhite;
    private final boolean isOnline;
    public GameScene(GameData data){
        this(data, new UserData("Player","",""),
                new UserData("Player","",""), true);
    }
    private void makeMove(ChessMove move){
        try{
            if(GameScene.this.isOnline){
                var canMove = WebsocketManager.sendMessageWithResponse(
                        new MakeMoveCommand(PlayData.currAuth.authToken(), this.data.gameID, move));

                if(!(canMove instanceof SuccessMessage successMessage) || !successMessage.success){
                    if(canMove instanceof ErrorMessage errorMessage){
                        GameScene.this.dialogMessage=errorMessage.errorMessage;
                        return;
                    }
                    GameScene.this.dialogMessage="Something went wrong";
                    return;
                }
            }

            this.builder.setPositions(null, null);
            if(move instanceof EnPassantMove ||
                    this.data.game.getBoard().getPiece(move.getEndPosition())!=null)
                this.builder.setCapture(true);

            this.data.game.makeMove(move);
            this.builder.setLastMove(move);
            this.pawnPromotionMove=null;
        }catch(InvalidMoveException e){
            GameScene.this.dialogMessage="Illegal move";
        }
    }
    public GameScene(GameData data, UserData player1, UserData player2, boolean isOnline){
        super();
        this.data=data;
        this.isOnline=isOnline;

        this.consumer = new ArgConsumer(Map.of(
                "move", args -> {
                    if(args.length<2){
                        GameScene.this.dialogMessage="Missing start/end position";
                        return;
                    }
                    ChessPosition start=null;
                    ChessPosition end=null;
                    try{ start = Serialization.deserializeChessPosition(args[0]); }
                    catch(Exception e){ this.dialogMessage="Invalid start position"; }
                    try{ end = Serialization.deserializeChessPosition(args[1]); }
                    catch(Exception e){ this.dialogMessage="Invalid end position"; }

                    if(this.isOnline&&!this.data.game.getTeamTurn().whiteOrBlack(whiteUser,blackUser).username()
                            .equals(PlayData.selfData.username())){
                        GameScene.this.dialogMessage="Not your turn";
                        return;
                    }
                    Collection<ChessMove> moves=this.data.game.validMoves(start);
                    if(moves==null){
                        GameScene.this.dialogMessage="Illegal move";
                        return;
                    }
                    ChessPosition finalEnd = end;
                    var possibleMoves = moves.stream().filter(maybeMove ->
                            maybeMove.getEndPosition().equals(finalEnd)).toList();

                    if(possibleMoves.size()==0){
                        GameScene.this.dialogMessage="Illegal move";
                    }else if(possibleMoves.size()==1) {
                        this.makeMove(possibleMoves.get(0));
                    }else{
                        this.pawnPromotionMove=possibleMoves.get(0);
                    }
                },
                "show", args -> {
                    try {
                        var start= Serialization.deserializeChessPosition(args[0]);
                        this.builder.setPositions(this.data.game.validMoves(start), start);
                    }catch(Exception e){
                        this.builder.setPositions(null,null);
                        GameScene.this.dialogMessage="Invalid position";
                    }
                },
                "back", args -> this.changeScene(new PlayMenuScene())
        ),ArgConsumer.helpCommandMaker(
                "move [startpos] [endpos]", "Makes the specified move",
                "show [pos]", "Shows all legal moves a piece at that position can make",
                "back", "Returns to the setup scene"
        ));

        if(data.whiteUsername!=null){
            if(PlayData.selfData!=null&&data.whiteUsername.equals(PlayData.selfData.username())) {
                this.whiteUser = PlayData.selfData;
                this.facingWhite=true;
            }else{
                Online.request(Online.ReqMethod.GET, "user/"+data.whiteUsername,
                                (String)null, UserData.class)
                        .ifSuccess(userData -> {
                            this.whiteUser = userData;
                        }).ifError(error -> {
                            this.whiteUser = new UserData("Opponent", "", "");
                        });
            }
        }

        if(data.blackUsername!=null){
            if(PlayData.selfData!=null&&data.blackUsername.equals(PlayData.selfData.username())){
                this.blackUser=PlayData.selfData;
                this.facingWhite=false;
            }else{
                Online.request(Online.ReqMethod.GET, "user/"+data.blackUsername,
                                (String)null, UserData.class)
                        .ifSuccess(userData -> {
                            this.blackUser = userData;
                        }).ifError(error -> {
                            this.blackUser = new UserData("Opponent", "", "");
                        });
            }
        }

        if(isOnline){
            WebsocketManager.init();
        }

        if(this.whiteUser==null) this.whiteUser = player1;
        if(this.blackUser==null) this.blackUser = player2;

        this.whitePFP = PFPMaker.pfpToSprite(this.whiteUser.pfp());
        this.blackPFP = PFPMaker.pfpToSprite(this.blackUser.pfp());
    }
    public GameData data;

    private final ChessRenderer.RenderData.Builder builder = new ChessRenderer.RenderData.Builder();
    private ChessPiece.Offset boardPos;
    @Override
    public void init() {
        super.init();
        this.builder.isBig(Config.displayBig());
        this.toRender.add(new Background());
        this.toRender.add(new Renderable(8) {
            @Override
            public void render(Pixel[][] screen) {
                var tempScreen = new Pixel[Config.screenHeight()][Config.screenWidth()];
                if(GameScene.this.data.game.history.size()>0){
                    var move = GameScene.this.data.game.history.get(GameScene.this.data.game.history.size()-1);
                    GameScene.this.builder.setLastMove(move.move);
                    GameScene.this.builder.setCapture(move.takenPiece!=null);
                }
                ChessRenderer.render(tempScreen, GameScene.this.data.game, GameScene.this.builder.build());

                int lastX=0;
                int lastY=0;
                for(int y=0;y<tempScreen.length;y++) {
                    for (int x = 0; x<tempScreen[y].length;x++){
                        if(tempScreen[y][x]!=null){
                            lastX=Math.max(lastX,x);
                            lastY=Math.max(lastY,y);
                        }
                    }
                }
                int startingX=(screen[0].length-lastX)/2;
                int startingY=(screen.length-lastY+1)/2;
                GameScene.this.boardPos = new ChessPiece.Offset(startingX+lastX+1, startingY);

                Sprite.Builder.fromStr(GameScene.this.data.gameName).withFGColor(Config.Palette.BOARD_TEXT)
                        .build().draw(startingX, startingY-2, screen);
                if(GameScene.this.data.gameID!=-1)
                    Sprite.Builder.fromStr(String.valueOf(GameScene.this.data.gameID)).withFGColor(Config.Palette.BOARD_GRAY)
                            .build().draw(startingX, startingY-1, screen);
                for(int y=0;y<=lastY;y++) {
                    for (int x = 0; x<=lastX;x++){
                        Renderable.overlayPixel(startingX+x, startingY+y, tempScreen[y][x], screen);
                    }
                }

                String toPrint;
                if(GameScene.this.data.game.isInCheckmate(GameScene.this.data.game.getTeamTurn())){
                    toPrint=" Checkmate! "+GameScene.this.data.game.getTeamTurn().opposite()+" wins ";
                }else if(GameScene.this.data.game.isInStalemate(GameScene.this.data.game.getTeamTurn())){
                    toPrint=" Stalemate ";
                }else{
                    toPrint=" "+GameScene.this.data.game.getTeamTurn().name + "'s move ";
                    if(GameScene.this.data.game.isInCheck(GameScene.this.data.game.getTeamTurn()))
                        toPrint+="(Check) ";
                }

                Sprite.Builder.fromStr(toPrint, false)
                        .withFGColor(GameScene.this.data.game.getTeamTurn().whiteOrBlack(Config.Palette.PIECE_WHITE, Config.Palette.PIECE_BLACK))
                        .withBGColor(GameScene.this.data.game.getTeamTurn().whiteOrBlack(Config.Palette.BOARD_WHITE, Config.Palette.BOARD_BLACK))
                        .build().draw(startingX+lastX+2, screen.length-2, screen);
            }
        });
        this.toRender.add(new Renderable(6) {
            @Override
            public void render(Pixel[][] screen) {
                var takenPieces = GameScene.this.data.game.history.stream()
                        .map(move->move.takenPiece).filter(Objects::nonNull).toList();
                var whiteMap = new HashMap<ChessPiece.PieceType, Integer>();
                var blackMap = new HashMap<ChessPiece.PieceType, Integer>();
                for(var piece : takenPieces){
                    var map=piece.getTeamColor().whiteOrBlack(whiteMap, blackMap);
                    if(map.containsKey(piece.getPieceType())){
                        map.put(piece.getPieceType(), map.get(piece.getPieceType())+1);
                    }else{
                        map.put(piece.getPieceType(),1);
                    }
                }

                for(int i=0;i<2;i++) {
                    var map = (i==0)==GameScene.this.facingWhite?whiteMap:blackMap;
                    var color = (i==0)==GameScene.this.facingWhite?ChessGame.TeamColor.WHITE:ChessGame.TeamColor.BLACK;

                    int xOffs = 0;
                    int yOffs = 0;
                    for (var piece : ChessPiece.PieceType.values()) {
                        if (map.containsKey(piece)) {
                            var isWhite = xOffs % 2 == yOffs % 2;
                            int altYAmt = i==0?yOffs*3+5:yOffs*-3+screen.length-8;

                            for (int y = 0; y < 3; y++) {
                                for (int x = 0; x < 8; x++) {
                                    Renderable.overlayPixel(x + xOffs * 8 + 4, y + altYAmt,
                                            new Pixel(' ', null,
                                                    isWhite ? Config.Palette.BOARD_WHITE : Config.Palette.BOARD_BLACK),
                                            screen);
                                }
                            }
                            color.whiteOrBlack(bigWhitePieces, bigBlackPieces)
                                    .get(piece).draw(xOffs * 8 + 5, altYAmt, screen);
                            Sprite.Builder.fromStr("x" + map.get(piece)).withFGColor(
                                            isWhite ? Config.Palette.BOARD_BLACK : Config.Palette.BOARD_WHITE)
                                    .build().draw(xOffs * 8 + 5 + 5, 2+altYAmt, screen);

                            xOffs++;
                            if (xOffs >= 3) {
                                xOffs = 0;
                                yOffs++;
                            }
                        }
                    }
                }
            }
        });
        this.toRender.add(new Renderable(7) {
            @Override
            public void render(Pixel[][] screen) {
                Sprite topSprite;
                Sprite bottomSprite;
                String topName;
                String bottomName;
                if(GameScene.this.facingWhite){
                    topSprite = GameScene.this.blackPFP;
                    bottomSprite = GameScene.this.whitePFP;
                    topName = GameScene.this.blackUser.username();
                    bottomName = GameScene.this.whiteUser.username();
                }else{
                    topSprite = GameScene.this.whitePFP;
                    bottomSprite = GameScene.this.blackPFP;
                    topName = GameScene.this.whiteUser.username();
                    bottomName = GameScene.this.blackUser.username();
                }

                topSprite.draw(2,1,screen);
                Sprite.Builder.fromStr(topName).withFGColor(Config.Palette.BOARD_TEXT)
                        .build().draw(9, 2, screen);
                bottomSprite.draw(2, screen.length-4,screen);
                Sprite.Builder.fromStr(bottomName).withFGColor(Config.Palette.BOARD_TEXT)
                        .build().draw(9, screen.length-3, screen);
            }
        });
        this.toRender.add(new Renderable(9) {
            @Override
            public void render(Pixel[][] screen) {
                if(GameScene.this.pawnPromotionMove==null) return;

                Background.darken(screen);
                highlightWithColor((Config.displayBig()?bigHighlight:smallHighlight), HighlightType.MOVE_DEST).draw(
                        GameScene.this.boardPos.x()-
                                (9-GameScene.this.pawnPromotionMove.getEndPosition().getColumn())*(Config.displayBig()?7:5),
                        GameScene.this.boardPos.y(), screen);

                var pieces = new ChessPiece.PieceType[]{
                        ChessPiece.PieceType.ROOK,
                        ChessPiece.PieceType.BISHOP,
                        ChessPiece.PieceType.KNIGHT,
                        ChessPiece.PieceType.QUEEN
                };
                for(int i=0;i<4;i++) {
                    for (int y = 0; y < (Config.displayBig() ? 3 : 2); y++)
                        for (int x = 0; x < (Config.displayBig() ? 7 : 5); x++)
                            Renderable.overlayPixel(GameScene.this.boardPos.x() + x + 3,
                                    GameScene.this.boardPos.y() + y + i * (Config.displayBig() ? 3 : 2),
                                    new Pixel(' ', null, i % 2 == (GameScene.this.facingWhite ? 1 : 0) ?
                                            Config.Palette.BOARD_BLACK : Config.Palette.BOARD_WHITE),
                                    screen);

                    (GameScene.this.facingWhite?bigWhitePieces:bigBlackPieces).get(pieces[i])
                            .draw(GameScene.this.boardPos.x() + 4,
                                    GameScene.this.boardPos.y()+ i * (Config.displayBig() ? 3 : 2),
                                    screen);
                }

                for(int i=0;i<(Config.displayBig()?3:2)*4;i++) {
                    Renderable.overlayPixel(GameScene.this.boardPos.x() + 2,
                            GameScene.this.boardPos.y() + i,
                            new Pixel(' ', null, Config.Palette.BOARD_GRAY),
                            screen);
                    Renderable.overlayPixel(GameScene.this.boardPos.x() + 2 + (Config.displayBig()?8:6),
                            GameScene.this.boardPos.y() + i,
                            new Pixel(' ', null, Config.Palette.BOARD_GRAY),
                            screen);
                }
                for(int i=0;i<(Config.displayBig()?9:7);i++){
                    Renderable.overlayPixel(GameScene.this.boardPos.x()+2+i,
                            GameScene.this.boardPos.y()-1,
                            new Pixel('▄',Config.Palette.BOARD_GRAY, null),
                            screen);
                    Renderable.overlayPixel(GameScene.this.boardPos.x()+2+i,
                            GameScene.this.boardPos.y()+(Config.displayBig()?3:2)*4,
                            new Pixel('▀',Config.Palette.BOARD_GRAY, null),
                            screen);
                }
            }
        });
        this.toRender.add(new Renderable(5) {
            @Override
            public void render(Pixel[][] screen) {
                for(int i=0;i<Math.min(GameScene.this.data.game.history.size(),
                        screen.length-9+(PlayData.loggedIn()?0:2));i++){
                    var revMove = GameScene.this.data.game.history.get(GameScene.this.data.game.history.size()-i-1);

                    Sprite.Builder.fromStr(" ".repeat(8-revMove.toAlgNotation().length())+
                                    revMove.toAlgNotation()+"   ")
                            .withBGColor(revMove.piece.getTeamColor().whiteOrBlack(
                                    Config.Palette.BOARD_BLACK,Config.Palette.BOARD_WHITE))
                            .withBGColor(revMove.piece.getTeamColor().whiteOrBlack(
                                    Config.Palette.BOARD_WHITE,Config.Palette.BOARD_BLACK))
                            .build().draw(screen[0].length-11-3, 5+i-(PlayData.loggedIn()?0:2), screen);
                }
            }
        });
    }

    @Override
    public void uninit() {

    }

    private final ArgConsumer consumer;
    private ChessMove pawnPromotionMove;
    private final ArgConsumer pawnPromotionConsumer = new ArgConsumer(Map.of(
            "back", args -> GameScene.this.pawnPromotionMove=null,
            "rook", args ->
                    GameScene.this.makeMove(pawnPromotionMove.withPromotionPiece(ChessPiece.PieceType.ROOK)),
            "bishop", args ->
                    GameScene.this.makeMove(pawnPromotionMove.withPromotionPiece(ChessPiece.PieceType.BISHOP)),
            "knight", args ->
                    GameScene.this.makeMove(pawnPromotionMove.withPromotionPiece(ChessPiece.PieceType.KNIGHT)),
            "queen", args ->
                    GameScene.this.makeMove(pawnPromotionMove.withPromotionPiece(ChessPiece.PieceType.QUEEN))
    ),ArgConsumer.helpCommandMaker(
            "back", "Returns to the game without making the move",
            "rook", "Promotes to a rook",
            "bishop", "Promotes to a rook",
            "knight", "Promotes to a knight",
            "queen", "Promotes to a queen"
    ));
    @Override
    public void onLine(String[] args) {
        var consumer = this.pawnPromotionMove==null?this.consumer:this.pawnPromotionConsumer;
        consumer.tryConsumeArgs(args);
        if(consumer.shouldShowHelp) this.dialogMessage = consumer.helpCommand;

        if(!this.isOnline) this.facingWhite = this.data.game.getTeamTurn()==ChessGame.TeamColor.WHITE;
        this.builder.facingWhite(this.facingWhite);

        super.onLine(args);
    }
}
