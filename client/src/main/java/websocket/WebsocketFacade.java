package websocket;

import chess.ChessGame;
import client.ServerMessageHandler;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.net.URI;

// WsFacade needs to set up container and session and send commands to the WsServer
public class WebsocketFacade extends Endpoint {
    Session session;
    ServerMessageHandler serverMessageHandler;
    ChessGame game;

    public WebsocketFacade(Integer port, ServerMessageHandler handler)  {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        serverMessageHandler = handler;
        try {
            session = container.connectToServer(this, new URI("ws://localhost:" + port + "/ws"));
            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String msg) {
                    var serializer = new Gson();
                    ServerMessage serverMessage = serializer.fromJson(msg, ServerMessage.class);
                    switch (serverMessage.getServerMessageType()) {
                        case LOAD_GAME:
                            var loadGameMessage = serializer.fromJson(msg, LoadGameMessage.class);
                            var game = loadGameMessage.game;
                            serverMessageHandler.loadGame(game);
                            break;
                        case NOTIFICATION:
                            var notificationMessage = serializer.fromJson(msg, NotificationMessage.class);
                            var message = notificationMessage.message;
                            serverMessageHandler.notify(message);
                            break;
                    }
                }
            });
        } catch (Exception ex) {
            System.out.println("Unable to start websocket: " + ex.getMessage());
        }
    }

    public void send(UserGameCommand command) throws Exception {
        String commandString = new Gson().toJson(command);
        session.getBasicRemote().sendText(commandString);
    }


    // needs to be used but not necessarily implemented
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
