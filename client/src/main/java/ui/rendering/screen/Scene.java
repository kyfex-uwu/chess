package ui.rendering.screen;

import ui.Main;
import ui.rendering.Pixel;
import ui.rendering.Renderable;

import java.util.ArrayList;
import java.util.List;

public abstract class Scene extends Renderable{
    public final List<Renderable> toRender = new ArrayList<>();
    protected boolean shouldRender=true;
    protected final void changeScene(Scene newScene){
        Main.changeScene(newScene);
        Main.rerender();
        this.shouldRender=false;
    }

    public abstract void init();
    public abstract void uninit();
    public void onLine(String[] args){
        if(this.shouldRender) Renderable.render(this.toRender);
        this.shouldRender=true;
    }
    @Override
    public void render(Pixel[][] screen) {
        Renderable.render(screen[0].length,screen.length, this.toRender);
    }
    @Override
    public int getOrder() {
        return 0;
    }
}
