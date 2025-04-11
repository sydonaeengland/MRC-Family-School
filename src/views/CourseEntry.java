package views;

import controllers.CourseController;
import models.Course;
import utils.Subjects;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CourseEntry extends JDialog {
    private JTextField courseIDField;
    private JComboBox<String> subjectDropdown, gradeDropdown, examTypeDropdown, teacherDropdown;
    private JButton saveButton, cancelButton;
    private CourseManagementUI courseManagementUI;
    private CourseController courseController;
    private Course existingCourse;

    public CourseEntry(JFrame parent, CourseController courseController, Course course, CourseManagementUI ui) {
        super(parent, course == null ? "Add New Course" : "Update Course", true);
        this.courseController = courseController;
        this.existingCourse = course;
        this.courseManagementUI = ui;

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("COURSE ENTRY", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setOpaque(true);
        headerLabel.setBackground(new Color(0, 70, 140));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(headerLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.weightx = 1;
        gbc.gridx = 0;

        addSectionHeader(formPanel, "COURSE INFORMATION", gbc, 0);

        courseIDField = new JTextField(15);
        courseIDField.setEditable(false);
        addInputField(formPanel, "Course ID:", courseIDField, gbc, 1);

        subjectDropdown = new JComboBox<>(Subjects.getAvailableSubjects().toArray(new String[0]));
        addDropdownField(formPanel, "Subject:", subjectDropdown, gbc, 2);

        gradeDropdown = new JComboBox<>(new String[]{"10", "11", "12", "13"});
        addDropdownField(formPanel, "Grade Level:", gradeDropdown, gbc, 3);

        examTypeDropdown = new JComboBox<>(new String[]{"CSEC", "CAPE", "A-LEVELS", "GCSE"});
        addDropdownField(formPanel, "Exam Type:", examTypeDropdown, gbc, 4);

        teacherDropdown = new JComboBox<>();
        addDropdownField(formPanel, "Assign Teacher:", teacherDropdown, gbc, 5);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        saveButton = createStyledButton("Save");
        cancelButton = createStyledButton("Cancel");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setupListeners();

        if (existingCourse == null) {
            updateCourseID();
            updateTeacherList();
        } else {
            loadCourseData();
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

    private void addInputField(JPanel panel, String label, JTextField field, GridBagConstraints gbc, int gridY) {
        gbc.gridy = gridY;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addDropdownField(JPanel panel, String label, JComboBox<String> comboBox, GridBagConstraints gbc, int gridY) {
        gbc.gridy = gridY;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(comboBox, gbc);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(150, 40));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 123, 255));
        button.setFocusPainted(false);
        return button;
    }

    private void setupListeners() {
        subjectDropdown.addActionListener(e -> {
            updateCourseID();
            updateTeacherList();
        });

        gradeDropdown.addActionListener(e -> updateCourseID());
        examTypeDropdown.addActionListener(e -> updateCourseID());

        saveButton.addActionListener(e -> saveCourse());
        cancelButton.addActionListener(e -> dispose());
    }

    private void updateCourseID() {
        if (existingCourse == null) {
            String subject = (String) subjectDropdown.getSelectedItem();
            String grade = (String) gradeDropdown.getSelectedItem();
            String examType = (String) examTypeDropdown.getSelectedItem();
            String generatedID = CourseController.generateCourseID(subject, grade, examType);
            courseIDField.setText(generatedID);
        }
    }

    private void updateTeacherList() {
        String selectedSubject = (String) subjectDropdown.getSelectedItem();
        teacherDropdown.removeAllItems();

        if (selectedSubject != null) {
            List<String> teachers = courseController.getEligibleTeachers(selectedSubject);
            if (teachers.isEmpty()) {
                teacherDropdown.addItem("No eligible teacher found");
                teacherDropdown.setEnabled(false);
            } else {
                for (String teacher : teachers) {
                    teacherDropdown.addItem(teacher);
                }
                teacherDropdown.setEnabled(true);
            }
        }
    }

    private void loadCourseData() {
        courseIDField.setText(existingCourse.getCourseID());
        subjectDropdown.setSelectedItem(existingCourse.getSubject());
        gradeDropdown.setSelectedItem(existingCourse.getGradeLevel());
        examTypeDropdown.setSelectedItem(existingCourse.getExamType());

        updateTeacherList();
        for (int i = 0; i < teacherDropdown.getItemCount(); i++) {
            String item = teacherDropdown.getItemAt(i);
            if (item != null && item.contains(existingCourse.getTeacher())) {
                teacherDropdown.setSelectedIndex(i);
                break;
            }
        }
    }

    private void saveCourse() {
        String newSubject = (String) subjectDropdown.getSelectedItem();
        String newGrade = (String) gradeDropdown.getSelectedItem();
        String newExamType = (String) examTypeDropdown.getSelectedItem();
        String selectedTeacher = (String) teacherDropdown.getSelectedItem();
    
        if (selectedTeacher == null || selectedTeacher.startsWith("No eligible")) {
            JOptionPane.showMessageDialog(this, "Please select a valid teacher.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        String teacherName = selectedTeacher.substring(selectedTeacher.indexOf("-") + 2).trim();
    
        try {
            if (existingCourse == null) {
                String generatedID = CourseController.generateCourseID(newSubject, newGrade, newExamType);
                courseIDField.setText(generatedID);
                courseController.createCourse(generatedID, newGrade, newSubject, newExamType, teacherName);
                JOptionPane.showMessageDialog(this, "Course successfully created!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String oldID = existingCourse.getCourseID();
                String newID = CourseController.generateCourseID(newSubject, newGrade, newExamType);
                boolean idChanged = !oldID.equals(newID);
                boolean gradeChanged = !existingCourse.getGradeLevel().equals(newGrade);
    
                if (idChanged) {
                    courseController.renameCourseFiles(oldID, newID);
                    courseController.updateCourseIDInDatabase(oldID, newID);
                    existingCourse.setCourseID(newID);
                }
    
                existingCourse.setSubject(newSubject);
                existingCourse.setGradeLevel(newGrade);
                existingCourse.setExamType(newExamType);
    

                List<String> eligible = courseController.getEligibleTeachers(newSubject);
                if (!eligible.stream().anyMatch(t -> t.contains(teacherName))) {
                    existingCourse.setTeacher("");
                } else {
                    existingCourse.setTeacher(teacherName);
                }
    
                if (gradeChanged) {
                    courseController.clearStudentsFromCourse(existingCourse.getCourseID());
                }
    
                courseController.saveUpdatedCourseData(existingCourse, false);
                JOptionPane.showMessageDialog(this, "Course successfully updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
    
            if (courseManagementUI != null) {
                courseManagementUI.refreshCoursesImmediately();
                courseManagementUI.switchToSubjectTab(newSubject);
                courseManagementUI.revalidate();
                courseManagementUI.repaint();
            }
            
    
            dispose(); 
    
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving course: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
    
    
}
