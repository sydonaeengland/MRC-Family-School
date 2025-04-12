package views;

import controllers.AssessmentController;
import controllers.CourseController;
import controllers.GradebookController;
import models.Assessment;
import models.Course;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GradebookView extends JPanel {
    private Course course;
    private JTable table;
    private JFrame parentFrame;
    private DefaultTableModel model;
    private GradebookController gradebookController;
    private List<Assessment> assessments;

    public GradebookView(JFrame parentFrame, Course course, GradebookController controller) {
        this.parentFrame = parentFrame;
        this.course = course;
        this.gradebookController = controller;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("GRADEBOOK - " + course.getCourseID(), SwingConstants.CENTER);
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

        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        buttonPanel.setBackground(new Color(0, 70, 140));

        JButton addBtn = createStyledButton("Add Assessment");
        JButton editBtn = createStyledButton("Edit Assessment");
        JButton deleteBtn = createStyledButton("Delete Assessment");
        JButton enterGradesBtn = createStyledButton("Enter Grades");
        JButton editGradesBtn = createStyledButton("Edit Grades");
        JButton backBtn = createStyledButton("Back");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(enterGradesBtn);
        buttonPanel.add(editGradesBtn);
        buttonPanel.add(backBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        reloadAssessments();
        gradebookController.populateGradebookTable(course, model);

        addBtn.addActionListener(e ->
                gradebookController.openAddAssessmentDialog(parentFrame, course, this::reloadAssessments));

        editBtn.addActionListener(e -> {
            Assessment selected = showAssessmentSelectionDialog();
            if (selected != null)
                gradebookController.openEditAssessmentDialog(parentFrame, selected, this::reloadAssessments);
        });

        deleteBtn.addActionListener(e -> {
            Assessment selected = showAssessmentSelectionDialog();
            if (selected != null)
                gradebookController.deleteAssessmentAndGrades(parentFrame, course, selected, this::reloadAssessments);
        });

        enterGradesBtn.addActionListener(e -> {
            Assessment selected = showAssessmentSelectionDialog();
            if (selected != null)
                gradebookController.openGradeEntryDialog(parentFrame, course, selected, this::reloadAssessments);
        });

        editGradesBtn.addActionListener(e -> {
            Assessment selected = showAssessmentSelectionDialog();
            if (selected != null)
                gradebookController.openGradeEntryDialog(parentFrame, course, selected, this::reloadAssessments);
        });

        backBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Gradebook Management");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(900, 600);
                frame.setLocationRelativeTo(null);

                CourseController courseController = new CourseController();
                GradebookManagementUI gradebookManagementUI = new GradebookManagementUI(frame, courseController);
                frame.add(gradebookManagementUI);
                frame.setVisible(true);
            });
        });
    }

    private void reloadAssessments() {
        assessments = new AssessmentController().getAssessmentsForCourse(course.getCourseID());
    
        model.setRowCount(0);
        model.setColumnCount(0);
        gradebookController.populateGradebookTable(course, model);
    }
    

    private Assessment showAssessmentSelectionDialog() {
        reloadAssessments();
        if (assessments == null || assessments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No assessments found for this course.");
            return null;
        }

        String[] options = assessments.stream()
                .map(a -> a.getId() + " - " + a.getName())
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select an assessment:",
                "Choose Assessment",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selected != null) {
            String selectedID = selected.split(" - ")[0];
            return assessments.stream()
                    .filter(a -> a.getId().equals(selectedID))
                    .findFirst()
                    .orElse(null);
        }
        return null;
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
