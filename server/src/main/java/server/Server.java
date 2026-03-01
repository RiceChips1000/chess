package server;
import io.javalin.Javalin;

public class Server {

    private final Javalin httpHandler;


    public Server() {

        httpHandler = Javalin.create(config -> config.staticFiles.add("web"));

    }





    public int run(int port) {
        httpHandler.start(port);
        return httpHandler.port();
    }

    public void stop() {
        httpHandler.stop();
    }
}
