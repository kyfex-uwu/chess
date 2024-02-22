package ui;

import ui.rendering.Color;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

    public static void init() {
        Config.setInst(Config.dfault);
        try {
            FileInputStream propsInput = new FileInputStream(Main.configFileName);
            Properties prop = new Properties();
            prop.load(propsInput);

            var newConfig = new Builder();
            newConfig.screenWidth = Math.max(Integer.parseInt(prop.getProperty("screenWidth")), 120);
            newConfig.screenHeight = Math.max(Integer.parseInt(prop.getProperty("screenHeight")), 29);
            newConfig.displayBig = Boolean.parseBoolean(prop.getProperty("displayBig"));
            newConfig.currPalette = prop.getProperty("currPalette");
            if(newConfig.currPalette==null) newConfig.currPalette="Default";
            Config.setInst(newConfig.build());
        }catch(Exception e){
            Config.setInst(Config.dfault);
        }
    }
    public static boolean save(){
        try {
            Properties prop = new Properties();
            try {
                FileInputStream propsInput = new FileInputStream(Main.configFileName);
                prop.load(propsInput);
            }catch(Exception ignored){} //todo: replace this with a check

            try (Writer inputStream = new FileWriter(Main.configFileName)) {
                prop.setProperty("screenWidth", String.valueOf(inst.screenWidth));
                prop.setProperty("screenHeight", String.valueOf(inst.screenHeight));
                prop.setProperty("displayBig", String.valueOf(inst.displayBig));
                prop.setProperty("currPalette", inst.currPalette);

                prop.store(inputStream, Main.confingVer);
            }

            return true;
        }catch(Exception e){ return false; }
    }

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
        public static Color DIALOG_MAIN;
        public static Color DIALOG_TEXT;
        public static Color DIALOG_OUTLINE;
        public static Color INPUT_MAIN;
        public static Color INPUT_ACCENT;
        public static Color BOARD_WHITE;
        public static Color BOARD_GRAY;
        public static Color BOARD_BLACK;
        public static Color PIECE_WHITE;
        public static Color PIECE_BLACK;
        public static Color BOARD_TEXT;

        public final Color[] colors;
        public Palette(Color... colors){ this.colors=colors; }
        public void apply(){
            int x=0;

            BG_MAIN = this.colors[x]; x++;
            BG_CHECKER = this.colors[x]; x++;
            BG_OUTLINE = this.colors[x]; x++;
            BUTTON_MAIN = this.colors[x]; x++;
            BUTTON_TEXT = this.colors[x]; x++;
            BUTTON_OUTLINE = this.colors[x]; x++;
            DIALOG_MAIN = this.colors[x]; x++;
            DIALOG_TEXT = this.colors[x]; x++;
            DIALOG_OUTLINE = this.colors[x]; x++;
            INPUT_MAIN = this.colors[x]; x++;
            INPUT_ACCENT = this.colors[x]; x++;
            BOARD_WHITE = this.colors[x]; x++;
            BOARD_GRAY = this.colors[x]; x++;
            BOARD_BLACK = this.colors[x]; x++;
            PIECE_WHITE = this.colors[x]; x++;
            PIECE_BLACK = this.colors[x]; x++;
            BOARD_TEXT = this.colors[x]; x++;
        }

        private static final ArrayList<Runnable> paletteChangeConsumers = new ArrayList<>();
        public static void onPaletteChange(Runnable toRun){
            if(Palette.BG_MAIN!=null) toRun.run();
            paletteChangeConsumers.add(toRun);
        }
    }
    public static final Map<String, Palette> palettes = new HashMap<>();
    static{
        /*
        BG_MAIN
        BG_CHECKER
        BG_OUTLINE

        BUTTON_MAIN
        BUTTON_TEXT
        BUTTON_OUTLINE

        DIALOG_MAIN
        DIALOG_TEXT
        DIALOG_OUTLINE

        INPUT_MAIN
        INPUT_ACCENT

        BOARD_WHITE
        BOARD_GRAY
        BOARD_BLACK

        PIECE_WHITE
        PIECE_BLACK
        BOARD_TEXT
         */
        palettes.put("Default", new Palette(
                new Color(20,20,20),
                new Color(30,30,30),
                new Color(70,70,70),

                new Color(145, 105, 80),
                new Color(240, 212, 187),
                new Color(181, 144, 121),

                new Color(63,63,229),
                new Color(255,255,255),
                new Color(187,201,252),

                new Color(181,238,233),
                new Color(86, 20, 6),

                new Color(148, 121, 105),
                new Color(107, 81, 66),
                new Color(77, 56, 44),

                new Color(255,255,255),
                new Color(0,0,0),
                new Color(189, 161, 145)
        ));
        palettes.put("Light", new Palette(
                new Color(125,120,130),
                new Color(145,140,150),
                new Color(235,230,240),

                new Color(45,74,5),
                new Color(165,212,100),
                new Color(67,106,15),

                new Color(129,63,229),
                new Color(255,255,255),
                new Color(187,201,252),

                new Color(86, 20, 6),
                new Color(181,238,233),

                new Color(143, 122, 163),
                new Color(118, 93, 143),
                new Color(99, 70, 128),

                new Color(229,207,251),
                new Color(39, 24, 56),
                new Color(195, 171, 219)
        ));
    }
}
