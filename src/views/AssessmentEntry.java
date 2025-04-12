package views;

import controllers.AssessmentController;
import models.Assessment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

public class AssessmentEntry extends JDialog {
    private JTextField idField, nameField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> dayBox, monthBox, yearBox;
    private JButton saveBtn, closeBtn;

    private Assessment existingAssessment;
    private Runnable onSaveCallback;
    private AssessmentController assessmentController = new AssessmentController();

    public AssessmentEntry(JFrame parent, String courseID, Runnable onSaveCallback) {
        this(parent, new Assessment("", "", "Test", LocalDate.now().toString(), courseID), onSaveCallback);
    }

    public AssessmentEntry(JFrame parent, Assessment existingAssessment, Runnable onSaveCallback) {
        super(parent, true);
        this.existingAssessment = existingAssessment;
        this.onSaveCallback = onSaveCallback;

        boolean isCreate = existingAssessment.getId().isEmpty();
        setTitle(isCreate ? "Create Assessment" : "Edit Assessment");
        setSize(500, 360);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());


        JLabel header = new JLabel(getTitle(), SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setOpaque(true);
        header.setBackground(new Color(0, 70, 140));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(header, BorderLayout.NORTH);


        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        idField = new JTextField();
        idField.setEnabled(false);

        nameField = new JTextField();
        typeCombo = new JComboBox<>(new String[]{"Test", "Exam"});

        formPanel.add(new JLabel("Assessment ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Assessment Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Assessment Type:"));
        formPanel.add(typeCombo);

        formPanel.add(new JLabel("Date:"));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dayBox = new JComboBox<>();
        for (int i = 1; i <= 31; i++) dayBox.addItem(String.format("%02d", i));
        monthBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) monthBox.addItem(String.format("%02d", i));
        yearBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 1; i <= currentYear + 3; i++) yearBox.addItem(String.valueOf(i));
        datePanel.add(dayBox);
        datePanel.add(monthBox);
        datePanel.add(yearBox);
        formPanel.add(datePanel);

        add(formPanel, BorderLayout.CENTER);


        JPanel bottom = new JPanel();
        saveBtn = new JButton(isCreate ? "Create" : "Update");
        saveBtn.setBackground(new Color(0, 120, 215));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        saveBtn.addActionListener(this::handleSave);

        closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setBackground(Color.GRAY);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> dispose());

        bottom.add(saveBtn);
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        populateFields();

        if (isCreate) {
            typeCombo.addActionListener(e -> updateGeneratedID());
            updateGeneratedID();
        }
    }

    private void populateFields() {
        nameField.setText(existingAssessment.getName());
        typeCombo.setSelectedItem(existingAssessment.getType());

        String[] parts = existingAssessment.getDateString().split("-");
        if (parts.length == 3) {
            yearBox.setSelectedItem(parts[0]);
            monthBox.setSelectedItem(parts[1]);
            dayBox.setSelectedItem(parts[2]);
        }

        if (!existingAssessment.getId().isEmpty()) {
            idField.setText(existingAssessment.getId());
        }
    }

    private void updateGeneratedID() {
        String type = (String) typeCombo.getSelectedItem();
        if (type != null) {
            String generatedID = assessmentController.generateAssessmentID(type);
            idField.setText(generatedID);
        }
    }

    private void handleSave(ActionEvent e) {
        String name = nameField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        String dateStr = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-" + dayBox.getSelectedItem();
        String id = idField.getText().trim();
    
        if (name.isEmpty() || id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }
    
        try {
            if (existingAssessment.getId().isEmpty()) {
                assessmentController.createAssessment(name, type, dateStr, existingAssessment.getCourseID());
                JOptionPane.showMessageDialog(this, "Assessment created.");
            } else {

                existingAssessment.setName(name);
                existingAssessment.setType(type);
                existingAssessment.setDate(dateStr);
                assessmentController.updateAssessment(existingAssessment);
    
                new controllers.GradebookController().updateAssessmentNameInGradebook(
                    existingAssessment.getCourseID(),
                    existingAssessment
                );
    
                JOptionPane.showMessageDialog(this, "Assessment updated.");
            }
    
            if (onSaveCallback != null) onSaveCallback.run();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
}
