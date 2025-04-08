package controllers;

import models.Student;
import models.ContactInfo;
import models.GuardianInfo;

import java.io.*;
import java.util.*;

public class StudentController {
    private static final String DATABASE_PATH = "database" + File.separator;
    private static final String FILE_EXTENSION = ".txt";
    private Map<String, List<Student>> gradeStudentMap;

    public interface StudentChangeListener {
        void onStudentListChanged();
    }

    private StudentChangeListener listener;

    public void setStudentChangeListener(StudentChangeListener listener) {
        this.listener = listener;
    }

    public StudentController() {
        this.gradeStudentMap = new HashMap<>();
        loadStudentsFromFiles();
    }


    /**
     * ID GENERATION
     * - Creates a unique ID for new students.
     * - Prevents duplication across all grades.
     */

    /**
     * Generates a unique Student ID (L + random number).
     */
    public String generateUniqueID() {
        String id;
        do {
            id = "L" + (1000 + (int) (Math.random() * 9000));
        } while (isIDTaken(id));
        return id;
    }

    /**
     * Checks if a student ID is already used.
     */
    private boolean isIDTaken(String studentID) {
        return getStudentByID(studentID) != null;
    }

   /**
     * STUDENT MANAGEMENT
     * - Handles adding, updating, and deleting student records.
     * - Saves data to appropriate grade-level files.
     */


    /**
     * Adds a new student to the system and saves to the correct grade file.
     */
    public void addStudent(Student student) {
        String grade = student.getCurrentGrade();
        List<Student> studentsInGrade = gradeStudentMap.computeIfAbsent(grade, k -> new ArrayList<>());
    
        studentsInGrade.removeIf(s -> s.getStudentID().equals(student.getStudentID()));
    
        studentsInGrade.add(student);
        saveStudentsToFile(grade);
    

        if (listener != null) {
            listener.onStudentListChanged();
        }
    }
    

    /**
     * Updates a student’s information. If the grade changes, moves the student to the new grade.
     */
    public void updateStudent(Student updatedStudent) {
        String oldGrade = findStudentGrade(updatedStudent.getStudentID());
    
        if (oldGrade != null && !oldGrade.equals(updatedStudent.getCurrentGrade())) {
            List<Student> oldGradeStudents = gradeStudentMap.get(oldGrade);
            if (oldGradeStudents != null) {
                oldGradeStudents.removeIf(s -> s.getStudentID().equals(updatedStudent.getStudentID()));
                saveStudentsToFile(oldGrade); 
            }
    
            addStudent(updatedStudent);
        } else {
            List<Student> studentsInSameGrade = gradeStudentMap.get(updatedStudent.getCurrentGrade());
            if (studentsInSameGrade != null) {
                studentsInSameGrade.removeIf(s -> s.getStudentID().equals(updatedStudent.getStudentID()));
                studentsInSameGrade.add(updatedStudent);
                saveStudentsToFile(updatedStudent.getCurrentGrade());
            }
        }
    
        if (listener != null) {
            listener.onStudentListChanged();
        }
    }
    
    
    

    /**
     * Deletes a student from the system by ID.
     */
    public boolean deleteStudent(String studentID) {
        String grade = findStudentGrade(studentID);
        if (grade != null) {
            List<Student> students = gradeStudentMap.get(grade);
            if (students != null) {
                boolean removed = students.removeIf(student -> student.getStudentID().equals(studentID));
                if (removed) {
                    saveStudentsToFile(grade);

                    if (listener != null) {
                        listener.onStudentListChanged();
                    }

                    return true;
                }
            }
        }
        return false;
    }


    /**
     * FILE HANDLING
     * - Loads and saves student data from/to grade files.
     * - Organizes students by grade levels in memory.
     */

    /**
     * Loads students from all grade files into memory.
     */
    public void loadStudentsFromFiles() {
        File databaseDir = new File(DATABASE_PATH);
        if (!databaseDir.exists()) {
            System.err.println("⚠️ Database folder not found: " + databaseDir.getAbsolutePath());
            return; 
        }
    
        gradeStudentMap.clear();
        for (int grade = 10; grade <= 13; grade++) {
            String fileName = DATABASE_PATH + "grade" + grade + FILE_EXTENSION;
            List<Student> students = new ArrayList<>();
    
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Student student = Student.fromCSV(line);
                    if (student != null) {
                        students.add(student);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading students from " + fileName + ": " + e.getMessage());
            }
    
            gradeStudentMap.put(String.valueOf(grade), students);
        }
    }
    
    /**
     * Finds which grade a student belongs to using their ID.
     */
    private String findStudentGrade(String studentID) {
        for (Map.Entry<String, List<Student>> entry : gradeStudentMap.entrySet()) {
            for (Student student : entry.getValue()) {
                if (student.getStudentID().equals(studentID)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Saves students of a specific grade to its corresponding file.
     */
    private void saveStudentsToFile(String grade) {
        String fileName = DATABASE_PATH + "grade" + grade + FILE_EXTENSION;
        List<Student> students = gradeStudentMap.get(grade);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Student student : students) {
                writer.write(student.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving students to " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * STUDENT QUERYING
     * - Retrieves students by ID, name, grade, or all.
     * - Formats full student profile for display.
     */


    /**
     * Searches for a student by their ID.
     */
    public Student getStudentByID(String studentID) {
        for (List<Student> students : gradeStudentMap.values()) {
            for (Student student : students) {
                if (student.getStudentID().equals(studentID)) {
                    return student;
                }
            }
        }
        return null;
    }

    /**
     * Searches for students by first name or last name.
     * Returns a list of students matching the name.
     */
    public List<Student> searchStudentByName(String name) {
        List<Student> matchingStudents = new ArrayList<>();
        for (List<Student> students : gradeStudentMap.values()) {
            for (Student student : students) {
                if (student.getFirstName().equalsIgnoreCase(name) || student.getLastName().equalsIgnoreCase(name)) {
                    matchingStudents.add(student);
                }
            }
        }
    
        matchingStudents.sort(Comparator.comparing(Student::getLastName)
                                        .thenComparing(Student::getFirstName));
    
        return matchingStudents;
    }
    
    
    /**
     * Retrieves and formats full student details.
     */
    public String getStudentDetails(String studentID) {
        Student student = getStudentByID(studentID);
        if (student == null) {
            return "Student not found.";
        }

        ContactInfo contact = student.getContactInfo();
        List<GuardianInfo> guardians = student.getGuardians();

        StringBuilder details = new StringBuilder();
        details.append("<html><body style='width: 300px;'>");
        details.append("<h2 style='background-color:#00468C; color:white; padding:5px;'>Student Details</h2>");
        details.append("<b>First Name:</b> ").append(student.getFirstName()).append("<br>");
        details.append("<b>Last Name:</b> ").append(student.getLastName()).append("<br>");
        details.append("<b>Date of Birth:</b> ").append(student.getDateOfBirth()).append("<br>");
        details.append("<b>Grade:</b> ").append(student.getCurrentGrade()).append("<br>");
        details.append("<b>School:</b> ").append(student.getCurrentSchool()).append("<br>");
        details.append("<b>Contact Number:</b> ").append(contact != null ? contact.getPhoneNumber() : "N/A").append("<br>");
        details.append("<b>Email:</b> ").append(contact != null ? contact.getEmail() : "N/A").append("<br>");
        
        // Guardian Information
        if (!guardians.isEmpty()) {
            for (int i = 0; i < guardians.size(); i++) {
                details.append("<br><b>Guardian ").append(i + 1).append(":</b><br>");
                details.append("<b>First Name:</b> ").append(guardians.get(i).getGuardianFirstName()).append("<br>");
                details.append("<b>Last Name:</b> ").append(guardians.get(i).getGuardianLastName()).append("<br>");
                details.append("<b>Relation:</b> ").append(guardians.get(i).getRelation()).append("<br>");
                details.append("<b>Phone:</b> ").append(guardians.get(i).getPhoneNumber()).append("<br>");
                details.append("<b>Email:</b> ").append(guardians.get(i).getEmail()).append("<br>");
            }
        } else {
            details.append("<br><b>No Guardians Available</b><br>");
        }
        
        details.append("</body></html>");
        return details.toString();
    }



    
    /**
     * Returns a list of all students in a specific grade.
     */
    public List<Student> getStudentsByGrade(String grade) {
        List<Student> students = gradeStudentMap.getOrDefault(grade, new ArrayList<>());
    
        students.sort(Comparator.comparing(Student::getLastName)
                                .thenComparing(Student::getFirstName));
    
        return students;
    }
    

    
    /**
     * Returns all students in the system across all grades.
     */
    public List<Student> getAllStudents() {
        List<Student> allStudents = new ArrayList<>();
        for (List<Student> students : gradeStudentMap.values()) {
            allStudents.addAll(students);
        }
        
        allStudents.sort(Comparator.comparing(Student::getLastName)
                                   .thenComparing(Student::getFirstName));
        
        return allStudents;
    }
    
}
