package model;

public record UserData(String username, String password, String email) implements Data {
    public boolean matchesLoginData(LoginData data){
        return data.username().equals(this.username)&&
                data.password().equals(this.password);
    }

    public boolean isValid(){
        return Data.isValid(username, password, email);
    }
}
