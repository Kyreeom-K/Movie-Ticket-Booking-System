package dao;

import model.Booking;
import model.Seat;
import model.Show;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Database operations for bookings and booking_seats. */
public class BookingDAO {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    // One query that brings booking + customer + show + movie + theatre together
    private static final String BASE_SELECT =
            "SELECT b.booking_id, b.user_id, b.show_id, b.booking_time, b.status, u.full_name, "
          + "s.movie_id, s.theatre_id, s.show_date, s.show_time, s.regular_price, s.premium_price, "
          + "s.total_seats, m.title AS movie_title, t.name AS theatre_name "
          + "FROM bookings b "
          + "JOIN users u ON b.user_id = u.user_id "
          + "JOIN shows s ON b.show_id = s.show_id "
          + "JOIN movies m ON s.movie_id = m.movie_id "
          + "JOIN theatres t ON s.theatre_id = t.theatre_id ";

    /** CREATE booking row (inside the booking transaction). Returns the new booking_id. */
    public int insertBooking(Connection con, int userId, int showId) throws SQLException {
        String sql = "INSERT INTO bookings (user_id, show_id, status) VALUES (?, ?, 'CONFIRMED')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, showId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** CREATE one booking_seats row (seat + the price paid for it). */
    public void insertBookingSeat(Connection con, int bookingId, int seatId, double price) throws SQLException {
        String sql = "INSERT INTO booking_seats (booking_id, seat_id, price_paid) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, seatId);
            ps.setDouble(3, price);
            ps.executeUpdate();
        }
    }

    /**
     * UPDATE status. Only changes a booking that is currently in "fromStatus",
     * so two simultaneous cancels cannot both succeed. Returns true if a row changed.
     */
    public boolean updateStatus(Connection con, int bookingId, String fromStatus, String toStatus) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ? AND status = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, toStatus);
            ps.setInt(2, bookingId);
            ps.setString(3, fromStatus);
            return ps.executeUpdate() > 0;
        }
    }

    /** DELETE a booking row (admin clean-up only; normal flow cancels instead of deleting). */
    public boolean deleteBooking(int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    /** READ one complete booking (with seats and payment). Returns null if not found. */
    public Booking getBookingById(int bookingId) throws SQLException {
        String sql = BASE_SELECT + "WHERE b.booking_id = ?";
        Booking booking = null;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) booking = mapRow(rs);
            }
        }
        if (booking != null) fillDetails(booking);
        return booking;
    }

    /** READ booking history of a customer, newest first. */
    public List<Booking> getBookingsByUser(int userId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE b.user_id = ? ORDER BY b.booking_time DESC, b.booking_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        for (Booking b : list) fillDetails(b);
        return list;
    }

    // ---------- private helpers ----------

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setShowId(rs.getInt("show_id"));
        Timestamp ts = rs.getTimestamp("booking_time");
        b.setBookingTime(ts == null ? null : ts.toLocalDateTime());
        b.setStatus(rs.getString("status"));
        b.setCustomerName(rs.getString("full_name"));
        b.setShow(ShowDAO.mapRow(rs));      // the show columns are in the same row
        return b;
    }

    // Loads the seats and the payment of a booking
    private void fillDetails(Booking b) throws SQLException {
        String sql = "SELECT se.*, bs.price_paid FROM booking_seats bs "
                   + "JOIN seats se ON bs.seat_id = se.seat_id "
                   + "WHERE bs.booking_id = ? ORDER BY se.row_label, se.seat_number";
        List<Seat> seats = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, b.getBookingId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Seat seat = SeatDAO.mapRow(rs);
                    seat.setPricePaid(rs.getDouble("price_paid"));
                    seats.add(seat);
                }
            }
        }
        b.setSeats(seats);
        b.setPayment(paymentDAO.getByBooking(b.getBookingId()));
    }
}
