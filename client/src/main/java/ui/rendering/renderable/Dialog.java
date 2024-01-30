package ui.rendering.renderable;

import ui.rendering.Pixel;
import ui.rendering.Renderable;

public class Dialog extends Renderable {
    private static final Nineslice nineslice = new Nineslice("""
            \s*-*\s
            \s| |\s
            \s*-*\s""");
    private int w;
    private int h;
    private String message;
    public Dialog(String message, int w, int h){
        this.w=w;
        this.h=h;
        this.message=message;
    }
    @Override
    public void render(Pixel[][] screen) {
        nineslice.render(screen, this.x, this.y, this.w, this.h);
    }
}
