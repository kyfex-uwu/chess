package ui.rendering.renderable;

import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;

public class SpriteRenderer extends Renderable {
    private final Sprite sprite;
    private final int order;
    public SpriteRenderer(Sprite sprite, int order){
        this.sprite=sprite;
        this.order=order;
    }
    @Override
    public void render(Pixel[][] screen) {
        this.sprite.draw(this.x, this.y, screen);
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
