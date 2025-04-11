package views;

import controllers.AttendanceController;
import models.AttendanceRecord;
import models.Course;
import models.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class AttendanceEntry extends JDialog {
    private final Course course;
    private final AttendanceController attendanceController;
    private final Map<String, JComboBox<String>> statusSelectors = new LinkedHashMap<>();
    private final Runnable onSaveCallback;
    private String selectedDateToEdit = null;

    private JComboBox<String> dayCombo;
    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;
    private JLabel dynamicDateLabel;

    public AttendanceEntry(JFrame parent, Course course, Runnable onSaveCallback) {
        this(parent, course, onSaveCallback, null);
    }

    public AttendanceEntry(JFrame parent, Course course, Runnable onSaveCallback, String selectedDateToEdit) {
        super(parent, "Attendance - " + course.getCourseID(), true);
        this.course = course;
        this.attendanceController = new AttendanceController();
        this.onSaveCallback = onSaveCallback;
        this.selectedDateToEdit = selectedDateToEdit;

        setSize(600, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(0, 0));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setBackground(new Color(0, 70, 140));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel(selectedDateToEdit != null ? "EDIT ATTENDANCE FOR " : "MARK ATTENDANCE FOR ");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        dynamicDateLabel = new JLabel();
        dynamicDateLabel.setFont(new Font("Arial", Font.BOLD, 20));
        dynamicDateLabel.setForeground(Color.WHITE);

        headerPanel.add(title);
        headerPanel.add(Box.createHorizontalStrut(10));
        headerPanel.add(dynamicDateLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel subHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        subHeader.setBackground(new Color(0, 120, 215));

        JLabel dateLabel = new JLabel("Choose the date");
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));

        dayCombo = new JComboBox<>();
        for (int i = 1; i <= 31; i++) dayCombo.addItem(String.format("%02d", i));

        monthCombo = new JComboBox<>();
        String[] months = new SimpleDateFormat().getDateFormatSymbols().getMonths();
        for (int i = 0; i < 12; i++) monthCombo.addItem(months[i]);

        yearCombo = new JComboBox<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 1; i <= currentYear + 2; i++) yearCombo.addItem(String.valueOf(i));

        subHeader.add(dateLabel);
        subHeader.add(dayCombo);
        subHeader.add(monthCombo);
        subHeader.add(yearCombo);
        add(subHeader, BorderLayout.BEFORE_FIRST_LINE);

        ActionListener updateDateLabel = e -> updateHeaderDate();
        dayCombo.addActionListener(updateDateLabel);
        monthCombo.addActionListener(updateDateLabel);
        yearCombo.addActionListener(updateDateLabel);

        if (selectedDateToEdit != null) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(selectedDateToEdit);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                dayCombo.setSelectedIndex(cal.get(Calendar.DAY_OF_MONTH) - 1);
                monthCombo.setSelectedIndex(cal.get(Calendar.MONTH));
                yearCombo.setSelectedItem(String.valueOf(cal.get(Calendar.YEAR)));
            } catch (Exception ignored) {}
        } else {
            Calendar cal = Calendar.getInstance();
            dayCombo.setSelectedIndex(cal.get(Calendar.DAY_OF_MONTH) - 1);
            monthCombo.setSelectedIndex(cal.get(Calendar.MONTH));
            yearCombo.setSelectedItem(String.valueOf(cal.get(Calendar.YEAR)));
        }

        updateHeaderDate();

        JPanel studentPanel = new JPanel();
        studentPanel.setLayout(new BoxLayout(studentPanel, BoxLayout.Y_AXIS));
        studentPanel.setBackground(Color.WHITE);

        List<Student> students = attendanceController.getAssignedStudentsForCourse(course.getCourseID());
        for (Student student : students) {
            String display = student.getStudentID() + " - " + student.getFirstName() + " " + student.getLastName();
            JLabel nameLabel = new JLabel(display);
            nameLabel.setPreferredSize(new Dimension(280, 25));

            JComboBox<String> status = new JComboBox<>(new String[]{"Present", "Absent", "Late", "Joined Late"});
            status.setPreferredSize(new Dimension(100, 25));

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            row.setBackground(Color.WHITE);
            row.add(nameLabel);
            row.add(status);

            statusSelectors.put(display, status);
            studentPanel.add(row);
        }

        JScrollPane scrollPane = new JScrollPane(studentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(0, 70, 140));

        JButton saveBtn = new JButton("SAVE");
        saveBtn.setPreferredSize(new Dimension(100, 40));
        saveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(new Color(0, 120, 215));

        JButton closeBtn = new JButton("CLOSE");
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(0, 120, 215));

        buttonPanel.add(saveBtn);
        buttonPanel.add(closeBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> saveAttendance());
        closeBtn.addActionListener(e -> dispose());

        if (selectedDateToEdit != null) {
            List<String[]> data = attendanceController.getAttendanceDataForCourse(course.getCourseID());
            if (!data.isEmpty()) {
                String[] headers = data.get(0);
                int dateIndex = -1;
                for (int i = 2; i < headers.length; i++) {
                    if (headers[i].equals(selectedDateToEdit)) {
                        dateIndex = i;
                        break;
                    }
                }

                if (dateIndex != -1) {
                    for (int i = 1; i < data.size(); i++) {
                        String[] row = data.get(i);
                        if (row.length > dateIndex) {
                            String key = row[0] + " - " + row[1];
                            if (statusSelectors.containsKey(key)) {
                                statusSelectors.get(key).setSelectedItem(row[dateIndex]);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateHeaderDate() {
        String displayDate = getFormattedDate("MMMM dd, yyyy");
        dynamicDateLabel.setText(displayDate.toUpperCase());
    }

    private String getSelectedDate() {
        int day = dayCombo.getSelectedIndex() + 1;
        int month = monthCombo.getSelectedIndex(); 
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    private String getFormattedDate(String format) {
        int day = dayCombo.getSelectedIndex() + 1;
        int month = monthCombo.getSelectedIndex(); 
        int year = Integer.parseInt((String) yearCombo.getSelectedItem());

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return new SimpleDateFormat(format).format(cal.getTime());
    }

    private void saveAttendance() {
        String selectedDate = getSelectedDate();
        boolean isEditingDate = selectedDateToEdit != null && !selectedDateToEdit.equals(selectedDate);

        if (isEditingDate) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "You changed the date from " + selectedDateToEdit + " to " + selectedDate + ". This will move the attendance.\nProceed?",
                "Confirm Date Change",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        List<String[]> data = attendanceController.getAttendanceDataForCourse(course.getCourseID());
        Map<String, AttendanceRecord> attendanceMap = new LinkedHashMap<>();
        Set<String> allDates = new TreeSet<>();

        if (!data.isEmpty()) {
            String[] headerRow = data.get(0);
            List<String> existingDates = new ArrayList<>();
            for (int i = 2; i < headerRow.length; i++) {
                allDates.add(headerRow[i]);
                existingDates.add(headerRow[i]);
            }

            for (int i = 1; i < data.size(); i++) {
                String[] row = data.get(i);
                AttendanceRecord record = AttendanceRecord.fromRow(row, existingDates);
                String key = record.getStudentID() + " - " + record.getStudentName();
                attendanceMap.put(key, record);
            }
        }

        allDates.add(selectedDate);

        for (Map.Entry<String, JComboBox<String>> entry : statusSelectors.entrySet()) {
            String displayName = entry.getKey();
            String[] parts = displayName.split(" - ", 2);
            String studentID = parts[0];
            String studentName = parts[1];
            String status = (String) entry.getValue().getSelectedItem();
        
            AttendanceRecord record = attendanceMap.get(displayName);
        
            if (record == null) {
                record = new AttendanceRecord(studentID, studentName);
                for (String pastDate : allDates) {
                    if (!pastDate.equals(selectedDate)) {
                        record.setAttendanceStatus(pastDate, "Joined Late");
                    }
                }
            }
            
        
            record.setAttendanceStatus(selectedDate, status);
            attendanceMap.put(displayName, record);
        }
        
        

        List<String[]> updatedData = new ArrayList<>();
        List<String> sortedDates = new ArrayList<>(allDates);

        List<String> header = new ArrayList<>();
        header.add("Student ID");
        header.add("Student Name");
        header.addAll(sortedDates);
        updatedData.add(header.toArray(new String[0]));

        for (AttendanceRecord record : attendanceMap.values()) {
            updatedData.add(record.toRow(sortedDates));
        }

        try {
            attendanceController.saveAttendanceData(course.getCourseID(), updatedData);
            if (isEditingDate) {
                attendanceController.deleteAttendanceColumn(course.getCourseID(), selectedDateToEdit);
            }

            JOptionPane.showMessageDialog(this, "Attendance saved for " + selectedDate + "!");
            if (onSaveCallback != null) onSaveCallback.run();
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
