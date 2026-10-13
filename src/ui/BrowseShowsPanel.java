package ui;

import model.Customer;
import model.Show;
import service.BookingService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** Customer tab: list of upcoming shows. Select one to open the seat map. */
public class BrowseShowsPanel extends JPanel {

    private final Customer customer;
    private final BookingService bookingService = new BookingService();
    private final DefaultTableModel tableModel = UIUtil.readOnlyModel(
            new String[]{"ID", "Movie", "Theatre", "Date", "Time", "Regular (Rs.)", "Premium (Rs.)", "Seats"});
    private final JTable table = new JTable(tableModel);
    private List<Show> shows = new ArrayList<>();

    public BrowseShowsPanel(Customer customer) {
        this.customer = customer;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton selectBtn = new JButton("Select Show & View Seats");
        JButton refreshBtn = new JButton("Refresh");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshBtn);
        bottom.add(selectBtn);
        add(bottom, BorderLayout.SOUTH);

        selectBtn.addActionListener(e -> openSeatMap());
        refreshBtn.addActionListener(e -> loadShows());
        table.addMouseListener(new MouseAdapter() {          // double-click also opens the seat map
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSeatMap();
            }
        });

        loadShows();
    }

    public void loadShows() {
        try {
            shows = bookingService.getUpcomingShows();
            tableModel.setRowCount(0);
            for (Show s : shows) {
                tableModel.addRow(new Object[]{s.getShowId(), s.getMovieTitle(), s.getTheatreName(),
                        s.getShowDate(), s.getShowTime(), s.getRegularPrice(), s.getPremiumPrice(), s.getTotalSeats()});
            }
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private void openSeatMap() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.error(this, "Please select a show first.");
            return;
        }
        new SeatMapDialog(SwingUtilities.getWindowAncestor(this), customer, shows.get(row)).setVisible(true);
    }
}
