package model;

public record LoginData(String username, String password) implements Data {
    public boolean isValid() {
        return Data.isValid(username, password);
    }
}
