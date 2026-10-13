package ui;

import model.Admin;
import model.Customer;
import model.User;
import service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

/** First screen: login for Admin or Customer, with a Register button. */
public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"CUSTOMER", "ADMIN"});
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Ticket Booking System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Movie Ticket Booking System", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(title, BorderLayout.NORTH);

        JPanel form = UIUtil.formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        UIUtil.addRow(form, 0, "Login as:", roleBox);
        UIUtil.addRow(form, 1, "Email:", emailField);
        UIUtil.addRow(form, 2, "Password:", passwordField);
        add(form, BorderLayout.CENTER);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(loginButton);
        buttons.add(registerButton);
        add(buttons, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());          // Enter key logs in
        registerButton.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        getRootPane().setDefaultButton(loginButton);

        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        try {
            User user = authService.login(emailField.getText(),
                    new String(passwordField.getPassword()),
                    (String) roleBox.getSelectedItem());
            dispose();                                            // close login window
            // Role-based screen: open the dashboard that matches the user type
            if (user instanceof Admin) {
                new AdminDashboard((Admin) user).setVisible(true);
            } else {
                new CustomerDashboard((Customer) user).setVisible(true);
            }
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }
}
