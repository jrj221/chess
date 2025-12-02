package server;

import dataaccess.*;
import datamodel.*;
import io.javalin.*;
import service.*;
import websocket.*;


public class Server {

    private final Javalin server;
    private final WebsocketHandler websocketHandler;
    private final HttpHandler httpHandler;


    public Server() {
        var dataAccess = new SQLDataAccess();
        websocketHandler = new WebsocketHandler(dataAccess);
        httpHandler = new HttpHandler(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.post("user", httpHandler::register);
        server.post("session", httpHandler::login);
        server.delete("session", httpHandler::logout);
        server.get("game", httpHandler::listGames);
        server.post("game", httpHandler::createGame);
        server.put("game", httpHandler::joinGame);
        server.delete("db", httpHandler::clear);
        server.ws("/ws", ws -> {
            ws.onConnect(websocketHandler::onConnect);
            ws.onMessage(websocketHandler::onMessage);
            ws.onClose(websocketHandler::onClose);
        });
    }


    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
