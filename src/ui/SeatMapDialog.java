package ui;

import exception.SeatUnavailableException;
import model.Booking;
import model.Payment;
import model.Seat;
import model.Show;
import model.User;
import service.BookingService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Seat map: green = regular, gold = premium, red = booked, blue = your selection.
 * The total is recalculated every time you click a seat.
 */
public class SeatMapDialog extends JDialog {

    private static final Color REGULAR_COLOR = new Color(170, 230, 170);
    private static final Color PREMIUM_COLOR = new Color(250, 215, 110);
    private static final Color BOOKED_COLOR = new Color(225, 90, 90);
    private static final Color SELECTED_COLOR = new Color(90, 150, 240);
    private static final int MAX_SEATS = 10;

    private final User customer;
    private final Show show;
    private final BookingService bookingService = new BookingService();

    private final JPanel seatGrid = new JPanel();
    private final JLabel selectedLabel = new JLabel("Selected seats: none");
    private final JLabel totalLabel = new JLabel("Total: Rs. 0.00");
    private final JComboBox<String> paymentBox = new JComboBox<>(Payment.MODES);

    private final Set<Integer> selectedIds = new LinkedHashSet<>();     // seat ids chosen by the user
    private final Map<Integer, Seat> seatById = new HashMap<>();
    private final Map<Integer, JButton> buttonById = new HashMap<>();

    public SeatMapDialog(Window owner, User customer, Show show) {
        super(owner, "Select Seats", ModalityType.APPLICATION_MODAL);
        this.customer = customer;
        this.show = show;
        setLayout(new BorderLayout(10, 10));

        // ----- top: show info + screen -----
        JLabel info = new JLabel(String.format("%s  |  %s  |  %s %s  |  Regular Rs. %.2f, Premium Rs. %.2f",
                show.getMovieTitle(), show.getTheatreName(), show.getShowDate(), show.getShowTime(),
                show.getRegularPrice(), show.getPremiumPrice()), JLabel.CENTER);
        info.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel screen = new JLabel("[ SCREEN THIS WAY ]", JLabel.CENTER);
        screen.setOpaque(true);
        screen.setBackground(Color.DARK_GRAY);
        screen.setForeground(Color.WHITE);
        JPanel top = new JPanel(new BorderLayout(5, 8));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        top.add(info, BorderLayout.NORTH);
        top.add(screen, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // ----- center: seat grid -----
        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gridWrapper.add(seatGrid);
        add(new JScrollPane(gridWrapper), BorderLayout.CENTER);

        // ----- bottom: legend, summary, payment, buttons -----
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 2));
        legend.add(legendItem("Regular", REGULAR_COLOR));
        legend.add(legendItem("Premium", PREMIUM_COLOR));
        legend.add(legendItem("Booked", BOOKED_COLOR));
        legend.add(legendItem("Selected", SELECTED_COLOR));

        JPanel summary = new JPanel(new GridLayout(0, 1, 2, 2));
        summary.add(selectedLabel);
        summary.add(totalLabel);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

        JButton bookBtn = new JButton("Pay & Book");
        JButton cancelBtn = new JButton("Close");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.add(new JLabel("Payment mode:"));
        actions.add(paymentBox);
        actions.add(bookBtn);
        actions.add(cancelBtn);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bottom.add(legend, BorderLayout.NORTH);
        bottom.add(summary, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        bookBtn.addActionListener(e -> book());
        cancelBtn.addActionListener(e -> dispose());

        loadSeats();
        setSize(720, 560);
        setLocationRelativeTo(owner);
    }

    private JLabel legendItem(String text, Color color) {
        JLabel label = new JLabel("  " + text + "  ");
        label.setOpaque(true);
        label.setBackground(color);
        return label;
    }

    /** (Re)loads the seat map from the database and rebuilds the buttons. */
    private void loadSeats() {
        seatGrid.removeAll();
        selectedIds.clear();
        seatById.clear();
        buttonById.clear();
        try {
            List<Seat> seats = bookingService.getSeatMap(show.getShowId());
            int columns = 1;
            for (Seat s : seats) columns = Math.max(columns, s.getSeatNumber());
            seatGrid.setLayout(new GridLayout(0, columns, 6, 6));
            for (Seat seat : seats) {
                seatById.put(seat.getSeatId(), seat);
                JButton button = createSeatButton(seat);
                buttonById.put(seat.getSeatId(), button);
                seatGrid.add(button);
            }
        } catch (SQLException ex) {
            UIUtil.handle(this, ex);
        }
        updateSummary();
        seatGrid.revalidate();
        seatGrid.repaint();
    }

    private JButton createSeatButton(Seat seat) {
        JButton button = new JButton(seat.getLabel());
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setMargin(new Insets(2, 2, 2, 2));
        button.setPreferredSize(new Dimension(54, 34));
        button.setBackground(baseColor(seat));
        if (seat.isBooked()) {
            button.setToolTipText("Already booked");
        } else {
            button.setToolTipText(seat.getSeatType() + " - Rs. " + seat.getPrice(show));   // polymorphic price
            button.addActionListener(e -> toggleSeat(seat));
        }
        return button;
    }

    private Color baseColor(Seat seat) {
        if (seat.isBooked()) return BOOKED_COLOR;
        return "PREMIUM".equals(seat.getSeatType()) ? PREMIUM_COLOR : REGULAR_COLOR;
    }

    private void toggleSeat(Seat seat) {
        JButton button = buttonById.get(seat.getSeatId());
        if (selectedIds.contains(seat.getSeatId())) {
            selectedIds.remove(seat.getSeatId());
            button.setBackground(baseColor(seat));
        } else {
            if (selectedIds.size() >= MAX_SEATS) {
                UIUtil.info(this, "You can select at most " + MAX_SEATS + " seats.");
                return;
            }
            selectedIds.add(seat.getSeatId());
            button.setBackground(SELECTED_COLOR);
        }
        updateSummary();
    }

    /** Auto-calculates the total from the selected seats. */
    private void updateSummary() {
        double total = 0;
        StringBuilder labels = new StringBuilder();
        for (int id : selectedIds) {
            Seat seat = seatById.get(id);
            total += seat.getPrice(show);
            if (labels.length() > 0) labels.append(", ");
            labels.append(seat.getLabel());
        }
        selectedLabel.setText("Selected seats: " + (labels.length() == 0 ? "none" : labels));
        totalLabel.setText(String.format("Total: Rs. %.2f", total));
    }

    private void book() {
        try {
            Booking booking = bookingService.bookSeats(customer, show,
                    new ArrayList<>(selectedIds), (String) paymentBox.getSelectedItem());
            dispose();
            new TicketDialog(getOwner(), booking).setVisible(true);   // show the ticket summary
        } catch (SeatUnavailableException ex) {
            UIUtil.error(this, ex.getMessage());
            loadSeats();                                              // refresh so booked seats turn red
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }
}
