package ui.rendering.screen;

import ui.ArgConsumer;
import ui.Config;
import ui.Main;
import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.Button;
import ui.rendering.renderable.Nineslice;

import java.util.Map;

public class ConfigScene extends Scene{
    private static final Nineslice nineslice = new Nineslice("""
            \s┏━┓\s
             ┃ ┃\s
             ┗━┛\s""");
    private static final Nineslice paletteNineslice = new Nineslice("""
            \s▄▄▄\s
            \s▀ ▄\s
            \s▀▀▀\s""");
    private static final Sprite.Builder bigPawn = Sprite.Builder.fromStr("""
            \s ()
            \s_/\\_
            /____\\""", true);
    static{
        Config.Palette.onPaletteChange(()->{
            nineslice.setColors(Config.Palette.BUTTON_OUTLINE, Config.Palette.BUTTON_MAIN);
            paletteNineslice.setColors(Config.Palette.BUTTON_OUTLINE, Config.Palette.BUTTON_MAIN);
        });
    }
    private static Color hueToColor(int hue){
        //33 hues, 11 11 11
        int r=0;
        int g=0;
        int b=0;
        var scalar = 255*2/11;
        if(hue<22) r=Math.min(255, (11-Math.abs(11-hue))*scalar);
        if(hue>=11) g=Math.min(255, (11-Math.abs(22-hue))*scalar);
        if(hue<11||hue>=22) b=Math.min(255, (11-Math.abs(11-(hue+11)%33))*scalar);

        return new Color(r, g, b);
    }

    private String currEditing="";
    private Config.Builder unsavedConfig = new Config.Builder();
    private boolean setDims(String wStr, String hStr){
        try{
            var w = Integer.decode(wStr);
            var h = Integer.decode(hStr);
            if(w>=90&&h>=25){
                this.unsavedConfig.screenWidth=w;
                this.unsavedConfig.screenHeight=h;
                return true;
            }
        }catch(Exception ignored){}
        return false;
    }
    @Override
    public void init() {
        this.toRender.add(new Background());
        this.toRender.add(new Button("Back", 12, 5){
            @Override
            public void render(Pixel[][] screen) {
                this.setPos(screen[0].length/2-1-12, screen.length-7);
                super.render(screen);
            }
        });
        this.toRender.add(new Button("Save", 12, 5){
            @Override
            public void render(Pixel[][] screen) {
                this.setPos(screen[0].length/2+1, screen.length-7);
                super.render(screen);
            }
        });

        this.toRender.add(new Renderable() {
            @Override
            public void render(Pixel[][] screen) {
                nineslice.render(screen, 5, 5, 13, 5);
                Sprite.Builder.fromStr("┬  ├"+Config.screenWidth()+"┤\n" +
                        Config.screenHeight()+"\n" +
                        "┴").build().draw(7,6,screen);
                Sprite.Builder.fromStr("Size").withFGColor(Config.Palette.BUTTON_TEXT).build()
                        .draw(11,8,screen);
            }
        });
        this.toRender.add(new Renderable() {
            @Override
            public void render(Pixel[][] screen) {
                int startingX=(screen[0].length-13)/2;
                paletteNineslice.render(screen, startingX, 5,13, 5);
                for(int i=1;i<12;i++){
                    Renderable.overlayPixel(startingX+i, 5, new Pixel((char)0,
                            hueToColor(i-1), Config.Palette.BUTTON_MAIN), screen);
                    Renderable.overlayPixel(startingX+12-i, 9, new Pixel((char)0,
                            hueToColor(i+16), Config.Palette.BUTTON_MAIN), screen);
                }
                for(int i=1;i<4;i++){
                    Renderable.overlayPixel(startingX+11, 5+i, new Pixel((char)0,
                            hueToColor(i*2+10), hueToColor(i*2+9)), screen);
                    Renderable.overlayPixel(startingX+1, 9-i, new Pixel((char)0,
                            hueToColor(i*2+27), hueToColor(i*2+26)), screen);
                }

                Sprite.Builder.fromStr("Palette").withFGColor(Config.Palette.BUTTON_TEXT).build()
                        .draw(startingX+3, 7, screen);

                Sprite.Builder.fromStr("▄▀")
                        .withFGColor(Config.Palette.BOARD_BLACK)
                        .withBGColor(Config.Palette.BOARD_WHITE)
                        .build().draw(startingX+2, 8, screen);
                Renderable.overlayPixel(startingX+4, 8, new Pixel('▀',
                        Config.Palette.PIECE_WHITE,Config.Palette.PIECE_BLACK), screen);
            }
        });
        this.toRender.add(new Renderable() {
            @Override
            public void render(Pixel[][] screen) {
                int startingX=screen[0].length-5-17;
                nineslice.render(screen, startingX, 5, 17, 5);
                bigPawn.withFGColor(Config.displayBig()?
                                Config.Palette.PIECE_WHITE:Config.Palette.BUTTON_OUTLINE)
                        .build().draw(startingX+9, 6, screen);
                Sprite.Builder.fromStr("o\nA").withFGColor(Config.displayBig()?
                                Config.Palette.BUTTON_OUTLINE:Config.Palette.PIECE_WHITE)
                        .build().draw(startingX+7, 7, screen);
                Sprite.Builder.fromStr("Board\nSize").withFGColor(Config.Palette.BUTTON_TEXT).build()
                        .draw(startingX+2,6,screen);
            }
        });
        this.toRender.add(new Renderable() {
            @Override
            public void render(Pixel[][] screen) {
                switch(currEditing){
                    case "size":
                        break;
                    case "palette":
                        break;
                    case "boardsize":
                        break;
                }
            }
        });
    }

    @Override
    public void uninit() {

    }

    private final ArgConsumer consumer = new ArgConsumer(Map.of(
            "back", args -> {
                this.changeScene(new TitleScene());
            },

            "size", args -> {
                this.currEditing="size";
                try{ this.setDims(args[0], args[1]); }catch(Exception ignored){}
            },
            "palette", args -> {
                this.currEditing="palette";
            },
            "boardsize", args -> {
                this.currEditing="boardsize";
            },

            "save", args -> {
                Config.setInst(this.unsavedConfig.build());
            },
            "reset", args -> {
                Config.setInst(Config.dfault);
                this.unsavedConfig = new Config.Builder();
            }
    ));
    @Override
    public void onLine(String[] args) {
        switch(this.currEditing){
            case "size":
                break;
            case "palette":
                break;
            case "boardsize":
                break;
        }
        this.consumer.tryConsumeArgs(args);

        if(this.shouldRender)
            Renderable.render(this.unsavedConfig.screenWidth, this.unsavedConfig.screenHeight, this.toRender);
        this.shouldRender=true;
    }
}
