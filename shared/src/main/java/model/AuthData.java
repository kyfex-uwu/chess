package model;

public record AuthData(String authToken, String username) implements Data {
    public boolean isValid() {
        return Data.isValid(authToken, username);
    }
}
