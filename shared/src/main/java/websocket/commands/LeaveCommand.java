package websocket.commands;

public class LeaveCommand extends UserGameCommand {
    public String username;

    public LeaveCommand(String username, String authToken, Integer gameID) {
        super(CommandType.LEAVE, authToken, gameID);
        this.username = username;
    }
}
