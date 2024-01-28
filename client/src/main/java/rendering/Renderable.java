package rendering;

import java.util.Comparator;
import java.util.List;

public interface Renderable {
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
                System.out.print(
                        (pixel.fg==null?"":"\u001b[38:5:"+pixel.fg.num+"m")+
                        (pixel.bg==null?"":"\u001b[48:5:"+pixel.bg.num+"m"));
                System.out.print(pixel.character);
            }
            System.out.println("\u001b[0;0m");
        }
    }

    //--

    void render(Pixel[][] screen);
    int getOrder();
}
