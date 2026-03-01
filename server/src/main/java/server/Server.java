package server;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.DataAccess;
import service.UserService;
import model.UserData;
import model.AuthData;
import java.util.Map;

public class Server {

    private final Javalin httpHandler;
    private final DataAccess dataAccess;
    private final UserService userService;


    public Server() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);

        httpHandler = Javalin.create(config -> config.staticFiles.add("web"));

        httpHandler.post("/user", this::handleRegister);
        httpHandler.delete("/db", this::handleClear);
    }


    private void handleRegister(Context ctx) {
        Gson gson = new Gson();
        UserData req = gson.fromJson(ctx.body(), UserData.class);

        try {
            AuthData result = userService.register(req);
            ctx.status(200);
            ctx.result(gson.toJson(result));
        }
        catch (DataAccessException e) {
            if (e.getMessage().equals("bad request")) {
                ctx.status(400);
                ctx.result(gson.toJson(Map.of("message", "Error: bad request")));
            } else if (e.getMessage().equals("already taken")) {
                ctx.status(403);
                ctx.result(gson.toJson(Map.of("message", "Error: already taken")));
            }
            else {
                ctx.status(500);
                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            }
        }
    }

    private void handleClear(Context ctx) {
        try {
            dataAccess.clear();
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            ctx.status(500);
            Gson gson = new Gson();
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }



    public int run(int port) {
        httpHandler.start(port);
        return httpHandler.port();
    }

    public void stop() {
        httpHandler.stop();
    }
}
