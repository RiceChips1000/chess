package client;

import com.google.gson.Gson;
import model.*;
import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var path = "/user";
        var request = new UserData(username, password, email);
        return makeRequest("POST", path, request, AuthData.class, null);
    }
}