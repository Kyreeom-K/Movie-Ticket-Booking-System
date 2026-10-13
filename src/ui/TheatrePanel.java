package ui;

import model.Theatre;
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

/** Admin tab: add, update, delete and view theatres. */
public class TheatrePanel extends JPanel {

    private final AdminService adminService = new AdminService();
    private final DefaultTableModel tableModel = UIUtil.readOnlyModel(
            new String[]{"ID", "Name", "City", "Address"});
    private final JTable table = new JTable(tableModel);
    private List<Theatre> theatres = new ArrayList<>();

    private final JTextField nameField = new JTextField(15);
    private final JTextField cityField = new JTextField(15);
    private final JTextField addressField = new JTextField(15);

    public TheatrePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = UIUtil.formPanel();
        UIUtil.addRow(form, 0, "Name:", nameField);
        UIUtil.addRow(form, 1, "City:", cityField);
        UIUtil.addRow(form, 2, "Address:", addressField);

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

        addBtn.addActionListener(e -> addTheatre());
        updateBtn.addActionListener(e -> updateTheatre());
        deleteBtn.addActionListener(e -> deleteTheatre());
        clearBtn.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        loadTheatres();
    }

    private void loadTheatres() {
        try {
            theatres = adminService.getTheatres();
            tableModel.setRowCount(0);
            for (Theatre t : theatres) {
                tableModel.addRow(new Object[]{t.getTheatreId(), t.getName(), t.getCity(), t.getAddress()});
            }
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private Theatre readForm() {
        Theatre t = new Theatre();
        t.setName(nameField.getText().trim());
        t.setCity(cityField.getText().trim());
        t.setAddress(addressField.getText().trim());
        return t;
    }

    private void addTheatre() {
        try {
            adminService.addTheatre(readForm());
            clearForm();
            loadTheatres();
            UIUtil.info(this, "Theatre added.");
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private void updateTheatre() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.error(this, "Select a theatre in the table first.");
            return;
        }
        try {
            Theatre t = readForm();
            t.setTheatreId(theatres.get(row).getTheatreId());
            adminService.updateTheatre(t);
            loadTheatres();
            UIUtil.info(this, "Theatre updated.");
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private void deleteTheatre() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.error(this, "Select a theatre in the table first.");
            return;
        }
        if (!UIUtil.confirm(this, "Delete the selected theatre?")) return;
        try {
            adminService.deleteTheatre(theatres.get(row).getTheatreId());
            clearForm();
            loadTheatres();
        } catch (Exception ex) {
            UIUtil.handle(this, ex);
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= theatres.size()) return;
        Theatre t = theatres.get(row);
        nameField.setText(t.getName());
        cityField.setText(t.getCity());
        addressField.setText(t.getAddress() == null ? "" : t.getAddress());
    }

    private void clearForm() {
        nameField.setText("");
        cityField.setText("");
        addressField.setText("");
        table.clearSelection();
    }
}
