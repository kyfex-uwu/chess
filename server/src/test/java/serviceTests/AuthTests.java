package serviceTests;

import dataAccess.DataAccessException;
import model.LoginData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import services.AuthService;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTests {
    @BeforeEach
    public void setup() {
        AuthService.clear();
    }

    private static void registerUser(String username, String password, String email) throws DataAccessException {
        assertTrue(AuthService.registerUser(new UserData(username, password, email)), "Could not register user");
    }

    //.registerUser should only be called after LoginData#isValid returns true
    @Test @Order(1) @DisplayName("Register")
    public void register() throws Exception{
        assertFalse(AuthService.login(new LoginData("username", "password")).isPresent(),
                "Got a token even though login info should not exist yet");
        registerUser("username", "password", "email");
        assertTrue(AuthService.login(new LoginData("username", "password")).isPresent(),
                "No token given for valid login credentials");
    }
    @Test @Order(2) @DisplayName("Bad Register")
    public void badRegister() throws Exception{
        registerUser("username", "password", "email");
        assertFalse(AuthService.registerUser(new UserData("username", "password2", "email2")),
                "Registered user with duplicate username");
    }

    //.login should only be called after UserData#isValid returns true
    @Test @Order(3) @DisplayName("Login")
    public void login() throws Exception{
        registerUser("username", "password", "email");

        var token=AuthService.login(new LoginData("username", "password"));
        assertTrue(token.isPresent(), "No token given for valid login credentials");

        var token2=AuthService.login(new LoginData("username", "password"));
        assertTrue(token2.isPresent(), "No token given for valid login credentials");
        assertNotEquals(token.get(), token2.get(), "Tokens returned were the same");
    }
    @Test @Order(4) @DisplayName("Bad Login")
    public void badLogin() throws Exception{
        assertFalse(AuthService.login(new LoginData("username", "password")).isPresent(),
                "Got a token, even though login info hasn't been registered");

        registerUser("username", "password", "email");

        assertFalse(AuthService.login(new LoginData("username", "wrongPassword")).isPresent(),
                "Got a token even though the wrong password was used");
        assertFalse(AuthService.login(new LoginData("wrongUsername", "password")).isPresent(),
                "Got a token even though the wrong username was used");
        assertFalse(AuthService.login(new LoginData("wrongUsername", "wrongPassword")).isPresent(),
                "Got a token even though the wrong credentials were used");
    }

    //token cannot be null or empty string
    @Test @Order(5) @DisplayName("Validate Token")
    public void validateToken() throws Exception{
        registerUser("username", "password", "email");

        var token = AuthService.login(new LoginData("username", "password"));
        assertTrue(token.isPresent(), "No token given for valid login credentials");
        assertTrue(AuthService.validateToken(token.get()), "Token could not be validated");
    }
    @Test @Order(6) @DisplayName("Validate Bad Token")
    public void validateBadToken() throws Exception{
        assertFalse(AuthService.validateToken("a"), "Invalid token was validated");

        registerUser("username", "password", "email");
        var token = AuthService.login(new LoginData("username", "password"));
        assertTrue(token.isPresent(), "No token given for valid login credentials");

        AuthService.logout(token.get());
        assertFalse(AuthService.validateToken(token.get()),"Validated expired token");
    }

    @Test @Order(7) @DisplayName("Logout")
    public void logout() throws Exception{
        registerUser("username", "password", "email");

        var token = AuthService.login(new LoginData("username", "password"));
        assertTrue(token.isPresent(), "No token given for valid login credentials");

        AuthService.logout(token.get());
        assertFalse(AuthService.validateToken(token.get()), "Validated expired token");
    }

    @Test @Order(8) @DisplayName("Get User From Token")
    public void getUserFromToken() throws Exception{
        registerUser("username", "password", "email");

        var token = AuthService.login(new LoginData("username", "password"));
        assertTrue(token.isPresent(), "No token given for valid login credentials");

        var userData = AuthService.getUserFromToken(token.get());
        assertTrue(
                userData.username().equals("username")&&
                userData.email().equals("email")&&
                new BCryptPasswordEncoder().matches("password", userData.password()),

                "User data does not match"
        );
    }
    @Test @Order(9) @DisplayName("Get User From Bad Token")
    public void getUserFromBadToken() throws Exception{
        assertNull(AuthService.getUserFromToken("a"), "User retrieved from bad token");
    }

    @Test @Order(10) @DisplayName("Clear")
    public void clear() throws Exception{
        registerUser("username","password","email");

        var token = AuthService.login(new LoginData("username","password"));
        assertTrue(token.isPresent(), "No token given for valid login credentials");

        assertNotNull(AuthService.getUserFromToken(token.get()), "No user returned from valid token");

        AuthService.clear();
        assertFalse(AuthService.validateToken(token.get()), "Token is still valid");
        assertTrue(AuthService.login(new LoginData("username", "password")).isEmpty(),
                "User still exists");
    }
}
