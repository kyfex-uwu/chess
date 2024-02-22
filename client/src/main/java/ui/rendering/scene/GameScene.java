package ui.rendering.scene;

import chess.*;
import model.GameData;
import model.UserData;
import ui.*;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.ChessRenderer;
import ui.rendering.renderable.PFPMaker;
import webSocketMessages.serverMessages.SuccessMessage;
import webSocketMessages.userCommands.MakeMoveCommand;

import java.text.ParseException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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
    public GameScene(GameData data, UserData player1, UserData player2, boolean isOnline){
        super();
        this.data=data;
        this.isOnline=isOnline;

        this.consumer = new ArgConsumer(Map.of(
                "move", args -> {
                    ChessPosition start=null;
                    ChessPosition end=null;
                    try{ start = Serialization.deserializeChessPosition(args[0]); }
                    catch(ParseException e){ this.dialogMessage="Invalid start position"; }
                    try{ end = Serialization.deserializeChessPosition(args[1]); }
                    catch(ParseException e){ this.dialogMessage="Invalid end position"; }

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
                    var move = moves.stream().filter(maybeMove ->
                            maybeMove.getEndPosition().equals(finalEnd)).findFirst();

                    try{
                        if(GameScene.this.isOnline&&move.isPresent()){
                            var canMove = WebsocketManager.sendMessageWithResponse(
                                    new MakeMoveCommand(PlayData.currAuth.authToken(), this.data.gameID, move.get()));

                            if(!(canMove instanceof SuccessMessage successMessage) || !successMessage.success)
                                move = Optional.empty();
                        }

                        if (move.isPresent()) {
                            this.builder.setPositions(null, null);
                            this.data.game.makeMove(move.get());
                            this.builder.setLastMove(move.get());
                        }else{
                            GameScene.this.dialogMessage="Illegal move";
                        }
                    }catch(InvalidMoveException e){
                        GameScene.this.dialogMessage="Illegal move";
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

    private ChessRenderer.RenderData.Builder builder = new ChessRenderer.RenderData.Builder();
    @Override
    public void init() {
        super.init();
        this.builder.isBig(Config.displayBig());
        this.toRender.add(new Background());
        this.toRender.add(new Renderable(9) {
            @Override
            public void render(Pixel[][] screen) {
                var tempScreen = new Pixel[Config.screenHeight()][Config.screenWidth()];
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
                Sprite.Builder.fromStr(GameScene.this.data.gameName).withFGColor(Config.Palette.BOARD_TEXT)
                        .build().draw(startingX, startingY-2, screen);
                if(GameScene.this.data.gameID!=-1)
                    Sprite.Builder.fromStr(GameScene.this.data.gameID+"").withFGColor(Config.Palette.BOARD_GRAY)
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
                    toPrint=" "+GameScene.this.data.game.getTeamTurn() + "'s move ";
                    if(GameScene.this.data.game.isInCheck(GameScene.this.data.game.getTeamTurn()))
                        toPrint+="(Check) ";
                }

                Sprite.Builder.fromStr(toPrint, false)
                        .withFGColor(GameScene.this.data.game.getTeamTurn().whiteOrBlack(Config.Palette.PIECE_WHITE, Config.Palette.PIECE_BLACK))
                        .withBGColor(GameScene.this.data.game.getTeamTurn().whiteOrBlack(Config.Palette.BOARD_WHITE, Config.Palette.BOARD_BLACK))
                        .build().draw(startingX+lastX+2, screen.length-2, screen);
            }
        });
        this.toRender.add(new Renderable(8) {
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
    }

    @Override
    public void uninit() {

    }

    private final ArgConsumer consumer;
    @Override
    public void onLine(String[] args) {
        if(this.data.game.history.size()>0){
            //this.builder.setLastMove(this.data.game.history.get(this.data.game.history.size()-1));
        }

        this.consumer.tryConsumeArgs(args);
        if(this.consumer.shouldShowHelp) this.dialogMessage = this.consumer.helpCommand;

        if(!this.isOnline) this.facingWhite = this.data.game.getTeamTurn()==ChessGame.TeamColor.WHITE;
        this.builder.facingWhite(this.facingWhite);

        super.onLine(args);
    }
}
