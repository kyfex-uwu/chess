package ui.rendering;

import ui.Config;

import java.util.Comparator;
import java.util.List;

public abstract class Renderable {
    public static void overlayPixel(int x, int y, Pixel pixel, Pixel[][] screen){
        if(y<0||y>=screen.length||x<0||x>=screen[y].length||pixel==null) return;

        var old=screen[y][x];
        screen[y][x]=new Pixel(
                pixel.character!=0?pixel.character:old.character,
                pixel.fg!=null?pixel.fg:old==null?null:old.fg,
                pixel.bg!=null?pixel.bg:old==null?null:old.bg
        );
    }
    public static void render(List<Renderable> toRender){
        render(Config.screenWidth(), Config.screenHeight(), toRender);
    }
    public static void render(int width, int height, List<Renderable> toRender){
        var toPrint = new StringBuilder("\u001b[3J\u001b[2J\u001b[0;0H");

        toRender.sort(Comparator.comparingInt(Renderable::getOrder));

        var screen = new Pixel[height][width];
        for(var row : screen)
            for(int i=0;i<row.length;i++)
                row[i]=new Pixel(' ');

        for(var renderable : List.copyOf(toRender))
            renderable.render(screen);

        for(var row : screen) {
            for (var pixel : row) {
                if(pixel.fg!=null) toPrint.append("\u001b[38;2;"+pixel.fg.r+";"+pixel.fg.g+";"+pixel.fg.b+"m");
                if(pixel.bg!=null) toPrint.append("\u001b[48;2;"+pixel.bg.r+";"+pixel.bg.g+";"+pixel.bg.b+"m");
                toPrint.append(pixel.character+"\u001b[0m");
            }
            toPrint.append("\u001b[0m\n");
        }

        System.out.print(toPrint);
    }

    //--

    protected int x;
    protected int y;
    private final int order;
    public Renderable(int order){ this.order=order; }
    public Renderable(){ this(0); }
    public abstract void render(Pixel[][] screen);

    public int getOrder(){ return this.order; }
    public <T extends Renderable> T setPos(int x, int y) {
        this.x=x;
        this.y=y;
        return (T) this;
    }
}
