package clientTests;

import chess.Json;
import model.AuthData;
import model.JoinGameData;
import model.LoginData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import ui.Online;
import ui.PlayData;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class ServerFacadeTests {
    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        Online.changeBaseUrl("http://localhost:"+port+"/");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void beforeEach(){
        Online.request(Online.ReqMethod.DELETE, "db", new Online.EmptyData());
        PlayData.currAuth = null;
    }

    public static void getToken(){
        Online.request(Online.ReqMethod.POST, "user",
                        new UserData("username","password","email@email.com"), AuthData.class)
                .ifError(a->{
                    Assertions.fail("could not register");
                });
        Online.request(Online.ReqMethod.POST, "session",
                        new LoginData("username","password"), AuthData.class)
                .ifError(a->Assertions.fail("could not log in"))
                .ifSuccess(authData -> {
                    PlayData.currAuth = authData;
                });
    }

    @Test @DisplayName("Register")
    public void register(){
        Online.request(Online.ReqMethod.POST, "user",
                        new UserData("username","password","email@email.com"), AuthData.class)
                .ifSuccess(authData -> {
                    Assertions.assertTrue(true);
                }).
                ifError(error -> {
                    Assertions.fail();
                });
    }
    @Test @DisplayName("Bad Register")
    public void badRegister(){
        Online.request(Online.ReqMethod.POST, "user",
                        new UserData("username","","email@email.com"), AuthData.class)
                .ifSuccess(authData -> {
                    Assertions.fail();
                }).
                ifError(error -> {
                    Assertions.assertTrue(true);
                });
    }

    @Test @DisplayName("Login")
    public void login(){
        Online.request(Online.ReqMethod.POST, "user",
                        new UserData("username","password","email@email.com"), AuthData.class)
                .ifError(error -> {
                    Assertions.fail("Register doesnt work");
                });

        Online.request(Online.ReqMethod.POST, "session",
                        new LoginData("username","password"), AuthData.class)
                .ifSuccess(authData -> {
                    Assertions.assertTrue(true);
                }).
                ifError(error -> {
                    Assertions.fail();
                });
    }
    @Test @DisplayName("Bad Login")
    public void badLogin(){
        Online.request(Online.ReqMethod.POST, "session",
                        new LoginData("username","password"), AuthData.class)
                .ifSuccess(authData -> {
                    Assertions.fail();
                }).
                ifError(error -> {
                    Assertions.assertTrue(true);
                });
    }

    @Test @DisplayName("Log Out")
    public void logout(){
        getToken();

        Online.request(Online.ReqMethod.DELETE, "session", new Online.EmptyData(), AuthData.class)
                .ifSuccess(authData -> {
                    Assertions.assertTrue(true);
                }).
                ifError(error -> {
                    Assertions.fail();
                });
    }
    @Test @DisplayName("Bad Log Out")
    public void badLogout(){
        Online.request(Online.ReqMethod.DELETE, "session", new Online.EmptyData(), AuthData.class)
                .ifSuccess(authData -> {
                    Assertions.fail();
                }).
                ifError(error -> {
                    Assertions.assertTrue(true);
                });
        PlayData.currAuth = new AuthData("bad token", "username");
        Online.request(Online.ReqMethod.DELETE, "session", new Online.EmptyData(), AuthData.class)
                .ifSuccess(authData -> {
                    Assertions.fail();
                }).
                ifError(error -> {
                    Assertions.assertTrue(true);
                });
    }

    @Test @DisplayName("List Games")
    public void listGames(){
        getToken();
        Online.request(Online.ReqMethod.GET, "game", (String)null)
                .ifSuccess(authData -> {
                    Assertions.assertTrue(true);
                }).
                ifError(error -> {
                    Assertions.fail();
                });
    }
    @Test @DisplayName("Bad List Games")
    public void badListGames(){
        Online.request(Online.ReqMethod.GET, "game", (String)null)
                .ifSuccess(authData -> {
                    Assertions.fail();
                }).
                ifError(error -> {
                    Assertions.assertTrue(true);
                });
    }

    @Test @DisplayName("Create Game")
    public void createGame(){
        getToken();
        Online.request(Online.ReqMethod.POST, "game", "{\"gameName\":\"name\"}")
                .ifSuccess(authData -> {
                    Assertions.assertTrue(true);
                }).
                ifError(error -> {
                    Assertions.fail();
                });
    }
    @Test @DisplayName("Create Bad Game")
    public void createBadGame(){
        Online.request(Online.ReqMethod.POST, "game", "{\"gameName\":\"name\"}")
                .ifSuccess(authData -> {
                    Assertions.fail();
                }).
                ifError(error -> {
                    Assertions.assertTrue(true);
                });
        getToken();
        Online.request(Online.ReqMethod.POST, "game", new Online.EmptyData())
                .ifSuccess(authData -> {
                    Assertions.fail();
                }).
                ifError(error -> {
                    Assertions.assertTrue(true);
                });
    }

    @Test @DisplayName("Join Game")
    public void joinGame(){
        getToken();
        Online.request(Online.ReqMethod.POST, "game", "{\"gameName\":\"name\"}")
                .ifSuccess(data -> {
                    Online.request(Online.ReqMethod.PUT, "game", new JoinGameData("WHITE",
                                    ((Double) Json.GSON.fromJson(data, Map.class).get("gameID")).intValue()))
                            .ifSuccess(authData -> {
                                Assertions.assertTrue(true);
                            }).
                            ifError(error -> {
                                Assertions.fail();
                            });
                }).
                ifError(error -> {
                    Assertions.fail("could not create game");
                });


    }
    @Test @DisplayName("Join Bad Game")
    public void joinBadGame(){
        Online.request(Online.ReqMethod.POST, "game", "{\"gameName\":\"name\"}")
                .ifSuccess(data -> {
                    Assertions.fail();
                }).
                ifError(error -> {
                    Assertions.assertTrue(true);
                });
        getToken();
        Online.request(Online.ReqMethod.POST, "game", new Online.EmptyData())
                .ifSuccess(data -> {
                    Assertions.fail();
                }).
                ifError(error -> {
                    Assertions.assertTrue(true);
                });
    }
}
