package exception;

/** Thrown when the email/password is wrong or the role does not match. */
public class InvalidLoginException extends Exception {
    public InvalidLoginException(String message) {
        super(message);
    }
}
