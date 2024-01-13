import chess.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static chess.ChessRenderer.renderGame;

public class Main {
    private static Collection<ChessMove> movesToShow = List.of();
    public static void main(String[] args) {
        var game = new ChessGame();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Color mode? (y)es/(n)o\n" +
                "(if you're not sure, if \u001b[91m this \u001b[0m is red, then you can enable color mode) ");
        String ans = scanner.nextLine();
        if(ans.startsWith("y")) ChessRenderer.canShowColor=true;
        while (true) {
            renderGame(game, ChessRenderer.RenderData.from(movesToShow));
            System.out.println();
            String line = scanner.nextLine();

            if(line.startsWith("help")){
                System.out.println("help: shows this command\n" +
                        "show [position]: shows all valid moves that piece can make\n" +
                        "move [position from] [position to]: moves a piece from position to position");
            }else if(line.startsWith("show")){
                try {
                    movesToShow = game.validMoves(new ChessPosition(line.charAt(6) - 48, line.charAt(5) - 96));
                }catch(Exception e){
                    movesToShow = Collections.emptyList();
                }
            }else if(line.startsWith("move")){
                movesToShow = List.of();

                var startPos=new ChessPosition(line.charAt(6)-48, line.charAt(5)-96);
                var endPos=new ChessPosition(line.charAt(9)-48, line.charAt(8)-96);

                var move = game.validMoves(startPos).stream().filter(maybeMove->
                        maybeMove.getEndPosition().equals(endPos)).findFirst();
                if(move.isPresent()){
                    try {
                        game.makeMove(move.get());
                    }catch(Exception ignored){}
                }
            }else{
                System.out.println("command not recognized, type \"help\" to see all commands");
            }
            System.out.println();
        }

    }
}