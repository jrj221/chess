package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;

import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService;

    public Server() {
        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", ctx -> register(ctx));
        // Register your endpoints and exception handlers here.


    }
    // start with the simplest method (functions) and later on rework into
    // classes or interfaces to see if it needs to change
    private void register(Context ctx)  {
        try {
            var serializer = new Gson();
            String requestJson = ctx.body();
            var user = serializer.fromJson(requestJson, UserData.class); // "username":"john" looks like a map so lets convert to that

            var authData = userService.register(user);
            ctx.result(serializer.toJson(authData));
        } catch (Exception ex) {
            // when userService throws an exception>
            var message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).result(message);
        }
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
