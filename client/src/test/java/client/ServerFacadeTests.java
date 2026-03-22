package client;

import model.*;
import org.junit.jupiter.api.*;

import server.Server;


import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {


    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init()  {

        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer()    {

        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {

        facade.clear();
    }

    // ===== POSITIVE TESTS =======

    @Test
    void registerPositive() throws Exception {

        var authData = facade.register("player1", "password", "p1@email.com");
        assertNotNull(authData);
        assertNotNull(authData.authToken());

        assertTrue(authData.authToken().length() > 10);
        assertEquals("player1", authData.username());
    }

    @Test
    void loginPositive() throws Exception
    {
        facade.register("player1", "password", "p1@email.com");
        var authData = facade.login("player1", "password");
        assertNotNull(authData);

        assertNotNull(authData.authToken());
        assertEquals("player1", authData.username());
    }

    @Test
    void logoutPositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");

        assertDoesNotThrow(() -> facade.logout(authData.authToken()));
    }

    @Test
    void createGamePositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");

        var result = facade.createGame(authData.authToken(), "TestGame");
        assertNotNull(result);
        assertNotNull(result.gameID());
        assertTrue(result.gameID() > 0);


    }

    @Test
    void listGamesPositive() throws Exception {


        var authData = facade.register("player1", "password", "p1@email.com");
        facade.createGame(authData.authToken(), "Game1");
        facade.createGame(authData.authToken(), "Game2");
        var games = facade.listGames(authData.authToken());
        assertNotNull(games);
        assertEquals(2, games.length);

    }

    @Test
    void joinGamePositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        var gameResult = facade.createGame(authData.authToken(), "TestGame");
        assertDoesNotThrow(() ->
                facade.joinGame(authData.authToken(), "WHITE", gameResult.gameID()));

    }

    @Test
    void clearPositive() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        assertDoesNotThrow(() -> facade.clear());
    }

    // ======  NEGATIVE TESTS =====

    @Test
    void registerNegativeDuplicate() throws Exception     {

        facade.register("player1", "password", "p1@email.com");
        assertThrows(Exception.class, () ->
                facade.register("player1", "password", "p1@email.com"));

    }

    @Test
    void loginNegativeBadPassword() {

        assertThrows(Exception.class, () ->
                facade.login("nonexistent", "wrongpassword"));
    }

    @Test
    void logoutNegativeBadToken() {
        assertThrows(Exception.class, () ->
                facade.logout("bad-auth-token"));
    }

    @Test
    void createGameNegativeBadAuth() {
        assertThrows(Exception.class, () ->
                facade.createGame("bad-auth-token", "TestGame"));
    }

    @Test
    void listGamesNegativeBadAuth() {

        assertThrows(Exception.class, () ->
                facade.listGames("bad-auth-token"));


    }

    @Test
    void joinGameNegativeBadAuth() {
        assertThrows(Exception.class, () ->

                facade.joinGame("bad-auth-token", "WHITE", 1));
    }

    @Test
    void joinGameNegativeBadGameId() throws Exception   {

        var authData = facade.register("player1", "password", "p1@email.com");
        assertThrows(Exception.class, () ->

                facade.joinGame(authData.authToken(), "WHITE", 99999));
    }

//should be good for tests
}
