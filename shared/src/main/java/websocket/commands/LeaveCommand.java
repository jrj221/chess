package websocket.commands;

public class LeaveCommand extends UserGameCommand {
    public String username;
    public String teamColor;

    public LeaveCommand(String username, String authToken, Integer gameID, String teamColor) {
        super(CommandType.LEAVE, authToken, gameID);
        this.username = username;
        this.teamColor = teamColor;
    }
}
