package controllers;

import models.Course;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CourseController {
    private static final String COURSE_DATABASE_PATH = "database/courses/";
    private static final String ATTENDANCE_DATABASE_PATH = "database/attendance/";
    private static final String GRADEBOOK_DATABASE_PATH = "database/grades/";
    private static final String COURSES_FILE = "database/courses.txt";
    private StaffController staffController = new StaffController();

    private static final Map<String, String> EXAM_TYPE_ABBREVIATIONS = new HashMap<>();

    static {
        EXAM_TYPE_ABBREVIATIONS.put("CSEC", "CSEC");
        EXAM_TYPE_ABBREVIATIONS.put("CAPE", "CAPE");
        EXAM_TYPE_ABBREVIATIONS.put("A-LEVELS", "ALVL");
        EXAM_TYPE_ABBREVIATIONS.put("GCSE", "GCSE");
    }

    /**
     * GENERATECOURSEID
     * - Generates a unqiue course ID using subject, grade, and exam type.
     * - Includes subject code, grade, random digits, and exam abbreviation.
     */

    public static String generateCourseID(String subject, String gradeLevel, String examType) {
        String subjectCode = subject.replaceAll("\\s+", "").substring(0, Math.min(4, subject.length())).toUpperCase();
        String randomDigits = String.format("%02d", new Random().nextInt(100));
        String examAbbreviation = EXAM_TYPE_ABBREVIATIONS.getOrDefault(examType.toUpperCase(), "GEN");
        return subjectCode + gradeLevel + randomDigits + examAbbreviation;
    }

    // =============================
    // COURSE CRUD OPERATIONS
    // =============================
    // - Get, create, save, update, and delete courses.
    // - Handles all core course file logic and summary file updates.


    /**
     * GETCOURSES
     * - Reads all courses from courses.txt.
     * - Loads additional data from each course file.
     */

    public List<Course> getCourses() {
        List<Course> courseList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(COURSES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String courseID = parts[0];
                    String grade = parts[1];
                    String subject = parts[2];
                    String examType = parts[3];
                    String teacher = loadTeacherFromFile(courseID);

                    Course course = new Course(courseID, grade, subject, examType, teacher);
                    courseList.add(course);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading courses: " + e.getMessage());
        }
        return courseList;
    }

    /**
     * GETCOURSEBYID
     * - Retrieves a course by matching ID.
     */

    public Course getCourseByID(String courseID) {
        List<Course> allCourses = getCourses();
        for (Course course : allCourses) {
            if (course.getCourseID().equals(courseID)) {
                return course;
            }
        }
        return null; 
    }

    /**
     * CREATECOURSE
     * - Creates a new Course object and saves it.
     * - Generates the attendance and gradebook files.
     */

    public void createCourse(String courseID, String grade, String subject, String examType, String teacherName) throws IOException {
        Course course = new Course(courseID, grade, subject, examType, teacherName);
        saveCourse(course);
    }

    

    /**
     * SAVECOURSE
     * - Saves course info to file and summary to courses.txt.
     * - Creates the attendance and gradebook files.
     */

    public void saveCourse(Course course) throws IOException {
        new File(COURSE_DATABASE_PATH).mkdirs();
        new File(ATTENDANCE_DATABASE_PATH).mkdirs();
        new File(GRADEBOOK_DATABASE_PATH).mkdirs();

        String courseFilePath = COURSE_DATABASE_PATH + course.getCourseID() + ".txt";
        File courseFile = new File(courseFilePath);
        if (courseFile.exists()) {
            throw new IOException("Course with ID " + course.getCourseID() + " already exists.");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(courseFilePath))) {
            writer.write("Grade Level: " + course.getGradeLevel());
            writer.newLine();
            writer.write("Subject: " + course.getSubject());
            writer.newLine();
            writer.write("Exam Type: " + course.getExamType());
            writer.newLine();
            writer.write("Teacher: " + course.getTeacher());
            writer.newLine();
            writer.write("Students:");
            writer.newLine();
            for (String student : course.getStudents()) {
                writer.write("- " + student);
                writer.newLine();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COURSES_FILE, true))) {
            writer.write(course.getCourseID() + "," +
                         course.getGradeLevel() + "," +
                         course.getSubject() + "," +
                         course.getExamType());
            writer.newLine();
        }

        createAttendanceAndGradebookFiles(course);
    }

    /**
     * SAVEUPDATEDCOURSEDATA
     * - Updates course file with new details.
     * - Retains or clears students based on flag.
     * - Updates courses.txt with new summary line.
     */
    

    public void saveUpdatedCourseData(Course updatedCourse, boolean clearStudents) throws IOException {
        String courseID = updatedCourse.getCourseID();
        File courseFile = new File("database/courses/" + courseID + ".txt");
    
        List<String> lines = new ArrayList<>();
        lines.add("Course ID: " + courseID);
        lines.add("Subject: " + updatedCourse.getSubject());
        lines.add("Grade Level: " + updatedCourse.getGradeLevel());
        lines.add("Exam Type: " + updatedCourse.getExamType());
        lines.add("Teacher: " + updatedCourse.getTeacher());
        lines.add("Students:");
    
        if (!clearStudents && courseFile.exists()) {
            List<String> originalLines = Files.readAllLines(courseFile.toPath());
            boolean addStudents = false;
            for (String line : originalLines) {
                if (line.trim().equalsIgnoreCase("Students:")) {
                    addStudents = true;
                    continue;
                }
                if (addStudents && line.startsWith("- ")) {
                    lines.add(line);
                }
            }
        }
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(courseFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    
        File summaryFile = new File("database/courses.txt");
        List<String> summaryLines = new ArrayList<>();
        if (summaryFile.exists()) {
            summaryLines = Files.readAllLines(summaryFile.toPath());
        }
    
        String newLine = courseID + "," + updatedCourse.getGradeLevel() + "," + updatedCourse.getSubject() + "," + updatedCourse.getExamType();
        boolean found = false;
    
        for (int i = 0; i < summaryLines.size(); i++) {
            String[] parts = summaryLines.get(i).split(",", 2);
            if (parts.length > 0 && parts[0].equals(courseID)) {
                summaryLines.set(i, newLine);
                found = true;
                break;
            }
        }
    
        if (!found) {
            summaryLines.add(newLine); 
        }
    
        try (BufferedWriter summaryWriter = new BufferedWriter(new FileWriter(summaryFile))) {
            for (String line : summaryLines) {
                summaryWriter.write(line);
                summaryWriter.newLine();
            }
        }
    } 
    
    /**
     * UPDATECOURSESTEXTRECORD
     * - Updates course line in courses.txt by ID.
     */

    public void updateCoursesTextRecord(String oldCourseID, Course updatedCourse) throws IOException {
        File file = new File("database/courses.txt");
        if (!file.exists()) return;
    
        List<String> updatedLines = new ArrayList<>();
        List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
    
        for (String line : lines) {
            if (line.startsWith(oldCourseID + ",")) {
                String newLine = String.join(",",
                    updatedCourse.getCourseID(),
                    updatedCourse.getGradeLevel(),
                    updatedCourse.getSubject(),
                    updatedCourse.getExamType()
                );
                updatedLines.add(newLine);
            } else {
                updatedLines.add(line);
            }
        }
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : updatedLines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }


    /**
    * UPDATECOURSEIDINDATABASE
    * - Updates the course ID in courses.txt.
    */

    
    public void updateCourseIDInDatabase(String oldID, String newID) throws IOException {
        File dbFile = new File("database/courses.txt");
        List<String> updatedLines = new ArrayList<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader(dbFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(oldID + ",")) {
                    line = line.replaceFirst(oldID, newID);
                }
                updatedLines.add(line);
            }
        }
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dbFile))) {
            for (String updatedLine : updatedLines) {
                writer.write(updatedLine);
                writer.newLine();
            }
        }
    }

    /**
     * DELETECOURSEBYID
     * - Deletes course, attendance, and gradebook files.
     * - Removes course entry from courses.txt.
     */


    public void deleteCourseByID(String courseID) throws IOException {
        File courseFile = new File("database/courses/" + courseID + ".txt");
        File attendanceFile = new File("database/attendance/" + courseID + "_attendance.txt");
        File gradebookFile = new File("database/grades/" + courseID + "_gradebook.txt");
    
        if (courseFile.exists()) courseFile.delete();
        if (attendanceFile.exists()) attendanceFile.delete();
        if (gradebookFile.exists()) gradebookFile.delete();

        File coursesFile = new File("database/courses.txt");
        List<String> lines = new ArrayList<>();
        if (coursesFile.exists()) {
            for (String line : Files.readAllLines(coursesFile.toPath())) {
                if (!line.startsWith(courseID + ",")) {
                    lines.add(line);
                }
            }
            Files.write(coursesFile.toPath(), lines);
        }
    }

    // =============================
    // TEACHER ASSIGNMENT
    // =============================
    // - Retrieves eligible teachers by subject.
    // - Assigns or updates teachers in course files.

    /**
     * GETELIGIBLETEACHERS
     * - Returns list of teachers who can teach a given subject.
     */

    public List<String> getEligibleTeachers(String subject) {
        return staffController.getTeachersBySubject(subject);
    }

    /**
     * ASSIGNTEACHERTOCOURSE
     * - Updates the assigned teacher in the course file.
     */

    public void assignTeacherToCourse(String courseID, String teacherName) throws IOException {
        String filePath = COURSE_DATABASE_PATH + courseID + ".txt";
        File courseFile = new File(filePath);

        if (!courseFile.exists()) {
            throw new FileNotFoundException("Course file for ID " + courseID + " not found.");
        }

        List<String> lines = Files.readAllLines(courseFile.toPath(), StandardCharsets.UTF_8);
        List<String> updatedLines = new ArrayList<>();

        boolean teacherLineFound = false;

        for (String line : lines) {
            if (line.startsWith("Teacher:")) {
                updatedLines.add("Teacher: " + teacherName);
                teacherLineFound = true;
            } else {
                updatedLines.add(line);
            }
        }

        if (!teacherLineFound) {
            if (updatedLines.size() >= 3) {
                updatedLines.add(3, "Teacher: " + teacherName);
            } else {
                updatedLines.add("Teacher: " + teacherName);
            }
        }

        Files.write(courseFile.toPath(), updatedLines, StandardCharsets.UTF_8);
    }

    /**
     * LOADTEACHERFROMFILE
     * - Extracts the teacher's name from a course file.
     */

    private String loadTeacherFromFile(String courseID) {
        String filePath = COURSE_DATABASE_PATH + courseID + ".txt";
        File file = new File(filePath);

        if (!file.exists()) return "";

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Teacher:")) {
                    return line.substring("Teacher:".length()).trim();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading teacher from file: " + e.getMessage());
        }

        return "";
    }


    // =============================
    // STUDENT ASSIGNMENT
    // =============================
    // - Assigns or removes students from course files.
    // - Also updates attendance and gradebook records.

    /**
     * ASSIGNSTUDENTSTOCOURSE
     * - Adds students to course, attendance, and gradebook files.
     */


    public void assignStudentsToCourse(String courseID, List<String> studentIDsWithNames) throws IOException {
        File courseFile = new File("database/courses/" + courseID + ".txt");
        File attendanceFile = new File("database/attendance/" + courseID + "_attendance.txt");
        File gradebookFile = new File("database/grades/" + courseID + "_gradebook.txt");
    
        if (!courseFile.exists()) throw new FileNotFoundException("Course file not found.");
        if (!attendanceFile.exists()) throw new FileNotFoundException("Attendance file not found.");
        if (!gradebookFile.exists()) throw new FileNotFoundException("Gradebook file not found.");
    
        List<String> courseLines = Files.readAllLines(courseFile.toPath());
        List<String> updatedCourseLines = new ArrayList<>();
        boolean studentSectionFound = false;
    
        for (String line : courseLines) {
            updatedCourseLines.add(line);
            if (line.trim().equalsIgnoreCase("Students:")) {
                studentSectionFound = true;
            }
        }
    
        for (String student : studentIDsWithNames) {
            if (studentSectionFound && updatedCourseLines.stream().noneMatch(line -> line.trim().equals("- " + student))) {
                updatedCourseLines.add("- " + student);
            }
        }
    
        Files.write(courseFile.toPath(), updatedCourseLines);
    
        try (BufferedWriter attendanceWriter = new BufferedWriter(new FileWriter(attendanceFile, true))) {
            for (String student : studentIDsWithNames) {
                String[] parts = student.split(" - ");
                if (parts.length >= 2) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    attendanceWriter.write(id + ", " + name);
                    attendanceWriter.newLine();
                }
            }
        }
    
        try (BufferedWriter gradebookWriter = new BufferedWriter(new FileWriter(gradebookFile, true))) {
            for (String student : studentIDsWithNames) {
                String[] parts = student.split(" - ");
                if (parts.length >= 2) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    gradebookWriter.write(id + ", " + name + ", ");
                    gradebookWriter.newLine();
                }
            }
        }
    }

    /**
     * REMOVESTUDENTSFROMCOURSE
     * - Removes specified students from course, attendance, and gradebook files.
     */

    public void removeStudentsFromCourse(String courseID, List<String> idsToRemove) throws IOException {
        File courseFile = new File("database/courses/" + courseID + ".txt");
        File attendanceFile = new File("database/attendance/" + courseID + "_attendance.txt");
        File gradebookFile = new File("database/grades/" + courseID + "_gradebook.txt");
    
        if (!courseFile.exists()) return;
    
        List<String> updatedCourseLines = new ArrayList<>();
        List<String> originalCourseLines = Files.readAllLines(courseFile.toPath());
    
        boolean inStudentSection = false;
    
        for (String line : originalCourseLines) {
            if (line.trim().equalsIgnoreCase("Students:")) {
                inStudentSection = true;
                updatedCourseLines.add(line); 
                continue;
            }
    
            if (inStudentSection && line.trim().startsWith("- ")) {
                String studentID = extractIDFromLine(line);
                if (studentID != null && idsToRemove.contains(studentID)) {
                    continue; 
                }
                updatedCourseLines.add(line);
            } else if (!inStudentSection) {
                updatedCourseLines.add(line); 
            }
        }
    
        Files.write(courseFile.toPath(), updatedCourseLines);
    
        if (attendanceFile.exists()) {
            List<String> updatedAttendanceLines = new ArrayList<>();
            List<String> originalAttendanceLines = Files.readAllLines(attendanceFile.toPath());
    
            for (String line : originalAttendanceLines) {
                if (line.trim().isEmpty() || line.trim().startsWith("Student ID")) {
                    updatedAttendanceLines.add(line); 
                    continue;
                }
    
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    String id = parts[0].trim();
                    if (!idsToRemove.contains(id)) {
                        updatedAttendanceLines.add(line);
                    }
                }
            }
    
            Files.write(attendanceFile.toPath(), updatedAttendanceLines);
        }
    
        if (gradebookFile.exists()) {
            List<String> updatedGradebookLines = new ArrayList<>();
            List<String> originalGradebookLines = Files.readAllLines(gradebookFile.toPath());
    
            for (String line : originalGradebookLines) {
                if (line.trim().isEmpty() || line.trim().startsWith("Student ID")) {
                    updatedGradebookLines.add(line); 
                    continue;
                }
    
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    String id = parts[0].trim();
                    if (!idsToRemove.contains(id)) {
                        updatedGradebookLines.add(line);
                    }
                }
            }
    
            Files.write(gradebookFile.toPath(), updatedGradebookLines);
        }
    }


    /**
     * CLEARSTUDENTSFROMCOURSE
     * - Removes all students from course, attendance, and gradebook files.
     */

    public void clearStudentsFromCourse(String courseID) throws IOException {
        File courseFile = new File("database/courses/" + courseID + ".txt");
        if (!courseFile.exists()) {
            throw new FileNotFoundException("Course file not found: " + courseID);
        }
    
        List<String> lines = new ArrayList<>();
        List<String> originalLines = java.nio.file.Files.readAllLines(courseFile.toPath());
        boolean inStudentSection = false;
    
        for (String line : originalLines) {
            if (line.trim().equalsIgnoreCase("Students:")) {
                lines.add("Students:");
                inStudentSection = true;
                continue;
            }
            if (!inStudentSection) {
                lines.add(line);
            }
        }
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(courseFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    
        String attendancePath = "database/attendance/" + courseID + "_attendance.txt";
        String gradebookPath = "database/grades/" + courseID + "_gradebook.txt";
    
        java.nio.file.Files.write(java.nio.file.Paths.get(attendancePath), List.of("Student ID, Student Name"));
        java.nio.file.Files.write(java.nio.file.Paths.get(gradebookPath), List.of("Student ID, Student Name, Average"));
    }

   
    /**
     * GETASSIGNEDSTUDENTIDS
     * - Returns IDs of students assigned to a course.
     */

    public Set<String> getAssignedStudentIDs(String courseID) {
        Set<String> ids = new HashSet<>();
        String filePath = "database/courses/" + courseID + ".txt";

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            boolean inStudentSection = false;
            for (String line : lines) {
                if (line.trim().equalsIgnoreCase("Students:")) {
                    inStudentSection = true;
                    continue;
                }
                if (inStudentSection && line.startsWith("- ")) {
                    String[] parts = line.substring(2).split(" - ", 2);
                    if (parts.length > 0) ids.add(parts[0].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading assigned student IDs: " + e.getMessage());
        }

        return ids;
    }


    /**
     * GETASSIGNEDSTUDENTS
     * - Returns all student lines listed in a course file.
     */

    public List<String> getAssignedStudents(String courseID) {
        List<String> students = new ArrayList<>();
        File file = new File("database/courses/" + courseID + ".txt");
    
        if (!file.exists()) return students;
    
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean inStudents = false;
    
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("Students:")) {
                    inStudents = true;
                    continue;
                }
    
                if (inStudents && line.trim().startsWith("- ")) {
                    students.add(line.trim());  
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading students for course " + courseID + ": " + e.getMessage());
        }
    
        return students;
    }

    // =============================
    // COURSE LOOKUP & SEARCH
    // =============================
    // - Supports retrieval of course details by ID.
    // - Finds courses based on student ID/name or teacher.

    /**
     * GETCOURSEDETAILS
     * - Returns detailed info of a course including student list.
     */

    public String getCourseDetails(String courseID) {
        String filePath = "database/courses/" + courseID + ".txt";
        File file = new File(filePath);
    
        if (!file.exists()) {
            return "Course file not found for ID: " + courseID;
        }
    
        StringBuilder details = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean inStudentSection = false;
    
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("Students:")) {
                    details.append("\nAssigned Students:\n");
                    inStudentSection = true;
                    continue;
                }
    
                if (inStudentSection && line.startsWith("- ")) {
                    details.append("• ").append(line.substring(2)).append("\n");
                } else if (!inStudentSection) {
                    details.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            return "Error reading course details: " + e.getMessage();
        }
    
        return details.toString();
    }

    /**
     * GETCOURSESFORSTUDENT
     * - Finds all courses a student is enrolled in by ID or name.
     */

    public List<Course> getCoursesForStudent(String studentIDOrName) {
        List<Course> matchingCourses = new ArrayList<>();
        File courseFolder = new File("database/courses");
    
        if (!courseFolder.exists() || !courseFolder.isDirectory()) {
            System.err.println("Course folder not found.");
            return matchingCourses;
        }
    
        File[] courseFiles = courseFolder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (courseFiles == null) return matchingCourses;
    
        for (File courseFile : courseFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(courseFile))) {
                boolean found = false;
                String courseID = courseFile.getName().replace(".txt", "");
                String grade = "", subject = "", examType = "", teacher = "";
                List<String> students = new ArrayList<>();
    
                String line;
                boolean inStudentSection = false;
    
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Grade Level:")) grade = line.substring("Grade Level:".length()).trim();
                    else if (line.startsWith("Subject:")) subject = line.substring("Subject:".length()).trim();
                    else if (line.startsWith("Exam Type:")) examType = line.substring("Exam Type:".length()).trim();
                    else if (line.startsWith("Teacher:")) teacher = line.substring("Teacher:".length()).trim();
                    else if (line.trim().equalsIgnoreCase("Students:")) inStudentSection = true;
                    else if (inStudentSection && line.startsWith("- ")) {
                        students.add(line.substring(2).trim());
                        if (line.contains(studentIDOrName)) {
                            found = true;
                        }
                    }
                }
    
                if (found) {
                    Course course = new Course(courseID, grade, subject, examType, teacher);
                    for (String s : students) {
                        course.addStudent(s);
                    }
                    matchingCourses.add(course);
                }
    
            } catch (IOException e) {
                System.err.println("Error reading file: " + courseFile.getName());
            }
        }
    
        return matchingCourses;
    }

    /**
     * GETCOURSESBYTEACHER
     * - Finds courses assigned to a specific teacher by name or ID.
     */

    
    public List<Course> getCoursesByTeacher(String input) {
        List<Course> result = new ArrayList<>();
        String query = input.trim().toLowerCase();
    
        for (Course course : getCourses()) {
            String teacher = course.getTeacher();
            if (teacher != null && !teacher.isEmpty()) {
                if (teacher.toLowerCase().contains(query)) {
                    result.add(course);
                }
            }
        }
        return result;
    }

    /**
     * GETCOURSESSORTEDBYGRADETHENID
     * - Sorts all courses by grade level then course ID.
     */

    
 
    public List<Course> getCoursesSortedByGradeThenID() {
        List<Course> courses = getCourses();
    
        courses.sort(Comparator
            .comparingInt((Course c) -> Integer.parseInt(c.getGradeLevel()))
            .thenComparing(Course::getCourseID));
    
        return courses;
    }


    // =============================
    // FILE MANAGEMENT UTILITIES
    // =============================
    // - Helper methods for renaming files and extracting info from lines.
    // - Used internally by main operations for consistency and reuse.

    /**
     * CREATEATTENDANCEANDGRADEBOOKFILES
     * - Initializes attendance and gradebook files with appropriate headers.
     */

    private void createAttendanceAndGradebookFiles(Course course) throws IOException {
        String courseID = course.getCourseID();

        String attendanceFilePath = ATTENDANCE_DATABASE_PATH + courseID + "_attendance.txt";
        Files.write(Paths.get(attendanceFilePath), Arrays.asList("Student ID, Student Name"), StandardCharsets.UTF_8);

        String gradebookFilePath = GRADEBOOK_DATABASE_PATH + courseID + "_gradebook.txt";
        Files.write(Paths.get(gradebookFilePath), Arrays.asList("Student ID, Student Name, Average"), StandardCharsets.UTF_8);
    }



    /**
     * RENAMECOURSEFILES
     * - Renames course, attendance, and gradebook files.
     */

    public void renameCourseFiles(String oldID, String newID) throws IOException {
        renameFile("database/courses/" + oldID + ".txt", "database/courses/" + newID + ".txt");
        renameFile("database/attendance/" + oldID + "_attendance.txt", "database/attendance/" + newID + "_attendance.txt");
        renameFile("database/grades/" + oldID + "_gradebook.txt", "database/grades/" + newID + "_gradebook.txt");
    }

    /**
     * RENAMEFILE
     * - Renames a single file if it exists.
     */
    
    private void renameFile(String oldPath, String newPath) throws IOException {
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        if (oldFile.exists()) {
            if (!oldFile.renameTo(newFile)) {
                throw new IOException("Failed to rename file from " + oldPath + " to " + newPath);
            }
        }
    }
    
    
    /**
     * EXTRACTIDFROMLINE
     * - Extracts student ID from the formatted student line.
     */

    private String extractIDFromLine(String line) {
        try {
            String[] parts = line.split("-");
            return parts.length > 1 ? parts[1].trim() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    
    
}
