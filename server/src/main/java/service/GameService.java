package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;

import model.GameData;

import chess.ChessGame;

import java.util.List;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public List<GameData> listGames(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty())    {

            throw new DataAccessException("unauthorized");

        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {

            throw new DataAccessException("unauthorized");
        }

        return dataAccess.listGames();
    }


    public int createGame(String authToken, String gameName) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("unauthorized");
        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("bad request");
        }

        ChessGame game = new ChessGame();
        GameData data = new GameData(null, null, null, gameName, game);
        return dataAccess.createGame(data);
    }


    public void joinGame(String authToken, String color, Integer gameID) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {

            throw new DataAccessException("unauthorized" );
        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null)   {

            throw new DataAccessException("unauthorized");
        }

        if (gameID == null) {
            throw new DataAccessException("bad request");

        }

        if (color == null || color.isEmpty()) {
            throw new DataAccessException("bad request");
        }



        String upper = color.toUpperCase();
        if (!upper.equals("WHITE") && !upper.equals("BLACK")) {
               throw new DataAccessException("bad request");
        }

        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("bad request");
        }

        String username = auth.username();

        if (upper.equals("WHITE")) {
            if (game.whiteUsername() != null) {

                    throw new DataAccessException("already taken");
            }
            GameData updated = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
            dataAccess.updateGame(updated);
        } else {
            if (game.blackUsername() != null) {
                throw new DataAccessException("already taken");

            }
            GameData updated = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
            dataAccess.updateGame(updated);
        }
    }
}
