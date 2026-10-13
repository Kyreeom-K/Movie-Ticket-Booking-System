package ui;

import exception.ValidationException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.awt.GridBagLayout;

/** Small helper methods shared by all screens (dialogs, parsing, table models, form rows). */
public class UIUtil {

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Asks Yes/No. Returns true if the user clicked Yes. */
    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** One place to show any exception to the user. */
    public static void handle(Component parent, Exception e) {
        if (e instanceof SQLException) {
            error(parent, "Database error: " + e.getMessage());
        } else {
            error(parent, e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    /** A table model whose cells cannot be edited by typing. */
    public static DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Adds "label : field" as one row of a GridBagLayout form. */
    public static void addRow(JPanel panel, int row, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panel.add(field, c);
    }

    public static JPanel formPanel() {
        return new JPanel(new GridBagLayout());
    }

    // ---------- text -> value helpers (throw ValidationException with a clear message) ----------

    public static int parseInt(String text, String fieldName) throws ValidationException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a whole number.");
        }
    }

    public static double parseDouble(String text, String fieldName) throws ValidationException {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a number.");
        }
    }

    public static LocalDate parseDate(String text, String fieldName) throws ValidationException {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException(fieldName + " must be in yyyy-MM-dd format (e.g. 2026-12-31).");
        }
    }

    /** Blank text gives null (used for the optional release date). */
    public static LocalDate parseOptionalDate(String text, String fieldName) throws ValidationException {
        if (text == null || text.trim().isEmpty()) return null;
        return parseDate(text, fieldName);
    }

    public static LocalTime parseTime(String text, String fieldName) throws ValidationException {
        try {
            return LocalTime.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException(fieldName + " must be in HH:mm format (e.g. 18:30).");
        }
    }
}
