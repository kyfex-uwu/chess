package ui.rendering.screen;

import ui.rendering.Pixel;
import ui.rendering.Renderable;

import java.util.ArrayList;
import java.util.List;

public abstract class Scene implements Renderable{
    public final List<Renderable> toRender = new ArrayList<>();

    public abstract void init();
    public abstract void uninit();
    public abstract void onLine(String[] args);
    @Override
    public void render(Pixel[][] screen) {
        Renderable.render(screen[0].length,screen.length, this.toRender);
    }
    @Override
    public int getOrder() {
        return 0;
    }
}
