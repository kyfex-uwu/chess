import chess.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static chess.ChessRenderer.renderGame;

public class Client {
    private static Collection<ChessMove> movesToShow = List.of();
    private static ChessPosition positionToShow = null;
    public static void main(String[] args) {
        var game = new ChessGame();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Color mode? (y)es/(n)o\n" +
                "(if you're not sure, enable color mode if \u001b[91m this \u001b[0m is red) ");
        String ans = scanner.nextLine();
        if(ans.startsWith("y")) ChessRenderer.canShowColor=true;
        while (true) {
            var builder = new ChessRenderer.RenderData.Builder();
            if(positionToShow!=null){
                builder.setPositions(movesToShow, positionToShow);
            }
            renderGame(game, builder.facingWhite(game.getTeamTurn().whiteOrBlack(true,false)).build());
            System.out.println();
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
            System.out.println();
        }

    }
}