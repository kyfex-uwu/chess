package ui.rendering.screen;

import ui.rendering.Color;
import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.ChessRenderer;
import ui.rendering.renderable.Container;
import ui.rendering.renderable.SpriteRenderer;

import java.util.Arrays;

public class TitleScene extends Scene{
    private static final Sprite logo;
    static {
        var builder = Sprite.Builder.fromStr("""
                ╔═════╗╔═╗b╔═╗ ╔════╗╔═════╗bbb╔═══╗b╔═╗b╔═╗╔═════╗╔═════╗╔═╗b╔═╗╔═════╗╔═════╗
                ╚═══╗ ║║ ║b║ ║║ ═══╗╝╚═╗ ╔═╝bb║  ═  ║║  ╚╝ ║║ ╔═╗ ║╚═╗ ╔═╝║ ╚═╝ ║║  ═╦═╝║  ═  ║
                ╔╗bb║ ║║ ║b║ ║╚═══╗ ║bb║ ║bbbb║ ╔═╗ ║║ ╔╗  ║║ ║b║ ║bb║ ║bb║ ╔═╗ ║║ ╔═╝bb║  ═╗═╝
                ║ ╚═╝ ║║ ╚═╝ ║╔═══╝ ║bb║ ║bbbb║ ║b║ ║║ ║b║ ║║ ╚═╝ ║bb║ ║bb║ ║b║ ║║ ╚═══╗║ ║╗ ╚╗
                b╚═══╝bb╚═══╝b╚════╝bbb╚═╝bbbb╚═╝b╚═╝╚═╝b╚═╝╚═════╝bb╚═╝bb╚═╝b╚═╝╚═════╝╚═╝b╚═╝
                b╔════╗╔═╗b╔═╗▄▄▄▄▄▄▄▄b╔════╗b╔════╗bbb╔════╗╔═╗bbbbb▄██▄ ╔═════╗╔═╗b╔═╗╔═════╗
                ║  ╔══╝║ ╚═╝ ║▄▀▀▀▀▀▀▀║ ═══╗╝║ ═══╗╝bb║  ╔══╝║ ║bbbbb▀██▀ ║  ═╦═╝║  ╚╝ ║╚═╗ ╔═╝
                ║ ║bbbb║ ╔═╗ ║▄▀▄▀▄bbb╚═══╗ ║╚═══╗ ║bb║ ║    ║ ║bbbbb▄██▄ ║ ╔═╝bb║ ╔╗  ║bb║ ║bb
                ║  ╚══╗║ ║b║ ║▄▀▄▄▄▄▄▄╔═══╝ ║╔═══╝ ║bb║  ╚══╗║ ╚═══╗▀████▀║ ╚═══╗║ ║b║ ║bb║ ║bb
                b╚════╝╚═╝b╚═╝▀▀▀▀▀▀▀▀╚════╝b╚════╝bbbb╚════╝╚═════╝▀▀▀▀▀▀╚═════╝╚═╝b╚═╝bb╚═╝bb"""
                    .replaceAll("b", String.valueOf((char)0)), false);
        var colors = Arrays.stream("""
                1111111222 222 3333334444444   55555 666 66677777778888888999 9990000000aaaaaaa
                1111111222 22233333334444444  555555566666667777777888888899999990000000aaaaaaa
                11  111222 2223333333  444    55555556666666777 777  888  999999900000  aaaaaaa
                111111122222223333333  444    555 555666 6667777777  888  999 9990000000aaaaaaa
                 11111  22222 333333   444    555 555666 6667777777  888  999 9990000000aaa aaa
                 bbbbbbccc ccc         dddddd eeeeee   ffffffggg          hhhhhhhiii iiimmmmmmm
                bbbbbbbccccccc        dddddddeeeeeee  fffffffggg          hhhhhhhiiiiiiimmmmmmm
                bbb    ccccccc        dddddddeeeeeee  fff    ggg          hhhhh  iiiiiii  mmm \s
                bbbbbbbccc ccc        dddddddeeeeeee  fffffffggggggg      hhhhhhhiii iii  mmm \s
                 bbbbbbccc ccc        dddddd eeeeee    ffffffggggggg      hhhhhhhiii iii  mmm \s"""
                .split("\n")).map(String::toCharArray).toList().toArray(new char[0][0]);

        var redFG=new Color(255,61,19);
        var blueFG=new Color(181,238,233);
        var brownFG=new Color(215,129,76);
        var redBG=new Color(179,39,10);
        var blueBG=new Color(126,164,165);
        var brownBG=new Color(151,87,51);

        for(int y=0;y<colors.length;y++){
            for(int x=0;x<colors[y].length;x++){
                Color fg=null;
                Color bg=null;
                switch(colors[y][x]){
                    case '1':
                    case '2':
                    case '6':
                    case '9':
                    case 'd':
                    case 'h':
                    case 'm':
                        fg=redFG;
                        bg=redBG;
                        break;
                    case '3':
                    case '5':
                    case '0':
                    case 'a':
                    case 'b':
                    case 'f':
                    case 'g':
                        fg=blueFG;
                        bg=blueBG;
                        break;
                    case '4':
                    case '7':
                    case '8':
                    case 'c':
                    case 'e':
                    case 'i':
                        fg=brownFG;
                        bg=brownBG;
                        break;
                }
                builder.pixels[y][x].fg=fg;
                builder.pixels[y][x].bg=bg;
            }
        }

        var chessboardPatterns= Arrays.stream("""
                12121212
                00212121
                00000333
                00121212
                21212121""".split("\n")).map(String::toCharArray).toList().toArray(new char[0][0]);
        for(int y=0;y<5;y++){
            for(int x=0;x<8;x++){
                switch(chessboardPatterns[y][x]){
                    case '0':
                        builder.pixels[y+5][x+14].fg = ChessRenderer.ChessColor.BOARD_WHITE.color;
                        builder.pixels[y+5][x+14].bg = ChessRenderer.ChessColor.BOARD_BLACK.color;
                        break;
                    case '1':
                        builder.pixels[y+5][x+14].fg = ChessRenderer.ChessColor.BOARD_WHITE.color;
                        builder.pixels[y+5][x+14].bg = null;
                        break;
                    case '2':
                        builder.pixels[y+5][x+14].fg = ChessRenderer.ChessColor.BOARD_BLACK.color;
                        builder.pixels[y+5][x+14].bg = null;
                        break;
                    case '3':
                        builder.pixels[y+5][x+14].fg = null;
                        builder.pixels[y+5][x+14].bg = null;
                        break;
                }
            }
        }

        for(int y=0;y<5;y++){
            for(int x=0;x<6;x++){
                if(builder.pixels[y+5][x+52].character!=0)
                    builder.pixels[y+5][x+52].fg = ChessRenderer.ChessColor.WHITE.color;
            }
        }

        logo = builder.build();
    }
    @Override
    public void init() {
        this.toRender.add(new Background());
        this.toRender.add(new SpriteRenderer(logo, 10).setPos(3,3));
    }

    @Override
    public void uninit() {

    }

    @Override
    public void onLine(String[] args) {
        Renderable.render(this.toRender);
    }
}
