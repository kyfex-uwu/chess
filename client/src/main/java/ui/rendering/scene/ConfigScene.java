package ui.rendering.scene;

import ui.ArgConsumer;
import ui.Config;
import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.Nineslice;

import java.util.Map;

public class ConfigScene extends Scene{
    private static final Nineslice paletteNineslice = new Nineslice("""
            \s▄▄▄\s
            \s▀ ▄\s
            \s▀▀▀\s""", ()->Config.Palette.BUTTON_OUTLINE, ()->Config.Palette.BUTTON_MAIN, ()->Config.Palette.BUTTON_TEXT);
    private static final Sprite.Builder bigPawn = Sprite.Builder.fromStr("""
            \s ()
            \s_/\\_
            /____\\""", true);
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

    private Config.Builder unsavedConfig = new Config.Builder();
    private boolean setDims(String wStr, String hStr){
        try{
            var w = Integer.decode(wStr);
            var h = Integer.decode(hStr);
            if(w>=120&&h>=29){
                this.unsavedConfig.screenWidth=w;
                this.unsavedConfig.screenHeight=h;
                return true;
            }
        }catch(Exception ignored){}
        return false;
    }
    @Override
    public void init() {
        super.init();
        this.toRender.add(new Background());
        this.toRender.add(new Renderable(11) {
            @Override
            public void render(Pixel[][] screen) {
                //nav buttons
                Nineslice.Style.NAV_PANEL.nineslice.render(screen,
                        screen[0].length/2-1-12, screen.length-7, 12, 5, "Back");
                Nineslice.Style.NAV_PANEL.nineslice.render(screen,
                        screen[0].length/2+1, screen.length-7, 12, 5, "Save");
            }
        });
        this.toRender.add(new Renderable(9) {
            @Override
            public void render(Pixel[][] screen) {
                //size
                Nineslice.Style.PANEL.nineslice.render(screen, 5, 5, 13, 5);
                Sprite.Builder.fromStr("┬  ├"+ConfigScene.this.unsavedConfig.screenWidth+"┤\n" +
                        ConfigScene.this.unsavedConfig.screenHeight+"\n" +
                        "┴").withFGColor(Config.Palette.BUTTON_TEXT).build().draw(7,6,screen);
                Sprite.Builder.fromStr("Size").withFGColor(Config.Palette.BUTTON_TEXT).build()
                        .draw(11,8,screen);

                //palettes
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

                //board size
                startingX=screen[0].length-5-17;
                Nineslice.Style.PANEL.nineslice.render(screen, startingX, 5, 17, 5);
                bigPawn.withFGColor(ConfigScene.this.unsavedConfig.displayBig?
                                Config.Palette.PIECE_WHITE:Config.Palette.BUTTON_OUTLINE)
                        .build().draw(startingX+9, 6, screen);
                Sprite.Builder.fromStr("o\nA").withFGColor(ConfigScene.this.unsavedConfig.displayBig?
                                Config.Palette.BUTTON_OUTLINE:Config.Palette.PIECE_WHITE)
                        .build().draw(startingX+7, 7, screen);
                Sprite.Builder.fromStr("Board\nSize").withFGColor(Config.Palette.BUTTON_TEXT).build()
                        .draw(startingX+2,6,screen);
            }
        });

        this.toRender.add(new Renderable(100) {
            @Override
            public void render(Pixel[][] screen) {
                if(ConfigScene.this.unsavedConfig.currPalette.equals(Config.currPalette())) return;

                var newPalette = Config.palettes.get(ConfigScene.this.unsavedConfig.currPalette);
                var currPalette = Config.palettes.get(Config.currPalette());
                for (var row : screen) {
                    for (var pixel : row) {
                        for(int i=0;i<currPalette.colors.length;i++) {
                            if (pixel.fg == currPalette.colors[i])
                                pixel.fg = newPalette.colors[i];
                            if(pixel.bg==currPalette.colors[i])
                                pixel.bg = newPalette.colors[i];
                        }
                    }
                }
            }
        });
    }

    @Override
    public void uninit() {

    }

    private final ArgConsumer consumer = new ArgConsumer(Map.of(
            "back", args -> this.changeScene(new TitleScene()),
            "save", args -> {
                Config.setInst(this.unsavedConfig.build());
                this.dialogMessage=Config.save()?"Saved config":"Failed to save";
            },
            "reset", args -> {
                Config.setInst(Config.dfault);
                this.unsavedConfig = new Config.Builder();
            },

            "size", args -> {
                try{
                    this.setDims(args[0], args[1]);
                }catch(Exception ignored){}
            },
            "palette", args -> {
                if(args.length==0){
                    var names = Config.palettes.keySet();
                    var namesString = "";
                    int maxWidth = 0;
                    for(var name : names) maxWidth = Math.max(maxWidth, name.length());
                    int index=1;
                    for(var name : names){
                        namesString+=name;
                        if(index%4!=0) namesString+=" ".repeat(maxWidth-name.length());
                        else namesString+="\n";
                        index++;
                    }

                    this.dialogMessage = "Set palette: 'palette [palette identifier]'\n"+namesString;
                }else if(Config.palettes.containsKey(args[0])){
                    this.unsavedConfig.currPalette=args[0];
                }
            },
            "boardsize", args -> {
                try{
                    if(args[0].equals("big")||args[0].equals("small")){
                        this.unsavedConfig.displayBig=args[0].equals("big");
                    }
                }catch(Exception ignored){}
            }
    ), ArgConsumer.helpCommandMaker(
            "back","Returns to the title screen",
            "save","Saves and applies your new config",
            "reset","Resets the config to the default settings",
            "size [width] [height]","Sets the size of the screen",
            "palette", "Opens the palette editor",
            "boardsize [big|small]", "Sets the board size to either big or small"
    ));
    @Override
    public void onLine(String[] args) {
        this.consumer.tryConsumeArgs(args);
        if(this.consumer.shouldShowHelp) this.dialogMessage = this.consumer.helpCommand;

        if(this.shouldRender)
            Renderable.render(this.unsavedConfig.screenWidth, this.unsavedConfig.screenHeight, this.toRender);
        this.shouldRender=true;
    }
}
