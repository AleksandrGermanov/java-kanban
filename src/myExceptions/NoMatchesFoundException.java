package myExceptions;

public class NoMatchesFoundException extends RuntimeException {

    public NoMatchesFoundException() {
    }

    public NoMatchesFoundException(String message) {
        super(message);
    }
}
