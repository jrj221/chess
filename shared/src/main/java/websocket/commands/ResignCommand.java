package websocket.commands;

import chess.ChessGame;

public class ResignCommand extends UserGameCommand {
    public String username;
    public ChessGame.TeamColor enemyColor;

    public ResignCommand(String username, String authToken, Integer gameID, String teamColor) {
        super(CommandType.RESIGN, authToken, gameID);
        this.username = username;
        this.enemyColor = teamColor.equals("WHITE") ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
    }
}
