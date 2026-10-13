package ui;

import model.Customer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

/** Main window for customers: browse/book shows and view booking history. */
public class CustomerDashboard extends JFrame {

    public CustomerDashboard(Customer customer) {
        setTitle("Customer Dashboard - Ticket Booking System");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcome = new JLabel(customer.getWelcomeMessage());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 15));
        JButton logout = new JButton("Logout");
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        header.add(welcome, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        final BrowseShowsPanel browsePanel = new BrowseShowsPanel(customer);
        final MyBookingsPanel bookingsPanel = new MyBookingsPanel(customer);
        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Browse Shows", browsePanel);
        tabs.addTab("My Bookings", bookingsPanel);

        // refresh the tab that is opened, so new bookings / cancellations always show up
        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected == bookingsPanel) bookingsPanel.loadBookings();
            else if (selected == browsePanel) browsePanel.loadShows();
        });

        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}
