package ui.rendering.renderable;

import ui.Config;
import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Supplier;

public class Nineslice extends Renderable {
    public enum Style{
        NAV_PANEL("""
            \s.▄.\s
             █ █\s
             ˙▀˙\s""", ()->Config.Palette.BUTTON_OUTLINE, ()->Config.Palette.BUTTON_MAIN, ()->Config.Palette.BUTTON_TEXT),
        PANEL("""
                \s┏━┓\s
                 ┃ ┃\s
                 ┗━┛\s""", ()->Config.Palette.BUTTON_OUTLINE, ()->Config.Palette.BUTTON_MAIN, ()->Config.Palette.BUTTON_TEXT),
        DIALOG("""
                \s+-+\s
                 | |\s
                 +-+\s""", ()->Config.Palette.DIALOG_OUTLINE, ()->Config.Palette.DIALOG_MAIN, ()->Config.Palette.DIALOG_TEXT),
        INPUT("""
            \s/~\\\s
            \s   \s
            \s\\~/\s""", ()->Config.Palette.INPUT_ACCENT, ()->Config.Palette.INPUT_MAIN, ()->Config.Palette.INPUT_ACCENT);
        public final Nineslice nineslice;
        Style(String pattern, Supplier<Color> fg, Supplier<Color> bg, Supplier<Color> text){
            this.nineslice = new Nineslice(pattern, fg, bg, text);
        }
        static{
            INPUT.nineslice.floatText(FloatDir.LEFT);
        }
    }
    public enum FloatDir {
        LEFT,
        RIGHT,
        CENTER
    }

    private int w;
    private int h;
    private String message;
    private Supplier<Color> fg;
    private Supplier<Color> bg;
    private Supplier<Color> text;
    private FloatDir floatDir = FloatDir.CENTER;
    private char[][] pattern;
    public Nineslice(String pattern, Supplier<Color> fg, Supplier<Color> bg, Supplier<Color> text){
        this.pattern=Arrays.stream(pattern.split("\n")).map(String::toCharArray).toList().toArray(new char[0][0]);
        this.setColors(fg, bg, text);
    }
    public void setColors(Supplier<Color> fg, Supplier<Color> bg, Supplier<Color> text){
        this.fg=fg;
        this.bg=bg;
        this.text=text;
    }
    public Nineslice floatText(FloatDir floatDir){ this.floatDir=floatDir; return this; }

    public void render(Pixel[][] screen, int x, int y, int w, int h){
        this.render(screen, x, y, w, h, "");
    }
    public void render(Pixel[][] screen, int x, int y, int w, int h, String message){
        this.setPos(x, y);
        this.w=w;
        this.h=h;
        this.message=message;

        this.render(screen);

        this.setPos(0,0);
        this.w=0;
        this.h=0;
        this.message=null;
    }
    @Override
    public void render(Pixel[][] screen) {
        for(int y=0;y<this.h;y++){
            for(int x=0;x<this.w;x++){
                var NSX=2;
                var NSY=1;
                if(x==0) NSX=0;
                if(x==1) NSX=1;
                if(x==this.w-2) NSX=3;
                if(x==this.w-1) NSX=4;
                if(y==0) NSY=0;
                if(y==this.h-1) NSY=2;

                Renderable.overlayPixel(x+this.x, y+this.y, new Pixel(
                        pattern[NSY][NSX],
                        this.fg.get(), this.bg.get()
                ), screen);
            }
        }
        if(this.message!=null&&!this.message.isEmpty()) {
            var messageLines = this.message.split("\n");
            var messageW = Arrays.stream(messageLines).map(String::length).max(Comparator.comparingInt(o -> o)).get();
            Sprite.Builder.fromStr(this.message).withFGColor(this.text.get()).build().draw(
                    this.x + switch(this.floatDir){
                        case LEFT -> 2;
                        case RIGHT -> this.w-messageW-2;
                        case CENTER -> (this.w - messageW) / 2;
                    },this.y + (this.h - messageLines.length) / 2, screen);
        }
    }
}
