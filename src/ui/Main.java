package ui;

import util.DBConnection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;
import java.sql.SQLException;

/** Program entry point. Run this class. */
public class Main {
    public static void main(String[] args) {
        // Cross-platform look keeps the seat colours visible on every operating system
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // default look is fine
        }

        // Check the database first so the user gets a clear message if MySQL is not ready
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("Connected to MySQL successfully.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to the database:\n" + e.getMessage()
                    + "\n\nCheck that MySQL is running, the ticket_booking.sql script was executed,\n"
                    + "and USER/PASSWORD in util/DBConnection.java are correct.",
                    "Database connection failed", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
