package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.LoadGameMessage;
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

    public void broadcastNotif(NotificationMessage notificationMessage) throws Exception {
        var notificationMessageString = new Gson().toJson(notificationMessage);
        // do notifs need to go to the sender as well? instructions are vague
        for (Session session : connections.values()) {
            // petshop checks to see if the session is open first, not sure what that means
            session.getRemote().sendString(notificationMessageString);
        }
    }

    // alternative broadcast that sends everyone (including sender) the new game board (ex. after a move is made)
    public void broadcastGame(LoadGameMessage loadGameMessage) throws Exception {
        var loadGameMessageString = new Gson().toJson(loadGameMessage);
        for (Session session : connections.values()) {
            // petshop checks to see if the session is open first, not sure what that means
            session.getRemote().sendString(loadGameMessageString);
        }
    }
}
