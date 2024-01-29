package ui.rendering.renderable;

import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;

public class SpriteRenderer implements Renderable {
    private final Sprite sprite;
    private final int order;
    private int x;
    private int y;
    public SpriteRenderer(Sprite sprite, int order){
        this.sprite=sprite;
        this.order=order;
    }
    public SpriteRenderer setPos(int x, int y){
        this.x=x;
        this.y=y;
        return this;
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
