package ui.rendering;

import ui.Config;

import java.util.Arrays;

public class Sprite {
    public static class Builder{
        public Pixel[][] pixels;
        private Builder(Pixel[][] pixels){
            this.pixels = pixels;
        }
        public static Builder fromStr(String string, boolean removeSpaces){
            if(string==null||string.isEmpty()) return Sprite.Builder.fromDims(0,0);
            var lines = string.split("\n");
            return new Builder(Arrays.stream(lines).map(line->
                    Arrays.stream(line.split("")).map(chr->
                                    new Pixel(removeSpaces&&chr.equals(" ")?
                                            (char)0:
                                            chr.charAt(0), null, null))
                            .toList().toArray(new Pixel[0]))
                    .toList().toArray(new Pixel[0][0]));
        }
        public static Builder fromStr(String string){ return fromStr(string, false); }
        public static Builder fromSprite(Sprite sprite){
            Pixel[][] bSprite = new Pixel[sprite.pixels.length][sprite.pixels[0].length];
            for(int y=0;y<sprite.pixels.length;y++)
                for(int x=0;x<sprite.pixels[y].length;x++)
                    bSprite[y][x]=sprite.pixels[y][x].copy();
            return new Builder(bSprite);
        }
        public static Builder fromBuilder(Builder builder){
            Pixel[][] bSprite = new Pixel[builder.pixels.length][builder.pixels[0].length];
            for(int y=0;y<builder.pixels.length;y++)
                for(int x=0;x<builder.pixels[y].length;x++)
                    bSprite[y][x]=builder.pixels[y][x].copy();
            return new Builder(bSprite);
        }
        public static Builder fromDims(int w, int h){
            Pixel[][] pixels = new Pixel[h][w];
            for(int y=0;y<h;y++)
                for(int x=0;x<w;x++)
                    pixels[y][x]=new Pixel((char)0, null);
            return new Builder(pixels);
        }
        public Builder withFGColor(Color color){
            for(var row : this.pixels)
                for(var pixel : row)
                    pixel.fg=color;
            return this;
        }
        public Builder withBGColor(Color color){
            for(var row : this.pixels)
                for(var pixel : row)
                    pixel.bg=color;
            return this;
        }
        public Iterable<Pixel> getPixels(){
            return Arrays.stream(this.pixels).flatMap(Arrays::stream).toList();
        }

        public Sprite build(){
            return new Sprite(this.pixels);
        }
    }
    public final Pixel[][] pixels;
    public Sprite(Pixel[][] pixels){
        this.pixels = pixels;
    }
    public void draw(int xPos, int yPos, Pixel[][] screen){
        for(int y = 0; y<this.pixels.length; y++){
            for(int x = 0; x<this.pixels[y].length; x++){
                Renderable.overlayPixel(xPos+x, yPos+y, this.pixels[y][x], screen);
            }
        }
    }

    public static final Sprite NULL = new Sprite(new Pixel[0][0]);
}
