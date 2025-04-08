package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Course represents a subject being offered to a grade level with a certain exam type and teacher.
 */
public class Course {
    private String courseID;
    private String gradeLevel;
    private String subject;
    private String examType;
    private String teacher;
    private List<String> students;

    public Course(String courseID, String gradeLevel, String subject, String examType, String teacher) {
        this.courseID = courseID;
        this.gradeLevel = gradeLevel;
        this.subject = subject;
        this.examType = examType;
        this.teacher = teacher;
        this.students = new ArrayList<>();
    }

    /** 
     * ============================
     *       GETTER METHODS
     * ============================
     */

    public String getCourseID() { return courseID; }
    public String getGradeLevel() { return gradeLevel; }
    public String getSubject() { return subject; }
    public String getExamType() { return examType; }
    public String getTeacher() { return teacher; }
    public List<String> getStudents() { return students; }

    /** 
     * ============================
     *       SETTER METHODS
     * ============================
     */

    public void setCourseID(String courseID) { this.courseID = courseID; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setExamType(String examType) { this.examType = examType; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    public void setStudents(List<String> students) { this.students = students; }

    public void addStudent(String studentID) {
        if (!students.contains(studentID)) {
            students.add(studentID);
        }
    }

    public void removeStudent(String studentID) {
        students.remove(studentID);
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseID='" + courseID + '\'' +
                ", gradeLevel='" + gradeLevel + '\'' +
                ", subject='" + subject + '\'' +
                ", examType='" + examType + '\'' +
                ", teacher='" + teacher + '\'' +
                ", students=" + students +
                '}';
    }
}
