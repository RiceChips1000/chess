package server;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;
import service.UserService;
import service.GameService;
import model.UserData;
import model.AuthData;
import model.LoginRequest;
import model.ListGamesResult;
import model.GameData;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.JoinGameRequest;
import java.util.Map;
import java.util.List;

public class Server {

    private final Javalin httpHandler;
    private final DataAccess dataAccess;
    private final UserService userService;
    private final GameService gameService;

    //fix the fallback hopefully and also catch the exception so it does the fallback and not crash the server
    public Server() {
        DataAccess built;
        try {
            built = new MySqlDataAccess();
        } catch (Exception ex)     {
            
            built = new MemoryDataAccess();
        }
        dataAccess = built;

        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);

        httpHandler = Javalin.create(config -> config.staticFiles.add("web"));

        httpHandler.post("/user", this::handleRegister);
        httpHandler.post("/session", this::handleLogin);
        httpHandler.post("/game", this::handleCreateGame);
        httpHandler.put("/game", this::handleJoinGame);
        httpHandler.delete("/session", this::handleLogout);
        httpHandler.get("/game", this::handleListGames);
        
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


    private void handleLogin(Context ctx) {
        Gson gson = new Gson();
        LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);
        try {
            AuthData result = userService.login(req);
            ctx.status(200);
            ctx.result(gson.toJson(result));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("bad request")) {
                ctx.status(400);
                ctx.result(gson.toJson(Map.of("message", "Error: bad request")));
            } else if (e.getMessage().equals("unauthorized")) {
                ctx.status(401);
                ctx.result(gson.toJson(Map.of("message", "Error: unauthorized")));
            } else {
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


    private void handleLogout(Context ctx) {
        Gson gson = new Gson();
        String authToken = ctx.header("authorization");

        try {
            userService.logout(authToken);
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            handleAuthError(ctx, gson, e);  
        }
    }


    private void handleListGames(Context ctx) {
        Gson gson = new Gson();
        String authToken = ctx.header("authorization");

        try {
            List<GameData> games = gameService.listGames(authToken);
            ctx.status(200);
            ctx.result(gson.toJson(new ListGamesResult(games.toArray(new GameData[0]))));
        } catch (DataAccessException e) {
             handleAuthError(ctx, gson, e);
        }
    }


    private void handleCreateGame(Context ctx) {
        Gson gson = new Gson();
        String authToken = ctx.header("authorization");

        CreateGameRequest req = gson.fromJson(ctx.body(), CreateGameRequest.class);


        try {
            int id = gameService.createGame(authToken, req.gameName());
            ctx.status(200);

            ctx.result(gson.toJson(new CreateGameResult(id)));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("unauthorized")) {


                ctx.status(401);
                ctx.result(gson.toJson(Map.of("message", "Error: unauthorized")));
            } else if (e.getMessage().equals("bad request"))   {

                ctx.status(400);
                ctx.result(gson.toJson(Map.of("message", "Error: bad request")));
            } else {
                ctx.status(500);

                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));

            }

        }

    }



    private void handleJoinGame(Context ctx) {
        Gson gson = new Gson();
        String authToken = ctx.header("authorization");

        JoinGameRequest req = gson.fromJson(ctx.body(), JoinGameRequest.class);

        try {
            gameService.joinGame(authToken, req.playerColor(), req.gameID());

            ctx.status(200);
            ctx.result("{}");
            //then catch ye
        } catch (DataAccessException e) {
            if (e.getMessage().equals("bad request")) {

                ctx.status(400);
                ctx.result(gson.toJson(Map.of("message", "Error: bad request")));
            } else if (e.getMessage().equals("unauthorized"))       {
                ctx.status(401);

                ctx.result(gson.toJson(Map.of("message", "Error: unauthorized")));
            } else if (e.getMessage().equals("already taken")) {
                ctx.status(403);

                ctx.result(gson.toJson(Map.of("message", "Error: already taken")));
            } else {
                ctx.status(500);

                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            }
        }
    }


    private void handleAuthError(Context ctx, Gson gson, DataAccessException e) {
        if (e.getMessage().equals("unauthorized")) {
            ctx.status(401);
            ctx.result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } else {
            
            ctx.status(500);
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
