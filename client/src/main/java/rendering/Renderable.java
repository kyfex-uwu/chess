package rendering;

import java.util.Comparator;
import java.util.List;

public interface Renderable {
    enum CLColor{
        WHITE(97,107),
        GRAY(37,47),
        DARK_GRAY(90,100),
        BLACK(30,40),
        CLEAR(0,0),

        YELLOW(93,43),
        GREEN(92,42),
        PINK(95,45),
        BLUE(94,44),
        CYAN(96,46);

        public final int fg;
        public final int bg;
        CLColor(int fg, int bg){
            this.fg=fg;
            this.bg=bg;
        }
    }

    static void overlayPixel(int x, int y, Pixel pixel, Pixel[][] screen){
        if(y<0||y>=screen.length||x<0||x>=screen[y].length) return;

        var old=screen[y][x];
        screen[y][x]=new Pixel(
                pixel.character!=0?pixel.character:old.character,
                pixel.fg!=null?pixel.fg:old.fg,
                pixel.bg!=null?pixel.bg:old.bg
        );
    }

    static void render(int width, int height, List<Renderable> toRender){
        System.out.println("-".repeat(width));
        System.out.println("\u001b[2J");
        toRender.sort(Comparator.comparingInt(Renderable::getOrder));

        var screen = new Pixel[height][width];
        for(var row : screen)
            for(int i=0;i<row.length;i++)
                row[i]=new Pixel(' ');

        for(var renderable : toRender)
            renderable.render(screen);

        for(var row : screen) {
            for (var pixel : row) {
                System.out.print("\u001b["+pixel.fgInt()+";"+pixel.bgInt()+"m");
                System.out.print(pixel.character);
            }
            System.out.println();
        }
    }

    //--

    void render(Pixel[][] screen);
    int getOrder();
}
