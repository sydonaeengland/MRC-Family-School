package views;

import controllers.CourseController;
import controllers.StudentController;
import models.Course;
import models.Student;
import utils.Subjects;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class CourseManagementUI extends JPanel {
    private JFrame parentFrame;
    private CourseController courseController;

    private JTabbedPane subjectTabs;
    private Map<String, JTable> subjectTables = new HashMap<>();
    private Map<String, DefaultTableModel> tableModels = new HashMap<>();

    public CourseManagementUI(JFrame parentFrame, CourseController courseController) {
        this.parentFrame = parentFrame;
        this.courseController = courseController;
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("MRC COURSE MANAGEMENT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0, 70, 140));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        subjectTabs = new JTabbedPane();
        add(subjectTabs, BorderLayout.CENTER);

        setupSubjectTabs();
        setupButtons();
        loadAllCourses();
    }

    private void setupSubjectTabs() {
        for (String subject : Subjects.getAvailableSubjects()) {
            DefaultTableModel model = new DefaultTableModel(new String[]{"Course ID", "Grade", "Subject", "Exam Type"}, 0);
            JTable table = new JTable(model);
            table.setRowHeight(25);
            table.setFont(new Font("Arial", Font.PLAIN, 12));
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
            table.getTableHeader().setBackground(new Color(0, 120, 215));
            table.getTableHeader().setForeground(Color.WHITE);

            tableModels.put(subject, model);
            subjectTables.put(subject, table);
            subjectTabs.add(subject, new JScrollPane(table));
        }
    }

    private void setupButtons() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 9, 6, 0)); 
        buttonPanel.setBackground(new Color(0, 70, 140));
    
        buttonPanel.add(createButton("Add New Course", e -> openCourseEntryForm()));
        buttonPanel.add(createButton("Update Course", e -> updateCourse()));
        buttonPanel.add(createButton("Delete Course", e -> deleteCourse()));
        buttonPanel.add(createButton("Assign Students", e -> assignStudents()));
        buttonPanel.add(createButton("Remove Student", e -> removeStudent()));
        buttonPanel.add(createButton("View Course Data", e -> viewCourseDetails()));
        buttonPanel.add(createButton("View Student Courses", e -> viewStudentCourses()));
        buttonPanel.add(createButton("View Teacher Courses", e -> viewTeacherCourses()));
    
        JButton backBtn = new JButton("<html><center>Back</center></html>");
        backBtn.setFont(new Font("Arial", Font.BOLD, 11));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(30, 60, 90)); 
        backBtn.setFocusPainted(false);
        backBtn.setMargin(new Insets(4, 4, 4, 4));
        backBtn.setPreferredSize(new Dimension(90, 48));

        backBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();  

            SwingUtilities.invokeLater(() -> {
                MainMenu mainMenu = new MainMenu();
                mainMenu.setVisible(true);
            });
        });

        buttonPanel.add(backBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    
    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton("<html><center>" + text + "</center></html>");
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 120, 215));
        button.setFocusPainted(false);
        button.setMargin(new Insets(4, 4, 4, 4));
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setPreferredSize(new Dimension(90, 48));
        button.addActionListener(actionListener);
        return button;
    }

    public void loadAllCourses() {
        for (DefaultTableModel model : tableModels.values()) model.setRowCount(0);
    
        List<Course> sortedCourses = courseController.getCoursesSortedByGradeThenID();
    
        for (Course course : sortedCourses) {
            String subject = course.getSubject();
            DefaultTableModel model = tableModels.get(subject);
            if (model != null) {
                model.addRow(new Object[]{
                    course.getCourseID(),
                    course.getGradeLevel(),
                    course.getSubject(),
                    course.getExamType()
                });
            }
        }
    }
    

    private JTable getCurrentTable() {
        int index = subjectTabs.getSelectedIndex();
        if (index == -1) return null;
        String subject = subjectTabs.getTitleAt(index);
        return subjectTables.get(subject);
    }

    private void openCourseEntryForm() {
        CourseEntry courseEntry = new CourseEntry(parentFrame, courseController, null, this);
        courseEntry.setVisible(true);
    }

    private void updateCourse() {
        JTable table = getCurrentTable();
        if (table == null || table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to update.");
            return;
        }

        String oldCourseID = (String) table.getValueAt(table.getSelectedRow(), 0);
        Course oldCourse = courseController.getCourseByID(oldCourseID);

        if (oldCourse == null) {
            JOptionPane.showMessageDialog(this, "Course not found.");
            return;
        }

        CourseEntry entry = new CourseEntry(parentFrame, courseController, oldCourse, this);
        entry.setVisible(true); 

        Course updatedCourse = courseController.getCourseByID(oldCourseID);
        if (updatedCourse == null) return;

        String newID = CourseController.generateCourseID(
                updatedCourse.getSubject(),
                updatedCourse.getGradeLevel(),
                updatedCourse.getExamType()
        );

        boolean idChanged = !newID.equals(oldCourseID);
        boolean gradeChanged = !Objects.equals(oldCourse.getGradeLevel(), updatedCourse.getGradeLevel());
        boolean subjectChanged = !Objects.equals(oldCourse.getSubject(), updatedCourse.getSubject());

        try {
            if (idChanged) {
                courseController.renameCourseFiles(oldCourseID, newID);
                courseController.updateCourseIDInDatabase(oldCourseID, newID);
                updatedCourse.setCourseID(newID);
            }

            courseController.saveUpdatedCourseData(updatedCourse, gradeChanged);

            loadAllCourses();
            if (subjectChanged) {
                switchToSubjectTab(updatedCourse.getSubject());
            }

            JOptionPane.showMessageDialog(this, "Course updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating course: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCourse() {
        JTable table = getCurrentTable();
        if (table == null || table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.");
            return;
        }
    
        String courseID = (String) table.getValueAt(table.getSelectedRow(), 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete course " + courseID + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
    
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                courseController.deleteCourseByID(courseID);
                loadAllCourses();
                JOptionPane.showMessageDialog(this, "Course " + courseID + " deleted successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error deleting course: " + e.getMessage());
            }
        }
    }
    
    
    
    private void assignStudents() {
        JTable table = getCurrentTable();
        if (table == null || table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course first.");
            return;
        }

        String courseID = (String) table.getValueAt(table.getSelectedRow(), 0);
        String gradeLevel = (String) table.getValueAt(table.getSelectedRow(), 1);

        StudentController studentController = new StudentController();
        List<Student> students = studentController.getStudentsByGrade(gradeLevel);
        Set<String> assigned = courseController.getAssignedStudentIDs(courseID);

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        Map<JCheckBox, String> checkMap = new LinkedHashMap<>();

        for (Student s : students) {
            if (!assigned.contains(s.getStudentID())) {
                String label = s.getStudentID() + " - " + s.getFirstName() + " " + s.getLastName();
                JCheckBox box = new JCheckBox(label);
                checkMap.put(box, label);
                checkBoxPanel.add(box);
            }
        }

        if (checkMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All students are already assigned.");
            return;
        }

        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setPreferredSize(new Dimension(350, 350));

        JButton assignBtn = new JButton("Assign Selected");
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(assignBtn);

        JDialog dialog = new JDialog(parentFrame, "Assign Students to " + courseID, true);
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        assignBtn.addActionListener(e -> {
            List<String> selected = new ArrayList<>();
            for (Map.Entry<JCheckBox, String> entryBox : checkMap.entrySet()) {
                if (entryBox.getKey().isSelected()) {
                    selected.add(entryBox.getValue());
                }
            }

            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No students selected.");
                return;
            }

            try {
                courseController.assignStudentsToCourse(courseID, selected);
                JOptionPane.showMessageDialog(dialog, "Students assigned.");
                dialog.dispose();
                loadAllCourses();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void removeStudent() {
        JTable table = getCurrentTable();
        if (table == null || table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course first.");
            return;
        }
    
        String courseID = (String) table.getValueAt(table.getSelectedRow(), 0);
        List<String> enrolledStudents = courseController.getAssignedStudents(courseID); 
    
        if (enrolledStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students assigned to this course.");
            return;
        }
    
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        Map<JCheckBox, String> checkMap = new LinkedHashMap<>();
    
        for (String line : enrolledStudents) {
            JCheckBox box = new JCheckBox(line);
            checkMap.put(box, line);
            checkBoxPanel.add(box);
        }
    
        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setPreferredSize(new Dimension(400, 350));
    
        JButton removeBtn = new JButton("Remove Selected");
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(removeBtn);
    
        JDialog dialog = new JDialog(parentFrame, "Remove Students from " + courseID, true);
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
    
        removeBtn.addActionListener(e -> {
            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<JCheckBox, String> entry : checkMap.entrySet()) {
                if (entry.getKey().isSelected()) {
                    String id = extractStudentID(entry.getValue());
                    if (id != null) toRemove.add(id);
                }
            }
    
            if (toRemove.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No students selected.");
                return;
            }
    
            try {
                courseController.removeStudentsFromCourse(courseID, toRemove);
                JOptionPane.showMessageDialog(dialog, "Selected students removed.");
                dialog.dispose();
                loadAllCourses();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
    
        dialog.setVisible(true);
    }
    
    
    private void viewCourseDetails() {
        JTable table = getCurrentTable();
        if (table == null || table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course.");
            return;
        }

        String courseID = (String) table.getValueAt(table.getSelectedRow(), 0);
        String courseDetails = courseController.getCourseDetails(courseID);

        if (courseDetails == null || courseDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No details found for the selected course.");
            return;
        }

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Course Details for " + courseID);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(title);
        detailsPanel.add(Box.createVerticalStrut(10));

        JPanel courseCard = new JPanel(new BorderLayout());
        courseCard.setBorder(BorderFactory.createLineBorder(new Color(0, 70, 140), 2));
        courseCard.setBackground(Color.WHITE);
        courseCard.setPreferredSize(new Dimension(500, 350));

        JPanel header = new JPanel();
        header.setBackground(new Color(0, 70, 140));
        JLabel headerLabel = new JLabel("  Course Info", SwingConstants.LEFT);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerLabel.setForeground(Color.WHITE);
        header.add(headerLabel);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] lines = courseDetails.split("\n");
        boolean inStudents = false;

        for (String line : lines) {
            if (line.trim().equalsIgnoreCase("Students:")) {
                inStudents = true;
                body.add(Box.createVerticalStrut(10));
                body.add(new JLabel("<html><b>Assigned Students:</b></html>"));
                continue;
            }

            if (inStudents && line.startsWith("•")) {
                body.add(new JLabel(line.trim()));
            } else if (!inStudents) {
                String[] labelSplit = line.split(":", 2);
                if (labelSplit.length == 2) {
                    body.add(new JLabel("<html><b>" + labelSplit[0].trim() + ":</b> " + labelSplit[1].trim() + "</html>"));
                } else {
                    body.add(new JLabel("<html><b>" + line.trim() + "</b></html>"));
                }
            }
        }

        courseCard.add(header, BorderLayout.NORTH);
        courseCard.add(body, BorderLayout.CENTER);
        detailsPanel.add(courseCard);

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setPreferredSize(new Dimension(600, 500));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JOptionPane.showMessageDialog(this, scrollPane, "Course Details", JOptionPane.PLAIN_MESSAGE);
    }


    private void viewStudentCourses() {
        String input = JOptionPane.showInputDialog(this, "Enter student ID or name:");
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        List<Course> courses = courseController.getCoursesForStudent(input.trim());

        if (courses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No courses found for: " + input);
            return;
        }

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Courses for \"" + input + "\"");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.add(title);
        resultPanel.add(Box.createVerticalStrut(10));

        for (Course course : courses) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBorder(BorderFactory.createLineBorder(new Color(0, 70, 140), 2));
            card.setBackground(Color.WHITE);
            card.setPreferredSize(new Dimension(500, 100));

            JPanel header = new JPanel();
            header.setBackground(new Color(0, 70, 140));
            JLabel headerLabel = new JLabel("  Course Info", SwingConstants.LEFT);
            headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
            headerLabel.setForeground(Color.WHITE);
            header.add(headerLabel);

            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBackground(Color.WHITE);
            body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            body.add(new JLabel("Course ID: " + course.getCourseID()));
            body.add(new JLabel("Subject: " + course.getSubject()));
            body.add(new JLabel("Grade Level: " + course.getGradeLevel()));
            body.add(new JLabel("Exam Type: " + course.getExamType()));
            body.add(new JLabel("Teacher: " + course.getTeacher()));

            card.add(header, BorderLayout.NORTH);
            card.add(body, BorderLayout.CENTER);
            resultPanel.add(card);
            resultPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(resultPanel);
        scroll.setPreferredSize(new Dimension(600, 500));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JOptionPane.showMessageDialog(this, scroll, "Student Course Enrollment", JOptionPane.PLAIN_MESSAGE);
    }

    private void viewTeacherCourses() {
        String input = JOptionPane.showInputDialog(this, "Enter teacher name or ID:");
        if (input == null || input.trim().isEmpty()) {
            return;
        }
    
        List<Course> matchingCourses = courseController.getCoursesByTeacher(input.trim());
    
        if (matchingCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No courses found for: " + input);
            return;
        }
    
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(Color.WHITE);
    
        JLabel title = new JLabel("Courses for \"" + input + "\"");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.add(title);
        resultPanel.add(Box.createVerticalStrut(10));
    
        for (Course course : matchingCourses) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBorder(BorderFactory.createLineBorder(new Color(0, 70, 140), 2));
            card.setBackground(Color.WHITE);
            card.setPreferredSize(new Dimension(500, 100));
    
            JPanel header = new JPanel();
            header.setBackground(new Color(0, 70, 140));
            JLabel headerLabel = new JLabel("  Course Info", SwingConstants.LEFT);
            headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
            headerLabel.setForeground(Color.WHITE);
            header.add(headerLabel);
    
            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBackground(Color.WHITE);
            body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            body.add(new JLabel("Course ID: " + course.getCourseID()));
            body.add(new JLabel("Subject: " + course.getSubject()));
            body.add(new JLabel("Grade Level: " + course.getGradeLevel()));
            body.add(new JLabel("Exam Type: " + course.getExamType()));
            body.add(new JLabel("Teacher: " + course.getTeacher()));
    
            card.add(header, BorderLayout.NORTH);
            card.add(body, BorderLayout.CENTER);
            resultPanel.add(card);
            resultPanel.add(Box.createVerticalStrut(10));
        }
    
        JScrollPane scroll = new JScrollPane(resultPanel);
        scroll.setPreferredSize(new Dimension(600, 500));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
    
        JOptionPane.showMessageDialog(this, scroll, "Teacher Course Assignments", JOptionPane.PLAIN_MESSAGE);
    }
    

    public void switchToSubjectTab(String subjectName) {
        for (int i = 0; i < subjectTabs.getTabCount(); i++) {
            if (subjectTabs.getTitleAt(i).equalsIgnoreCase(subjectName)) {
                subjectTabs.setSelectedIndex(i);
                break;
            }
        }
    }

    public void refreshCoursesImmediately() {
        loadAllCourses();
    }

    private String extractStudentID(String line) {
        try {
            String[] parts = line.split("-");
            return parts.length > 1 ? parts[1].trim() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
}
