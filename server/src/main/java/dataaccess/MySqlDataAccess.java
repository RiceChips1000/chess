package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.AuthData;

import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import java.util.List;

public class MySqlDataAccess implements DataAccess {

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    public MySqlDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        createTables();
    }

    private void createTables() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {    

            try (Statement st = conn.createStatement()) {  

                st.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS users (  
                            username VARCHAR(256) NOT NULL,

                             password VARCHAR(256) NOT NULL,  
                            email VARCHAR(256) NOT NULL,


                            PRIMARY KEY (username)
                        )
                        """);


                st.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS auth (
                            authToken VARCHAR(256) NOT NULL,
                            username VARCHAR(256) NOT NULL,
                            PRIMARY KEY (authToken),
                            INDEX (username),
                            FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE

                        )
                        """);



                st.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS games (       
                            gameID INT NOT NULL AUTO_INCREMENT,
                            whiteUsername VARCHAR(256),     
                            blackUsername VARCHAR(256), 
                            gameName VARCHAR(256) NOT NULL,
                            gameState TEXT NOT NULL, 
                            PRIMARY KEY (gameID)
                        )
                        """);
            }

        } catch (SQLException ex) {
            throw new DataAccessException("failed to create tables", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0")) {
                ps.executeUpdate();
            }
            try (var ps1 = conn.prepareStatement("TRUNCATE TABLE auth")) {
                ps1.executeUpdate();
            }
            try (var ps2 = conn.prepareStatement("TRUNCATE TABLE games")) {
                ps2.executeUpdate();
            }
            try (var ps3 = conn.prepareStatement("TRUNCATE TABLE users")) {
                ps3.executeUpdate();
            }
            try (var ps = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 1")) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) { 
            throw new DataAccessException("failed to clear", ex);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

        var sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)"; 

        try (var conn = DatabaseManager.getConnection(); 
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.username()); 
            ps.setString(2, user.password());  
            ps.setString(3, user.email()); 
            ps.executeUpdate(); 
        } catch (SQLException ex) {
            if (isDuplicateKey(ex)) {
                throw new DataAccessException("user already exists");
            }
            throw new DataAccessException("failed to create user", ex);     
        }
    }



    @Override
    public UserData getUser(String username) throws DataAccessException {
        var sql = "SELECT username, password, email FROM users WHERE username = ?";  
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UserData(rs.getString("username"),
                        rs.getString("password"),

                        rs.getString("email"));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get user", ex);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        var sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        // then try catch  should work now
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, auth.authToken());

            ps.setString(2, auth.username());
            ps.executeUpdate();
        } catch (SQLException ex) {

            throw new DataAccessException("failed to create auth", ex);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var sql = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AuthData(rs.getString("authToken"), rs.getString("username"));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get auth", ex);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var sql = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete auth", ex);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        var sql = "SELECT gameID, whiteUsername, blackUsername, gameName, gameState FROM games";
        try (var conn = DatabaseManager.getConnection();  
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            List<GameData> list = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("gameID");
                String white = rs.getString("whiteUsername");
                String black = rs.getString("blackUsername");

                String name = rs.getString("gameName");

                ChessGame game = gson.fromJson(rs.getString("gameState"), ChessGame.class);
                list.add(new GameData(id, white, black, name, game));
            }
            return list;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to list games", ex);
        }
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        var sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, gameState) VALUES (?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());

            ps.setString(3, game.gameName());


            ps.setString(4, gson.toJson(game.game()));
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new DataAccessException("failed to create game");
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create game", ex);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var sql = "SELECT gameID, whiteUsername, blackUsername, gameName, gameState FROM games WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();

             var ps = conn.prepareStatement(sql)) {  
             ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {   
                if (!rs.next()) {
                    return null;
                }

                int id = rs.getInt("gameID");
                String white = rs.getString("whiteUsername");
                String black = rs.getString("blackUsername");

                String name = rs.getString("gameName");
                ChessGame game = gson.fromJson(rs.getString("gameState"), ChessGame.class);

                return new GameData(id, white, black, name, game);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get game", ex);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, gameState = ? WHERE gameID = ?";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());


            ps.setString(4, gson.toJson(game.game()));
            ps.setInt(5, game.gameID());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                // just throw for the bad req
                throw new DataAccessException("bad request"); 
            }
        } catch (SQLException ex) {

            throw new DataAccessException("failed to update game", ex);
        }
    }

    private boolean isDuplicateKey(SQLException ex) {
        return ex.getErrorCode() == 1062;
    }
}

