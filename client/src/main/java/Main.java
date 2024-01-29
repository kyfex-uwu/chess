import chess.*;
import chess.specialmoves.DoublePawnMove;
import rendering.renderable.ChessRenderer;
import rendering.Renderable;
import rendering.renderable.Page;

import java.util.*;

public class Main {
    private static Collection<ChessMove> movesToShow = List.of();
    private static ChessPosition positionToShow = null;
    public static void main(String[] args) {
        System.out.println("\u001b[0m");
        System.out.println("\nIf \u001b[38;2;200;0;0m this \u001b[0m is not red, make sure your terminal can display colors\n" +
                "If you don't see a checker pattern here -> ▀▄▀▄, make sure your terminal can display unicode characters " +
                "(if you're on windows, try the command \"chcp 65001\", it temporarily enables unicode characters)\n" +
                "Press Enter to start!");

        var game = new ChessGame();

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        while (true) {
            var builder = new ChessRenderer.RenderData.Builder()
                    .isBig(true)
                    .facingWhite(game.getTeamTurn()== ChessGame.TeamColor.WHITE);
            if(positionToShow!=null){
                builder.setPositions(movesToShow, positionToShow);
            }
            if(game.history.size()>0){
                builder.setLastMove(game.history.get(game.history.size()-1));
            }
            Renderable.render(120, 30, new ArrayList<>(List.of(
                    new ChessRenderer(game, builder.build()),
                    new Page()
            )));

            movesToShow = Collections.emptyList();
            positionToShow=null;
            String line = scanner.nextLine();

            if(line.startsWith("help")){
                System.out.println("help: shows this command\n" +
                        "show [position]: shows all valid moves that piece can make\n" +
                        "move [position from] [position to]: moves a piece from position to position");
            }else if(line.startsWith("show")){
                try {
                    var start=new ChessPosition(line.charAt(6) - 48, line.charAt(5) - 96);
                    movesToShow = game.validMoves(start);
                    if(movesToShow==null) movesToShow = Collections.emptyList();
                    positionToShow = start;
                }catch(Exception e){
                    movesToShow = Collections.emptyList();
                    positionToShow=null;
                    System.out.println("Invalid position");
                }
            }else if(line.startsWith("move")){
                movesToShow = List.of();

                ChessPosition startPos;
                ChessPosition endPos;
                try {
                    startPos = new ChessPosition(line.charAt(6) - 48, line.charAt(5) - 96);
                }catch(Exception e){ System.out.println("Invalid start position"); continue; }
                try {
                    endPos = new ChessPosition(line.charAt(9) - 48, line.charAt(8) - 96);
                }catch(Exception e){ System.out.println("Invalid end position"); continue; }

                Collection<ChessMove> moves=game.validMoves(startPos);
                if(moves==null){ System.out.println("No piece at this position"); continue; }
                var move = moves.stream().filter(maybeMove ->
                        maybeMove.getEndPosition().equals(endPos)).findFirst();

                try{
                    if (move.isPresent())
                        game.makeMove(move.get());
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
}