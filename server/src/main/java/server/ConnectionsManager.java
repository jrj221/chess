package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.NotificationMessage;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsManager {
    private final ConcurrentHashMap<Session, Session> connections = new ConcurrentHashMap<>();

    public void add(Session session) {
        connections.put(session, session);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(Session sender, NotificationMessage notificationMessage) throws Exception{
        var notificationMessageString = new Gson().toJson(notificationMessage);
        for (Session session : connections.values()) {
            // petshop checks to see if the session is open first, not sure what that means
            if (session != sender) {
                session.getRemote().sendString(notificationMessageString);
            }
        }
    }
}
