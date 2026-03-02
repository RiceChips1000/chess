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
}

