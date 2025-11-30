package websocket.commands;

public class ResignCommand extends UserGameCommand {
    public String username;
    public String teamColor;

    public ResignCommand(String username, String authToken, Integer gameID, String teamColor) {
        super(CommandType.RESIGN, authToken, gameID);
        this.username = username;
        this.teamColor = teamColor;
    }
}
