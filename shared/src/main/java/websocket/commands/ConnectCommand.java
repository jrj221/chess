package websocket.commands;

public class ConnectCommand extends UserGameCommand {
    public String username;
    public String teamColor;
    public String state;

    public ConnectCommand(String username, String authToken, Integer gameID, String teamColor, String state) {
        super(CommandType.CONNECT, authToken, gameID);
        this.username = username;
        this.teamColor = teamColor;
        this.state = state;
    }
}
