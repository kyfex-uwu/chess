package serviceTests;

import dataAccess.DataAccessException;
import model.LoginData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import server.Server;
import services.AuthService;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTests {
    @BeforeEach @AfterAll
    public static void setup() throws DataAccessException{
        AuthService.clear();
    }

    private static void registerUser(String username, String password, String email) throws Exception {
        assertTrue(AuthService.registerUser(new UserData(username, password, email)), "Could not register user");
    }

    @Test @DisplayName("Register")
    public void register() throws Exception{
        assertFalse(AuthService.login(new LoginData("username", "password")).isPresent(),
                "Got a token even though login info should not exist yet");
        registerUser("username", "password", "email");
        assertTrue(AuthService.login(new LoginData("username", "password")).isPresent(),
                "No token given for valid login credentials");
    }
    @Test @DisplayName("Bad Register")
    public void badRegister() throws Exception{
        registerUser("username", "password", "email");
        assertFalse(AuthService.registerUser(new UserData("username", "password2", "email2")),
                "Registered user with duplicate username");

        assertThrows(Server.InvalidRequestException.class,
                ()->AuthService.registerUser(new UserData(null, "password2", "email2")),
                "Registered user with bad data");
    }

    @Test @DisplayName("Login")
    public void login() throws Exception{
        registerUser("username", "password", "email");

        var token=AuthService.login(new LoginData("username", "password"));
        assertTrue(token.isPresent(), "No token given for valid login credentials");

        var token2=AuthService.login(new LoginData("username", "password"));
        assertTrue(token2.isPresent(), "No token given for valid login credentials");
        assertNotEquals(token.get(), token2.get(), "Tokens returned were the same");
    }
    @Test @DisplayName("Bad Login")
    public void badLogin() throws Exception{
        assertThrows(Server.InvalidRequestException.class,
                ()->AuthService.login(new LoginData(null, "password2")),
                "Logged in user with bad data");

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
    @Test @DisplayName("Validate Token")
    public void validateToken() throws Exception{
        registerUser("username", "password", "email");

        var token = AuthService.login(new LoginData("username", "password"));
        assertTrue(token.isPresent(), "No token given for valid login credentials");
        assertTrue(AuthService.validateToken(token.get()), "Token could not be validated");
    }
    @Test @DisplayName("Validate Bad Token")
    public void validateBadToken() throws Exception{
        assertFalse(AuthService.validateToken("a"), "Invalid token was validated");

        registerUser("username", "password", "email");
        var token = AuthService.login(new LoginData("username", "password"));
        assertTrue(token.isPresent(), "No token given for valid login credentials");

        AuthService.logout(token.get());
        assertFalse(AuthService.validateToken(token.get()),"Validated expired token");
    }

    @Test @DisplayName("Logout")
    public void logout() throws Exception{
        registerUser("username", "password", "email");

        var token = AuthService.login(new LoginData("username", "password"));
        assertTrue(token.isPresent(), "No token given for valid login credentials");

        AuthService.logout(token.get());
        assertFalse(AuthService.validateToken(token.get()), "Validated expired token");
    }

    @Test @DisplayName("Get User From Token")
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
    @Test @DisplayName("Get User From Bad Token")
    public void getUserFromBadToken() throws Exception{
        assertNull(AuthService.getUserFromToken("a"), "User retrieved from bad token");
    }

    @Test @DisplayName("Clear")
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
