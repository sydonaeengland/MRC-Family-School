package views;

import controllers.StaffController;
import controllers.StudentController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame {
    private JButton studentBtn, classBtn, gradeBtn, staffBtn, reportBtn, attendBtn;

    public MainMenu() {
        setTitle("School Management System");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(0, 120, 215));
        titleBar.setPreferredSize(new Dimension(getWidth(), 70));

        JLabel menuIcon = new JLabel("\u2630"); 
        menuIcon.setFont(new Font("SansSerif", Font.BOLD, 28));
        menuIcon.setForeground(Color.WHITE);
        menuIcon.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        titleBar.add(menuIcon, BorderLayout.WEST);

        JLabel title = new JLabel("MRC SCHOOL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        titleBar.add(title, BorderLayout.CENTER);

        add(titleBar, BorderLayout.NORTH);


        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 30, 30));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 60, 60));

        studentBtn = createStyledButton("Student Management", "/resources/icons/student_icon.png", e -> openStudentManagement());
        classBtn = createStyledButton("Course Management", "/resources/icons/class_icon.png", e -> openClassManagement());
        attendBtn = createStyledButton("Attendance Management", "/resources/icons/attendance_icon.png", e -> openAttendanceManagement());
        gradeBtn = createStyledButton("Grade Management", "/resources/icons/grades_icon.png", e -> openGradeManagement());
        staffBtn = createStyledButton("Staff Management", "/resources/icons/staff_icon.png", e -> openStaffManagement());
        reportBtn = createStyledButton("Analytics & Reports", "/resources/icons/report_icon.png", e -> openReportManagement());

        gridPanel.add(studentBtn);
        gridPanel.add(classBtn);
        gridPanel.add(attendBtn);
        gridPanel.add(gradeBtn);
        gridPanel.add(staffBtn);
        gridPanel.add(reportBtn);

        add(gridPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text, String iconPath, ActionListener action) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image scaledImage = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH); // larger icon
            JLabel iconLabel = new JLabel(new ImageIcon(scaledImage), JLabel.CENTER);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
            button.add(iconLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            System.err.println("Icon not found: " + iconPath);
        }

        JPanel textPanel = new JPanel();
        textPanel.setBackground(new Color(0, 120, 215));
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        textPanel.add(label);
        button.add(textPanel, BorderLayout.SOUTH);

        button.addActionListener(action);
        return button;
    }

    private void openStudentManagement() {
        StudentController studentController = new StudentController();
        openManagementWindow("Student Management", new StudentManagementUI(studentController));
    }

    private void openClassManagement() {
        openManagementWindow("Course Management", new CourseManagementUI(this, new controllers.CourseController()));
    }

    private void openAttendanceManagement() {
    }

    private void openGradeManagement() {
    }

    private void openStaffManagement() {
        StaffController staffController = new StaffController();
        openManagementWindow("Staff Management", new StaffManagementUI(staffController));
    }


    private void openReportManagement() {
    }

    private void openManagementWindow(String title, JPanel panel) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 600);
        frame.add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}
