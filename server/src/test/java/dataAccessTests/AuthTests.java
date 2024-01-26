package dataAccessTests;

import dataAccess.AuthDataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthTests {
    @BeforeEach @AfterAll
    public static void setup() throws DataAccessException {
        AuthDataAccess.clear();
    }

    @Test @DisplayName("Create User")
    public void createUser() throws DataAccessException{
        AuthDataAccess.createUser(new UserData("username", "password", "email"));
        var userData = AuthDataAccess.getUser("username");
        Assertions.assertEquals(userData.username(), "username",
                "User data is not equal");
        Assertions.assertTrue(new BCryptPasswordEncoder().matches("password", userData.password()),
                "User data is not equal");
        Assertions.assertEquals(userData.email(), "email",
                "User data is not equal");
    }
    @Test @DisplayName("Create Bad User")
    public void createBadUser() throws DataAccessException{
        AuthDataAccess.createUser(new UserData("username", "password", "email"));
        Assertions.assertThrows(DataAccessException.class,
                ()->AuthDataAccess.createUser(new UserData("username", "password2", "email2")),
                "Created user of same username");

        Assertions.assertThrows(DataAccessException.class,
                ()->AuthDataAccess.createUser(new UserData(null, "password2", "email2")),
                "Created user with invalid data");
    }

    @Test @DisplayName("Get User")
    public void getUser() throws DataAccessException{
        AuthDataAccess.createUser(new UserData("username", "password", "email"));
        Assertions.assertNotNull(AuthDataAccess.getUser("username"),
                "Could not get valid user");
    }
    @Test @DisplayName("Get Bad User")
    public void getBadUser() throws DataAccessException{
        Assertions.assertNull(AuthDataAccess.getUser("username"),
                "Got user that doesn't exist");

        AuthDataAccess.createUser(new UserData("username", "password", "email"));
        Assertions.assertNotNull(AuthDataAccess.getUser("username"),
                "Couldn't get existing user");

        Assertions.assertThrows(DataAccessException.class, ()->
                AuthDataAccess.getUser(null),
                "Called getUser with bad argument");
    }

    @Test @DisplayName("Create Token")
    public void createToken() throws DataAccessException{
        AuthDataAccess.createUser(new UserData("username","password","email"));
        AuthDataAccess.createToken(new AuthData("token", "username"));
    }
    @Test @DisplayName("Create Bad Token")
    public void createBadToken() throws DataAccessException{
        Assertions.assertThrows(DataAccessException.class,()->
                AuthDataAccess.createToken(new AuthData(null, "username")),
                "Created token with invalid data");
    }

    @Test @DisplayName("Delete Token")
    public void deleteToken() throws DataAccessException{
        AuthDataAccess.createUser(new UserData("username","password","email"));
        AuthDataAccess.createToken(new AuthData("token", "username"));
        AuthDataAccess.deleteToken("token");
        Assertions.assertNull(AuthDataAccess.userFromToken("token"),
                "Token is valid after being deleted");
    }

    @Test @DisplayName("User From Token")
    public void userFromToken() throws DataAccessException{
        AuthDataAccess.createUser(new UserData("username","password","email"));
        AuthDataAccess.createToken(new AuthData("token", "username"));
        var userData=AuthDataAccess.userFromToken("token");
        Assertions.assertEquals(userData.username(), "username",
                "User data is not equal");
        Assertions.assertTrue(new BCryptPasswordEncoder().matches("password", userData.password()),
                "User data is not equal");
        Assertions.assertEquals(userData.email(), "email",
                "User data is not equal");
    }
    @Test @DisplayName("User From Bad Token")
    public void userFromBadToken() throws DataAccessException{
        Assertions.assertNull(AuthDataAccess.userFromToken("a"),
                "User retrieved from invalid token");
        Assertions.assertThrows(DataAccessException.class, ()->AuthDataAccess.userFromToken(null),
                "User retrieved from null token");
    }

    @Test @DisplayName("Clear")
    public void clear() throws DataAccessException{
        AuthDataAccess.createUser(new UserData("username","password","email"));
        AuthDataAccess.clear();
        Assertions.assertNull(AuthDataAccess.getUser("username"),
                "Got user after database was cleared");
    }
}
