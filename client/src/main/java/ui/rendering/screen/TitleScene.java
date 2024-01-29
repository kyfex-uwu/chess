package ui.rendering.screen;

import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;

public class TitleScene extends Scene{
    private static final Sprite logo=null;
    @Override
    public void init() {
        this.toRender.add(new Background());
    }

    @Override
    public void uninit() {

    }

    @Override
    public void onLine(String[] args) {
        Renderable.render(this.toRender);
    }
}
