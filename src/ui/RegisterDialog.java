package ui;

import service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** Registration form for new Customers and Admins (admins need the secret key). */
public class RegisterDialog extends JDialog {

    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"CUSTOMER", "ADMIN"});
    private final JPasswordField adminKeyField = new JPasswordField(20);
    private final AuthService authService = new AuthService();

    public RegisterDialog(JFrame owner) {
        super(owner, "Register", true);
        setLayout(new BorderLayout(10, 10));

        JPanel form = UIUtil.formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        UIUtil.addRow(form, 0, "Register as:", roleBox);
        UIUtil.addRow(form, 1, "Full name:", nameField);
        UIUtil.addRow(form, 2, "Email:", emailField);
        UIUtil.addRow(form, 3, "Phone (10 digits):", phoneField);
        UIUtil.addRow(form, 4, "Password (min 6):", passwordField);
        UIUtil.addRow(form, 5, "Admin secret key:", adminKeyField);
        add(form, BorderLayout.CENTER);

        adminKeyField.setEnabled(false);                       // only needed for ADMIN
        roleBox.addActionListener(e ->
                adminKeyField.setEnabled("ADMIN".equals(roleBox.getSelectedItem())));

        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(registerButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> doRegister());
        cancelButton.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
    }

    private void doRegister() {
        try {
            authService.register(nameField.getText(), emailField.getText(), phoneField.getText(),
                    new String(passwordField.getPassword()),
                    (String) roleBox.getSelectedItem(),
                    new String(adminKeyField.getPassword()));
            UIUtil.info(this, "Registration successful! You can now log in.");
            dispose();
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }
}
