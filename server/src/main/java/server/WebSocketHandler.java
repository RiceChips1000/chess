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

    private void handleMakeMove(Session session, MakeMoveCommand command) throws IOException {


        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());


            if (auth == null)     {
                sendError(session, "Error: unauthorized");
                return;
            }

            GameData gameData = dataAccess.getGame(command.getGameID());

            if (gameData == null) {
                sendError(session, "Error: game not found");
                return;
            }

            ChessGame game = gameData.game();

            String username = auth.username();

            if (game.getTeamTurn() == null) {
                sendError(session, "Error: game is already over");
                return;

            }

            ChessGame.TeamColor playerColor = null;
            if (username.equals(gameData.whiteUsername())) {
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (username.equals(gameData.blackUsername())) {

                playerColor = ChessGame.TeamColor.BLACK;
            }

            if (playerColor == null) {

                sendError(session, "Error: you are an observer, you can't make moves");
                return;
            }

            if (game.getTeamTurn() != playerColor) {

                sendError(session, "Error: it is not your turn");
                return;
            }

            try {

                game.makeMove(command.getMove());
            } catch (chess.InvalidMoveException e) {
                sendError(session, "Error: invalid move - " + e.getMessage());
                return;
            }

            GameData updatedGame = new GameData(gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), game);
            dataAccess.updateGame(updatedGame);

            ServerMessage loadGame =    new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(game);

            connectionManager.broadcastToAll(command.getGameID(), gson.toJson(loadGame));

            var move = command.getMove();
            String moveDesc = posToString(move.getStartPosition()) + " to " + posToString(move.getEndPosition());
            ServerMessage moveNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            moveNotification.setMessage(username + " moved " + moveDesc);
            connectionManager.broadcast(command.getGameID(), session, gson.toJson(moveNotification));

            ChessGame.TeamColor opponent = (playerColor == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            String opponentName = (opponent == ChessGame.TeamColor.WHITE)
                    ? gameData.whiteUsername() : gameData.blackUsername();

            if (game.isInCheckmate(opponent)) {
                ServerMessage cm = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                cm.setMessage(opponentName + " is in checkmate! " + username + " wins!");
                connectionManager.broadcastToAll(command.getGameID(), gson.toJson(cm));
                game.setTeamTurn(null);

                dataAccess.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(),
                        gameData.blackUsername(), gameData.gameName(), game));
            } else if (game.isInStalemate(opponent)) {

                ServerMessage sm = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                sm.setMessage("Stalemate! The game is a draw.");
                connectionManager.broadcastToAll(command.getGameID(), gson.toJson(sm));
                game.setTeamTurn(null);

                dataAccess.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(),
                        gameData.blackUsername(), gameData.gameName(), game));
            } else if (game.isInCheck(opponent)) {
                ServerMessage chk = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                chk.setMessage(opponentName + " is in check!");
                connectionManager.broadcastToAll(command.getGameID(), gson.toJson(chk));

                }

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void handleLeave(Session session, UserGameCommand command) throws IOException      {

        try {

            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {

                sendError(session, "Error: unauthorized");
                return;
            }



            GameData gameData = dataAccess.getGame(command.getGameID());
            String username = auth.username();


            if (gameData != null) {

                String white = gameData.whiteUsername();

                String black = gameData.blackUsername();
                if (username.equals(white)) {
                    white = null;
                } else if (username.equals(black)) {
                    black = null;
                }
                GameData updated = new GameData(gameData.gameID(), white, black,
                        gameData.gameName(), gameData.game());

                dataAccess.updateGame(updated);
            }

            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(username + " left the game");
            connectionManager.broadcast(command.getGameID(), session, gson.toJson(notification));

            connectionManager.remove(session);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void handleResign(Session session, UserGameCommand command) throws IOException {


        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());

            if (auth == null) {
                sendError(session, "Error: unauthorized");
                return;
            }

            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null)      {
                sendError(session, "Error: game not found");
                return;

            }

            String username = auth.username();
            ChessGame game = gameData.game();


            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                sendError(session, "Error: observers cannot resign");

                return;
            }

            if (game.getTeamTurn() == null) {

                sendError(session, "Error: game is already over");
                return;
            }


            game.setTeamTurn(null);

            GameData updated = new GameData(gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), game);
            dataAccess.updateGame(updated);

            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(username + " resigned. Game over.");

            connectionManager.broadcastToAll(command.getGameID(), gson.toJson(notification));

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }


    private String posToString(chess.ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        return "" + col + pos.getRow();
    }


    private void sendError(Session session, String errorMsg) throws IOException {
        ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);

        error.setErrorMessage(errorMsg);

        session.getRemote().sendString(gson.toJson(error));
    }
}
