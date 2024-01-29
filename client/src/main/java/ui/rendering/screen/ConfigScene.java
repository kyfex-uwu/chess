package ui.rendering.screen;

import ui.Main;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.Button;

import java.util.Locale;

public class ConfigScene extends Scene{
    @Override
    public void init() {
        this.toRender.add(new Background());
        this.toRender.add(new Button("Back", 10, 5){
            @Override
            public void render(Pixel[][] screen) {
                this.setPos((screen[0].length-10)/2, screen.length-7);
                super.render(screen);
            }
        });
    }

    @Override
    public void uninit() {

    }

    @Override
    public void onLine(String[] args) {
        if(args.length>=1)
            switch(args[0].toLowerCase(Locale.ROOT)){
                case "back":
                    Main.changeScene(new TitleScene());
                    Main.rerender();
                    return;
            }

        Renderable.render(this.toRender);
    }
}
