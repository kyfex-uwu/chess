package serviceTests;

import dataAccess.DataAccessException;
import org.junit.jupiter.api.*;
import services.AuthService;
import services.GamesService;

import static org.junit.jupiter.api.Assertions.*;

public class GamesTests {
    @BeforeEach
    public void setup() throws DataAccessException{
        GamesService.clear();
    }
    @AfterAll
    public static void finish() throws DataAccessException{
        GamesService.clear();
    }

    @Test @DisplayName("Create Game")
    public void createGame() throws Exception {
        int gameID = GamesService.createGame("name");
        assertTrue(GamesService.joinGame(gameID, "WHITE", "username").isEmpty(),
                "Server error was given");
    }
    @Test @DisplayName("Create Bad Game")
    public void createBadGame() throws Exception {
        assertThrows(Exception.class, ()->GamesService.createGame(null),
                "Created game with name null");
        assertThrows(Exception.class, ()->GamesService.createGame(""),
                "Created game with empty name");
    }

    @Test @DisplayName("Join Game")
    public void joinGame() throws Exception{
        int gameID = GamesService.createGame("name");
        assertTrue(GamesService.joinGame(gameID, "WHITE", "username").isEmpty(),
                "Returned a failing response when joining valid game as white (black is empty)");
        assertTrue(GamesService.joinGame(gameID, "BLACK", "username").isEmpty(),
                "Returned a failing response when joining valid game as black (white is filled)");
    }
    @Test @DisplayName("Join Bad Game")
    public void joinBadGame() throws Exception{
        int gameID = GamesService.createGame("name");
        assertFalse(GamesService.joinGame(gameID, "RED", "username").isEmpty(),
                "Successfully joined game as red");
        assertFalse(GamesService.joinGame(gameID, null, "username").isEmpty(),
                "Successfully joined game with null color");
        assertFalse(GamesService.joinGame(gameID==69?420:69, "BLACK", "username").isEmpty(),
                "Successfully joined game with invalid id");
        assertFalse(GamesService.joinGame(gameID, "BLACK", "").isEmpty(),
                "Successfully joined game with empty username");
        assertFalse(GamesService.joinGame(gameID, "BLACK", null).isEmpty(),
                "Successfully joined game with null username");
    }

    @Test @DisplayName("Get Games")
    public void getGames() throws Exception{
        assertEquals(0, GamesService.getGames().length,
                "Games list is not empty to start");
        GamesService.createGame("name");
        GamesService.createGame("name2");
        GamesService.createGame("name");
        assertEquals(3, GamesService.getGames().length,
                "Games List length is not 3");
    }

    @Test @DisplayName("Clear")
    public void clear() throws Exception{
        GamesService.createGame("name");
        GamesService.clear();
        assertEquals(0, GamesService.getGames().length);
    }
}
