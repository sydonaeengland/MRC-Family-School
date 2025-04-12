package views;

import controllers.CourseController;
import controllers.GradebookController;
import models.Course;
import utils.Subjects;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradebookManagementUI extends JPanel {
    private JFrame parentFrame;
    private CourseController courseController;
    private GradebookController gradebookController;

    private JTabbedPane subjectTabs;
    private Map<String, JTable> subjectTables = new HashMap<>();
    private Map<String, DefaultTableModel> tableModels = new HashMap<>();

    public GradebookManagementUI(JFrame parentFrame, CourseController courseController) {
        this.parentFrame = parentFrame;
        this.courseController = courseController;
        this.gradebookController = new GradebookController();

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("MRC GRADEBOOK MANAGEMENT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0, 70, 140));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        subjectTabs = new JTabbedPane();
        for (String subject : Subjects.getAvailableSubjects()) {
            DefaultTableModel model = new DefaultTableModel(new String[]{"Course ID", "Grade", "Exam Type", "Teacher"}, 0);
            JTable table = new JTable(model);
            table.setRowHeight(25);
            table.setFont(new Font("Arial", Font.PLAIN, 12));
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
            table.getTableHeader().setBackground(new Color(0, 120, 215));
            table.getTableHeader().setForeground(Color.WHITE);

            subjectTables.put(subject, table);
            tableModels.put(subject, model);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            btnPanel.setBackground(new Color(0, 70, 140));

            JButton openGradebookBtn = createStyledButton("Open Gradebook");
            JButton backBtn = createStyledButton("Back");

            openGradebookBtn.addActionListener(e -> openGradebookForSelected(subject));
            backBtn.addActionListener(e -> {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) window.dispose();
                SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
            });

            btnPanel.add(openGradebookBtn);
            btnPanel.add(backBtn);
            panel.add(btnPanel, BorderLayout.SOUTH);

            subjectTabs.add(subject, panel);
        }

        add(subjectTabs, BorderLayout.CENTER);
        loadCourses();
    }

    private void loadCourses() {
        List<Course> allCourses = courseController.getCourses();
        for (String subject : Subjects.getAvailableSubjects()) {
            DefaultTableModel model = tableModels.get(subject);
            model.setRowCount(0);
            for (Course c : allCourses) {
                if (c.getSubject().equalsIgnoreCase(subject)) {
                    model.addRow(new Object[]{c.getCourseID(), c.getGradeLevel(), c.getExamType(), c.getTeacher()});
                }
            }
        }
    }

    private void openGradebookForSelected(String subject) {
        JTable table = subjectTables.get(subject);
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course first.");
            return;
        }
    
        String courseID = (String) table.getValueAt(selectedRow, 0);
        Course course = courseController.getCourseByID(courseID);
        if (course != null) {
            Window currentWindow = SwingUtilities.getWindowAncestor(this);
            if (currentWindow != null) currentWindow.dispose();
    
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Gradebook - " + courseID);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(900, 600);
                frame.setLocationRelativeTo(null);
    
                GradebookView view = new GradebookView(frame, course, gradebookController);
                frame.add(view);
                frame.setVisible(true);
            });
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
}
