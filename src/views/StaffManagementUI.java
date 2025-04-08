package views;

import controllers.StaffController;
import models.ContactInfo;
import models.Staff;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StaffManagementUI extends JPanel {
    private JTable staffTable;
    private DefaultTableModel staffTableModel;
    private StaffController staffController;

    public StaffManagementUI(StaffController staffController) {
        super(new BorderLayout());
        this.staffController = staffController;

        setupTable();
        setupUIComponents();
        refreshTable();
    }

    private void setupUIComponents() {
        JLabel titleLabel = new JLabel("MRC STAFF MANAGEMENT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0, 70, 140));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        setupButtons();
    }

    private void setupButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(0, 70, 140));

        JButton addButton = createButton("Add Staff");
        JButton updateButton = createButton("Update Staff");
        JButton deleteButton = createButton("Delete Staff");
        JButton searchByIDButton = createButton("Search by Staff ID");
        JButton searchByNameButton = createButton("Search by Name");

        addButton.addActionListener(e -> {
          StaffEntry staffEntry = new StaffEntry(staffController);
          staffEntry.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
          
          staffEntry.addWindowListener(new java.awt.event.WindowAdapter() {
              @Override
              public void windowClosed(java.awt.event.WindowEvent e) {
                  refreshTable();
              }
          });
      
          staffEntry.setVisible(true);
      });
      

        updateButton.addActionListener(e -> {
            String staffID = JOptionPane.showInputDialog(this, "Enter Staff ID to Update:");
            if (staffID != null && !staffID.trim().isEmpty()) {
                Staff staff = staffController.getStaffByID(staffID.trim());
                if (staff != null) {
                    StaffEntry updateEntry = new StaffEntry(staffController, staff);
                    updateEntry.setVisible(true);
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Staff ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(e -> {
            String staffID = JOptionPane.showInputDialog(this, "Enter Staff ID to Delete:");
            if (staffID != null && !staffID.trim().isEmpty()) {
                if (staffController.deleteStaff(staffID.trim())) {
                    JOptionPane.showMessageDialog(this, "Staff deleted successfully.");
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Staff ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        searchByIDButton.addActionListener(e -> {
            String staffID = JOptionPane.showInputDialog(this, "Enter Staff ID to Search:");
            if (staffID != null && !staffID.trim().isEmpty()) {
                Staff staff = staffController.getStaffByID(staffID.trim());
                if (staff != null) {
                    showStaffSearchResults(List.of(staff), "ID: " + staffID.trim());
                } else {
                    JOptionPane.showMessageDialog(this, "Staff not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        searchByNameButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter Staff First Name or Last Name to Search:");
            if (name != null && !name.trim().isEmpty()) {
                List<Staff> results = staffController.searchStaffByName(name.trim());
                if (!results.isEmpty()) {
                    showStaffSearchResults(results, name.trim());
                } else {
                    JOptionPane.showMessageDialog(this, "No staff found with that name.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(searchByIDButton);
        buttonPanel.add(searchByNameButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupTable() {
        String[] columnNames = { "Staff ID", "Name", "Phone", "Email", "Roles", "Subjects" };
        staffTableModel = new DefaultTableModel(columnNames, 0);
        staffTable = new JTable(staffTableModel);
    }

    private void refreshTable() {
        List<Staff> staffList = staffController.getAllStaff();
        staffTableModel.setRowCount(0);

        for (Staff staff : staffList) {
            ContactInfo contact = staff.getContactInfo();
            String name = staff.getFirstName() + " " + staff.getLastName();
            String roles = String.join(", ", staff.getRoles());
            String subjects = staff.getRoles().contains("Teacher") ? String.join(", ", staff.getSubjects()) : "";

            staffTableModel.addRow(new Object[]{
                staff.getStaffID(),
                name,
                contact != null ? contact.getPhoneNumber() : "",
                contact != null ? contact.getEmail() : "",
                roles,
                subjects
            });
        }
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(180, 40));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 120, 215));
        button.setFocusPainted(false);
        return button;
    }

    private void showStaffSearchResults(List<Staff> staffList, String query) {
        if (staffList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No staff found.", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(Color.WHITE);

        JLabel resultLabel = new JLabel("  " + staffList.size() + " record(s) found for \"" + query + "\" ");
        resultLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        resultLabel.setForeground(new Color(50, 50, 50));
        resultLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        resultPanel.add(resultLabel);
        resultPanel.add(Box.createVerticalStrut(5));

        for (Staff s : staffList) {
            JPanel staffCard = new JPanel(new BorderLayout());
            staffCard.setBorder(BorderFactory.createLineBorder(new Color(0, 70, 140), 2));
            staffCard.setBackground(Color.WHITE);
            staffCard.setPreferredSize(new Dimension(550, 330));

            JPanel header = new JPanel();
            header.setBackground(new Color(0, 70, 140));
            JLabel headerLabel = new JLabel("  Staff Details", SwingConstants.LEFT);
            headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
            headerLabel.setForeground(Color.WHITE);
            header.add(headerLabel);

            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBackground(Color.WHITE);
            body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            body.add(new JLabel("<html><b>Staff ID:</b> " + s.getStaffID() + "</html>"));
            body.add(new JLabel("<html><b>First Name:</b> " + s.getFirstName() + "</html>"));
            body.add(new JLabel("<html><b>Last Name:</b> " + s.getLastName() + "</html>"));
            body.add(new JLabel("<html><b>Date of Birth:</b> " + s.getDateOfBirth() + "</html>"));
            body.add(new JLabel("<html><b>Phone:</b> " + s.getContactInfo().getPhoneNumber() + "</html>"));
            body.add(new JLabel("<html><b>Email:</b> " + s.getContactInfo().getEmail() + "</html>"));
            body.add(new JLabel("<html><b>Address:</b> " + s.getContactInfo().getAddress() + "</html>"));
            body.add(new JLabel("<html><b>Roles:</b> " + String.join(", ", s.getRoles()) + "</html>"));

            if (s.getRoles().contains("Teacher")) {
                body.add(new JLabel("<html><b>Subjects:</b> " + String.join(", ", s.getSubjects()) + "</html>"));
            } else {
                body.add(new JLabel("<html><b>Subjects:</b> N/A</html>"));
            }

            body.add(Box.createVerticalStrut(5));
            body.add(new JLabel("<html><b>Emergency Contact:</b></html>"));
            body.add(new JLabel("• Name: " + s.getEmergencyContact().getName()));
            body.add(new JLabel("• Relationship: " + s.getEmergencyContact().getRelationship()));
            body.add(new JLabel("• Phone: " + s.getEmergencyContact().getPhone()));
            body.add(new JLabel("• Address: " + s.getEmergencyContact().getAddress()));

            staffCard.add(header, BorderLayout.NORTH);
            staffCard.add(body, BorderLayout.CENTER);
            resultPanel.add(staffCard);
            resultPanel.add(Box.createVerticalStrut(15));
        }

        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setPreferredSize(new Dimension(600, 550));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JOptionPane.showMessageDialog(this, scrollPane, "Search Results", JOptionPane.PLAIN_MESSAGE);
    }

}
