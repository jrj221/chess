import server.Server;

public class Main {

    public static void main(String[] args) {
        Server server = new Server();
        server.run(0);

        System.out.println("♕ 240 Chess Server");
    }
}