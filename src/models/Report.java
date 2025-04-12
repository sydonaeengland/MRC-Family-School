package models;

import java.time.LocalDateTime;
import java.util.Map;

public class Report {
    private String courseID;
    private String subject;
    private String gradeLevel;
    private String examType;
    private String teacher;
    private LocalDateTime generatedAt;
    private Map<String, String> studentNames;        
    private Map<String, Double> studentAverages;     
    private double classAverage;
    private Map<String, Integer> performanceDistribution; 

    public Report(String courseID, String subject, String gradeLevel, String examType, String teacher,
                  LocalDateTime generatedAt,
                  Map<String, String> studentNames,
                  Map<String, Double> studentAverages,
                  double classAverage,
                  Map<String, Integer> performanceDistribution) {
        this.courseID = courseID;
        this.subject = subject;
        this.gradeLevel = gradeLevel;
        this.examType = examType;
        this.teacher = teacher;
        this.generatedAt = generatedAt;
        this.studentNames = studentNames;
        this.studentAverages = studentAverages;
        this.classAverage = classAverage;
        this.performanceDistribution = performanceDistribution;
    }

    public String getCourseID() {
        return courseID;
    }

    public String getSubject() {
        return subject;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public String getExamType() {
        return examType;
    }

    public String getTeacher() {
        return teacher;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public Map<String, String> getStudentNames() {
        return studentNames;
    }

    public Map<String, Double> getStudentAverages() {
        return studentAverages;
    }

    public double getClassAverage() {
        return classAverage;
    }

    public Map<String, Integer> getPerformanceDistribution() {
        return performanceDistribution;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public void setStudentNames(Map<String, String> studentNames) {
        this.studentNames = studentNames;
    }

    public void setStudentAverages(Map<String, Double> studentAverages) {
        this.studentAverages = studentAverages;
    }

    public void setClassAverage(double classAverage) {
        this.classAverage = classAverage;
    }

    public void setPerformanceDistribution(Map<String, Integer> performanceDistribution) {
        this.performanceDistribution = performanceDistribution;
    }

    public String toTextFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("COURSE REPORT - ").append(courseID).append("\n");
        sb.append("Subject: ").append(subject)
          .append(" | Grade Level: ").append(gradeLevel)
          .append(" | Exam Type: ").append(examType)
          .append(" | Teacher: ").append(teacher).append("\n");
        sb.append("Generated At: ").append(generatedAt.toString()).append("\n\n");

        sb.append("Student ID, Student Name, Average\n");
        sb.append("--------------------------------------\n");

        for (String studentID : studentAverages.keySet()) {
            String name = studentNames.getOrDefault(studentID, "Unknown");
            double avg = studentAverages.get(studentID);
            sb.append(String.format("%s, %s, %.2f%n", studentID, name, avg));
        }

        sb.append("\nClass Average: ").append(String.format("%.2f", classAverage)).append("\n\n");

        sb.append("Performance Distribution:\n");
        for (Map.Entry<String, Integer> entry : performanceDistribution.entrySet()) {
            sb.append(String.format("%s → %d students%n", entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }
} 

