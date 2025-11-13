import serverfacade.ServerFacade;

public class PostLoginClient {

    public void printPrompt() {
        System.out.print("[LOGGED_IN] >>> ");
    }

    static ServerFacade facade = new ServerFacade(8080);

    public static void eval(String command) throws Exception {
        var inputWords = command.toLowerCase().split(" ");
        switch (inputWords[0]) {
            case "clear": { // FOR TESTING ONLY, REMOVE BEFORE COMPLETION
                facade.clear();
                break;
            } case "help": {
                System.out.println("""
                        \tcreate <GAME_NAME> | Create a new game
                        \tlist | List existing games
                        \tjoin <ID> <WHITE|BLACK> | Join a game
                        \tobserve <ID> | Observe a game
                        \tlogout | Logout when you are done
                        \tquit | Quit the chess program
                        \thelp | See possible commands""");
                break;
            } case "quit": {
                facade.logout();
                System.exit(0); // best way to exit?
            }  case "logout": {
                facade.logout();
                break;
            } case "create": {
                facade.create(inputWords);
                break;
            } case "list": {
                facade.list();
                break;
            } case "play": {
                facade.join(inputWords);
                break;
            } case "observe": {
                facade.observe(inputWords);
                break;
            }
        }
    }
}
