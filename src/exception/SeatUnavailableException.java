package exception;

/** Thrown when a customer tries to book a seat that is already taken (or does not exist). */
public class SeatUnavailableException extends Exception {
    public SeatUnavailableException(String message) {
        super(message);
    }
}
