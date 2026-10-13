package dao;

import model.Theatre;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** CRUD operations for the theatres table. */
public class TheatreDAO {

    public int addTheatre(Theatre t) throws SQLException {
        String sql = "INSERT INTO theatres (name, city, address) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getCity());
            ps.setString(3, t.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateTheatre(Theatre t) throws SQLException {
        String sql = "UPDATE theatres SET name = ?, city = ?, address = ? WHERE theatre_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getCity());
            ps.setString(3, t.getAddress());
            ps.setInt(4, t.getTheatreId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteTheatre(int theatreId) throws SQLException {
        String sql = "DELETE FROM theatres WHERE theatre_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, theatreId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Theatre> getAllTheatres() throws SQLException {
        List<Theatre> list = new ArrayList<>();
        String sql = "SELECT * FROM theatres ORDER BY theatre_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Theatre getTheatreById(int theatreId) throws SQLException {
        String sql = "SELECT * FROM theatres WHERE theatre_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, theatreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private Theatre mapRow(ResultSet rs) throws SQLException {
        return new Theatre(rs.getInt("theatre_id"), rs.getString("name"),
                rs.getString("city"), rs.getString("address"));
    }
}
