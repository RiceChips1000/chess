package server;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Session, Connection> connections = new ConcurrentHashMap<>();

    public void add(String visitorName, Session session, int gameID) {
        var connection = new Connection(visitorName, session, gameID);

        connections.put(session, connection);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(int gameID, Session excludeSession, String message)
            throws IOException {
        var removeList = new ArrayList<Session>();

        for (var entry : connections.entrySet()) {
            var c = entry.getValue();

            if (c.gameID == gameID) {
                if (c.session.isOpen()) {
                    if (!c.session.equals(excludeSession)) {
                        c.send(message);
                    }
                } else {
                    removeList.add(entry.getKey());
                }
            }
        }
        for (var session : removeList) {
            connections.remove(session);
        }
    }

    public void broadcastToAll(int gameID, String message) throws IOException {

        broadcast(gameID, null, message);
    }
}
