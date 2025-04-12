package views;

import controllers.CourseController;
import controllers.GradebookController;
import controllers.StudentController;
import models.Assessment;
import models.Course;
import models.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GradeEntryForm extends JDialog {
    private Course course;
    private Assessment assessment;
    private GradebookController gradebookController;
    private Runnable onSaveCallback;

    private StudentController studentController = new StudentController();
    private Map<String, JTextField> gradeFields = new HashMap<>();
    private List<Student> students;

    public GradeEntryForm(JFrame parent, Course course, GradebookController gradebookController, Assessment assessment, Runnable onSaveCallback) {
        super(parent, "Enter Grades for " + assessment.getName() + " (" + assessment.getId() + ")", true);
        this.course = course;
        this.assessment = assessment;
        this.gradebookController = gradebookController;
        this.onSaveCallback = onSaveCallback;

        setSize(600, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("GRADES FOR " + assessment.getName().toUpperCase(), SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(new Color(0, 70, 140));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        students = getStudentsFromGradebookFile();
        students.sort(Comparator.comparing(Student::getLastName));

        for (Student s : students) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
            JLabel label = new JLabel(s.getStudentID() + " - " + s.getFirstName() + " " + s.getLastName());
            label.setPreferredSize(new Dimension(300, 25));
            JTextField gradeField = new JTextField(5);
            gradeField.setFont(new Font("Arial", Font.PLAIN, 14));
            gradeFields.put(s.getStudentID(), gradeField);

            row.add(label);
            row.add(gradeField);
            centerPanel.add(row);
        }

        loadExistingGrades(); 

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 70, 140));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton saveBtn = createStyledButton("SAVE");
        JButton closeBtn = createStyledButton("CLOSE");

        saveBtn.addActionListener(this::handleSave);
        closeBtn.addActionListener(e -> dispose());

        bottomPanel.add(saveBtn);
        bottomPanel.add(closeBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private List<Student> getStudentsFromGradebookFile() {
        List<Student> studentList = new ArrayList<>();
        File gradebookFile = new File("database/grades/" + course.getCourseID() + "_gradebook.txt");

        if (!gradebookFile.exists()) return studentList;

        try (BufferedReader reader = new BufferedReader(new FileReader(gradebookFile))) {
            String headerLine = reader.readLine(); 

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    String studentID = parts[0].trim();
                    Student s = studentController.getStudentByID(studentID);
                    if (s != null) {
                        studentList.add(s);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return studentList;
    }

    private void loadExistingGrades() {
        Map<String, String> existingGrades = gradebookController.getGradesForAssessment(course, assessment.getId());

        for (Student s : students) {
            String grade = existingGrades.get(s.getStudentID());
            if (grade != null) {
                gradeFields.get(s.getStudentID()).setText(grade);
            }
        }
    }

    private void handleSave(ActionEvent e) {
        Map<String, String> grades = new HashMap<>();
        for (Student s : students) {
            String input = gradeFields.get(s.getStudentID()).getText().trim();
            grades.put(s.getStudentID(), input);
        }

        gradebookController.saveAssessmentGrades(course, assessment.getId(), grades);
        JOptionPane.showMessageDialog(this, "Grades saved.");

        if (onSaveCallback != null) onSaveCallback.run(); 
        dispose();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(0, 120, 215));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
        return button;
    }
}
