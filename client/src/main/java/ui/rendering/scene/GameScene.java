package ui.rendering.scene;

import chess.*;
import ui.ArgConsumer;
import ui.Config;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.renderable.ChessRenderer;
import ui.rendering.renderable.Background;

import java.util.*;

public class GameScene extends Scene{
    private ChessGame game = new ChessGame();
    private Collection<ChessMove> movesToShow = List.of();
    private ChessPosition positionToShow = null;

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
                ChessRenderer.render(tempScreen, GameScene.this.game, GameScene.this.builder.build());

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
                int startingY=(screen.length-lastY)/2;
                for(int y=0;y<=lastY;y++) {
                    for (int x = 0; x<=lastX;x++){
                        Renderable.overlayPixel(startingX+x, startingY+y, tempScreen[y][x], screen);
                    }
                }
            }
        });
    }

    @Override
    public void uninit() {

    }

    private final ArgConsumer consumer = new ArgConsumer(Map.of(
            "move", args -> {
                try {
                    var start = Json.deserializeChessPosition(args[0]);
                    var end = Json.deserializeChessPosition(args[1]);

                    GameScene.this.movesToShow = List.of();

                    Collection<ChessMove> moves=game.validMoves(start);
                    var move = moves.stream().filter(maybeMove ->
                            maybeMove.getEndPosition().equals(end)).findFirst();

                    try{
                        if (move.isPresent())
                            this.game.makeMove(move.get());
                        else{
                            GameScene.this.dialogMessage="Illegal move";
                        }
                    }catch(InvalidMoveException e){
                        GameScene.this.dialogMessage="Illegal move";
                    }

                    /*
                    Renderable.render(new ArrayList<>(List.of(
                            new ChessRenderer(this.game, this.builder.build()),
                            new Background()
                    )));

                    this.movesToShow = Collections.emptyList();
                    this.positionToShow=null;

                    if(args[0].equals("help")){
                        System.out.println("help: shows this command\n" +
                                "show [position]: shows all valid moves that piece can make\n" +
                                "move [position from] [position to]: moves a piece from position to position");
                    }else if(args[0].equals("show")){
                        try {
                            var start=new ChessPosition(args[1].charAt(1) - 48, args[1].charAt(0) - 96);
                            this.movesToShow = this.game.validMoves(start);
                            if(this.movesToShow==null) this.movesToShow = Collections.emptyList();
                            this.positionToShow = start;
                        }catch(Exception e){
                            this.movesToShow = Collections.emptyList();
                            this.positionToShow=null;
                            System.out.println("Invalid position");
                        }
                    }else if(args[0].equals("move")){
                        this.movesToShow = List.of();

                        ChessPosition startPos;
                        ChessPosition endPos;
                        try {
                            startPos = new ChessPosition(args[1].charAt(1) - 48, args[1].charAt(0) - 96);
                        }catch(Exception e){ System.out.println("Invalid start position"); return; }
                        try {
                            endPos = new ChessPosition(args[1].charAt(1) - 48, args[1].charAt(0) - 96);
                        }catch(Exception e){ System.out.println("Invalid end position"); return; }

                        Collection<ChessMove> moves=game.validMoves(startPos);
                        if(moves==null){ System.out.println("No piece at this position"); return; }
                        var move = moves.stream().filter(maybeMove ->
                                maybeMove.getEndPosition().equals(endPos)).findFirst();

                        try{
                            if (move.isPresent())
                                this.game.makeMove(move.get());
                            else
                                System.out.println("Illegal move");
                        }catch(InvalidMoveException e){
                            System.out.println("Illegal move");
                        }
                    }else{
                        System.out.println("command not recognized, type \"help\" to see all commands");
                    }
                     */
                }catch(Exception e){
                    e.printStackTrace();
                }
            },
            "back", args -> this.changeScene(new PlayMenuScene())
    ),ArgConsumer.helpCommandMaker(

    ));
    @Override
    public void onLine(String[] args) {
        this.builder.facingWhite(this.game.getTeamTurn()== ChessGame.TeamColor.WHITE);
        if(this.positionToShow!=null){
            this.builder.setPositions(this.movesToShow, this.positionToShow);
        }
        if(this.game.history.size()>0){
            this.builder.setLastMove(this.game.history.get(this.game.history.size()-1));
        }

        this.consumer.tryConsumeArgs(args);
        if(this.consumer.shouldShowHelp) this.dialogMessage = this.consumer.helpCommand;

        super.onLine(args);
    }
}
