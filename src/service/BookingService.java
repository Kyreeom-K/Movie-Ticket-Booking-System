package service;

import dao.BookingDAO;
import dao.PaymentDAO;
import dao.SeatDAO;
import dao.ShowDAO;
import exception.BookingException;
import exception.SeatUnavailableException;
import exception.ValidationException;
import model.Booking;
import model.Payment;
import model.Seat;
import model.Show;
import model.User;
import util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer-side business logic: browse shows, book seats, cancel, history.
 * bookSeats() and cancelBooking() use TRANSACTIONS so either everything succeeds
 * or nothing changes.
 */
public class BookingService {

    private static final int MAX_SEATS_PER_BOOKING = 10;

    private final ShowDAO showDAO = new ShowDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    public List<Show> getUpcomingShows() throws SQLException {
        return showDAO.getUpcomingShows();
    }

    public List<Seat> getSeatMap(int showId) throws SQLException {
        return seatDAO.getSeatsByShow(showId);
    }

    public List<Booking> getHistory(int userId) throws SQLException {
        return bookingDAO.getBookingsByUser(userId);
    }

    /**
     * Books the given seats in ONE transaction:
     *   1. lock the seat rows (SELECT ... FOR UPDATE)
     *   2. make sure none is already booked  -> else SeatUnavailableException
     *   3. insert booking + booking_seats, mark seats BOOKED, insert payment
     *   4. commit  (any error -> rollback, nothing is saved)
     */
    public Booking bookSeats(User customer, Show show, List<Integer> seatIds, String paymentMode)
            throws SQLException, SeatUnavailableException, ValidationException {

        if (seatIds == null || seatIds.isEmpty()) throw new ValidationException("Please select at least one seat.");
        if (seatIds.size() > MAX_SEATS_PER_BOOKING) throw new ValidationException("You can book at most " + MAX_SEATS_PER_BOOKING + " seats at a time.");
        if (show.getShowDateTime().isBefore(LocalDateTime.now())) throw new ValidationException("This show has already started.");

        Connection con = null;
        boolean committed = false;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);                       // BEGIN TRANSACTION

            // Step 1 + 2: lock and check
            List<Seat> seats = seatDAO.lockSeats(con, show.getShowId(), seatIds);
            if (seats.size() != seatIds.size()) {
                throw new SeatUnavailableException("Some selected seats do not exist for this show.");
            }
            for (Seat seat : seats) {
                if (seat.isBooked()) {
                    throw new SeatUnavailableException("Seat " + seat.getLabel()
                            + " was just booked by someone else. Please pick another seat.");
                }
            }

            // Step 3: write everything
            int bookingId = bookingDAO.insertBooking(con, customer.getUserId(), show.getShowId());
            double total = 0;
            for (Seat seat : seats) {
                double price = seat.getPrice(show);         // polymorphism: Regular vs Premium price
                total += price;                             // auto-calculated total
                bookingDAO.insertBookingSeat(con, bookingId, seat.getSeatId(), price);
            }
            seatDAO.markStatus(con, seatIds, "BOOKED");

            Payment payment = new Payment();
            payment.setBookingId(bookingId);
            payment.setAmount(total);
            payment.setPaymentMode(paymentMode);
            payment.setPaymentStatus("SUCCESS");            // payment gateway is simulated
            paymentDAO.insertPayment(con, payment);

            con.commit();                                   // Step 4: COMMIT
            committed = true;
            return bookingDAO.getBookingById(bookingId);    // full booking for the ticket

        } finally {
            closeQuietly(con, committed);
        }
    }

    /** Cancels a booking: status -> CANCELLED, seats -> AVAILABLE, payment -> REFUNDED. */
    public void cancelBooking(int bookingId, int userId) throws SQLException, BookingException {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null || booking.getUserId() != userId) {
            throw new BookingException("Booking not found.");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BookingException("This booking is already cancelled.");
        }
        if (booking.getShow().getShowDateTime().isBefore(LocalDateTime.now())) {
            throw new BookingException("You cannot cancel a booking for a show that has already started.");
        }

        Connection con = null;
        boolean committed = false;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Only succeeds if the booking is still CONFIRMED (guards against double cancel)
            if (!bookingDAO.updateStatus(con, bookingId, "CONFIRMED", "CANCELLED")) {
                throw new BookingException("This booking is already cancelled.");
            }
            seatDAO.releaseSeatsOfBooking(con, bookingId);
            paymentDAO.updateStatus(con, bookingId, "REFUNDED");

            con.commit();
            committed = true;
        } finally {
            closeQuietly(con, committed);
        }
    }

    // Rolls back if we did not commit, then closes the connection
    private void closeQuietly(Connection con, boolean committed) {
        if (con == null) return;
        try {
            if (!committed) con.rollback();
            con.close();
        } catch (SQLException ignored) {
            // nothing more we can do
        }
    }
}
