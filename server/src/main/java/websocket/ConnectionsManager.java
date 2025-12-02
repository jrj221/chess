package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsManager {
    private final ConcurrentHashMap<Integer, ArrayList<Session>> connections = new ConcurrentHashMap<>();

    public void add(Session session, int gameID) {
        ArrayList<Session> sessions  = connections.get(gameID);
        if (sessions == null) {
            var list = new ArrayList<Session>();
            list.add(session);
            connections.put(gameID, list);
        } else {
            sessions.add(session);
            connections.put(gameID, sessions); // update
        }
    }

    public void remove(Session session, int gameID) {
        ArrayList<Session> sessions = connections.get(gameID);
        sessions.remove(session);
        connections.put(gameID, sessions); // update
    }

    public void broadcastNotif(Session sender, NotificationMessage notificationMessage, int gameID) throws Exception {
        var notificationMessageString = new Gson().toJson(notificationMessage);
        List<Session> sessions = connections.get(gameID);
        for (Session session : sessions) {
            if ((session != sender || sender == null) && session.isOpen()) {
                // petshop checks to see if the session is open first, not sure what that means
                session.getRemote().sendString(notificationMessageString);
            }
        }
    }

    // alternative broadcast that sends everyone (including sender) the new game board (ex. after a move is made)
    public void broadcastGame(LoadGameMessage loadGameMessage, int gameID) throws Exception {
        var loadGameMessageString = new Gson().toJson(loadGameMessage);
        List<Session> sessions = connections.get(gameID);
        for (Session session : sessions) {
            if (session.isOpen()) {
                // petshop checks to see if the session is open first, not sure what that means
                session.getRemote().sendString(loadGameMessageString);
            }
        }
    }
}
