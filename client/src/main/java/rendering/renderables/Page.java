package rendering.renderables;

import rendering.Color;
import rendering.Pixel;
import rendering.Renderable;

public class Page implements Renderable {
    private static final Color backgroundColor = new Color(0,1,0);
    private static final Color outlineColor = new Color(0,2,0);
    @Override
    public void render(Pixel[][] screen) {
        for(int y=0;y<screen.length;y++){
            for(int x=0;x<screen[y].length;x++){
                screen[y][x] = new Pixel(' ', null,
                        (y==0||y==screen.length-1||x==0||x==1||x==screen[y].length-1||x==screen[y].length-2)?
                                outlineColor:backgroundColor);
            }
        }
    }

    @Override
    public int getOrder() { return -100; }
}
