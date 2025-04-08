package views;

import controllers.StaffController;
import models.ContactInfo;
import models.EmergencyContact;
import models.Staff;
import utils.Subjects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.*;
import java.util.List;

public class StaffEntry extends JFrame {
    private JTextField txtStaffID, txtFirstName, txtLastName, txtDOB;
    private JTextField txtPhone, txtEmail, txtAddress;
    private JTextField txtEName, txtERelationship, txtEPhone, txtEAddress;
    private JCheckBox adminBox, teacherBox;
    private List<JCheckBox> subjectCheckboxes = new ArrayList<>();
    private JPanel subjectsPanel;
    private JButton saveButton, cancelButton;
    private StaffController staffController;
    private Staff staffToEdit;

    public StaffEntry(StaffController controller) {
        this(controller, null); 
    }

    public StaffEntry(StaffController controller, Staff staffToEdit) {
        this.staffController = controller;
        this.staffToEdit = staffToEdit;
        initializeForm();
        if (staffToEdit != null) {
            populateFields(staffToEdit);
        }
    }

    private void initializeForm() {
        setTitle("Staff Entry");
        setSize(600, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(2, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.weightx = 1;
        gbc.gridx = 0;
        int row = 0;

        addSectionHeader(formPanel, "STAFF INFORMATION", gbc, row++);
        txtStaffID = addInputField(formPanel, "Staff ID:", gbc, row++, true);
        if (staffToEdit == null) {
            txtStaffID.setText(staffController.generateStaffID());
        }
        txtFirstName = addInputField(formPanel, "First Name:", gbc, row++, false);
        txtLastName = addInputField(formPanel, "Last Name:", gbc, row++, false);
        txtDOB = addInputField(formPanel, "Date of Birth (YYYY/MM/DD):", gbc, row++, false);
        txtPhone = addInputField(formPanel, "Phone (876)XXX-XXXX:", gbc, row++, false);
        txtEmail = addInputField(formPanel, "Email:", gbc, row++, false);
        txtAddress = addInputField(formPanel, "Address:", gbc, row++, false);

        addSectionHeader(formPanel, "ROLE", gbc, row++);
        adminBox = new JCheckBox("Admin");
        teacherBox = new JCheckBox("Teacher");

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.add(adminBox);
        rolePanel.add(teacherBox);

        gbc.gridy = row++;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Select Role(s):"), gbc);
        gbc.gridx = 1;
        formPanel.add(rolePanel, gbc);

        JLabel subjectLabel = new JLabel("Subjects:");
        subjectsPanel = new JPanel(new GridLayout(0, 2));
        for (String subject : Subjects.getAvailableSubjects()) {
            JCheckBox box = new JCheckBox(subject);
            subjectCheckboxes.add(box);
            subjectsPanel.add(box);
        }

        gbc.gridy = row++;
        gbc.gridx = 0;
        formPanel.add(subjectLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(subjectsPanel, gbc);
        subjectLabel.setVisible(false);
        subjectsPanel.setVisible(false);

        teacherBox.addItemListener(e -> {
            boolean visible = e.getStateChange() == ItemEvent.SELECTED;
            subjectLabel.setVisible(visible);
            subjectsPanel.setVisible(visible);
        });

        addSectionHeader(formPanel, "EMERGENCY CONTACT INFORMATION", gbc, row++);
        txtEName = addInputField(formPanel, "Name:", gbc, row++, false);
        txtERelationship = addInputField(formPanel, "Relationship:", gbc, row++, false);
        txtEPhone = addInputField(formPanel, "Phone (876)XXX-XXXX:", gbc, row++, false);
        txtEAddress = addInputField(formPanel, "Address:", gbc, row++, false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveButton = createStyledButton("Save");
        cancelButton = createStyledButton("Close");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        saveButton.addActionListener(e -> saveStaff());
        cancelButton.addActionListener(e -> dispose());

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane);
    }

    private void saveStaff() {
        Set<String> roles = new HashSet<>();
        if (adminBox.isSelected()) roles.add("Admin");
        if (teacherBox.isSelected()) roles.add("Teacher");

        List<String> selectedSubjects = new ArrayList<>();
        if (teacherBox.isSelected()) {
            for (JCheckBox box : subjectCheckboxes) {
                if (box.isSelected()) selectedSubjects.add(box.getText());
            }
        }

        ContactInfo contact = new ContactInfo(txtPhone.getText().trim(), txtEmail.getText().trim(), txtAddress.getText().trim());
        EmergencyContact emergency = new EmergencyContact(txtEName.getText().trim(), txtERelationship.getText().trim(), txtEPhone.getText().trim(), txtEAddress.getText().trim());

        Staff staff = new Staff(
                txtStaffID.getText().trim(),
                txtFirstName.getText().trim(),
                txtLastName.getText().trim(),
                txtDOB.getText().trim(),
                contact,
                roles,
                selectedSubjects,
                emergency
        );

        StringBuilder errorMsg = new StringBuilder();
        if (!staffController.validateStaffInfo(staff, errorMsg)) {
            showError(errorMsg.toString());
            return;
        }

        staffController.addOrUpdateStaff(staff);
        JOptionPane.showMessageDialog(this, "Staff member saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void populateFields(Staff staff) {
      txtStaffID.setText(staff.getStaffID());
      txtFirstName.setText(staff.getFirstName());
      txtLastName.setText(staff.getLastName());
      txtDOB.setText(staff.getDateOfBirth());
  
      ContactInfo contact = staff.getContactInfo();
      txtPhone.setText(contact.getPhoneNumber());
      txtEmail.setText(contact.getEmail());
      txtAddress.setText(contact.getAddress());
  
      EmergencyContact emergency = staff.getEmergencyContact();
      txtEName.setText(emergency.getName());
      txtERelationship.setText(emergency.getRelationship());
      txtEPhone.setText(emergency.getPhone());
      txtEAddress.setText(emergency.getAddress());
  
      Set<String> roles = staff.getRoles();
      adminBox.setSelected(roles.contains("Admin"));
      teacherBox.setSelected(roles.contains("Teacher"));
  
      if (roles.contains("Teacher")) {
          for (JCheckBox cb : subjectCheckboxes) {
              cb.setSelected(staff.getSubjects().contains(cb.getText()));
          }
      }
    }
  

    private void addSectionHeader(JPanel panel, String title, GridBagConstraints gbc, int gridY) {
        gbc.gridy = gridY;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(new Color(0, 70, 140));
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        panel.add(label, gbc);
        gbc.gridwidth = 1;
    }

    private JTextField addInputField(JPanel panel, String label, GridBagConstraints gbc, int gridY, boolean readOnly) {
        gbc.gridy = gridY;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);

        JTextField field = new JTextField(20);
        field.setEditable(!readOnly);

        gbc.gridx = 1;
        panel.add(field, gbc);
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(150, 45));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 123, 255));
        button.setFocusPainted(false);
        return button;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

}
