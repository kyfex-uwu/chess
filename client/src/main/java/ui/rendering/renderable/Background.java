package ui.rendering.renderable;

import ui.Config;
import ui.PlayData;
import ui.rendering.PFPMaker;
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
}
