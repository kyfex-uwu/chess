import chess.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

import static chess.ChessBoard.printGame;
import static chess.ChessGame.TeamColor.WHITE;

public class Main {
    private static Collection<ChessMove> movesToShow = List.of();
    public static void main(String[] args) {
        var game = new ChessGame();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            printGame(game, movesToShow);
            String line = scanner.nextLine();

            if(line.charAt(0)=='s'){
                movesToShow = game.validMoves(new ChessPosition(line.charAt(3)-48, line.charAt(2)-96));
                continue;
            }
            movesToShow = List.of();

            var startPos=new ChessPosition(line.charAt(1)-48, line.charAt(0)-96);
            var endPos=new ChessPosition(line.charAt(4)-48, line.charAt(3)-96);

            var move = game.validMoves(startPos).stream().filter(maybeMove->
                    maybeMove.getEndPosition().equals(endPos)).findFirst();
            if(move.isPresent()){
                try {
                    game.makeMove(move.get());
                }catch(Exception ignored){}
            }
        }

    }
}