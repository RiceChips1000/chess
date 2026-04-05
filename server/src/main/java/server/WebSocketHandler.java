package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connectionManager = new ConnectionManager();
    private final DataAccess dataAccess;


    private final Gson gson = new Gson();

    public WebSocketHandler(DataAccess dataAccess)    {
        this.dataAccess = dataAccess;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {

        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> handleConnect(session, command);
            case MAKE_MOVE -> {
                MakeMoveCommand moveCmd = gson.fromJson(message, MakeMoveCommand.class);
                handleMakeMove(session, moveCmd);
            }
            case LEAVE -> handleLeave(session, command);
            case RESIGN -> handleResign(session, command);
        }
    }

    private void handleConnect(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {

                sendError(session, "Error: unauthorized");
                return;
            }

            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Error: game not found");

                return;
            }

            connectionManager.add(auth.username(), session, command.getGameID());


            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(gameData.game());
            session.getRemote().sendString(gson.toJson(loadGame));

            String username = auth.username();

            String notificationMsg;
            if (username.equals(gameData.whiteUsername())) {
                notificationMsg = username + " joined the game as WHITE";
            } else if (username.equals(gameData.blackUsername())) {
                notificationMsg = username + " joined the game as BLACK";
            } else {
                notificationMsg = username + " is observing the game";
            }

            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(notificationMsg);

            connectionManager.broadcast(command.getGameID(), session, gson.toJson(notification));

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void sendError(Session session, String errorMsg) throws IOException {
        ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);

        error.setErrorMessage(errorMsg);
        session.getRemote().sendString(gson.toJson(error));
    }
}
