package views;

import controllers.CourseController;
import controllers.ReportController;
import models.Course;
import models.Report;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.List;

public class ReportManagementUI extends JPanel {
    private JFrame parentFrame;
    private CourseController courseController;
    private ReportController reportController;

    private JComboBox<String> courseDropdown;
    private JTable gradeTable;
    private JTextArea distributionArea;
    private ChartPanel chartPanel;
    private JButton saveBtn, backBtn, viewPastBtn;
    private JLabel reportTitleLabel;

    private Report currentReport;

    public ReportManagementUI(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.courseController = new CourseController();
        this.reportController = new ReportController();

        setLayout(new BorderLayout());

        JLabel mainTitle = new JLabel("MRC REPORTS AND ANALYTICS MANAGEMENT", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Arial", Font.BOLD, 26));
        mainTitle.setOpaque(true);
        mainTitle.setBackground(new Color(0, 70, 140));
        mainTitle.setForeground(Color.WHITE);
        mainTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(mainTitle, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Select Course:"));

        courseDropdown = new JComboBox<>();
        List<Course> courses = courseController.getCourses();
        for (Course c : courses) {
            courseDropdown.addItem(c.getCourseID() + " - " + c.getSubject());
        }
        topPanel.add(courseDropdown);

        JButton generateBtn = new JButton("Generate Report");
        styleButton(generateBtn);
        generateBtn.addActionListener(e -> generateReport());
        topPanel.add(generateBtn);

        viewPastBtn = new JButton("View Past Report");
        styleButton(viewPastBtn);
        viewPastBtn.addActionListener(e -> viewPastReport());
        topPanel.add(viewPastBtn);

        add(topPanel, BorderLayout.BEFORE_FIRST_LINE);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        reportTitleLabel = new JLabel("", SwingConstants.CENTER);
        reportTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        reportTitleLabel.setForeground(new Color(0, 70, 140));
        reportTitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        center.add(reportTitleLabel, BorderLayout.NORTH);

        gradeTable = new JTable(new DefaultTableModel(new Object[]{"Student ID", "Name", "Average"}, 0));
        gradeTable.setRowHeight(25);
        gradeTable.setFont(new Font("Arial", Font.PLAIN, 12));
        gradeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        gradeTable.getTableHeader().setBackground(new Color(0, 120, 215));
        gradeTable.getTableHeader().setForeground(Color.WHITE);
        JScrollPane tableScroll = new JScrollPane(gradeTable);
        center.add(tableScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        distributionArea = new JTextArea();
        distributionArea.setEditable(false);
        distributionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        distributionArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 70, 140), 2),
                "Performance Summary",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(0, 70, 140)
        ));

        chartPanel = new ChartPanel();
        chartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 70, 140), 2),
                "Performance Chart",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(0, 70, 140)
        ));

        JScrollPane distributionScroll = new JScrollPane(distributionArea);
        distributionScroll.setPreferredSize(new Dimension(400, 200));
        distributionArea.setPreferredSize(new Dimension(400, 200));
        
        chartPanel.setPreferredSize(new Dimension(400, 200));
        
        bottomPanel.setPreferredSize(new Dimension(800, 220));
        bottomPanel.add(distributionScroll);
        bottomPanel.add(chartPanel);
        

        center.add(bottomPanel, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setBackground(new Color(0, 70, 140));

        saveBtn = new JButton("Save Report");
        styleButton(saveBtn);
        saveBtn.setEnabled(false);
        saveBtn.addActionListener(e -> {
            if (currentReport != null) {
                reportController.saveReportAsTextFile(currentReport);
                JOptionPane.showMessageDialog(this, "Report saved.");
            }
        });
        actionPanel.add(saveBtn);

        backBtn = new JButton("Back");
        styleButton(backBtn);
        backBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
                SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
            }
        });
        actionPanel.add(backBtn);

        add(actionPanel, BorderLayout.SOUTH);
    }

    private void viewPastReport() {
        File folder = new File("database/reports");
        if (!folder.exists()) return;

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) return;

        String[] choices = Arrays.stream(files).map(File::getName).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Choose a report:",
                "Past Reports",
                JOptionPane.PLAIN_MESSAGE,
                null,
                choices,
                choices[0]
        );

        if (selected != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(new File(folder, selected)))) {
                String courseLine = reader.readLine();
                String subjectLine = reader.readLine();
                String timeLine = reader.readLine();
                reader.readLine(); 
                reader.readLine(); 
                reader.readLine(); 

                Map<String, String> names = new LinkedHashMap<>();
                Map<String, Double> averages = new LinkedHashMap<>();
                String line;
                while ((line = reader.readLine()) != null && !line.startsWith("Class Average")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        names.put(parts[0].trim(), parts[1].trim());
                        averages.put(parts[0].trim(), Double.parseDouble(parts[2].trim()));
                    }
                }

                double classAvg = Double.parseDouble(line.split(":")[1].trim());
                reader.readLine(); 

                Map<String, Integer> performance = new LinkedHashMap<>();
                while ((line = reader.readLine()) != null && line.contains("→")) {
                    String[] parts = line.split("→");
                    performance.put(parts[0].trim(), Integer.parseInt(parts[1].trim().split(" ")[0]));
                }

                DefaultTableModel model = (DefaultTableModel) gradeTable.getModel();
                model.setRowCount(0);
                for (String id : names.keySet()) {
                    model.addRow(new Object[]{id, names.get(id), String.format("%.2f", averages.get(id))});
                }

                reportTitleLabel.setText(subjectLine);
                chartPanel.setDistribution(performance);
                chartPanel.repaint();

                StringBuilder sb = new StringBuilder();
                sb.append("Class Average: ").append(String.format("%.2f", classAvg)).append("\n\n");
                for (Map.Entry<String, Integer> entry : performance.entrySet()) {
                    sb.append(String.format("%-8s → %d students%n", entry.getKey(), entry.getValue()));
                }
                distributionArea.setText(sb.toString());

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Could not load report.");
            }
        }
    }

    private void generateReport() {
        String selected = (String) courseDropdown.getSelectedItem();
        if (selected == null) return;

        String courseID = selected.split(" - ")[0];
        Course course = courseController.getCourseByID(courseID);
        if (course == null) return;

        currentReport = reportController.generateReportForCourse(course);
        updateTable(currentReport);
        updateSummary(currentReport);
        chartPanel.setDistribution(currentReport.getPerformanceDistribution());
        chartPanel.repaint();
        reportTitleLabel.setText("Course: " + course.getSubject() + " | Grade: " + course.getGradeLevel() +
                " | Exam: " + course.getExamType() + " | Teacher: " + course.getTeacher());
        saveBtn.setEnabled(true);
    }

    private void updateTable(Report report) {
        DefaultTableModel model = (DefaultTableModel) gradeTable.getModel();
        model.setRowCount(0);

        for (String studentID : report.getStudentAverages().keySet()) {
            String name = report.getStudentNames().get(studentID);
            double avg = report.getStudentAverages().get(studentID);
            model.addRow(new Object[]{studentID, name, String.format("%.2f", avg)});
        }
    }

    private void updateSummary(Report report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Class Average: ").append(String.format("%.2f", report.getClassAverage())).append("\n\n");
        for (Map.Entry<String, Integer> entry : report.getPerformanceDistribution().entrySet()) {
            sb.append(String.format("%-8s → %d students%n", entry.getKey(), entry.getValue()));
        }
        distributionArea.setText(sb.toString());
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(0, 120, 215));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 40));
    }

    private static class ChartPanel extends JPanel {
        private Map<String, Integer> distribution = Map.of();

        public void setDistribution(Map<String, Integer> distribution) {
            this.distribution = distribution;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (distribution == null || distribution.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(new Font("Arial", Font.PLAIN, 12));

            int width = getWidth() - 40;
            int height = getHeight() - 40;
            int barWidth = width / distribution.size();
            int max = distribution.values().stream().max(Integer::compareTo).orElse(1);

            int i = 0;
            for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
                int barHeight = (int) ((entry.getValue() / (double) max) * (height - 40));
                int x = 20 + i * barWidth;
                int y = height - barHeight;

                g2.setColor(new Color(0, 120, 215));
                g2.fillRect(x, y, barWidth - 10, barHeight);
                g2.setColor(Color.BLACK);
                g2.drawString(entry.getKey(), x + 5, height + 15);
                g2.drawString(String.valueOf(entry.getValue()), x + 5, y - 5);
                i++;
            }
        }
    }
}
