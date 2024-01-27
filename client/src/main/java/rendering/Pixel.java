package rendering;

public class Pixel{
    public char character;
    public Renderable.CLColor fg;
    public Renderable.CLColor bg;
    public Pixel(char character, Renderable.CLColor fg, Renderable.CLColor bg){
        this.character=character;
        this.fg=fg;
        this.bg=bg;
    }
    public Pixel(char character, Renderable.CLColor color){
        this(character, color, color);
    }
    public Pixel(char character){
        this(character, Renderable.CLColor.CLEAR);
    }
    public int fgInt(){ return this.fg.fg; }
    public int bgInt(){ return this.bg.bg; }
    public Pixel clearColor(){ this.fg=null; this.bg=null; return this; }
    public Pixel copy(){
        return new Pixel(this.character, this.fg, this.bg);
    }
}