package ui.rendering.screen;

import ui.ArgConsumer;
import ui.Config;
import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.Nineslice;

import java.util.Arrays;
import java.util.Map;

public class TitleScene extends Scene{
    private static Sprite logo;
    static {
        Config.Palette.onPaletteChange(()-> {
            var builder = Sprite.Builder.fromStr("""
                    ╔═════╗╔═╗b╔═╗▄╔════╗╔═════╗bb▄╔═══╗▄╔═╗▄╔═╗╔═════╗╔═════╗╔═╗b╔═╗╔═════╗╔═════╗
                    ╚═══╗ ║║ ║b║ ║║ ═══╗╝╚═╗ ╔═╝bb║  ═  ║║  ╚╝ ║║ ╔═╗ ║╚═╗ ╔═╝║ ╚═╝ ║║  ═╦═╝║  ═  ║
                    ╔╗▄b║ ║║ ║b║ ║╚═══╗ ║bb║ ║bbbb║ ╔═╗ ║║ ╔╗  ║║ ║b║ ║bb║ ║bb║ ╔═╗ ║║ ╔═╝bb║  ═╗═╝
                    ║ ╚═╝ ║║ ╚═╝ ║╔═══╝ ║bb║ ║bbbb║ ║b║ ║║ ║b║ ║║ ╚═╝ ║bb║ ║bb║ ║b║ ║║ ╚═══╗║ ║╗ ╚╗
                    ▀╚═══╝▀▀╚═══╝▀╚════╝▀bb╚═╝bbbb╚═╝b╚═╝╚═╝b╚═╝╚═════╝bb╚═╝bb╚═╝b╚═╝╚═════╝╚═╝▀╚═╝
                    ▄╔════╗╔═╗b╔═╗▄▄▄▄▄▄▄▄▄╔════╗▄╔════╗bb▄╔════╗╔═╗bbbbb▄██▄ ╔═════╗╔═╗▄╔═╗╔═════╗
                    ║  ╔══╝║ ╚═╝ ║▄▀▀▀▀▀▀▀║ ═══╗╝║ ═══╗╝bb║  ╔══╝║ ║bbbbb▀▀▀▀ ║  ═╦═╝║  ╚╝ ║╚═╗ ╔═╝
                    ║ ║bbbb║ ╔═╗ ║▄▀▄▀▄bbb╚═══╗ ║╚═══╗ ║bb║ ║    ║ ║bbbbb▄██▄ ║ ╔═╝bb║ ╔╗  ║bb║ ║bb
                    ║  ╚══╗║ ║b║ ║▄▀▄▄▄▄▄▄╔═══╝ ║╔═══╝ ║bb║  ╚══╗║ ╚═══╗▀▀▀▀▀▀║ ╚═══╗║ ║▀║ ║bb║ ║bb
                    ▀╚════╝╚═╝b╚═╝▀▀▀▀▀▀▀▀╚════╝▀╚════╝▀bb▀╚════╝╚═════╝▀▀▀▀▀▀╚═════╝╚═╝b╚═╝bb╚═╝bb"""
                    .replaceAll("b", String.valueOf((char) 0)), false);
            var colors = Arrays.stream("""
                    1111111222 22233333334444444  5555555666666677777778888888999 9990000000aaaaaaa
                    1111111222 22233333334444444  555555566666667777777888888899999990000000aaaaaaa
                    111 111222 2223333333  444    55555556666666777 777  888  999999900000  aaaaaaa
                    111111122222223333333  444    555 555666 6667777777  888  999 9990000000aaaaaaa
                    111111122222223333333  444    555 555666 6667777777  888  999 9990000000aaaaaaa
                    bbbbbbbccc ccc        dddddddeeeeeee  fffffffggg          hhhhhhhiiiiiiimmmmmmm
                    bbbbbbbccccccc        dddddddeeeeeee  fffffffggg          hhhhhhhiiiiiiimmmmmmm
                    bbb    ccccccc        dddddddeeeeeee  fff    ggg          hhhhh  iiiiiii  mmm \s
                    bbbbbbbccc ccc        dddddddeeeeeee  fffffffggggggg      hhhhhhhiiiiiii  mmm \s
                    bbbbbbbccc ccc        dddddddeeeeeee  fffffffggggggg      hhhhhhhiii iii  mmm \s"""
                    .split("\n")).map(String::toCharArray).toList().toArray(new char[0][0]);

            var redFG = new Color(255, 61, 19);
            var blueFG = new Color(181, 238, 233);
            var brownFG = new Color(215, 129, 76);
            var redBG = new Color(179, 39, 10);
            var blueBG = new Color(126, 164, 165);
            var brownBG = new Color(151, 87, 51);

            for (int y = 0; y < colors.length; y++) {
                for (int x = 0; x < colors[y].length; x++) {
                    Color fg = null;
                    Color bg = null;
                    switch (colors[y][x]) {
                        case '1':
                        case '2':
                        case '6':
                        case '9':
                        case 'd':
                        case 'f':
                        case 'm':
                            fg = redFG;
                            bg = redBG;
                            break;
                        case '3':
                        case '5':
                        case '0':
                        case 'a':
                        case 'b':
                        case 'h':
                        case 'g':
                            fg = blueFG;
                            bg = blueBG;
                            break;
                        case '4':
                        case '7':
                        case '8':
                        case 'c':
                        case 'e':
                        case 'i':
                            fg = brownFG;
                            bg = brownBG;
                            break;
                    }

                    if (builder.pixels[y][x].character == '▄' || builder.pixels[y][x].character == '▀') {
                        builder.pixels[y][x].fg = bg;
                        builder.pixels[y][x].bg = null;
                    } else {
                        builder.pixels[y][x].fg = fg;
                        builder.pixels[y][x].bg = bg;
                    }
                }
            }

            var chessboardPatterns = Arrays.stream("""
                    12121212
                    00212121
                    00000333
                    00121212
                    21212121""".split("\n")).map(String::toCharArray).toList().toArray(new char[0][0]);
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 8; x++) {
                    switch (chessboardPatterns[y][x]) {
                        case '0':
                            builder.pixels[y + 5][x + 14].fg = Config.Palette.BOARD_WHITE;
                            builder.pixels[y + 5][x + 14].bg = Config.Palette.BOARD_BLACK;
                            break;
                        case '1':
                            builder.pixels[y + 5][x + 14].fg = Config.Palette.BOARD_WHITE;
                            builder.pixels[y + 5][x + 14].bg = null;
                            break;
                        case '2':
                            builder.pixels[y + 5][x + 14].fg = Config.Palette.BOARD_BLACK;
                            builder.pixels[y + 5][x + 14].bg = null;
                            break;
                        case '3':
                            builder.pixels[y + 5][x + 14].fg = null;
                            builder.pixels[y + 5][x + 14].bg = null;
                            break;
                    }
                }
            }

            var pawnShadMap = Arrays.stream("""
                    011110
                    032230
                    011110
                    122221
                    111111""".split("\n")).map(String::toCharArray).toList().toArray(new char[0][0]);
            var shadow = new Color(
                    Config.Palette.PIECE_WHITE.r - 70,
                    Config.Palette.PIECE_WHITE.g - 70,
                    Config.Palette.PIECE_WHITE.b - 70
            );
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 6; x++) {
                    if (builder.pixels[y + 5][x + 52].character != 0) {
                        switch (pawnShadMap[y][x]) {
                            case '1':
                                builder.pixels[y + 5][x + 52].fg = Config.Palette.PIECE_WHITE;
                                break;
                            case '2':
                                builder.pixels[y + 5][x + 52].fg = Config.Palette.PIECE_WHITE;
                                builder.pixels[y + 5][x + 52].bg = shadow;
                                break;
                            case '3':
                                builder.pixels[y + 5][x + 52].fg = shadow;
                                builder.pixels[y + 5][x + 52].bg = null;
                                break;

                        }
                    }
                }
            }

            logo = builder.build();
        });
    }
    @Override
    public void init() {
        this.toRender.add(new Background());
        this.toRender.add(new Renderable(9) {
            @Override
            public void render(Pixel[][] screen) {
                logo.draw((screen[0].length-logo.pixels[0].length)/2, 2, screen);
            }
        });
        this.toRender.add(new Renderable(10) {
            @Override
            public void render(Pixel[][] screen) {
                Nineslice.Style.NAV_PANEL.nineslice.render(screen,
                        4, screen.length-7, 12, 5, "Config");
                Nineslice.Style.NAV_PANEL.nineslice.render(screen,
                        screen[0].length-16, screen.length-7, 12, 5, "Log In");
                Nineslice.Style.NAV_PANEL.nineslice.render(screen,
                        (screen[0].length-12)/2, screen.length-7, 12, 5, "Play");
            }
        });
    }

    @Override
    public void uninit() {

    }

    private boolean shouldRender=true;
    private final ArgConsumer consumer = new ArgConsumer(Map.of(
            "config", args -> {
                this.changeScene(new ConfigScene());
            }
    ));
    @Override
    public void onLine(String[] args) {
        this.consumer.tryConsumeArgs(args);

        super.onLine(args);
    }
}
