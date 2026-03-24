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

    public AuthData login(String username, String password) throws Exception {
        var path = "/session";

        var request = new LoginRequest(username, password);
        return makeRequest("POST", path, request, AuthData.class, null);
    }
    public void logout(String authToken) throws Exception    {
        var path = "/session";
        makeRequest("DELETE", path, null, null, authToken);

    }

    public CreateGameResult createGame(String authToken, String gameName) throws Exception    {
        var path = "/game";
        var request = new CreateGameRequest(gameName);

        return makeRequest("POST", path, request, CreateGameResult.class, authToken);
    }

    public void clear() throws Exception {
        var path = "/db";

        makeRequest("DELETE", path, null, null, null);
    }


    public GameData[] listGames(String authToken) throws Exception {
        var path = "/game";
        var result = makeRequest("GET", path, null, ListGamesResult.class, authToken);
        return result.games();


    }


    public void joinGame(String authToken, String playerColor, int gameID) throws Exception {
        var path = "/game";

        var request = new JoinGameRequest(playerColor, gameID);

        makeRequest("PUT", path, request, null, authToken);
    }


    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken)
            throws Exception {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);

            if (authToken != null) {

                http.addRequestProperty("authorization", authToken);
            }
            if (request != null)  {
                http.setDoOutput(true);
                //should fix because Im not supposed to send the body I have the calls the  set doOutput
                writeBody(request, http);
            }
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);

        } catch (Exception ex) {
            throw ex;
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);

            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws Exception {
        var status = http.getResponseCode();

        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw new Exception(new String(respErr.readAllBytes()));
                }
            }
            throw new Exception("failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;

        if (http.getContentLength() != 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);

                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }

        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}