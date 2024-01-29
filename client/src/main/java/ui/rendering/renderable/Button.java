package ui.rendering.renderable;

import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;

import java.util.Arrays;

public class Button implements Renderable {
    private static final Color buttonColor = new Color(145, 105, 80);
    private static final Color buttonOutline = new Color(212, 182,155);
    private static final char[][] nineslice= Arrays.stream("""
            .▄.
            ▐ ▌
            ˙▀˙""".split("\n")).map(String::toCharArray).toList().toArray(new char[0][0]);
    private int x;
    private int y;
    private int w;
    private int h;
    private String message;
    public Button(String message, int w, int h){
        this.w=w;
        this.h=h;
        this.message=message;
    }
    public Button setPos(int x, int y){
        this.x=x;
        this.y=y;
        return this;
    }
    @Override
    public void render(Pixel[][] screen) {
        for(int y=0;y<this.h;y++){
            for(int x=0;x<this.w;x++){
                Renderable.overlayPixel(x+this.x, y+this.y, new Pixel(
                        nineslice[y==0?0:(y==this.h-1?2:1)][x==0?0:(x==this.w-1?2:1)],
                        buttonOutline, buttonColor
                ), screen);
            }
        }
        Sprite.Builder.fromStr(this.message, false).build().draw(
                this.x+(this.w-this.message.length())/2,
                this.y+(this.h-1)/2,screen);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
