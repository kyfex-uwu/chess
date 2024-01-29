package ui.rendering;

public class Pixel{
    public char character;
    public Color fg;
    public Color bg;
    public Pixel(char character, Color fg, Color bg){
        this.character=character;
        this.fg=fg;
        this.bg=bg;
    }
    public Pixel(char character, Color color){
        this(character, color, color);
    }
    public Pixel(char character){
        this(character, null);
    }
    public Pixel clearColor(){ this.fg=null; this.bg=null; return this; }
    public Pixel copy(){
        return new Pixel(this.character, this.fg, this.bg);
    }
}