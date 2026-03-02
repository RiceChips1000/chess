package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;


import dataaccess.MemoryDataAccess;
import model.UserData;
import model.AuthData;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {

    private DataAccess dataAccess;
    private UserService userService;
    private GameService gameService;


    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
    }


    @Test
    void listGamesPositiveEmpty() throws DataAccessException {
        AuthData reg = userService.register(new UserData("listGuy", "pw", "l@l.com"));
        //if this doesnt list ima tweak fr
        var result = gameService.listGames(reg.authToken());
        assertNotNull(result);

        assertEquals(0, result.size());
    }


    @Test
    void listGamesNegativeUnauthorized() {
        assertThrows(DataAccessException.class, () ->
            gameService.listGames("badTokenThing"));
    }


    @Test
    void createGamePositive() throws DataAccessException {
        AuthData reg = userService.register(new UserData("maker", "pw", "m@m.com"));

        int id = gameService.createGame(reg.authToken(), "my cool game made by the goat");
        assertTrue(id > 0);
    }



    @Test
    void createGameNegativeBadRequest() throws DataAccessException {
        AuthData reg = userService.register(new UserData("maker2", "pw", "m2@m.com"));
        assertThrows(DataAccessException.class, () ->
            gameService.createGame(reg.authToken(), null));

    }


    @Test
    void joinGamePositive() throws DataAccessException {
        AuthData reg = userService.register(new UserData("joiner", "pw", "j@j.com"));

        int id = gameService.createGame(reg.authToken(), "joinable");

        gameService.joinGame(reg.authToken(), "WHITE", id);
        var games = gameService.listGames(reg.authToken());

        assertEquals(1, games.size());
        assertEquals("joiner", games.get(0).whiteUsername());
    }


    @Test
    void joinGameNegativeUnauthorized() throws DataAccessException {
      //tests for neg. unathorized
        AuthData reg = userService.register(new UserData("joiner2", "pw", "j2@j.com"));
        int id = gameService.createGame(reg.authToken(), "joinable2");

        assertThrows(DataAccessException.class, () ->
            gameService.joinGame("bad token lol", "WHITE", id));
    }


    @Test
    void joinGameNegativeBadColor() throws DataAccessException {
        AuthData reg = userService.register(new UserData("joiner3", "pw", "j3@j.com"));
        int id = gameService.createGame(reg.authToken(), "joinable3");

        assertThrows(DataAccessException.class, () ->

            gameService.joinGame(reg.authToken(), null, id));
        assertThrows(DataAccessException.class, () ->

            gameService.joinGame(reg.authToken(), "", id));
        assertThrows(DataAccessException.class, () ->

            gameService.joinGame(reg.authToken(), "GREEN", id));
    }


    @Test
    void joinGameNegativeStealColor() throws DataAccessException {
        AuthData regA = userService.register(new UserData("A", "pw", "a@a.com"));
        int id = gameService.createGame(regA.authToken(), "steal");
        gameService.joinGame(regA.authToken(), "BLACK", id);

        AuthData regB = userService.register(new UserData("B", "pw", "b@b.com"));

        assertThrows(DataAccessException.class, () ->
            gameService.joinGame(regB.authToken(), "BLACK", id));
    }


    @Test
    void joinGameNegativeBadGameId() throws DataAccessException {
        AuthData reg = userService.register(new UserData("joiner4", "pw", "j4@j.com"));
        gameService.createGame(reg.authToken(), "game4");

        assertThrows(DataAccessException.class, () ->
            gameService.joinGame(reg.authToken(), "WHITE", null));
    }
}

