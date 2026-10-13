package dao;

import model.Show;
import util.DBConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/** CRUD operations for the shows table. Creating a show also creates its seat map. */
public class ShowDAO {

    // Base query: joins movie title and theatre name so the UI can display them
    private static final String BASE_SELECT =
            "SELECT s.*, m.title AS movie_title, t.name AS theatre_name "
          + "FROM shows s "
          + "JOIN movies m ON s.movie_id = m.movie_id "
          + "JOIN theatres t ON s.theatre_id = t.theatre_id ";

    /**
     * CREATE: inserts the show AND generates its seats inside ONE transaction,
     * so we never end up with a show that has no seats.
     */
    public int addShow(Show show) throws SQLException {
        String sql = "INSERT INTO shows (movie_id, theatre_id, show_date, show_time, "
                   + "regular_price, premium_price, total_seats) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);                      // start transaction
            try {
                int newId;
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    fillStatement(ps, show);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        newId = keys.getInt(1);
                    }
                }
                generateSeats(con, newId);
                con.commit();                              // all good
                return newId;
            } catch (SQLException e) {
                con.rollback();                            // undo everything
                throw e;
            }
        }
    }

    /**
     * UPDATE: changes show details. If total_seats changed, the seat map is rebuilt
     * (the service layer makes sure no seat is booked before this happens).
     */
    public boolean updateShow(Show show) throws SQLException {
        String sql = "UPDATE shows SET movie_id = ?, theatre_id = ?, show_date = ?, show_time = ?, "
                   + "regular_price = ?, premium_price = ?, total_seats = ? WHERE show_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int oldTotal = getTotalSeats(con, show.getShowId());
                boolean updated;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    fillStatement(ps, show);
                    ps.setInt(8, show.getShowId());
                    updated = ps.executeUpdate() > 0;
                }
                if (updated && oldTotal != show.getTotalSeats()) {
                    try (PreparedStatement del = con.prepareStatement("DELETE FROM seats WHERE show_id = ?")) {
                        del.setInt(1, show.getShowId());
                        del.executeUpdate();
                    }
                    generateSeats(con, show.getShowId());
                }
                con.commit();
                return updated;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** DELETE: fails (foreign key) if bookings exist for the show. Seats are deleted automatically. */
    public boolean deleteShow(int showId) throws SQLException {
        String sql = "DELETE FROM shows WHERE show_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            return ps.executeUpdate() > 0;
        }
    }

    /** READ: all shows (admin view). */
    public List<Show> getAllShows() throws SQLException {
        return query(BASE_SELECT + "ORDER BY s.show_date, s.show_time", null);
    }

    /** READ: shows that have not started yet (customer view). */
    public List<Show> getUpcomingShows() throws SQLException {
        return query(BASE_SELECT
                + "WHERE TIMESTAMP(s.show_date, s.show_time) > NOW() "
                + "ORDER BY s.show_date, s.show_time", null);
    }

    public Show getShowById(int showId) throws SQLException {
        List<Show> list = query(BASE_SELECT + "WHERE s.show_id = ?", showId);
        return list.isEmpty() ? null : list.get(0);
    }

    /** Counts BOOKED seats of a show (used before changing total seats). */
    public int countBookedSeats(int showId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM seats WHERE show_id = ? AND status = 'BOOKED'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // ---------- private helpers ----------

    private void generateSeats(Connection con, int showId) throws SQLException {
        // Calls the stored procedure from the SQL script
        try (CallableStatement cs = con.prepareCall("{CALL sp_generate_seats(?)}")) {
            cs.setInt(1, showId);
            cs.execute();
        }
    }

    private int getTotalSeats(Connection con, int showId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT total_seats FROM shows WHERE show_id = ?")) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void fillStatement(PreparedStatement ps, Show s) throws SQLException {
        ps.setInt(1, s.getMovieId());
        ps.setInt(2, s.getTheatreId());
        ps.setDate(3, Date.valueOf(s.getShowDate()));
        ps.setTime(4, Time.valueOf(s.getShowTime()));
        ps.setDouble(5, s.getRegularPrice());
        ps.setDouble(6, s.getPremiumPrice());
        ps.setInt(7, s.getTotalSeats());
    }

    // Runs a SELECT; "param" is optional (one int parameter)
    private List<Show> query(String sql, Integer param) throws SQLException {
        List<Show> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Also used by BookingDAO-style code: maps a row of BASE_SELECT to a Show. */
    static Show mapRow(ResultSet rs) throws SQLException {
        Show s = new Show();
        s.setShowId(rs.getInt("show_id"));
        s.setMovieId(rs.getInt("movie_id"));
        s.setTheatreId(rs.getInt("theatre_id"));
        s.setShowDate(rs.getDate("show_date").toLocalDate());
        s.setShowTime(rs.getTime("show_time").toLocalTime());
        s.setRegularPrice(rs.getDouble("regular_price"));
        s.setPremiumPrice(rs.getDouble("premium_price"));
        s.setTotalSeats(rs.getInt("total_seats"));
        s.setMovieTitle(rs.getString("movie_title"));
        s.setTheatreName(rs.getString("theatre_name"));
        return s;
    }
}
