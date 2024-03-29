package ui.rendering.renderable;

import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Sprite;

import java.util.AbstractMap;
import java.util.Map;

public class PFPMaker {
    private static final Map<Character, Color> colorChars = Map.ofEntries(
            new AbstractMap.SimpleImmutableEntry<>('0', new Color(0,0,0)),
            new AbstractMap.SimpleImmutableEntry<>('1', new Color(81,81,81)),
            new AbstractMap.SimpleImmutableEntry<>('2', new Color(140,140,140)),
            new AbstractMap.SimpleImmutableEntry<>('3', new Color(202,202,202)),
            new AbstractMap.SimpleImmutableEntry<>('4', new Color(226,226,226)),
            new AbstractMap.SimpleImmutableEntry<>('5', new Color(255,255,255)),

            new AbstractMap.SimpleImmutableEntry<>('a', new Color(125,18,18)),
            new AbstractMap.SimpleImmutableEntry<>('g', new Color(196,43,19)),
            new AbstractMap.SimpleImmutableEntry<>('m', new Color(244,58,20)),
            new AbstractMap.SimpleImmutableEntry<>('s', new Color(255,174,167)),

            new AbstractMap.SimpleImmutableEntry<>('b', new Color(94,61,20)),
            new AbstractMap.SimpleImmutableEntry<>('h', new Color(192,123,37)),
            new AbstractMap.SimpleImmutableEntry<>('n', new Color(249,160,49)),
            new AbstractMap.SimpleImmutableEntry<>('t', new Color(255,222,172)),

            new AbstractMap.SimpleImmutableEntry<>('c', new Color(102,100,16)),
            new AbstractMap.SimpleImmutableEntry<>('i', new Color(188,185,74)),
            new AbstractMap.SimpleImmutableEntry<>('o', new Color(240,237,102)),
            new AbstractMap.SimpleImmutableEntry<>('u', new Color(255,255,190)),

            new AbstractMap.SimpleImmutableEntry<>('d', new Color(0,113,13)),
            new AbstractMap.SimpleImmutableEntry<>('j', new Color(9,183,30)),
            new AbstractMap.SimpleImmutableEntry<>('p', new Color(17,229,41)),
            new AbstractMap.SimpleImmutableEntry<>('v', new Color(167,255,170)),

            new AbstractMap.SimpleImmutableEntry<>('e', new Color(37,31,155)),
            new AbstractMap.SimpleImmutableEntry<>('k', new Color(29,126,197)),
            new AbstractMap.SimpleImmutableEntry<>('q', new Color(17,170,229)),
            new AbstractMap.SimpleImmutableEntry<>('w', new Color(167,229,255)),

            new AbstractMap.SimpleImmutableEntry<>('f', new Color(88,16,127)),
            new AbstractMap.SimpleImmutableEntry<>('l', new Color(147,66,192)),
            new AbstractMap.SimpleImmutableEntry<>('r', new Color(185,90,236)),
            new AbstractMap.SimpleImmutableEntry<>('x', new Color(239,185,255))
    );
    public static Sprite pfpToSprite(String pfpString){
        if(pfpString.length()!=54) return Sprite.NULL;

        var toReturn = Sprite.Builder.fromDims(6,3);
        for(int y=0;y<3;y++){
            for(int x=0;x<6;x++){
                toReturn.pixels[y][x]=new Pixel(pfpString.charAt(y*6+x),
                        colorChars.get(pfpString.charAt(18+y*6+x)),
                        colorChars.get(pfpString.charAt(36+y*6+x)));
            }
        }

        return toReturn.build();
    }
}
