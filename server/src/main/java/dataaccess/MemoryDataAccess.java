package dataaccess;

import model.UserData;
import model.AuthData;
import model.GameData;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;



public class MemoryDataAccess implements DataAccess {

    private HashMap<String, UserData> users = new HashMap<>();

    private HashMap<String, AuthData> auths = new HashMap<>();  
    private HashMap<Integer, GameData> games = new HashMap<>();  


    @Override
    public void clear() throws DataAccessException {
        users.clear();
        auths.clear();
        games.clear();
        System.out.println("Cleared all d ata");
    }   

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {

            throw new DataAccessException("user already  exists");     

        }
        users.put(user.username(), user);   
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {

        
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        //should work now hopefully
        auths.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {  
        auths.remove(authToken);  
    }


    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

}
