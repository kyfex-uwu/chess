package rendering.renderable;

import rendering.Color;
import rendering.Pixel;
import rendering.Renderable;

public class Page implements Renderable {
    public static final Color backgroundColor = new Color(20,20,20);
    public static final Color outlineColor = new Color(70,70,70);
    public static final Color checkerColor = new Color(30,30,30);
    @Override
    public void render(Pixel[][] screen) {
        for(int y=0;y<screen.length;y++){
            for(int x=0;x<screen[y].length;x++){
                var bgColor = (x==0||x==screen[y].length-1)?outlineColor:backgroundColor;
                char ch = x/2%2==y%2?'▀':'▄';
                if(y==0&&ch=='▄'){
                    bgColor=checkerColor;
                    ch='▀';
                }else if(y==screen.length-1&&ch=='▀'){
                    bgColor=checkerColor;
                    ch='▄';
                }
                if(x==0||x==screen[y].length-1){
                    bgColor=outlineColor;
                    ch=' ';
                }

                screen[y][x] = new Pixel(ch, (y==0||y==screen.length-1)?outlineColor:checkerColor, bgColor);
            }
        }
    }

    @Override
    public int getOrder() { return -100; }
}
