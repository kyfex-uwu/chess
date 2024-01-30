package ui;

import ui.rendering.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static Config getInst() { return inst; }
    public static void setInst(Config config){
        Config.inst = config;

        palettes.get(config.currPalette).apply();
        for(var toRun : Palette.paletteChangeConsumers)
            toRun.run();
    }

    public static Config inst;

    public static final Config dfault = new Config(120, 29, true, "Default");
    public final int screenWidth;
    public static int screenWidth(){ return inst.screenWidth; }
    public final int screenHeight;
    public static int screenHeight(){ return inst.screenHeight; }
    public final boolean displayBig;
    public static boolean displayBig(){ return inst.displayBig; }
    public final String currPalette;
    public static String currPalette(){ return inst.currPalette; }

    public static class Builder{
        public int screenWidth;
        public int screenHeight;
        public boolean displayBig;
        public String currPalette;
        public Builder(){
            this.screenWidth = Config.inst.screenWidth;
            this.screenHeight = Config.inst.screenHeight;
            this.displayBig = Config.inst.displayBig;
            this.currPalette = Config.inst.currPalette;
        }
        public Config build(){
            return new Config(this.screenWidth, this.screenHeight, this.displayBig, this.currPalette);
        }
    }
    public Config(int screenWidth, int screenHeight, boolean displayBig, String currPalette){
        this.screenWidth=screenWidth;
        this.screenHeight=screenHeight;
        this.displayBig=displayBig;
        this.currPalette=currPalette;
    }

    public static class Palette{
        public static Color BG_MAIN;
        public static Color BG_CHECKER;
        public static Color BG_OUTLINE;
        public static Color BUTTON_MAIN;
        public static Color BUTTON_TEXT;
        public static Color BUTTON_OUTLINE;
        public static Color BOARD_WHITE;
        public static Color BOARD_GRAY;
        public static Color BOARD_BLACK;
        public static Color PIECE_WHITE;
        public static Color PIECE_BLACK;

        private final Color[] colors;
        public Palette(Color... colors){ this.colors=colors; }
        public void apply(){
            BG_MAIN=this.colors[0];
            BG_CHECKER=this.colors[1];
            BG_OUTLINE=this.colors[2];
            BUTTON_MAIN=this.colors[3];
            BUTTON_TEXT=this.colors[4];
            BUTTON_OUTLINE=this.colors[5];
            BOARD_WHITE=this.colors[6];
            BOARD_GRAY=this.colors[7];
            BOARD_BLACK=this.colors[8];
            PIECE_WHITE=this.colors[9];
            PIECE_BLACK=this.colors[10];
        }

        private static final ArrayList<Runnable> paletteChangeConsumers = new ArrayList<>();
        public static void onPaletteChange(Runnable toRun){
            if(Palette.BG_MAIN!=null) toRun.run();
            paletteChangeConsumers.add(toRun);
        }
    }
    private static final Map<String, Palette> palettes = new HashMap<>();
    static{
        /*
        BG_MAIN
        BG_CHECKER
        BG_OUTLINE

        BUTTON_MAIN
        BUTTON_TEXT
        BUTTON_OUTLINE

        BOARD_WHITE
        BOARD_GRAY
        BOARD_BLACK
        PIECE_WHITE
        PIECE_BLACK
         */
        palettes.put("Default", new Palette(
                new Color(20,20,20),
                new Color(30,30,30),
                new Color(70,70,70),

                new Color(145, 105, 80),
                new Color(240, 212, 187),
                new Color(181, 144, 121),

                new Color(148, 121, 105),
                new Color(107, 81, 66),
                new Color(77, 56, 44),
                new Color(255,255,255),
                new Color(0,0,0)
        ));
    }
}
