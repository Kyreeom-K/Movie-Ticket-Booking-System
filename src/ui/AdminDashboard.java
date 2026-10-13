package ui;

import model.Admin;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

/** Main window for admins: tabs for Movies, Theatres, Shows and Reports. */
public class AdminDashboard extends JFrame {

    public AdminDashboard(Admin admin) {
        setTitle("Admin Dashboard - Ticket Booking System");
        setSize(1050, 620);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ----- header with welcome text and logout -----
        JLabel welcome = new JLabel(admin.getWelcomeMessage());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 15));
        JButton logout = new JButton("Logout");
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        header.add(welcome, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        // ----- tabs -----
        final ShowPanel showPanel = new ShowPanel();
        final ReportPanel reportPanel = new ReportPanel();
        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Movies", new MoviePanel());
        tabs.addTab("Theatres", new TheatrePanel());
        tabs.addTab("Shows", showPanel);
        tabs.addTab("Reports", reportPanel);

        // Reload data when a tab is opened so lists are never stale
        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected == showPanel) showPanel.refreshAll();
            else if (selected == reportPanel) reportPanel.refresh();
        });

        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}
