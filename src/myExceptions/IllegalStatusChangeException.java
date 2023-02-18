package myExceptions;

public class IllegalStatusChangeException extends Exception {

    public IllegalStatusChangeException() {
    }

    public IllegalStatusChangeException(String message) {
        super(message);
    }
}

