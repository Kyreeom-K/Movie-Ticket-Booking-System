package ui;

import exception.ValidationException;
import model.Movie;
import model.Show;
import model.Theatre;
import service.AdminService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Admin tab: add, update, delete and view shows (date, time, prices, total seats). */
public class ShowPanel extends JPanel {

    private final AdminService adminService = new AdminService();
    private final DefaultTableModel tableModel = UIUtil.readOnlyModel(
            new String[]{"ID", "Movie", "Theatre", "Date", "Time", "Regular (Rs.)", "Premium (Rs.)", "Seats"});
    private final JTable table = new JTable(tableModel);
    private List<Show> shows = new ArrayList<>();

    private final JComboBox<Movie> movieBox = new JComboBox<>();
    private final JComboBox<Theatre> theatreBox = new JComboBox<>();
    private final JTextField dateField = new JTextField(15);
    private final JTextField timeField = new JTextField(15);
    private final JTextField regularField = new JTextField(15);
    private final JTextField premiumField = new JTextField(15);
    private final JTextField seatsField = new JTextField(15);

    public ShowPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = UIUtil.formPanel();
        UIUtil.addRow(form, 0, "Movie:", movieBox);
        UIUtil.addRow(form, 1, "Theatre:", theatreBox);
        UIUtil.addRow(form, 2, "Date (yyyy-MM-dd):", dateField);
        UIUtil.addRow(form, 3, "Time (HH:mm):", timeField);
        UIUtil.addRow(form, 4, "Regular price:", regularField);
        UIUtil.addRow(form, 5, "Premium price:", premiumField);
        UIUtil.addRow(form, 6, "Total seats (max 260):", seatsField);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton clearBtn = new JButton("Clear Form");
        JPanel buttons = new JPanel(new GridLayout(0, 1, 5, 5));
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);

        JPanel right = new JPanel(new BorderLayout(5, 15));
        right.setPreferredSize(new Dimension(350, 380));
        right.add(form, BorderLayout.NORTH);
        right.add(buttons, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        addBtn.addActionListener(e -> addShow());
        updateBtn.addActionListener(e -> updateShow());
        deleteBtn.addActionListener(e -> deleteShow());
        clearBtn.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        refreshAll();
    }

    /** Reloads the movie/theatre drop-downs and the shows table. */
    public void refreshAll() {
        try {
            movieBox.removeAllItems();
            for (Movie m : adminService.getMovies()) movieBox.addItem(m);
            theatreBox.removeAllItems();
            for (Theatre t : adminService.getTheatres()) theatreBox.addItem(t);

            shows = adminService.getShows();
            tableModel.setRowCount(0);
            for (Show s : shows) {
                tableModel.addRow(new Object[]{s.getShowId(), s.getMovieTitle(), s.getTheatreName(),
                        s.getShowDate(), s.getShowTime(), s.getRegularPrice(), s.getPremiumPrice(), s.getTotalSeats()});
            }
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private Show readForm() throws ValidationException {
        Movie movie = (Movie) movieBox.getSelectedItem();
        Theatre theatre = (Theatre) theatreBox.getSelectedItem();
        if (movie == null) throw new ValidationException("Add a movie first, then select it.");
        if (theatre == null) throw new ValidationException("Add a theatre first, then select it.");

        Show s = new Show();
        s.setMovieId(movie.getMovieId());
        s.setTheatreId(theatre.getTheatreId());
        s.setShowDate(UIUtil.parseDate(dateField.getText(), "Date"));
        s.setShowTime(UIUtil.parseTime(timeField.getText(), "Time"));
        s.setRegularPrice(UIUtil.parseDouble(regularField.getText(), "Regular price"));
        s.setPremiumPrice(UIUtil.parseDouble(premiumField.getText(), "Premium price"));
        s.setTotalSeats(UIUtil.parseInt(seatsField.getText(), "Total seats"));
        return s;
    }

    private void addShow() {
        try {
            adminService.addShow(readForm());
            clearForm();
            refreshAll();
            UIUtil.info(this, "Show added and seat map created.");
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private void updateShow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.error(this, "Select a show in the table first.");
            return;
        }
        try {
            Show s = readForm();
            s.setShowId(shows.get(row).getShowId());
            adminService.updateShow(s);
            refreshAll();
            UIUtil.info(this, "Show updated.");
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private void deleteShow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.error(this, "Select a show in the table first.");
            return;
        }
        if (!UIUtil.confirm(this, "Delete the selected show and its seats?")) return;
        try {
            adminService.deleteShow(shows.get(row).getShowId());
            clearForm();
            refreshAll();
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= shows.size()) return;
        Show s = shows.get(row);
        // pick the matching movie / theatre in the drop-downs
        for (int i = 0; i < movieBox.getItemCount(); i++) {
            if (movieBox.getItemAt(i).getMovieId() == s.getMovieId()) movieBox.setSelectedIndex(i);
        }
        for (int i = 0; i < theatreBox.getItemCount(); i++) {
            if (theatreBox.getItemAt(i).getTheatreId() == s.getTheatreId()) theatreBox.setSelectedIndex(i);
        }
        dateField.setText(s.getShowDate().toString());
        timeField.setText(s.getShowTime().toString().substring(0, 5));   // HH:mm
        regularField.setText(String.valueOf(s.getRegularPrice()));
        premiumField.setText(String.valueOf(s.getPremiumPrice()));
        seatsField.setText(String.valueOf(s.getTotalSeats()));
    }

    private void clearForm() {
        dateField.setText(LocalDate.now().plusDays(1).toString());      // handy default: tomorrow
        timeField.setText("18:30");
        regularField.setText("");
        premiumField.setText("");
        seatsField.setText("");
        table.clearSelection();
    }
}
