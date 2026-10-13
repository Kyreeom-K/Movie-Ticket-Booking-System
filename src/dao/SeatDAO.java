package dao;

import model.Seat;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database operations for the seats table. */
public class SeatDAO {

    /** READ: the full seat map of a show (no locking). */
    public List<Seat> getSeatsByShow(int showId) throws SQLException {
        List<Seat> list = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE show_id = ? ORDER BY row_label, seat_number";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Locks the chosen seat rows with SELECT ... FOR UPDATE.
     * While we hold the lock inside a transaction, nobody else can book the same seats,
     * which is exactly what prevents double booking.
     * NOTE: uses the connection passed in so it is part of the caller's transaction.
     */
    public List<Seat> lockSeats(Connection con, int showId, List<Integer> seatIds) throws SQLException {
        List<Seat> list = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE show_id = ? AND seat_id IN (" + placeholders(seatIds.size())
                   + ") ORDER BY seat_id FOR UPDATE";    // ORDER BY avoids deadlocks
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            for (int i = 0; i < seatIds.size(); i++) {
                ps.setInt(i + 2, seatIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** UPDATE: set the status (BOOKED / AVAILABLE) of several seats inside a transaction. */
    public void markStatus(Connection con, List<Integer> seatIds, String status) throws SQLException {
        String sql = "UPDATE seats SET status = ? WHERE seat_id IN (" + placeholders(seatIds.size()) + ")";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            for (int i = 0; i < seatIds.size(); i++) {
                ps.setInt(i + 2, seatIds.get(i));
            }
            ps.executeUpdate();
        }
    }

    /** UPDATE: frees all seats that belong to a booking (used when cancelling). */
    public void releaseSeatsOfBooking(Connection con, int bookingId) throws SQLException {
        String sql = "UPDATE seats SET status = 'AVAILABLE' "
                   + "WHERE seat_id IN (SELECT seat_id FROM booking_seats WHERE booking_id = ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
    }

    // Builds "?,?,?" for an IN (...) clause. Only "?" characters are added,
    // the actual values are still bound with setInt, so this is safe from SQL injection.
    private String placeholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        return sb.toString();
    }

    /** Converts a row into RegularSeat or PremiumSeat (polymorphism via factory). */
    static Seat mapRow(ResultSet rs) throws SQLException {
        Seat seat = Seat.create(rs.getString("seat_type"));
        seat.setSeatId(rs.getInt("seat_id"));
        seat.setShowId(rs.getInt("show_id"));
        seat.setRowLabel(rs.getString("row_label"));
        seat.setSeatNumber(rs.getInt("seat_number"));
        seat.setStatus(rs.getString("status"));
        return seat;
    }
}
