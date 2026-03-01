package server;
import io.javalin.Javalin;

import java.util.Map;

public class Server {

    private final Javalin httpHandler;


    public Server() {

        httpHandler = Javalin.create(config -> config.staticFiles.add("web"));

    }





    public Server run(int port) {
        httpHandler.start(port);
        return this;
    }
}
