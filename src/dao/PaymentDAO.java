package dao;

import model.Payment;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/** Database operations for the payments table. */
public class PaymentDAO {

    /** CREATE: part of the booking transaction, so it uses the caller's connection. */
    public void insertPayment(Connection con, Payment p) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, amount, payment_mode, payment_status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getBookingId());
            ps.setDouble(2, p.getAmount());
            ps.setString(3, p.getPaymentMode());
            ps.setString(4, p.getPaymentStatus());
            ps.executeUpdate();
        }
    }

    /** READ: the payment of a booking (null if none). */
    public Payment getByBooking(int bookingId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE booking_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getInt("payment_id"));
                    p.setBookingId(rs.getInt("booking_id"));
                    p.setAmount(rs.getDouble("amount"));
                    p.setPaymentMode(rs.getString("payment_mode"));
                    p.setPaymentStatus(rs.getString("payment_status"));
                    Timestamp ts = rs.getTimestamp("payment_time");
                    p.setPaymentTime(ts == null ? null : ts.toLocalDateTime());
                    return p;
                }
            }
        }
        return null;
    }

    /** UPDATE: e.g. SUCCESS -> REFUNDED when a booking is cancelled. */
    public void updateStatus(Connection con, int bookingId, String status) throws SQLException {
        String sql = "UPDATE payments SET payment_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }
}
