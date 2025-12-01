package websocket.commands;

import chess.*;

public class MakeMoveCommand extends UserGameCommand {
    public ChessMove move;
    public String username;
    public String moveString;
    public ChessGame.TeamColor teamColor;
    public ChessGame.TeamColor enemyColor;


    public MakeMoveCommand(ChessMove move, String moveString, String username,
                           String authToken, Integer gameID, String teamColor) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
        this.moveString = moveString;
        this.username = username;
        this.enemyColor = teamColor.equals("WHITE") ? ChessGame.TeamColor.BLACK :  ChessGame.TeamColor.WHITE;
        this.teamColor = teamColor.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
    }
}
