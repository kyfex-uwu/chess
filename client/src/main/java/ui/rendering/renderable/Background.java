package ui.rendering.renderable;

import ui.Config;
import ui.PlayData;
import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;

public class Background extends Renderable {
    public static Sprite pfp;
    @Override
    public void render(Pixel[][] screen) {
        for(int y=0;y<screen.length;y++){
            for(int x=0;x<screen[y].length;x++){
                var bgColor = (x==0||x==screen[y].length-1)?Config.Palette.BG_CHECKER:Config.Palette.BG_MAIN;
                char ch = x/2%2==y%2?'▀':'▄';
                if(y==0&&ch=='▄'){
                    bgColor=Config.Palette.BG_CHECKER;
                    ch='▀';
                }else if(y==screen.length-1&&ch=='▀'){
                    bgColor=Config.Palette.BG_CHECKER;
                    ch='▄';
                }
                if(x==0||x==screen[y].length-1){
                    bgColor=Config.Palette.BG_OUTLINE;
                    ch=' ';
                }

                screen[y][x] = new Pixel(ch, (y==0||y==screen.length-1)?
                        Config.Palette.BG_OUTLINE:Config.Palette.BG_CHECKER, bgColor);
            }
        }

        if(!PlayData.loggedIn()) pfp=null;

        if(pfp==null){
            if(PlayData.loggedIn()) {
                pfp = PFPMaker.pfpToSprite(PlayData.selfData.pfp());
            }
        }
        if(pfp!=null)
            pfp.draw(screen[0].length-8, 1, screen);
    }

    @Override
    public int getOrder() { return -100; }

    public static void darken(Pixel[][] screen){
        for(var y=0;y<screen.length;y++){
            for(var x=0;x<screen[y].length;x++){
                screen[y][x] = new Pixel(screen[y][x].character, new Color(
                        (int) (screen[y][x].fg.r*0.5),
                        (int) (screen[y][x].fg.g*0.5),
                        (int) (screen[y][x].fg.b*0.5)
                ), new Color(
                        (int) (screen[y][x].bg.r*0.5),
                        (int) (screen[y][x].bg.g*0.5),
                        (int) (screen[y][x].bg.b*0.5)
                ));
            }
        }
    }
}
