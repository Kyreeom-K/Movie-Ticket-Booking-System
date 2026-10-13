package exception;

/** Thrown when user input is invalid (empty fields, negative price, bad date, etc.). */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
