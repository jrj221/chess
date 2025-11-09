package client;

import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
        facade.clear();
    }

    @BeforeEach
    public void clear() throws Exception {facade.clear();}

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerSuccessful() throws Exception {
        String[] InputWords = {"register", "joe", "email", "pass"};
        assertNull(facade.getAuthToken());
        facade.register(InputWords);
        assertNotNull(facade.getAuthToken());
    }

    @Test
    public void registerUsernameTaken() throws Exception {
        String[] InputWords = {"register", "joe", "email", "pass"};
        facade.register(InputWords);

        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        facade.register(InputWords); // attempt to register again
        System.setOut(System.out); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("Username already taken, try a different one\n", out.toString());
    }

    @Test
    public void loginSuccessful() throws Exception {
        String[] InputWords = {"register", "joe", "email", "pass"};
        facade.register(InputWords);
        facade.logout();
        assertNull(facade.getAuthToken());
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        String[] loginInputWords = {"login", "joe", "pass"};
        facade.login(loginInputWords); // attempt to register again
        System.setOut(System.out); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("Successfully logged in\n", out.toString());
        assertNotNull(facade.getAuthToken());
    }

    @Test
    public void login() throws Exception {
        String[] InputWords = {"register", "joe", "email", "pass"};
        facade.register(InputWords);
        facade.logout();

        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        String[] loginInputWords = {"login", "joe", "wrongPass"};
        facade.login(loginInputWords); // attempt to register again
        System.setOut(System.out); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("Incorrect username or password\n", out.toString());
    }

}
