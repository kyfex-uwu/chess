package ui.rendering;

public class Color {
    public final int r;
    public final int g;
    public final int b;
    public Color(int r, int g, int b){
        this.r=Math.min(Math.max(r,0),255);
        this.g=Math.min(Math.max(g,0),255);
        this.b=Math.min(Math.max(b,0),255);
    }
    public Color copy(){
        return new Color(this.r, this.g, this.b);
    }

    public boolean equals(Object other){
        if(other instanceof Color otherColor)
            return otherColor.r==this.r&&otherColor.g==this.g&&otherColor.b==this.b;
        return false;
    }
}
