package views;

import controllers.StudentController;
import models.ContactInfo;
import models.GuardianInfo;
import models.Student;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;

import java.util.List;

public class StudentEntry extends JFrame {
    private JTextField txtStudentID, txtFirstName, txtLastName, txtDOB, txtSchool;
    private JComboBox<String> cmbGrade;
    private JFormattedTextField txtContactNumber;
    private JTextField txtEmail, txtAddress, txtGuardianEmail1, txtGuardianEmail2;
    private JTextField txtGuardianFirstName1, txtGuardianLastName1, txtGuardianRelation1;
    private JTextField txtGuardianFirstName2, txtGuardianLastName2, txtGuardianRelation2;
    private JTextField txtGuardianPhone1, txtGuardianPhone2;
    private JButton saveButton, cancelButton;

    private Student existingStudent;
    private StudentController studentController;

    public StudentEntry(Student student, StudentController studentController) {
        this.existingStudent = student;
        this.studentController = studentController;

        initializeForm();
        setupEventHandlers();

        if (existingStudent != null) {
            loadStudentData();
        } else {
            txtStudentID.setText(studentController.generateUniqueID());
        }
    }

    private void initializeForm() {
        setTitle(existingStudent == null ? "Add New Student" : "Update Student");
        setSize(600, 750);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.weightx = 1;
        gbc.gridx = 0;

        addSectionHeader(formPanel, "STUDENT INFORMATION", gbc, 0);
        addStudentFields(formPanel, gbc);

        addSectionHeader(formPanel, "CONTACT INFORMATION", gbc, 7);
        addContactFields(formPanel, gbc);

        addSectionHeader(formPanel, "GUARDIAN INFORMATION", gbc, 11);
        addGuardianFields(formPanel, gbc);

        addButtons(formPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane);
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

    private void addInputField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int gridY, boolean isReadOnly) {
        gbc.gridy = gridY;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;

        if (field instanceof JTextField) {
            ((JTextField) field).setEditable(!isReadOnly);
        } else if (field instanceof JFormattedTextField) {
            ((JFormattedTextField) field).setEditable(!isReadOnly);
        }

        panel.add(field, gbc);
    }

    private void addDropdownField(JPanel panel, String label, JComboBox<String> comboBox, GridBagConstraints gbc, int gridY) {
        gbc.gridy = gridY;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(comboBox, gbc);
    }

    private void addStudentFields(JPanel panel, GridBagConstraints gbc) {
        addInputField(panel, "Student ID:", txtStudentID = new JTextField(15), gbc, 1, true);
        addInputField(panel, "First Name:", txtFirstName = new JTextField(15), gbc, 2, false);
        addInputField(panel, "Last Name:", txtLastName = new JTextField(15), gbc, 3, false);
        addInputField(panel, "Date of Birth (YYYY/MM/DD):", txtDOB = new JTextField(15), gbc, 4, false);
        addDropdownField(panel, "Grade:", cmbGrade = new JComboBox<>(new String[]{"10", "11", "12", "13"}), gbc, 5);
        addInputField(panel, "School:", txtSchool = new JTextField(15), gbc, 6, false);
    }

    private void addContactFields(JPanel panel, GridBagConstraints gbc) {
        txtContactNumber = createFormattedPhoneField();
        addInputField(panel, "Contact Number (XXX) XXX-XXXX:", txtContactNumber, gbc, 8, false);
        addInputField(panel, "Email:", txtEmail = new JTextField(15), gbc, 9, false);
        addInputField(panel, "Address:", txtAddress = new JTextField(15), gbc, 10, false);
    }

    private void addGuardianFields(JPanel panel, GridBagConstraints gbc) {
        addGuardianInputFields(panel, "Guardian 1", txtGuardianFirstName1 = new JTextField(15),
        txtGuardianLastName1 = new JTextField(15), txtGuardianRelation1 = new JTextField(15),
        txtGuardianPhone1 = new JTextField(15), txtGuardianEmail1 = new JTextField(15), gbc, 12);
        
        addGuardianInputFields(panel, "Guardian 2", txtGuardianFirstName2 = new JTextField(15),
        txtGuardianLastName2 = new JTextField(15), txtGuardianRelation2 = new JTextField(15),
        txtGuardianPhone2 = new JTextField(15), txtGuardianEmail2 = new JTextField(15), gbc, 18);

    }

    private void addGuardianInputFields(JPanel panel, String guardianLabel, JTextField firstName, JTextField lastName,
                                        JTextField relation, JTextField phone, JTextField email, GridBagConstraints gbc, int gridY) {
        gbc.gridy = gridY;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JLabel guardianLabelHeading = new JLabel(guardianLabel, SwingConstants.LEFT);
        guardianLabelHeading.setFont(new Font("Arial", Font.BOLD, 14));
        guardianLabelHeading.setForeground(new Color(0, 70, 140));

        panel.add(guardianLabelHeading, gbc);
        gbc.gridwidth = 1;

        addInputField(panel, "First Name:", firstName, gbc, gridY + 1, false);
        addInputField(panel, "Last Name:", lastName, gbc, gridY + 2, false);
        addInputField(panel, "Relation:", relation, gbc, gridY + 3, false);
        addInputField(panel, "Phone (XXX) XXX-XXXX:", phone, gbc, gridY + 4, false);
        addInputField(panel, "Email:", email, gbc, gridY + 5, false);


    }

    private void addButtons(JPanel panel, GridBagConstraints gbc) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        saveButton = createStyledButton("Save");
        cancelButton = createStyledButton("Close");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = 30;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        panel.add(buttonPanel, gbc);
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


    private void loadStudentData() {
        if (existingStudent == null) return;
    
        txtStudentID.setText(existingStudent.getStudentID());
        txtFirstName.setText(existingStudent.getFirstName());
        txtLastName.setText(existingStudent.getLastName());
        txtDOB.setText(existingStudent.getDateOfBirth());
        cmbGrade.setSelectedItem(existingStudent.getCurrentGrade());
        txtSchool.setText(existingStudent.getCurrentSchool());
    
        ContactInfo contact = existingStudent.getContactInfo();
        if (contact != null) {
            txtContactNumber.setText(contact.getPhoneNumber());
            txtEmail.setText(contact.getEmail());
            txtAddress.setText(contact.getAddress());
        }
    
        List<GuardianInfo> guardians = existingStudent.getGuardians();
        
        if (guardians.size() > 0) {
            txtGuardianFirstName1.setText(guardians.get(0).getGuardianFirstName());
            txtGuardianLastName1.setText(guardians.get(0).getGuardianLastName());
            txtGuardianRelation1.setText(guardians.get(0).getRelation());
            txtGuardianPhone1.setText(guardians.get(0).getPhoneNumber());
            txtGuardianEmail1.setText(guardians.get(0).getEmail());
        } else {
            txtGuardianFirstName1.setText("N/A");
            txtGuardianLastName1.setText("N/A");
            txtGuardianRelation1.setText("N/A");
            txtGuardianPhone1.setText("N/A");
            txtGuardianEmail1.setText("N/A");
        }
    
        if (guardians.size() > 1) {
            txtGuardianFirstName2.setText(guardians.get(1).getGuardianFirstName());
            txtGuardianLastName2.setText(guardians.get(1).getGuardianLastName());
            txtGuardianRelation2.setText(guardians.get(1).getRelation());
            txtGuardianPhone2.setText(guardians.get(1).getPhoneNumber());
            txtGuardianEmail2.setText(guardians.get(1).getEmail());
        } else {
            txtGuardianFirstName2.setText("N/A");
            txtGuardianLastName2.setText("N/A");
            txtGuardianRelation2.setText("N/A");
            txtGuardianPhone2.setText("N/A");
            txtGuardianEmail2.setText("N/A");
        }
    }
    


    private JFormattedTextField createFormattedPhoneField() {
        try {
            MaskFormatter formatter = new MaskFormatter("(###) ###-####");
            formatter.setPlaceholderCharacter('_');
            return new JFormattedTextField(formatter);
        } catch (Exception e) {
            return new JFormattedTextField();
        }
    }

    private void setupEventHandlers() {
        saveButton.addActionListener(e -> saveStudent());
        cancelButton.addActionListener(e -> dispose());
    }

    private void saveStudent() {
        try {
            String phone1 = txtGuardianPhone1.getText().trim();
            String phone2 = txtGuardianPhone2.getText().trim();
            String email1 = txtGuardianEmail1.getText().trim();
            String email2 = txtGuardianEmail2.getText().trim();
    
            if (!phone1.equalsIgnoreCase("N/A") && !phone1.matches("\\(\\d{3}\\) \\d{3}-\\d{4}")) {
                JOptionPane.showMessageDialog(this, "Guardian 1 Phone must be in (XXX) XXX-XXXX format or 'N/A'", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!phone2.equalsIgnoreCase("N/A") && !phone2.matches("\\(\\d{3}\\) \\d{3}-\\d{4}")) {
                JOptionPane.showMessageDialog(this, "Guardian 2 Phone must be in (XXX) XXX-XXXX format or 'N/A'", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!email1.equalsIgnoreCase("N/A") && !email1.matches("^[\\w\\.-]+@[\\w\\.-]+\\.\\w{2,}$")) {
                JOptionPane.showMessageDialog(this, "Guardian 1 Email must be a valid email or 'N/A'", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!email2.equalsIgnoreCase("N/A") && !email2.matches("^[\\w\\.-]+@[\\w\\.-]+\\.\\w{2,}$")) {
                JOptionPane.showMessageDialog(this, "Guardian 2 Email must be a valid email or 'N/A'", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            ContactInfo contactInfo = new ContactInfo(
                    txtContactNumber.getText(),
                    txtEmail.getText(),
                    txtAddress.getText()
            );
    
            Student newStudent = new Student(
                    txtStudentID.getText(),
                    txtFirstName.getText(),
                    txtLastName.getText(),
                    txtDOB.getText(),
                    (String) cmbGrade.getSelectedItem(),
                    txtSchool.getText(),
                    contactInfo
            );
    
            newStudent.addGuardian(new GuardianInfo(txtGuardianFirstName1.getText(), txtGuardianLastName1.getText(),
                    txtGuardianRelation1.getText(), phone1, email1));
    
            newStudent.addGuardian(new GuardianInfo(txtGuardianFirstName2.getText(), txtGuardianLastName2.getText(),
                    txtGuardianRelation2.getText(), phone2, email2));
    
            if (existingStudent == null) {
                studentController.addStudent(newStudent);
            } else {
                studentController.updateStudent(newStudent);
            }
    
            JOptionPane.showMessageDialog(this, "Student saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
    
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

}
