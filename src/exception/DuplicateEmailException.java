package exception;

/** Thrown when someone registers with an email that already exists. */
public class DuplicateEmailException extends Exception {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
