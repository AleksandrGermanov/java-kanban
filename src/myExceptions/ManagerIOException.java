package myExceptions;

public class ManagerIOException extends RuntimeException {
    public ManagerIOException() {
    }

    public ManagerIOException(String message) {
        super(message);
    }

    public ManagerIOException(Throwable cause) {
        super(cause);
    }
}

