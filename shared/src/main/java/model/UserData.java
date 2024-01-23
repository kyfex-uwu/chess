package model;

public record UserData(String username, String password, String email) implements Data {
    public boolean isValid(){
        return Data.isValid(username, password, email);
    }
}
