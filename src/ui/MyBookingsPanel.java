package ui;

import model.Booking;
import model.Customer;
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
import java.util.ArrayList;
import java.util.List;

/** Customer tab: booking history with View Ticket and Cancel Booking. */
public class MyBookingsPanel extends JPanel {

    private final Customer customer;
    private final BookingService bookingService = new BookingService();
    private final DefaultTableModel tableModel = UIUtil.readOnlyModel(
            new String[]{"Booking ID", "Movie", "Theatre", "Date", "Time", "Seats", "Amount (Rs.)", "Payment", "Status"});
    private final JTable table = new JTable(tableModel);
    private List<Booking> bookings = new ArrayList<>();

    public MyBookingsPanel(Customer customer) {
        this.customer = customer;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton ticketBtn = new JButton("View Ticket");
        JButton cancelBtn = new JButton("Cancel Booking");
        JButton refreshBtn = new JButton("Refresh");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshBtn);
        bottom.add(ticketBtn);
        bottom.add(cancelBtn);
        add(bottom, BorderLayout.SOUTH);

        ticketBtn.addActionListener(e -> viewTicket());
        cancelBtn.addActionListener(e -> cancelSelected());
        refreshBtn.addActionListener(e -> loadBookings());

        loadBookings();
    }

    public void loadBookings() {
        try {
            bookings = bookingService.getHistory(customer.getUserId());
            tableModel.setRowCount(0);
            for (Booking b : bookings) {
                String payment = b.getPayment() == null ? "-"
                        : b.getPayment().getPaymentMode() + " / " + b.getPayment().getPaymentStatus();
                tableModel.addRow(new Object[]{b.getBookingId(), b.getShow().getMovieTitle(),
                        b.getShow().getTheatreName(), b.getShow().getShowDate(), b.getShow().getShowTime(),
                        b.getSeatLabels(), String.format("%.2f", b.getTotalAmount()), payment, b.getStatus()});
            }
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private Booking selectedBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.error(this, "Select a booking in the table first.");
            return null;
        }
        return bookings.get(row);
    }

    private void viewTicket() {
        Booking b = selectedBooking();
        if (b != null) {
            new TicketDialog(SwingUtilities.getWindowAncestor(this), b).setVisible(true);
        }
    }

    private void cancelSelected() {
        Booking b = selectedBooking();
        if (b == null) return;
        if (!UIUtil.confirm(this, "Cancel booking #" + b.getBookingId() + "? The amount will be refunded.")) return;
        try {
            bookingService.cancelBooking(b.getBookingId(), customer.getUserId());
            UIUtil.info(this, "Booking cancelled. Seats released and payment marked REFUNDED.");
            loadBookings();
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }
}
