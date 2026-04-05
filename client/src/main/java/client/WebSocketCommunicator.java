package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import jakarta.websocket.*;


import java.io.IOException;
import java.net.URI;

public class WebSocketCommunicator extends Endpoint {

    private Session session;

    private final Gson gson = new Gson();
    private ServerMessageObserver messageObserver;

    public interface ServerMessageObserver    {

        void notify(ServerMessage message);
    }

    public WebSocketCommunicator(String url, ServerMessageObserver observer) throws Exception {
        this.messageObserver = observer;

        url = url.replace("http", "ws");

        URI socketURI = new URI(url + "/ws");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, socketURI);


        this.session.addMessageHandler(new MessageHandler.Whole<String>()       {

            @Override
            public void onMessage(String message) {
                ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                messageObserver.notify(serverMessage);
            }

        });
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }

    public void sendCommand(UserGameCommand command) throws IOException {

        this.session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
