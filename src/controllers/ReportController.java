package controllers;

import models.Course;
import models.Report;
import models.Student;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

public class ReportController {

    private GradebookController gradebookController = new GradebookController();
    private StudentController studentController = new StudentController();
    private CourseController courseController = new CourseController();

    /**
     * Generate a Report object for a course.
     */
    public Report generateReportForCourse(Course course) {
        String courseID = course.getCourseID();

        Map<String, String> studentNames = new LinkedHashMap<>();
        Map<String, Double> studentAverages = new LinkedHashMap<>();

        File file = new File("database/grades/" + courseID + "_gradebook.txt");
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String[] headers = reader.readLine().split(",", -1);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String averageStr = parts[2].trim();

                    try {
                        double avg = Double.parseDouble(averageStr);
                        studentNames.put(id, name);
                        studentAverages.put(id, avg);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        double classAverage = calculateClassAverage(studentAverages);
        Map<String, Integer> distribution = generatePerformanceDistribution(studentAverages);

        return new Report(
          courseID,
          course.getSubject(),
          course.getGradeLevel(),
          course.getExamType(),
          course.getTeacher(),
          LocalDateTime.now(),
          studentNames,
          studentAverages,
          classAverage,
          distribution
      );
    }

    /**
     * Calculate class average from student averages.
     */
    public double calculateClassAverage(Map<String, Double> studentAverages) {
        if (studentAverages.isEmpty()) return 0;
        double total = 0;
        for (double avg : studentAverages.values()) {
            total += avg;
        }
        return total / studentAverages.size();
    }

    /**
     * Group Averages
     */
    public Map<String, Integer> generatePerformanceDistribution(Map<String, Double> studentAverages) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("0–49%", 0);
        distribution.put("50–69%", 0);
        distribution.put("70–84%", 0);
        distribution.put("85–100%", 0);

        for (double avg : studentAverages.values()) {
            if (avg < 50) distribution.put("0–49%", distribution.get("0–49%") + 1);
            else if (avg < 70) distribution.put("50–69%", distribution.get("50–69%") + 1);
            else if (avg < 85) distribution.put("70–84%", distribution.get("70–84%") + 1);
            else distribution.put("85–100%", distribution.get("85–100%") + 1);
        }

        return distribution;
    }

    /**
     * Save a report to a .txt file in /database/reports/
     */
    public void saveReportAsTextFile(Report report) {
        File folder = new File("database/reports");
        if (!folder.exists()) folder.mkdirs();

        String fileName = "database/reports/" + report.getCourseID() + "_report_" +
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println(report.toTextFormat());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
