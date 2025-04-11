package views;

import controllers.CourseController;
import models.Course;
import utils.Subjects;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AttendanceManagementUI extends JPanel {
    private JFrame parentFrame;
    private CourseController courseController;
    private JTabbedPane subjectTabs;
    private Map<String, JTable> subjectTables = new HashMap<>();
    private Map<String, DefaultTableModel> tableModels = new HashMap<>();

    public AttendanceManagementUI(JFrame parentFrame, CourseController courseController) {
        this.parentFrame = parentFrame;
        this.courseController = courseController;
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("MRC ATTENDANCE MANAGEMENT", SwingConstants.CENTER);
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
        loadCoursesForAttendance();
    }

    private void setupSubjectTabs() {
        for (String subject : Subjects.getAvailableSubjects()) {
            DefaultTableModel model = new DefaultTableModel(new String[]{"Class ID", "Grade", "Subject", "Exam Type"}, 0);
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
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(0, 70, 140));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton openRegisterBtn = new JButton("Open Register");
        openRegisterBtn.setFont(new Font("Arial", Font.BOLD, 13));
        openRegisterBtn.setForeground(Color.WHITE);
        openRegisterBtn.setBackground(new Color(0, 120, 215));
        openRegisterBtn.setFocusPainted(false);
        openRegisterBtn.setPreferredSize(new Dimension(160, 40));
        openRegisterBtn.addActionListener(e -> openRegisterTab());

        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Arial", Font.BOLD, 13));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(0, 120, 215));
        backBtn.setFocusPainted(false);
        backBtn.setPreferredSize(new Dimension(160, 40));
        backBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }

            SwingUtilities.invokeLater(() -> {
                MainMenu mainMenu = new MainMenu();
                mainMenu.setVisible(true);
            });
        });

        buttonPanel.add(openRegisterBtn);
        buttonPanel.add(backBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCoursesForAttendance() {
        for (DefaultTableModel model : tableModels.values()) {
            model.setRowCount(0);
        }

        List<Course> courses = courseController.getCourses();

        for (Course course : courses) {
            DefaultTableModel model = tableModels.get(course.getSubject());
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

    public JTable getCurrentSelectedTable() {
        int index = subjectTabs.getSelectedIndex();
        if (index == -1) return null;
        String subject = subjectTabs.getTitleAt(index);
        return subjectTables.get(subject);
    }

    public String getSelectedCourseID() {
        JTable table = getCurrentSelectedTable();
        if (table != null && table.getSelectedRow() != -1) {
            return (String) table.getValueAt(table.getSelectedRow(), 0);
        }
        return null;
    }

    public void refreshCourseList() {
        loadCoursesForAttendance();
    }

    private void openRegisterTab() {
        JTable table = getCurrentSelectedTable();

        if (table == null || table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course from the table first.", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseID = (String) table.getValueAt(table.getSelectedRow(), 0);
        String grade = (String) table.getValueAt(table.getSelectedRow(), 1);
        String subject = (String) table.getValueAt(table.getSelectedRow(), 2);
        String examType = (String) table.getValueAt(table.getSelectedRow(), 3);

        Course course = new Course(courseID, grade, subject, examType, "");

        try {
            Window currentWindow = SwingUtilities.getWindowAncestor(this);
            if (currentWindow != null) currentWindow.dispose();

            JFrame registerFrame = new JFrame("Attendance Register - " + courseID);
            registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            registerFrame.setSize(850, 600);
            registerFrame.setLocationRelativeTo(null);

            AttendanceRegister registerPanel = new AttendanceRegister(registerFrame, course);
            registerFrame.add(registerPanel);
            registerFrame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening register: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
