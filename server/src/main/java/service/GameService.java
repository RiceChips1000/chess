package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;

import model.GameData;

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
}

