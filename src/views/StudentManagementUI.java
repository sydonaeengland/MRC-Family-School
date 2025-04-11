package views;

import controllers.StudentController;
import models.ContactInfo;
import models.GuardianInfo;
import models.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class StudentManagementUI extends JPanel {
    private JTable grade10Table, grade11Table, grade12Table, grade13Table;
    private DefaultTableModel grade10Model, grade11Model, grade12Model, grade13Model;
    private StudentController studentController;

    /** 
     * Constructor for Student Management UI 
     */
    public StudentManagementUI(StudentController studentController) {
        super(new BorderLayout());
        this.studentController = studentController;

        setupTables();
        setupUIComponents();
        refreshTable();
    }

    private void setupUIComponents() {
        JLabel titleLabel = new JLabel("MRC STUDENT MANAGEMENT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0, 70, 140));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel gradesPanel = new JPanel();
        gradesPanel.setLayout(new BoxLayout(gradesPanel, BoxLayout.Y_AXIS));
        gradesPanel.setBackground(new Color(220, 240, 255));
        gradesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gradesPanel.add(createTablePanel("Grade 10 Students", grade10Table));
        gradesPanel.add(createTablePanel("Grade 11 Students", grade11Table));
        gradesPanel.add(createTablePanel("Grade 12 Students", grade12Table));
        gradesPanel.add(createTablePanel("Grade 13 Students", grade13Table));

        JScrollPane scrollPane = new JScrollPane(gradesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        setupButtons();
    }

    private void setupButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(0, 70, 140));
    
        JButton addButton = createButton("Add Student");
        JButton updateButton = createButton("Update Student");
        JButton deleteButton = createButton("Delete Student");
        JButton searchByIDButton = createButton("Search by Student ID");
        JButton searchByNameButton = createButton("Search by Name");
        JButton backButton = new JButton("Back");
    
        backButton.setFont(new Font("Arial", Font.BOLD, 12));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(30, 60, 90)); 
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(120, 40));
    
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(searchByIDButton);
        buttonPanel.add(searchByNameButton);
        buttonPanel.add(backButton);
    
        addButton.addActionListener(e -> {
            StudentEntry studentEntry = new StudentEntry(null, studentController);
            studentEntry.setVisible(true);
            studentEntry.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    refreshTable();
                }
            });
        });
    
        updateButton.addActionListener(e -> {
            updateStudent();
            refreshTable();
        });
    
        deleteButton.addActionListener(e -> {
            deleteStudent();
            refreshTable();
        });
    
        searchByIDButton.addActionListener(e -> searchStudentByID());
        searchByNameButton.addActionListener(e -> searchStudentByName());
    
        backButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
    
            SwingUtilities.invokeLater(() -> {
                MainMenu mainMenu = new MainMenu();
                mainMenu.setVisible(true);
            });
        });
    
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(180, 40));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 120, 215));
        button.setFocusPainted(false);
        return button;
    }


    private void setupTables() {
        String[] columnNames = {
            "Student ID", "First Name", "Last Name", 
            "DOB", "Age", "Grade", "School",
            "Contact", "Email", "Address"
        };   

        grade10Model = new DefaultTableModel(columnNames, 0);
        grade11Model = new DefaultTableModel(columnNames, 0);
        grade12Model = new DefaultTableModel(columnNames, 0);
        grade13Model = new DefaultTableModel(columnNames, 0);

        grade10Table = new JTable(grade10Model);
        grade11Table = new JTable(grade11Model);
        grade12Table = new JTable(grade12Model);
        grade13Table = new JTable(grade13Model);
    }

    private JPanel createTablePanel(String gradeTitle, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 70, 140), 2));

        JLabel label = new JLabel(gradeTitle, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setOpaque(true);
        label.setBackground(new Color(0, 120, 215));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.NORTH);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private DefaultTableModel getTableModelByGrade(String grade) {
        switch (grade) {
            case "10":
                return grade10Model;
            case "11":
                return grade11Model;
            case "12":
                return grade12Model;
            case "13":
                return grade13Model;
            default:
                return null;
        }
    }

    private void refreshTable() {
        studentController.loadStudentsFromFiles(); 
        List<Student> students = studentController.getAllStudents(); 
        
        grade10Model.setRowCount(0);
        grade11Model.setRowCount(0);
        grade12Model.setRowCount(0);
        grade13Model.setRowCount(0);
        
        for (Student student : students) {
            DefaultTableModel model = getTableModelByGrade(student.getCurrentGrade());
            if (model != null) {
                ContactInfo contact = student.getContactInfo();
    
                model.addRow(new Object[]{
                    student.getStudentID(),
                    student.getFirstName(),
                    student.getLastName(),
                    student.getDateOfBirth(),
                    student.getAge(),
                    student.getCurrentGrade(),
                    student.getCurrentSchool(),
                    contact != null ? contact.getPhoneNumber() : "",
                    contact != null ? contact.getEmail() : "",
                    contact != null ? contact.getAddress() : ""
                });
            }
        }
    }
    
    
    

    private void updateStudent() {
        String studentID = JOptionPane.showInputDialog(this, "Enter Student ID to Update:");
        if (studentID != null && !studentID.trim().isEmpty()) {
            Student student = studentController.getStudentByID(studentID.trim());
            if (student != null) {
                StudentEntry studentEntry = new StudentEntry(student, studentController);
                studentEntry.setVisible(true);
                
                studentEntry.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        refreshTable();
                    }
                });
            } else {
                JOptionPane.showMessageDialog(this, "Student ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    private void deleteStudent() {
        String studentID = JOptionPane.showInputDialog(this, "Enter Student ID to Delete:");
        if (studentID != null && !studentID.trim().isEmpty()) {
            studentController.deleteStudent(studentID.trim());
            JOptionPane.showMessageDialog(this, "Student deleted successfully.");
            refreshTable();
        }
    }

    private void searchStudentByID() {
        String studentID = JOptionPane.showInputDialog(this, "Enter Student ID to Search:");
        if (studentID != null && !studentID.trim().isEmpty()) {
            Student student = studentController.getStudentByID(studentID.trim());

            if (student != null) {
                List<Student> singleStudentList = new ArrayList<>();
                singleStudentList.add(student);
                showStudentSearchResults(singleStudentList, "ID: " + studentID.trim()); 
            } else {
                JOptionPane.showMessageDialog(this, "Student ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    
    private void searchStudentByName() {
        String name = JOptionPane.showInputDialog(this, "Enter Student Name: First Name, Last name or Both to Search:");
        if (name != null && !name.trim().isEmpty()) {
            List<Student> students = studentController.searchStudentByName(name.trim());
            if (!students.isEmpty()) {
                showStudentSearchResults(students, name.trim());
            } else {
                JOptionPane.showMessageDialog(this, "No students found with that name.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    

    private void showStudentSearchResults(List<Student> students, String searchQuery){
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no students found.", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(Color.WHITE);


        JLabel recordCountLabel = new JLabel("  " + students.size() + " record(s) found for \"" + searchQuery + "\" ");
        recordCountLabel.setFont(new Font("Arial", Font.ITALIC, 13)); 
        recordCountLabel.setForeground(new Color(50, 50, 50)); 
        recordCountLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); 
        resultPanel.add(recordCountLabel);
        resultPanel.add(Box.createVerticalStrut(5));
    
        for (Student s : students) {
            List<GuardianInfo> guardians = s.getGuardians();
    
            String g1First = "N/A", g1Last = "N/A", g1Relation = "N/A", g1Phone = "N/A", g1Email = "N/A";
            String g2First = "N/A", g2Last = "N/A", g2Relation = "N/A", g2Phone = "N/A", g2Email = "N/A";
    
            if (guardians.size() > 0) {
                g1First = guardians.get(0).getGuardianFirstName();
                g1Last = guardians.get(0).getGuardianLastName();
                g1Relation = guardians.get(0).getRelation();
                g1Phone = guardians.get(0).getPhoneNumber();
                g1Email = guardians.get(0).getEmail();
            }
            if (guardians.size() > 1) {
                g2First = guardians.get(1).getGuardianFirstName();
                g2Last = guardians.get(1).getGuardianLastName();
                g2Relation = guardians.get(1).getRelation();
                g2Phone = guardians.get(1).getPhoneNumber();
                g2Email = guardians.get(1).getEmail();
            }
    
            int panelHeight = (guardians.size() > 1) ? 400 : 270;
    
            JPanel studentPanel = new JPanel();
            studentPanel.setLayout(new BorderLayout());
            studentPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 70, 140), 2));
            studentPanel.setBackground(Color.WHITE);
            studentPanel.setPreferredSize(new Dimension(500, panelHeight));
    
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(new Color(0, 70, 140));
            JLabel headerLabel = new JLabel("  Student Details", SwingConstants.LEFT);
            headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
            headerLabel.setForeground(Color.WHITE);
            headerPanel.add(headerLabel);
    
            JPanel bodyPanel = new JPanel();
            bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
            bodyPanel.setBackground(Color.WHITE);
            bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
            bodyPanel.add(new JLabel("<html><b>Student ID:</b> " + s.getStudentID() + "</html>"));
            bodyPanel.add(new JLabel("<html><b>First Name:</b> " + s.getFirstName() + "</html>"));
            bodyPanel.add(new JLabel("<html><b>Last Name:</b> " + s.getLastName() + "</html>"));
            bodyPanel.add(new JLabel("<html><b>Date of Birth:</b> " + s.getDateOfBirth() + "</html>"));
            bodyPanel.add(new JLabel("<html><b>Age:</b> " + s.getAge() + "</html>"));
            bodyPanel.add(new JLabel("<html><b>Grade:</b> " + s.getCurrentGrade() + "</html>"));
            bodyPanel.add(new JLabel("<html><b>School:</b> " + s.getCurrentSchool() + "</html>"));
    
            ContactInfo contact = s.getContactInfo();
            bodyPanel.add(new JLabel("<html><b>Contact Number:</b> " + (contact != null ? contact.getPhoneNumber() : "N/A") + "</html>"));
            bodyPanel.add(new JLabel("<html><b>Email:</b> " + (contact != null ? contact.getEmail() : "N/A") + "</html>"));
    
            bodyPanel.add(new JLabel("<html><b>Guardian 1:</b></html>"));
            bodyPanel.add(new JLabel("• First Name: " + g1First));
            bodyPanel.add(new JLabel("• Last Name: " + g1Last));
            bodyPanel.add(new JLabel("• Relation: " + g1Relation));
            bodyPanel.add(new JLabel("• Phone: " + g1Phone));
            bodyPanel.add(new JLabel("• Email: " + g1Email));
    
            bodyPanel.add(new JLabel("<html><b>Guardian 2:</b></html>"));
            bodyPanel.add(new JLabel("• First Name: " + g2First));
            bodyPanel.add(new JLabel("• Last Name: " + g2Last));
            bodyPanel.add(new JLabel("• Relation: " + g2Relation));
            bodyPanel.add(new JLabel("• Phone: " + g2Phone));
            bodyPanel.add(new JLabel("• Email: " + g2Email));
    
            studentPanel.add(headerPanel, BorderLayout.NORTH);
            studentPanel.add(bodyPanel, BorderLayout.CENTER);
    
            resultPanel.add(studentPanel);
            resultPanel.add(Box.createVerticalStrut(15));
        }
    
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setPreferredSize(new Dimension(600, 550));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    
        JOptionPane.showMessageDialog(this, scrollPane, "Search Results", JOptionPane.PLAIN_MESSAGE);
    }
    

}
