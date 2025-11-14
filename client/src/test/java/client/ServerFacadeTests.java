package client;

import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;
import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    // can i share the exceptions between client and server so I don't have to differentiate in tests?
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
    public void clear() throws Exception {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    /// Register works if there is a change in authToken state
    @Test
    public void registerSuccessful() throws Exception {
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        assertEquals("", facade.getAuthToken());
        facade.register(registerJsonBody);
        assertNotEquals("", facade.getAuthToken());
    }


    /// AlreadyTakenException should be thrown if you attempt to register with an existing username
    @Test
    public void registerUsernameTaken() throws Exception {
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJsonBody);
        facade.logout();
        assertThrows(datamodel.AlreadyTakenException.class, () -> facade.register(registerJsonBody));
    }

    /// Register, logout, then log back in. authToken should change states throughout
    @Test
    public void loginSuccessful() throws Exception {
        assertEquals("", facade.getAuthToken());
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJsonBody);
        assertNotEquals("", facade.getAuthToken());
        facade.logout();
        assertEquals("", facade.getAuthToken());
        var loginJsonBody = "{\"username\": \"joe\", \"password\": \"pass\"}";
        facade.login(loginJsonBody);
        assertNotEquals("", facade.getAuthToken());
    }

    /// UnauthorizedException should be thrown when you attempt to log in with an incorrect password
    @Test
    public void loginBadPassword() throws Exception {
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJsonBody);
        facade.logout();
        var loginJsonBody = "{\"username\": \"joe\", \"password\": \"wrongPass\"}";
        assertThrows(datamodel.UnauthorizedException.class, () -> facade.login(loginJsonBody));
    }

    /// UnauthorizedException should be thrown when you attempt to log in with an incorrect username
    @Test
    public void loginBadUsername() throws Exception {
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJsonBody);
        facade.logout();
        var loginJsonBody = "{\"username\": \"wrongJoe\", \"password\": \"pass\"}";
        assertThrows(datamodel.UnauthorizedException.class, () -> facade.login(loginJsonBody));
    }

    /// Register, then log out. authToken should change states
    @Test
    public void logoutSuccessful() throws Exception {
        assertEquals("", facade.getAuthToken());
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJsonBody);
        assertNotEquals("", facade.getAuthToken());
        facade.logout();
        assertEquals("", facade.getAuthToken());
    }

    /// UnauthorizedException should be thrown when you attempt to logout without being logged in
    @Test
    public void logoutBadAuth() throws Exception {
        assertThrows(datamodel.UnauthorizedException.class, () -> facade.logout());
    }

    @Test
    public void createGameSuccessful() throws Exception {
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJsonBody);
        var createJsonBody = "{\"gameName\": \"myGame\"}";
        assertDoesNotThrow(() -> facade.create(createJsonBody));
    }

    @Test
    public void createGameNotEnoughArgs() throws Exception {
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJsonBody);
        var createJsonBody = "{}";
        assertThrows(datamodel.BadRequestException.class, () -> facade.create(createJsonBody));
    }

    @Test
    public void listGamesSuccessful() throws Exception {
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJsonBody);
        assertThrows(Exception.class, () -> facade.list());
        var createJsonBody = "{\"gameName\": \"myGame\"}";
        facade.create(createJsonBody);
        assertDoesNotThrow(() -> facade.list());
    }

    @Test
    public void listGamesBadAuth() throws Exception {
        // listing without having registered. Shouldn't ever happen in the real thing
        facade.setAuthToken("badToken");
        assertThrows(datamodel.UnauthorizedException.class, () -> facade.list());
    }

    @Test
    public void playGameSuccessful() throws Exception {
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJsonBody);
        var createJsonBody = "{\"gameName\": \"myGame\"}";
        facade.create(createJsonBody);
        var playJsonBody = "{\"gameID\": \"1\", \"playerColor\": \"WHITE\"}";
        assertDoesNotThrow(() -> facade.play(playJsonBody));
    }


    /// Register, create a game, and join it. Registera another player and attempt to join, but it should throw
    /// an error since the team has been taken
    @Test
    public void joinGameTeamTaken() throws Exception {
        var registerJsonBody = "{\"username\": \"joe\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJsonBody);
        var createJsonBody = "{\"gameName\": \"myGame\"}";
        facade.create(createJsonBody);
        var playJsonBody = "{\"gameID\": \"1\", \"playerColor\": \"WHITE\"}";
        facade.play(playJsonBody);

        var registerJaneJsonBody = "{\"username\": \"jane\", \"email\": \"email\", \"password\": \"pass\"}";
        facade.register(registerJaneJsonBody);
        assertThrows(datamodel.AlreadyTakenException.class, () -> facade.play(playJsonBody));
    }
}
