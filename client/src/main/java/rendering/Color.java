package rendering;

public class Color {
    public final int r;
    public final int g;
    public final int b;
    public final int num;
    public Color(int r, int g, int b){
        this.r=Math.min(Math.max(r,0),5);
        this.g=Math.min(Math.max(g,0),5);
        this.b=Math.min(Math.max(b,0),5);

        this.num=16+this.r*36+this.g*6+this.b;
    }
    public Color(int val){
        this.r=0;
        this.g=0;
        this.b=0;
        this.num=232+Math.min(Math.max(val, 0),23);
    }
}
