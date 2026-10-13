package exception;

/** Thrown for booking/cancellation rule violations (already cancelled, show started, etc.). */
public class BookingException extends Exception {
    public BookingException(String message) {
        super(message);
    }
}
