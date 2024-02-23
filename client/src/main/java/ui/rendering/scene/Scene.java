package ui.rendering.scene;

import ui.Main;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.renderable.Nineslice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class Scene extends Renderable{
    public final List<Renderable> toRender = new ArrayList<>();
    protected boolean shouldRender=true;
    protected String dialogMessage;
    protected final void changeScene(Scene newScene){
        Main.changeScene(newScene);
        Main.rerender();
        this.shouldRender=false;
    }

    public void init(){
        this.toRender.add(new Renderable(10) {
            @Override
            public void render(Pixel[][] screen) {
                if(Scene.this.dialogMessage!=null) {
                    var width = Math.max(screen[0].length - 40, 6 + Arrays.stream(Scene.this.dialogMessage.split("\n"))
                            .map(String::length).max(Comparator.comparingInt(i -> i)).get());
                    Nineslice.Style.DIALOG.nineslice.render(screen,
                            (screen[0].length-width)/2, screen.length / 2 - 2,
                            width, 2 + Scene.this.dialogMessage.split("\n").length,
                            Scene.this.dialogMessage);
                }
                Scene.this.dialogMessage=null;
            }
        });
    }
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
    public float getOrder() {
        return 0;
    }
}
