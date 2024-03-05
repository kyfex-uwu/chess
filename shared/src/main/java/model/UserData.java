package model;

import java.util.Objects;

public record UserData(String username, String password, String email, String pfp, int achievements) implements Data {
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

    public UserData(String username, String password, String email, String pfp) {
        this(username, password, email, pfp, 0);
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
                pfp!=null&&pfp.length()==54;
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
