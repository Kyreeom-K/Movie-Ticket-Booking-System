package dao;

import model.Movie;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** CRUD operations for the movies table. */
public class MovieDAO {

    public int addMovie(Movie m) throws SQLException {
        String sql = "INSERT INTO movies (title, genre, language, duration_minutes, release_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, m);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateMovie(Movie m) throws SQLException {
        String sql = "UPDATE movies SET title = ?, genre = ?, language = ?, duration_minutes = ?, release_date = ? WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            fillStatement(ps, m);
            ps.setInt(6, m.getMovieId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteMovie(int movieId) throws SQLException {
        String sql = "DELETE FROM movies WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY movie_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Movie getMovieById(int movieId) throws SQLException {
        String sql = "SELECT * FROM movies WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // Sets the first 5 "?" placeholders (shared by insert and update)
    private void fillStatement(PreparedStatement ps, Movie m) throws SQLException {
        ps.setString(1, m.getTitle());
        ps.setString(2, m.getGenre());
        ps.setString(3, m.getLanguage());
        ps.setInt(4, m.getDurationMinutes());
        if (m.getReleaseDate() == null) {
            ps.setNull(5, Types.DATE);
        } else {
            ps.setDate(5, Date.valueOf(m.getReleaseDate()));
        }
    }

    private Movie mapRow(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setMovieId(rs.getInt("movie_id"));
        m.setTitle(rs.getString("title"));
        m.setGenre(rs.getString("genre"));
        m.setLanguage(rs.getString("language"));
        m.setDurationMinutes(rs.getInt("duration_minutes"));
        Date d = rs.getDate("release_date");
        m.setReleaseDate(d == null ? null : d.toLocalDate());
        return m;
    }
}
