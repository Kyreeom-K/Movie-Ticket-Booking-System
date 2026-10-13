package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates database connections. Change USER and PASSWORD to match your MySQL setup.
 * Every DAO calls DBConnection.getConnection() whenever it needs to talk to MySQL.
 */
public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/ticket_booking_db"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    private static final String USER = "root";
    private static final String PASSWORD = "Enter your password";   // <-- CHANGE THIS

    /** Opens a NEW connection. Callers must close it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");   // loads the MySQL driver
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. "
                    + "Put mysql-connector-j JAR in the lib folder and add it to the classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
