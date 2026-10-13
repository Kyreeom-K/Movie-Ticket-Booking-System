package ui;

import model.Booking;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.print.PrinterException;

/** Shows the ticket summary text and lets the user print it. */
public class TicketDialog extends JDialog {

    public TicketDialog(Window owner, Booking booking) {
        super(owner, "Ticket Summary", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));

        final JTextArea textArea = new JTextArea(booking.generateTicket());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JButton printBtn = new JButton("Print");
        JButton closeBtn = new JButton("Close");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(printBtn);
        buttons.add(closeBtn);
        add(buttons, BorderLayout.SOUTH);

        printBtn.addActionListener(e -> {
            try {
                textArea.print();          // opens the system print dialog
            } catch (PrinterException ex) {
                UIUtil.error(this, "Printing failed: " + ex.getMessage());
            }
        });
        closeBtn.addActionListener(e -> dispose());

        setSize(500, 520);
        setLocationRelativeTo(owner);
    }
}
