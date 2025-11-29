package websocket.commands;

public class ConnectCommand extends UserGameCommand {
    public String username;
    public String teamColor;

    public ConnectCommand(String username, String authToken, Integer gameID, String teamColor) {
        super(CommandType.CONNECT, authToken, gameID);
        this.username = username;
        this.teamColor = teamColor;
    }
}
