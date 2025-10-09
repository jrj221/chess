package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;

import java.util.Map;

public class Server {

    private final Javalin server;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));
        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", ctx -> register(ctx));
        // Register your endpoints and exception handlers here.


    }

    private void register(Context ctx) {
        var serializer = new Gson();
        String requestJson = ctx.body();
        var request = serializer.fromJson(requestJson, Map.class); // "username":"john" looks like a map so lets convert to that

        // call to service and actually register
        var response = Map.of("username", request.get("username"), "authToken", "1234"); // you'd actually want to grab a real authtoken
        var responseJson = serializer.toJson(response);
        ctx.result(responseJson);

    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
