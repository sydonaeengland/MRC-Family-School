package views;

import controllers.AttendanceController;
import controllers.CourseController;
import models.Course;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRegister extends JPanel {
    private Course course;
    private JTable table;
    private JFrame parentFrame;
    private DefaultTableModel model;
    private AttendanceController attendanceController;

    public AttendanceRegister(JFrame parentFrame, Course course) {
        this.parentFrame = parentFrame;
        this.course = course;
        this.attendanceController = new AttendanceController();

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(course.getCourseID() + " REGISTER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0, 70, 140));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(2, 2));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        infoPanel.setBackground(Color.WHITE);

        Course fullCourse = new CourseController().getCourseByID(course.getCourseID());

        infoPanel.add(new JLabel("Teacher: " + (fullCourse.getTeacher().isEmpty() ? "N/A" : fullCourse.getTeacher())));
        infoPanel.add(new JLabel("Subject: " + fullCourse.getSubject()));
        infoPanel.add(new JLabel("Exam Type: " + fullCourse.getExamType()));
        infoPanel.add(new JLabel("Grade Level: " + fullCourse.getGradeLevel()));

        centerPanel.add(infoPanel, BorderLayout.NORTH);

        model = new DefaultTableModel();
        table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0, 120, 215));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        buttonPanel.setBackground(new Color(0, 70, 140));

        JButton markBtn = createStyledButton("Mark Attendance");
        JButton updateBtn = createStyledButton("Update Attendance");
        JButton deleteBtn = createStyledButton("Delete Attendance");
        JButton backBtn = createStyledButton("Back");

        buttonPanel.add(markBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(backBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        markBtn.addActionListener(e -> markAttendance());
        updateBtn.addActionListener(e -> updateAttendance());
        deleteBtn.addActionListener(e -> deleteAttendanceColumn());

        backBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose(); 
            }
        
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Attendance Management");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(900, 600);
                frame.setLocationRelativeTo(null);
        
                CourseController courseController = new CourseController();
                AttendanceManagementUI attendanceManagementUI = new AttendanceManagementUI(frame, courseController);
                frame.add(attendanceManagementUI);
                frame.setVisible(true);
            });
        });
        
        

        loadAttendanceTable();
    }

    private void loadAttendanceTable() {
        model.setRowCount(0);
        model.setColumnCount(0);
    
        List<String[]> data = attendanceController.getAttendanceDataForCourse(course.getCourseID());
    
        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
    
            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) row[j] = "";
            }
    
            if (i == 0) {
                for (String col : row) model.addColumn(col);
            } else {
                model.addRow(row);
                for (int j = 2; j < row.length; j++) {
                    if (row[j].trim().isEmpty()) {
                        model.setValueAt("Added late", i - 1, j);
                    }
                }
            }
        }
    }
    

    private JButton createStyledButton(String text) {
        JButton button = new JButton("<html><center>" + text + "</center></html>");
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 120, 215));
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 10, 5, 10));
        button.setPreferredSize(new Dimension(140, 40));
        return button;
    }

    private void markAttendance() {
        AttendanceEntry entryForm = new AttendanceEntry(parentFrame, course, this::loadAttendanceTable);
        entryForm.setVisible(true);
    }

    private void updateAttendance() {
        List<String[]> data = attendanceController.getAttendanceDataForCourse(course.getCourseID());

        if (data.isEmpty() || data.size() < 2 || data.get(0).length <= 2) {
            JOptionPane.showMessageDialog(this, "No attendance records available to update.", "Nothing to Update", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] headers = data.get(0);
        List<String> attendanceDates = new ArrayList<>();
        for (int i = 2; i < headers.length; i++) {
            attendanceDates.add(headers[i]);
        }

        String selectedDate = (String) JOptionPane.showInputDialog(
            this,
            "Select a date to update attendance:",
            "Update Attendance",
            JOptionPane.QUESTION_MESSAGE,
            null,
            attendanceDates.toArray(),
            attendanceDates.get(0)
        );

        if (selectedDate != null) {
            AttendanceEntry updateForm = new AttendanceEntry(parentFrame, course, this::loadAttendanceTable, selectedDate);
            updateForm.setVisible(true);
        }
    }


    private void deleteAttendanceColumn() {
        List<String[]> data = attendanceController.getAttendanceDataForCourse(course.getCourseID());
    
        if (data.isEmpty() || data.size() < 2 || data.get(0).length <= 2) {
            JOptionPane.showMessageDialog(this, "No attendance dates available to delete.", "Nothing to Delete", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    
        String[] headers = data.get(0);
        List<String> attendanceDates = new ArrayList<>();
        for (int i = 2; i < headers.length; i++) {
            attendanceDates.add(headers[i]);
        }
    
        String selectedDate = (String) JOptionPane.showInputDialog(
            this,
            "Select a date to delete attendance:",
            "Delete Attendance",
            JOptionPane.QUESTION_MESSAGE,
            null,
            attendanceDates.toArray(),
            attendanceDates.get(0)
        );
    
        if (selectedDate != null) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete attendance for " + selectedDate + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );
    
            if (confirm == JOptionPane.YES_OPTION) {
                attendanceController.deleteAttendanceColumn(course.getCourseID(), selectedDate);
                loadAttendanceTable();
                JOptionPane.showMessageDialog(this, "Attendance for " + selectedDate + " was deleted.");
            }
        }
    }
    
}
