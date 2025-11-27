package websocketfacade;

import jakarta.websocket.*;

import java.net.URI;

// WsFacade needs to set up container and session and send commands to the WsServer
public class WebsocketFacade extends Endpoint {
    Session session;

    public WebsocketFacade(Integer port)  {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            session = container.connectToServer(this, new URI("ws://localhost:" + port + "/ws"));
            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String msg) {
                    System.out.println("Recieved: " + msg);
                }
            });
        } catch (Exception ex) {
            System.out.println("Unable to start websocket: " + ex.getMessage());
        }
    }

    public void send(String message) throws Exception {
        session.getBasicRemote().sendText(message);
    }


    // needs to be used but not necessarily implemented
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
