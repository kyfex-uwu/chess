package ui.rendering.screen;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import ui.Config;
import ui.rendering.Renderable;
import ui.rendering.renderable.ChessRenderer;
import ui.rendering.renderable.Background;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GameScene extends Scene{
    private ChessGame game;
    private Collection<ChessMove> movesToShow = List.of();
    private ChessPosition positionToShow = null;
    @Override
    public void init() {

    }

    @Override
    public void uninit() {

    }

    private ChessRenderer.RenderData.Builder builder = new ChessRenderer.RenderData.Builder()
            .isBig(Config.displayBig());
    @Override
    public void onLine(String[] args) {
        this.builder.facingWhite(this.game.getTeamTurn()== ChessGame.TeamColor.WHITE);
        if(this.positionToShow!=null){
            this.builder.setPositions(this.movesToShow, this.positionToShow);
        }
        if(this.game.history.size()>0){
            this.builder.setLastMove(this.game.history.get(this.game.history.size()-1));
        }
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
    }
}
