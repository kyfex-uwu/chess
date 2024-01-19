package model;

public interface Data {
    static boolean isValid(String... strings){
        for(var string : strings)
            if(string==null||string.isEmpty()) return false;
        return true;
    }
    boolean isValid();
}
