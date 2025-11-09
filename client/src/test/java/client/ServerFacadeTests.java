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
        String[] inputWords = {"register", "joe", "email", "pass"};
        assertNull(facade.getAuthToken());
        facade.register(inputWords);
        assertNotNull(facade.getAuthToken());
    }

    @Test
    public void registerUsernameTaken() throws Exception {
        String[] inputWords = {"register", "joe", "email", "pass"};
        facade.register(inputWords);

        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        facade.register(inputWords); // attempt to register again
        System.setOut(originalOut); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("Username already taken, try a different one\n", out.toString());
    }

    @Test
    public void loginSuccessful() throws Exception {
        String[] inputWords = {"register", "joe", "email", "pass"};
        facade.register(inputWords);
        facade.logout();
        assertNull(facade.getAuthToken());
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        String[] loginInputWords = {"login", "joe", "pass"};
        facade.login(loginInputWords); // attempt to register again
        System.setOut(originalOut); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("Successfully logged in\n", out.toString());
        assertNotNull(facade.getAuthToken());
    }

    @Test
    public void loginBadPassword() throws Exception {
        String[] inputWords = {"register", "joe", "email", "pass"};
        facade.register(inputWords);
        facade.logout();

        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        String[] loginInputWords = {"login", "joe", "wrongPass"};
        facade.login(loginInputWords); // attempt to register again
        System.setOut(originalOut); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("Incorrect username or password\n", out.toString());
    }

    @Test
    public void loginBadUsername() throws Exception {
        String[] inputWords = {"register", "joe", "email", "pass"};
        facade.register(inputWords);
        facade.logout();

        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        String[] loginInputWords = {"login", "joeShmoe", "pass"};
        facade.login(loginInputWords); // attempt to register again
        System.setOut(originalOut); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("Incorrect username or password\n", out.toString());
    }

    @Test
    public void logoutSuccessful() throws Exception {
        String[] inputWords = {"register", "joe", "email", "pass"};
        facade.register(inputWords);
        assertNotNull(facade.getAuthToken());
        facade.logout();
        assertNull(facade.getAuthToken());
    }

    @Test
    public void logoutBadAuth() throws Exception {
        // logout without having registered. Shouldn't ever happen in the real thing
        facade.setAuthToken("badToken");
        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        facade.logout(); // attempt to register again
        System.setOut(originalOut); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("bad authToken\n", out.toString());
    }

    @Test
    public void createGameSuccessful() throws Exception {
        String[] inputWords = {"register", "joe", "email", "pass"};
        facade.register(inputWords);
        String[] createInputWords = {"create", "myGame"};
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        facade.create(createInputWords);
        System.setOut(originalOut); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("Game myGame succesfully created. Use game ID 1 to join or observe it\n", out.toString());
    }

    @Test
    public void createGameNotEnoughArgs() throws Exception {
        String[] inputWords = {"register", "joe", "email", "pass"};
        facade.register(inputWords);
        String[] createInputWords = {"create"};
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        facade.create(createInputWords);
        System.setOut(originalOut); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("Creating a game requires 1 argument: GAME_NAME\n", out.toString());
    }

    @Test
    public void listGamesSuccessful() throws Exception {
        String[] inputWords = {"register", "joe", "email", "pass"};
        facade.register(inputWords);
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        facade.list();
        System.setOut(originalOut); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("No games yet. Create one using \"create <GAME_NAME>\"\n", out.toString());
        out.reset();

        String[] createInputWords = {"create", "myGame"};
        facade.create(createInputWords);

        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        facade.list();
        System.setOut(originalOut); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("""
                1.
                \tGame Name: myGame
                \tGame ID: 1
                \tWhite Player: No player
                \tBlack Player: No player
                """, out.toString());
    }

    @Test
    public void listGames() throws Exception {
        // listing without having registered. Shouldn't ever happen in the real thing
        facade.setAuthToken("badToken");
        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // buffers stream that captures stdout
        System.setOut(new PrintStream(out)); // redirects stdout to my buffer
        facade.list(); // attempt to list
        System.setOut(originalOut); // MUST UNDO REDIRECTION so that output goes to console like it should
        assertEquals("bad authToken\n", out.toString());
    }
}
