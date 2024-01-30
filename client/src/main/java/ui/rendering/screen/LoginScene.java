package ui.rendering.screen;

import ui.rendering.renderable.Background;

public class LoginScene extends Scene{
    @Override
    public void init() {
        this.toRender.add(new Background());
    }

    @Override
    public void uninit() {

    }
}
