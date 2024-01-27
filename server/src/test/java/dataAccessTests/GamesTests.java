package dataAccessTests;

import chess.ChessGame;
import dataAccess.DataAccessException;
import dataAccess.GamesDataAccess;
import org.junit.jupiter.api.*;
import services.AuthService;

public class GamesTests {
    @BeforeEach
    public void setup() throws DataAccessException{
        GamesDataAccess.clear();
    }
    @AfterAll
    public static void finish() throws DataAccessException{
        GamesDataAccess.clear();
    }

    @Test @DisplayName("Create Game")
    public void createGame() throws DataAccessException{
        GamesDataAccess.createGame(1, "name");
    }
    @Test @DisplayName("Create Bad Game")
    public void createBadGame() throws DataAccessException{
        GamesDataAccess.createGame(1, "name");
        Assertions.assertThrows(DataAccessException.class,()->
                GamesDataAccess.createGame(1, "name"),
                "Created game with duplicate id");
    }

    @Test @DisplayName("Join Game")
    public void joinGame() throws DataAccessException{
        GamesDataAccess.createGame(1, "name");
        GamesDataAccess.joinGame(1, ChessGame.TeamColor.WHITE, "username");
    }
    @Test @DisplayName("Join Bad Game")
    public void joinBadGame() throws DataAccessException{
        Assertions.assertThrows(DataAccessException.class,()->
                GamesDataAccess.joinGame(1, ChessGame.TeamColor.WHITE, "username"),
                "Joined game that doesn't exist");

        GamesDataAccess.createGame(1, "name");
        Assertions.assertThrows(DataAccessException.class,()->
                        GamesDataAccess.joinGame(1, null, "username"),
                "Joined game with invalid color");
        Assertions.assertThrows(DataAccessException.class,()->
                        GamesDataAccess.joinGame(1, ChessGame.TeamColor.WHITE, ""),
                "Joined game with invalid username");
    }

    @Test @DisplayName("Get Game")
    public void getGame() throws DataAccessException{
        GamesDataAccess.createGame(1, "name");
        Assertions.assertNotNull(GamesDataAccess.getGame(1),
                "Could not get game with valid id");
    }
    @Test @DisplayName("Get Bad Game")
    public void getBadGame() throws DataAccessException{
        Assertions.assertNull(GamesDataAccess.getGame(1),
                "Retrieved game with invalid id");
    }

    @Test @DisplayName("Get Games")
    public void getGames() throws DataAccessException{
        Assertions.assertEquals(GamesDataAccess.getGames().length,0,
                "Games returned but no games should exist");

        GamesDataAccess.createGame(1, "name");
        GamesDataAccess.createGame(2, "name");
        GamesDataAccess.createGame(3, "name");

        Assertions.assertEquals(GamesDataAccess.getGames().length,3,
                "Three games exist, but not 3 returned");
    }

    @Test @DisplayName("Clear")
    public void clear() throws DataAccessException{
        GamesDataAccess.createGame(1, "name");
        GamesDataAccess.clear();
        Assertions.assertEquals(GamesDataAccess.getGames().length,0,
                "Games returned when no games should exist");
    }
}
