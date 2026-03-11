package dataaccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.*;

import chess.ChessGame;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MySqlDataAccessTest {


    private MySqlDataAccess dao;

    @BeforeEach
    public void setup() throws DataAccessException {
        dao = new MySqlDataAccess();

        dao.clear();
    }

    @Test
    public void clearTest() throws DataAccessException {
        dao.createUser(new UserData("u", "p", "e"));

        dao.clear();
        assertNull(dao.getUser("u"));
    }

    @Test
    public void createUserPos() throws DataAccessException {

        UserData u = new UserData("user1", "pass1", "email1");
        dao.createUser(u);
        UserData fetched = dao.getUser("user1");

        assertNotNull(fetched);
        assertEquals("user1", fetched.username());
    }

    @Test
    public void createUserNeg() throws DataAccessException {
        UserData u = new UserData("user1", "pass1", "email1");
        dao.createUser(u);
        assertThrows(DataAccessException.class, () -> dao.createUser(u));
    }



    @Test
    public void getUserPos() throws DataAccessException {
        UserData u = new UserData("u2", "p2", "e2");
        dao.createUser(u);
        UserData fetched = dao.getUser("u2");
        assertNotNull(fetched);
    }

    @Test
    public void getUserNeg() throws DataAccessException {
        UserData fetched = dao.getUser("nonexistent");
        assertNull(fetched);
    }

    @Test
    public void createAuthPos() throws DataAccessException {
        dao.createUser(new UserData("u3", "p3", "e3"));
        AuthData a = new AuthData("token1", "u3");

        dao.createAuth(a);
        AuthData fetched = dao.getAuth("token1");
        assertNotNull(fetched);

        assertEquals("token1", fetched.authToken());
    }

    @Test
    public void createAuthNeg() throws DataAccessException {
        dao.createUser(new UserData("u4", "p4", "e4"));
        AuthData a = new AuthData("token2", "u4");

        dao.createAuth(a);
        assertThrows(DataAccessException.class, () -> dao.createAuth(a));
    }

    @Test
    public void getAuthPos() throws DataAccessException {
        dao.createUser(new UserData("u5", "p5", "e5"));
        AuthData a = new AuthData("token3", "u5");
        dao.createAuth(a);

        AuthData fetched = dao.getAuth("token3");
        assertNotNull(fetched);
    }

    @Test
    public void getAuthNeg() throws DataAccessException {
        AuthData fetched = dao.getAuth("badtoken");

        assertNull(fetched);
    }

    @Test
    public void deleteAuthPos() throws DataAccessException {
        dao.createUser(new UserData("u6", "p6", "e6"));
        AuthData a = new AuthData("token4", "u6");
        dao.createAuth(a);
        dao.deleteAuth("token4");

        assertNull(dao.getAuth("token4"));
    }

    @Test
    public void deleteAuthNeg() throws DataAccessException {
        assertDoesNotThrow(() -> dao.deleteAuth("fake_token"));
    }

    @Test
    public void listGamesPos() throws DataAccessException {
        dao.createGame(new GameData(1, null, null, "game1", new ChessGame()));
        dao.createGame(new GameData(2, null, null, "game2", new ChessGame()));

        List<GameData> games = dao.listGames();
        assertEquals(2, games.size());
    }

    @Test
    public void listGamesNeg() throws DataAccessException {
        List<GameData> games = dao.listGames();
        assertEquals(0, games.size());
    }

    @Test
    public void createGamePos() throws DataAccessException    {
        int id = dao.createGame(new GameData(0, null, null, "my_game", new ChessGame()));

        assertTrue(id > 0);
    }

    @Test
    public void createGameNeg() {
        assertThrows(DataAccessException.class, () -> dao.createGame(new GameData(0, null, null, null, new ChessGame())));
    }

    @Test
    public void getGamePos() throws DataAccessException {
        int id = dao.createGame(new GameData(0, null, null, "test_game", new ChessGame()));
        GameData fetched = dao.getGame(id);

        assertNotNull(fetched);
        assertEquals(id, fetched.gameID());
    }

    @Test
    public void getGameNeg() throws DataAccessException   {
        assertNull(dao.getGame(9999));
    }

    @Test
    public void updateGamePos() throws DataAccessException {
        int id = dao.createGame(new GameData(0, null, null, "game_x", new ChessGame()));
        GameData g = dao.getGame(id);
        GameData updated = new GameData(id, "w", "b", "game_x", g.game());
        dao.updateGame(updated);

        GameData fetched = dao.getGame(id);
        assertEquals("w", fetched.whiteUsername());
        assertEquals("b", fetched.blackUsername());
    }

    @Test
    public void updateGameNeg() {
        GameData g = new GameData(9999, "w", "b", "game_bad", new ChessGame());

        assertThrows(DataAccessException.class, () -> dao.updateGame(g));
    }

    @Test
    public void deleteAuthTwiceNeg() throws DataAccessException {
        dao.createUser(new UserData("u7", "p7", "e7"));
        AuthData a = new AuthData("token5", "u7");
        dao.createAuth(a);
        
        dao.deleteAuth("token5");
        // deleting again shouldn't crash, but auth should still be null
        assertDoesNotThrow(() -> dao.deleteAuth("token5"));
        assertNull(dao.getAuth("token5"));
    }



}
