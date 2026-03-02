package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;   
import dataaccess.DataAccess; 
import model.UserData;

import model.AuthData;
import model.LoginRequest;    

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*; 

public class UserServiceTests {

    private DataAccess dataAccess;
    private UserService userService;


    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MemoryDataAccess();  

        userService = new UserService(dataAccess);
    }


    @Test 
    void clearPositive() throws  DataAccessException  {  
        userService.register(new UserData("u", "p", "e@mail.com"));   
        dataAccess.clear();  

        assertNull(dataAccess.getUser("u"));   
    }


    @Test
    void registerPositive() throws DataAccessException {
        AuthData result = userService.register(new UserData("RhysIsTheFreakingGoat", "pass123", "RhysIsTheFreakingGoat@mail.com"));
        assertNotNull(result);
        assertEquals("RhysIsTheFreakingGoat", result.username());

        assertNotNull(result.authToken());  
        assertFalse(result.authToken().isEmpty()); 
    }


    @Test
    void registerNegativeAlreadyTaken() throws DataAccessException   {

        userService.register(new UserData("alice", "secret", "alice@mail.com")); 
        assertThrows(DataAccessException.class, () ->
            userService.register(new UserData("alice", "other", "a2@mail.com")));   

    }


    @Test
    void registerNegativeBadRequestNull() {
        assertThrows(DataAccessException.class, () ->
            userService.register(new UserData(null, "p", "e@m.com")));   

        assertThrows(DataAccessException.class, () ->
            userService.register(new UserData("u", null, "e@m.com")));
 
        assertThrows(DataAccessException.class, () ->
            userService.register(new UserData("u", "p", null)));
    }


    @Test
    void registerNegativeBadRequestEmpty() {
        assertThrows(DataAccessException.class, () ->
            userService.register(new UserData("", "p", "e@m.com"))); 
    }




    @Test
    void loginPositive() throws DataAccessException {
        userService.register(new UserData("loguser", "mypass", "log@mail.com"));
        AuthData result = userService.login(new LoginRequest("loguser", "mypass")); //login the user
        assertNotNull(result);
        assertEquals("loguser", result.username());
        assertNotNull(result.authToken());
    }
   
     

    @Test
    void loginNegativeWrongPassword() throws DataAccessException {
        userService.register(new UserData("u2", "right", "u2@mail.com"));
        assertThrows(DataAccessException.class, () ->
            userService.login(new LoginRequest("u2", "wrong")));
    }


    @Test
    void loginNegativeUserNotFound() {
        assertThrows(DataAccessException.class, () ->
            userService.login(new LoginRequest("nobody", "any")));
    }


    @Test
    void loginNegativeBadRequest() {
        assertThrows(DataAccessException.class, () ->
            userService.login(new LoginRequest(null, "p")));
        assertThrows(DataAccessException.class, () ->
            userService.login(new LoginRequest("u", null)));

        assertThrows(DataAccessException.class, () ->
            userService.login(new LoginRequest("", "p")));
    }


    @Test
    void logoutPositive() throws DataAccessException {
        AuthData reg = userService.register(new UserData("logoutUser", "pw", "l@mail.com"));
        AuthData login = userService.login(new LoginRequest("logoutUser", "pw"));

        String token = login.authToken();
        userService.logout(token);

        assertNull(((MemoryDataAccess)dataAccess).getAuth(token));
    }


    @Test
    void logoutNegativeUnauthorized() {
        assertThrows(DataAccessException.class, () ->
            userService.logout("totally_fake_token"));
    }
}
