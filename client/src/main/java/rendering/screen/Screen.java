package rendering.screen;

import rendering.Renderable;

import java.util.ArrayList;
import java.util.List;

public abstract class Screen {
    public final List<Renderable> toRender = new ArrayList<>();
    public abstract void render(int w, int h);
}
