import chess.*;
import rendering.renderables.ChessRenderer;
import rendering.Renderable;
import rendering.renderables.Page;

import java.util.*;

public class Main {
    private static Collection<ChessMove> movesToShow = List.of();
    private static ChessPosition positionToShow = null;
    public static void main(String[] args) {
        System.out.println("\nIf \u001b[38:5:9m this \u001b[0m is not red, find a console that handles colors pls\n" +
                //"(if you're on windows, try setting registry key HKEY_CURRENT_USER\\Console\\VirtualTerminalLevel to 1)\n" +
                "Press Enter to start!");

        var game = new ChessGame();

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        while (true) {
            var builder = new ChessRenderer.RenderData.Builder()
                    .isBig(false)
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