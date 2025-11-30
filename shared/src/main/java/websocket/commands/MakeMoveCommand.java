package websocket.commands;

import chess.ChessMove;
import chess.ChessPosition;

public class MakeMoveCommand extends UserGameCommand {
    public ChessMove move;
    public String username;

    public MakeMoveCommand(ChessMove move, String username, String authToken, Integer gameID) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
        this.username = username;
    }
}
