package ui.rendering.renderable;

import ui.Config;
import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;

import java.util.Arrays;

public class Button extends Renderable {
    private static final Nineslice nineslice= new Nineslice("""
            \s.▄.\s
            \s█ █\s
            \s˙▀˙\s""");
    private int w;
    private int h;
    private String message;
    public Button(String message, int w, int h){
        this.w=w;
        this.h=h;
        this.message=message;
    }

    @Override
    public void render(Pixel[][] screen) {
        nineslice.setColors(Config.Palette.BUTTON_OUTLINE, Config.Palette.BUTTON_MAIN)
                .render(screen, this.x, this.y, this.w, this.h);
        Sprite.Builder.fromStr(this.message).withFGColor(Config.Palette.BUTTON_TEXT).build().draw(
                this.x+(this.w-this.message.length())/2,
                this.y+(this.h-1)/2,screen);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
