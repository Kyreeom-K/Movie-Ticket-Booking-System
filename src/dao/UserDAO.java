package dao;

import model.Admin;
import model.Customer;
import model.User;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO = Data Access Object. All SQL for the users table lives here (CRUD). */
public class UserDAO {

    /** CREATE: inserts a user and returns the new user_id. */
    public int addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, phone, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole());          // polymorphism: Admin -> "ADMIN"
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /** READ: find a user by email (used for login). Returns null if not found. */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** READ: find a user by id. Returns null if not found. */
    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** READ: all users. */
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** UPDATE: changes name, phone and password hash. */
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, phone = ?, password_hash = ? WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getPasswordHash());
            ps.setInt(4, user.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    /** DELETE: removes a user (fails if the user has bookings - foreign key). */
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Converts one database row into an Admin or Customer object. */
    private User mapRow(ResultSet rs) throws SQLException {
        User user;
        if ("ADMIN".equals(rs.getString("role"))) {
            user = new Admin();
        } else {
            user = new Customer();
        }
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPhone(rs.getString("phone"));
        return user;
    }
}
