package model;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

public record UserData(String username, String password, String email, String pfp) implements Data {
    private record Color(int r, int g, int b){}
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
    private static String pfpGenerator(){
        boolean[] pixels = new boolean[6*3];
        for(int i=0;i<5;i++) pixels[(int) (Math.random()*18)]=true;
        for(int i=0;i<10;i++){
            var pos = (int) (Math.random()*18);
            if((pos>0&&pixels[pos-1])||
                    (pos<17&&pixels[pos+1])||
                    (pos>2&&pixels[pos-3])||
                    (pos<15&&pixels[pos+3]))
                pixels[pos]=true;
        }

        var charMap = new char[3][6];
        if(Math.random()<0.5){
            for(int i=0;i<18;i+=2){
                char toPlace=' ';
                if(pixels[i])
                    if(pixels[i+1]) toPlace='█'; else toPlace='▀';
                else
                if(pixels[i+1]) toPlace='▄'; else toPlace=' ';

                charMap[i/6][i%3]=toPlace;
                charMap[i/6][5-i%3]=toPlace;
            }
        }else{
            for(int i=0;i<6;i++){
                char toPlace=' ';
                if(pixels[i])
                    if(pixels[i+6]) toPlace='█'; else toPlace='▀';
                else
                if(pixels[i+6]) toPlace='▄'; else toPlace=' ';

                charMap[0][i%6]=toPlace;
                charMap[2][i%6]=switch(toPlace){
                    case '▄' -> '▀';
                    case '▀' -> '▄';
                    default -> toPlace;
                };
            }
            for(int i=0;i<6;i++){
                char toPlace=' ';
                if(pixels[i+12]) toPlace='█';

                charMap[1][i%6]=toPlace;
            }
        }

        var toReturn = "";
        for(int y=0;y<3;y++)
            for(int x=0;x<6;x++)
                toReturn+=charMap[y][x];
        return toReturn+
                String.valueOf(((char)('m'+(int)(Math.random()*6)))).repeat(18)+
                String.valueOf(((char)('a'+(int)(Math.random()*6)))).repeat(18);
    }

    public UserData(String username, String password, String email){
        this(username, password, email, pfpGenerator());
    }
    public static final String usernameRegex = "[A-Za-z0-9-_]{3,16}";
    public static final String passwordRegex = ".{8,256}";
    public boolean isValid(){
        return Data.isValid(username, password, email)&&
                username.matches(usernameRegex)&&
                password.matches(passwordRegex)&&
                pfp.length()==54;
    }

    //--

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserData userData = (UserData) o;
        return Objects.equals(username, userData.username) && Objects.equals(password, userData.password) && Objects.equals(email, userData.email);
    }
    @Override
    public int hashCode() {
        return Objects.hash(username, password, email);
    }
}
