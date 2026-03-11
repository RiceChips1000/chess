package service;


import dataaccess.DataAccess;
import dataaccess.DataAccessException;


import model.UserData;

import model.AuthData;
import model.LoginRequest;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;    
    
    public UserService(DataAccess dataAccess) { 
        this.dataAccess = dataAccess; 
         
    }


    public AuthData register(UserData req) throws DataAccessException { 



        if (req.username() == null || req.password() == null || req.email() == null) {
            throw new DataAccessException("bad request");

        }


        if (req.username().isEmpty() || req.password().isEmpty() || req.email().isEmpty()) { 
            throw new DataAccessException("bad request");     
        }

        UserData existing = dataAccess.getUser(req.username());
         
         
        if (existing != null) {     
            throw new DataAccessException("already taken");     
        }

        String hashedPassword = BCrypt.hashpw(req.password(), BCrypt.gensalt());
        dataAccess.createUser(new UserData(req.username(), hashedPassword, req.email()));
    
        String token = UUID.randomUUID().toString();  
        AuthData authData = new AuthData(token, req.username());  
        //then make the authdta

        dataAccess.createAuth(authData);  

        return authData;  


    }


    public AuthData login(LoginRequest req) throws DataAccessException {

        if (req.username() == null || req.password() == null) {
            throw new DataAccessException("bad request");

        }
        

        if (req.username().isEmpty() || req.password().isEmpty()) {
            throw new DataAccessException("bad request");
        }
        UserData theUser = dataAccess.getUser(req.username());      

        if (theUser == null)    //if the user is not found
         {  
            throw new DataAccessException("unauthorized");
        }
        if (!BCrypt.checkpw(req.password(), theUser.password())) {
            
            throw new DataAccessException("unauthorized");

        }
        String token = UUID.randomUUID().toString();

        AuthData authData = new AuthData(token, req.username());

        dataAccess.createAuth(authData);
        return authData;
    }


    public void logout(String authToken) throws DataAccessException {  
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("unauthorized"); 
        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {

            throw new DataAccessException("unauthorized");
        }

        dataAccess.deleteAuth(authToken);   
    }
}
