package controllers;

import models.Assessment;

import java.io.*;
import java.util.*;

public class AssessmentController {
    private static final String ASSESSMENT_FILE = "database/assessments.txt";

    /** ======================
     *  CREATE NEW ASSESSMENT
     *  ====================== */
    public void createAssessment(String name, String type, String dateStr, String courseID) throws IOException {
        String id = generateAssessmentID(type);
        Assessment assessment = new Assessment(id, name, type, dateStr, courseID);
    
        saveAssessmentToFile(assessment);
    
        new GradebookController().addAssessmentToGradebook(courseID, id + " - " + name);
    }
    

    /** ======================
     *  UPDATE EXISTING ASSESSMENT
     *  ====================== */
    public void updateAssessment(Assessment updated) throws IOException {
        List<Assessment> all = getAllAssessments();
        try (PrintWriter writer = new PrintWriter(new FileWriter(ASSESSMENT_FILE))) {
            for (Assessment a : all) {
                if (a.getId().equals(updated.getId())) {
                    a = updated;
                }
                writer.println(toCSV(a));
            }
        }
    }

    /** ======================
     *  LOAD ALL ASSESSMENTS
     *  ====================== */
    public List<Assessment> getAllAssessments() {
        List<Assessment> list = new ArrayList<>();
        File file = new File(ASSESSMENT_FILE);
        if (!file.exists()) return list;
    
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 5) continue;
    
                list.add(new Assessment(parts[0], parts[1], parts[2], parts[3], parts[4]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return list;
    }
    
    /** ======================
     *  FILTER BY COURSE ID
     *  ====================== */
    public List<Assessment> getAssessmentsByCourseID(String courseID) {
        List<Assessment> filtered = new ArrayList<>();
        for (Assessment a : getAllAssessments()) {
            if (a.getCourseID().equalsIgnoreCase(courseID)) {
                filtered.add(a);
            }
        }
        return filtered;
    }

    /** ======================
     *  SAVE SINGLE ASSESSMENT
     *  ====================== */
    private void saveAssessmentToFile(Assessment a) throws IOException {
        File file = new File(ASSESSMENT_FILE);
        file.getParentFile().mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println(toCSV(a));
        }
    }

    /** ======================
     *  CONVERT TO CSV LINE
     *  ====================== */
    private String toCSV(Assessment a) {
        return String.join(",", a.getId(), a.getName(), a.getType(), a.getDateString(), a.getCourseID());
    }    

    /** ======================
     *  ID GENERATOR: T0001 / E0001
     *  ====================== */
    public String generateAssessmentID(String type) {
      String prefix = type.equalsIgnoreCase("Test") ? "T" : "E";
      Set<String> existingIDs = new HashSet<>();
      for (Assessment a : getAllAssessments()) {
          existingIDs.add(a.getId());
      }
  
      Random rand = new Random();
      String id;
      do {
          int num = rand.nextInt(10000); 
          id = prefix + String.format("%04d", num); 
      } while (existingIDs.contains(id));
  
      return id;
  }
  
  

    /** ======================
     *  GET BY COURSE (for dropdowns etc.)
     *  ====================== */
    public List<Assessment> getAssessmentsForCourse(String courseID) {
        List<Assessment> result = new ArrayList<>();
        List<Assessment> all = getAllAssessments();

        for (Assessment a : all) {
            if (a.getCourseID().equals(courseID)) {
                result.add(a);
            }
        }

        return result;
    }
}
