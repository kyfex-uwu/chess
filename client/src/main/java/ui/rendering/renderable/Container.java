package ui.rendering.renderable;

import ui.rendering.Pixel;
import ui.rendering.Renderable;

public class Container implements Renderable {
    private final Renderable renderable;
    public int x;
    public int y;
    public int w;
    public int h;
    public Container(Renderable renderable, int w, int h){
        this.renderable=renderable;
        this.w=w;
        this.h=h;
    }
    public Container setPos(int x, int y){
        this.x=x;
        this.y=y;
        return this;
    }
    @Override
    public void render(Pixel[][] screen) {
        var subScreen = new Pixel[this.h][this.w];
        this.renderable.render(subScreen);
        for(int y=0;y<this.h;y++){
            for(int x=0;x<this.w;x++){
                Renderable.overlayPixel(x+this.x,y+this.y,subScreen[y][x], screen);
            }
        }
    }

    @Override
    public int getOrder() {
        return this.renderable.getOrder();
    }
}
