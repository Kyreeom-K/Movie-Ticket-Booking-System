package ui;

import service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;

/** Admin tab with three reports: bookings per show, total revenue, seat occupancy. */
public class ReportPanel extends JPanel {

    private final ReportService reportService = new ReportService();
    private final JLabel revenueLabel = new JLabel("Total revenue: Rs. 0.00");
    private final DefaultTableModel bookingsModel = UIUtil.readOnlyModel(ReportService.BOOKINGS_COLUMNS);
    private final DefaultTableModel occupancyModel = UIUtil.readOnlyModel(ReportService.OCCUPANCY_COLUMNS);

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        revenueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        JButton refreshBtn = new JButton("Refresh Reports");
        JPanel top = new JPanel(new BorderLayout());
        top.add(revenueLabel, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JTabbedPane inner = new JTabbedPane();
        inner.addTab("Bookings per Show", new JScrollPane(new JTable(bookingsModel)));
        inner.addTab("Seat Occupancy", new JScrollPane(new JTable(occupancyModel)));
        add(inner, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> refresh());
        refresh();
    }

    /** Re-runs all three report queries. */
    public void refresh() {
        try {
            revenueLabel.setText(String.format("Total revenue: Rs. %.2f", reportService.totalRevenue()));

            bookingsModel.setRowCount(0);
            for (Object[] row : reportService.bookingsPerShow()) bookingsModel.addRow(row);

            occupancyModel.setRowCount(0);
            for (Object[] row : reportService.seatOccupancy()) occupancyModel.addRow(row);
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }
}
