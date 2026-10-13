package ui;

import model.Movie;
import service.AdminService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** Admin tab: add, update, delete and view movies. */
public class MoviePanel extends JPanel {

    private final AdminService adminService = new AdminService();
    private final DefaultTableModel tableModel = UIUtil.readOnlyModel(
            new String[]{"ID", "Title", "Genre", "Language", "Duration (min)", "Release Date"});
    private final JTable table = new JTable(tableModel);
    private List<Movie> movies = new ArrayList<>();

    private final JTextField titleField = new JTextField(15);
    private final JTextField genreField = new JTextField(15);
    private final JTextField languageField = new JTextField(15);
    private final JTextField durationField = new JTextField(15);
    private final JTextField releaseField = new JTextField(15);

    public MoviePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ----- form on the right -----
        JPanel form = UIUtil.formPanel();
        UIUtil.addRow(form, 0, "Title:", titleField);
        UIUtil.addRow(form, 1, "Genre:", genreField);
        UIUtil.addRow(form, 2, "Language:", languageField);
        UIUtil.addRow(form, 3, "Duration (min):", durationField);
        UIUtil.addRow(form, 4, "Release (yyyy-MM-dd):", releaseField);

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
        right.setPreferredSize(new Dimension(330, 300));
        right.add(form, BorderLayout.NORTH);
        right.add(buttons, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        addBtn.addActionListener(e -> addMovie());
        updateBtn.addActionListener(e -> updateMovie());
        deleteBtn.addActionListener(e -> deleteMovie());
        clearBtn.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        loadMovies();
    }

    private void loadMovies() {
        try {
            movies = adminService.getMovies();
            tableModel.setRowCount(0);
            for (Movie m : movies) {
                tableModel.addRow(new Object[]{m.getMovieId(), m.getTitle(), m.getGenre(),
                        m.getLanguage(), m.getDurationMinutes(),
                        m.getReleaseDate() == null ? "" : m.getReleaseDate()});
            }
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private Movie readForm() throws exception.ValidationException {
        Movie m = new Movie();
        m.setTitle(titleField.getText().trim());
        m.setGenre(genreField.getText().trim());
        m.setLanguage(languageField.getText().trim());
        m.setDurationMinutes(UIUtil.parseInt(durationField.getText(), "Duration"));
        m.setReleaseDate(UIUtil.parseOptionalDate(releaseField.getText(), "Release date"));
        return m;
    }

    private void addMovie() {
        try {
            adminService.addMovie(readForm());
            clearForm();
            loadMovies();
            UIUtil.info(this, "Movie added.");
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private void updateMovie() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.error(this, "Select a movie in the table first.");
            return;
        }
        try {
            Movie m = readForm();
            m.setMovieId(movies.get(row).getMovieId());
            adminService.updateMovie(m);
            loadMovies();
            UIUtil.info(this, "Movie updated.");
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private void deleteMovie() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.error(this, "Select a movie in the table first.");
            return;
        }
        if (!UIUtil.confirm(this, "Delete the selected movie?")) return;
        try {
            adminService.deleteMovie(movies.get(row).getMovieId());
            clearForm();
            loadMovies();
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= movies.size()) return;
        Movie m = movies.get(row);
        titleField.setText(m.getTitle());
        genreField.setText(m.getGenre());
        languageField.setText(m.getLanguage());
        durationField.setText(String.valueOf(m.getDurationMinutes()));
        releaseField.setText(m.getReleaseDate() == null ? "" : m.getReleaseDate().toString());
    }

    private void clearForm() {
        titleField.setText("");
        genreField.setText("");
        languageField.setText("");
        durationField.setText("");
        releaseField.setText("");
        table.clearSelection();
    }
}
