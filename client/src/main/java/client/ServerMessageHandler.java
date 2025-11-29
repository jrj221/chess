package client;

import chess.ChessGame;

public interface ServerMessageHandler {
    void loadGame(ChessGame game);
    void sendError(String errorMessage);
    void notify(String message);
}
