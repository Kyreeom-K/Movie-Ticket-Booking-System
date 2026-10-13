package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only SQL for the admin reports.
 * Each report row is returned as an Object[] so the UI can put it straight into a table.
 */
public class ReportDAO {

    /** Report 1: confirmed bookings, seats sold and revenue for every show. */
    public List<Object[]> bookingsPerShow() throws SQLException {
        String sql = "SELECT s.show_id, m.title, t.name, s.show_date, s.show_time, "
                   + "COUNT(DISTINCT b.booking_id) AS bookings, COUNT(bs.seat_id) AS seats_sold, "
                   + "COALESCE(SUM(bs.price_paid), 0) AS revenue "
                   + "FROM shows s "
                   + "JOIN movies m ON s.movie_id = m.movie_id "
                   + "JOIN theatres t ON s.theatre_id = t.theatre_id "
                   + "LEFT JOIN bookings b ON b.show_id = s.show_id AND b.status = 'CONFIRMED' "
                   + "LEFT JOIN booking_seats bs ON bs.booking_id = b.booking_id "
                   + "GROUP BY s.show_id, m.title, t.name, s.show_date, s.show_time "
                   + "ORDER BY s.show_date, s.show_time";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("show_id"), rs.getString("title"), rs.getString("name"),
                        rs.getDate("show_date"), rs.getTime("show_time"),
                        rs.getInt("bookings"), rs.getInt("seats_sold"),
                        String.format("%.2f", rs.getDouble("revenue"))});
            }
        }
        return rows;
    }

    /** Report 2: total revenue = sum of all SUCCESS payments (refunded ones are excluded). */
    public double totalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_status = 'SUCCESS'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    /** Report 3: seat occupancy (booked / total and percentage) for every show. */
    public List<Object[]> seatOccupancy() throws SQLException {
        String sql = "SELECT s.show_id, m.title, t.name, s.show_date, s.show_time, "
                   + "COUNT(se.seat_id) AS total_seats, "
                   + "SUM(CASE WHEN se.status = 'BOOKED' THEN 1 ELSE 0 END) AS booked_seats "
                   + "FROM shows s "
                   + "JOIN movies m ON s.movie_id = m.movie_id "
                   + "JOIN theatres t ON s.theatre_id = t.theatre_id "
                   + "JOIN seats se ON se.show_id = s.show_id "
                   + "GROUP BY s.show_id, m.title, t.name, s.show_date, s.show_time "
                   + "ORDER BY s.show_date, s.show_time";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int total = rs.getInt("total_seats");
                int booked = rs.getInt("booked_seats");
                double percent = total == 0 ? 0 : booked * 100.0 / total;
                rows.add(new Object[]{
                        rs.getInt("show_id"), rs.getString("title"), rs.getString("name"),
                        rs.getDate("show_date"), rs.getTime("show_time"),
                        total, booked, total - booked, String.format("%.1f %%", percent)});
            }
        }
        return rows;
    }
}
