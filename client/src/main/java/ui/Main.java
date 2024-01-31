package ui;

import ui.rendering.screen.Scene;
import ui.rendering.screen.TitleScene;

import java.util.*;

public class Main {
    private static Scene currScene = new TitleScene();
    private static boolean quitBool=false;
    private static boolean rerender=false;

    public static void changeScene(Scene newScene){
        currScene.uninit();
        newScene.init();
        currScene=newScene;
    }
    public static void quit(){
        quitBool=true;
    }
    public static void rerender(){
        rerender=true;
    }

    public static void main(String[] args) {
        System.out.println("\u001b[0m");
        System.out.println("\nIf \u001b[38;2;200;0;0m this \u001b[0m is not red, make sure your terminal can display colors\n" +
                "If you don't see a checker pattern here -> ▀▄▀▄, make sure your terminal can display unicode characters " +
                "(if you're on windows, try the command \"chcp 65001\", it temporarily enables unicode characters)\n" +
                "Press Enter to start!");

        Scanner scanner = new Scanner(System.in);
        Config.setInst(Config.dfault);

        currScene.init();
        while (true) {
            if(rerender){
                currScene.onLine(new String[0]);
                rerender=false;
            }else {
                currScene.onLine(scanner.nextLine().split(" "));
            }
            if(quitBool) break;
        }

        System.out.println("Thanks for playing!");
    }
}