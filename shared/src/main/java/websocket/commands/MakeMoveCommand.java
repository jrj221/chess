package websocket.commands;

import chess.*;

public class MakeMoveCommand extends UserGameCommand {
    public ChessMove move;


    public MakeMoveCommand(ChessMove move, String authToken, Integer gameID) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
    }
}
