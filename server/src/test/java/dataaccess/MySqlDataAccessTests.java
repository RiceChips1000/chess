package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import org.junit.jupiter.api.*;


import java.util.List;

public class MySqlDataAccessTests {

    private MySqlDataAccess dao;

    @BeforeEach
    public void setup() throws DataAccessException {  

        dao = new MySqlDataAccess();
        dao.clear();
    }

    @Test
    public void clearPositive() throws DataAccessException {
        dao.createUser(new UserData("a", "pass", "a@mail.com"));
        dao.createAuth(new AuthData("t", "a"));   
        dao.createGame(new GameData(null, null, null, "g", new ChessGame()));   

        dao.clear();

        Assertions.assertNull(dao.getUser("a"));
        Assertions.assertNull(dao.getAuth("t"));
        Assertions.assertEquals(0, dao.listGames().size());
    }

    @Test
    public void createUserPositive() throws DataAccessException {
        dao.createUser(new UserData("bob", "pw", "b@mail.com"));
        UserData found = dao.getUser("bob");

        Assertions.assertNotNull(found);
        Assertions.assertEquals("bob", found.username());
    }

    @Test
    public void createUserDuplicateNegative() throws DataAccessException {
        dao.createUser(new UserData("bob", "pw", "b@mail.com"));

        Assertions.assertThrows(DataAccessException.class,
                () -> dao.createUser(new UserData("bob", "pw2", "bb@mail.com")));
    }

    @Test
    public void getUserNegative() throws DataAccessException {
        Assertions.assertNull(dao.getUser("missing"));
    }

    @Test
    public void createAuthPositive() throws DataAccessException {
        dao.createUser(new UserData("a", "pw", "a@mail.com"));
        dao.createAuth(new AuthData("token1", "a"));


        Assertions.assertNotNull(dao.getAuth("token1"));
    }

    @Test
    public void createAuthNegative() {
        Assertions.assertThrows(DataAccessException.class,
                () -> dao.createAuth(new AuthData("token1", "missingUser"))); 
    }

    @Test
    public void getAuthNegative() throws DataAccessException {

        Assertions.assertNull(dao.getAuth("nope"));
    }


    @Test
    public void deleteAuthPositive() throws DataAccessException {
        dao.createUser(new UserData("a", "pw", "a@mail.com"));
        dao.createAuth(new AuthData("token1", "a"));

        dao.deleteAuth("token1");

        Assertions.assertNull(dao.getAuth("token1"));
    }

    @Test
    public void deleteAuthNegative() throws DataAccessException {
        dao.deleteAuth("missingToken");
        Assertions.assertNull(dao.getAuth("missingToken"));
    }



    @Test
    public void createGamePositive() throws DataAccessException {
        int id = dao.createGame(new GameData(null, null, null, "game1", new ChessGame()));

        Assertions.assertTrue(id > 0);
    }   

    @Test
    public void createGameNegative() {
        Assertions.assertThrows(DataAccessException.class,
                () -> dao.createGame(new GameData(null, null, null, null, new ChessGame())));

    }

    @Test
    public void getGameNegative() throws DataAccessException {
         
        Assertions.assertNull(dao.getGame(99));
    }

    @Test
    public void updateGamePositive() throws DataAccessException {
        ChessGame game = new ChessGame();

        int id = dao.createGame(new GameData(null, null, null, "g", game));         

        GameData created = dao.getGame(id);
        GameData updated = new GameData(created.gameID(), "w", "b", created.gameName(), created.game());

        dao.updateGame(updated);    

        GameData after = dao.getGame(id);
        Assertions.assertEquals("w", after.whiteUsername());            
        Assertions.assertEquals("b", after.blackUsername());   


    }

    @Test

    public void updateGameNegative() {
        Assertions.assertThrows(DataAccessException.class,
                () -> dao.updateGame(new GameData(999, null, null, "g", new ChessGame())));
    }

    @Test
    public void listGamesPositive() throws DataAccessException {
        dao.createGame(new GameData(null, null, null, "a", new ChessGame()));

        dao.createGame(new GameData(null, null, null, "b", new ChessGame()));

        List<GameData> list = dao.listGames();
        Assertions.assertEquals(2, list.size());
    }

    @Test
    public void listGamesNegative() throws DataAccessException {
        dao.clear();
        Assertions.assertEquals(0, dao.listGames().size());
    }

    @Test
    public void updateGameMissingGameNegative() {

        GameData missingGame = new GameData(9999, "white", "black", "missing_game", new ChessGame());
        Assertions.assertThrows(DataAccessException.class,
                () -> dao.updateGame(missingGame));

    }
}

