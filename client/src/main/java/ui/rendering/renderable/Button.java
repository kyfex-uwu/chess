package ui.rendering.renderable;

import ui.rendering.Pixel;
import ui.rendering.Renderable;

public class Button implements Renderable {
    @Override
    public void render(Pixel[][] screen) {

    }

    @Override
    public int getOrder() {
        return 10;
    }
}
