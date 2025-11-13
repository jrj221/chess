package datamodel;

public class NoExistingGameException extends RuntimeException {
    public NoExistingGameException(String message) {
        super(message);
    }
}
