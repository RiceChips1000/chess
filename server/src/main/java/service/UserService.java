package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;

import model.AuthData;

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

        dataAccess.createUser(req);
    
        String token = UUID.randomUUID().toString();  
        AuthData authData = new AuthData(token, req.username());  
        //then make the authdta
        dataAccess.createAuth(authData);  

        return authData;  




    }
}
