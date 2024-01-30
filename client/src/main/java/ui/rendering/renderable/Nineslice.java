package ui.rendering.renderable;

import ui.Config;
import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Renderable;

import java.util.Arrays;

public class Nineslice extends Renderable {
    private int w;
    private int h;
    private Color fg;
    private Color bg;
    private final char[][] pattern;
    public Nineslice(String pattern){
        this.pattern=Arrays.stream(pattern.split("\n")).map(String::toCharArray).toList().toArray(new char[0][0]);
    }
    public Nineslice setColors(Color fg, Color bg){
        this.fg=fg;
        this.bg=bg;
        return this;
    }

    public void render(Pixel[][] screen, int x, int y, int w, int h){
        this.setPos(x, y);
        this.w=w;
        this.h=h;
        this.render(screen);
    }
    @Override
    public void render(Pixel[][] screen) {
        for(int y=0;y<this.h;y++){
            for(int x=0;x<this.w;x++){
                var NSX=2;
                var NSY=1;
                if(x==0) NSX=0;
                if(x==1) NSX=1;
                if(x==this.w-2) NSX=3;
                if(x==this.w-1) NSX=4;
                if(y==0) NSY=0;
                if(y==this.h-1) NSY=2;

                Renderable.overlayPixel(x+this.x, y+this.y, new Pixel(
                        pattern[NSY][NSX],
                        this.fg, this.bg
                ), screen);
            }
        }
    }
}
