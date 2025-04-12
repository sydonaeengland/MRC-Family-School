package controllers;

import models.Assessment;
import models.Course;
import models.Student;
import views.GradeEntryForm;
import views.AssessmentEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.*;

public class GradebookController {
    private static final String GRADEBOOK_FOLDER = "database/grades/";
    private AssessmentController assessmentController = new AssessmentController();
    private CourseController courseController = new CourseController();
    private StudentController studentController = new StudentController();
    private JFrame parentFrame;


    public void populateGradebookTable(Course course, DefaultTableModel model) {
        File file = new File(GRADEBOOK_FOLDER + course.getCourseID() + "_gradebook.txt");
    
        if (!file.exists()) {
            createInitialGradebookFile(course.getCourseID());
        }
    
        model.setRowCount(0);
        model.setColumnCount(0);
    
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return;
    
            String[] headers = headerLine.split(",", -1);
            for (String h : headers) model.addColumn(h);
    
            String line;
            while ((line = reader.readLine()) != null) {
                model.addRow(line.split(",", -1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private void createInitialGradebookFile(String courseID) {
        try {
            File file = new File(GRADEBOOK_FOLDER + courseID + "_gradebook.txt");
            file.getParentFile().mkdirs();

            List<String> studentLines = courseController.getAssignedStudents(courseID);
            List<Student> students = new ArrayList<>();
            for (String line : studentLines) {
                String[] parts = line.split(" - ");
                if (parts.length >= 2) {
                    Student student = studentController.getStudentByID(parts[0].trim());
                    if (student != null) {
                        students.add(student);
                    }
                }
            }

            PrintWriter writer = new PrintWriter(file);
            writer.println("Student ID,Student Name,Average");
            for (Student s : students) {
              writer.println(s.getStudentID() + "," + s.getFirstName() + " " + s.getLastName() + ",0.00");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void saveGradesImmediately(Course course, DefaultTableModel model) {
        File file = new File(GRADEBOOK_FOLDER + course.getCourseID() + "_gradebook.txt");
        try (PrintWriter writer = new PrintWriter(file)) {

            for (int i = 0; i < model.getColumnCount(); i++) {
                writer.print(model.getColumnName(i));
                if (i < model.getColumnCount() - 1) writer.print(",");
            }
            writer.println();

            for (int i = 0; i < model.getRowCount(); i++) {
                double total = 0;
                int count = 0;

                for (int j = 0; j < model.getColumnCount(); j++) {
                    String val = String.valueOf(model.getValueAt(i, j)).trim();
                    if (j >= 3 && !val.isEmpty()) {
                        try {
                            total += Double.parseDouble(val);
                            count++;
                        } catch (NumberFormatException ignored) {}
                    }
                    writer.print(val);
                    if (j < model.getColumnCount() - 1) writer.print(",");
                }

                double avg = count == 0 ? 0.0 : total / count;
                model.setValueAt(String.format("%.2f", avg), i, 2);
                writer.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAssessmentGrades(Course course, String assessmentID, Map<String, String> grades) {
        File file = new File(GRADEBOOK_FOLDER + course.getCourseID() + "_gradebook.txt");
        List<String[]> rows = new ArrayList<>();
        int columnIndex = -1;
    
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String[] headers = reader.readLine().split(",", -1);
            List<String> headerList = new ArrayList<>(Arrays.asList(headers));
    
            String displayColumn = assessmentID + " - " + getAssessmentNameByID(assessmentID);
            if (!headerList.contains(displayColumn)) {
                headerList.add(displayColumn);
                columnIndex = headerList.size() - 1;
            } else {
                columnIndex = headerList.indexOf(displayColumn);
            }
    
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                List<String> row = new ArrayList<>(Arrays.asList(parts));
                while (row.size() < headerList.size()) row.add("");
                rows.add(row.toArray(new String[0]));
            }
    
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println(String.join(",", headerList));
    
                for (String[] row : rows) {
                    String studentID = row[0];
                    String grade = grades.getOrDefault(studentID, "").trim();
                    if (!grade.isEmpty()) row[columnIndex] = grade;
    
                    double testTotal = 0, examTotal = 0;
                    int testCount = 0, examCount = 0;
    
                    for (int i = 3; i < row.length; i++) {
                        String colName = headerList.get(i);
                        String value = row[i];
    
                        try {
                            double score = Double.parseDouble(value);
                            String idPrefix = colName.trim().split(" ")[0].toUpperCase(); // e.g., T1234
                            if (idPrefix.startsWith("T")) {
                                testTotal += score;
                                testCount++;
                            } else if (idPrefix.startsWith("E")) {
                                examTotal += score;
                                examCount++;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
    
                    double testAvg = testCount > 0 ? testTotal / testCount : 0;
                    double examAvg = examCount > 0 ? examTotal / examCount : 0;
                    double weightedAvg = (testAvg * 0.4) + (examAvg * 0.6);
                    row[2] = String.format("%.2f", weightedAvg);
                }
    
                for (String[] updatedRow : rows) {
                    writer.println(String.join(",", updatedRow));
                }
            }
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void openAddAssessmentDialog(JFrame parent, Course course, Runnable onSave) {
        new AssessmentEntry(parent, course.getCourseID(), onSave).setVisible(true);
    }

    public void openEditAssessmentDialog(JFrame parent, Assessment selected, Runnable onSave) {
        new AssessmentEntry(parent, selected, onSave).setVisible(true);
    }

    public void deleteAssessmentAndGrades(JFrame parent, Course course, Assessment assessment, Runnable onSave) {
        String assessmentID = assessment.getId();
        String courseID = course.getCourseID();
        File gradebookFile = new File(GRADEBOOK_FOLDER + courseID + "_gradebook.txt");
        List<String[]> rows = new ArrayList<>();
        List<String> headers = new ArrayList<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader(gradebookFile))) {
            String[] headerLine = reader.readLine().split(",", -1);
            headers = new ArrayList<>(Arrays.asList(headerLine));
    
            int removeIndex = -1;
            for (int i = 0; i < headers.size(); i++) {
                if (headers.get(i).startsWith(assessmentID)) {
                    removeIndex = i;
                    break;
                }
            }
    
            if (removeIndex == -1) {
                JOptionPane.showMessageDialog(parent, "Assessment column not found in gradebook.");
                return;
            }
    
            headers.remove(removeIndex);
    
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                List<String> row = new ArrayList<>(Arrays.asList(parts));
                if (removeIndex < row.size()) {
                    row.remove(removeIndex);
                }
                rows.add(row.toArray(new String[0]));
            }
    
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    
        try (PrintWriter writer = new PrintWriter(gradebookFile)) {
            writer.println(String.join(",", headers));
    
            for (String[] row : rows) {
                double testTotal = 0, examTotal = 0;
                int testCount = 0, examCount = 0;
    
                for (int i = 3; i < headers.size(); i++) {
                    String header = headers.get(i);
                    String value = i < row.length ? row[i] : "";
    
                    try {
                        double score = Double.parseDouble(value);
                        if (header.startsWith("T")) {
                            testTotal += score;
                            testCount++;
                        } else if (header.startsWith("E")) {
                            examTotal += score;
                            examCount++;
                        }
                    } catch (NumberFormatException ignored) {}
                }
    
                double testAvg = testCount > 0 ? testTotal / testCount : 0;
                double examAvg = examCount > 0 ? examTotal / examCount : 0;
                double weightedAvg = (testAvg * 0.4) + (examAvg * 0.6);
    
                if (row.length > 2) {
                    row[2] = String.format("%.2f", weightedAvg);
                }
    
                writer.println(String.join(",", row));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    
        File assessmentsFile = new File("database/assessments.txt");
        List<String> updatedLines = new ArrayList<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader(assessmentsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(assessmentID + ",")) {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    
        try (PrintWriter writer = new PrintWriter(assessmentsFile)) {
            for (String updatedLine : updatedLines) {
                writer.println(updatedLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    
        if (onSave != null) onSave.run();
    }
    
    

    public void openGradeEntryDialog(JFrame parent, Course course, Assessment assessment, Runnable onSaveCallback){
      new GradeEntryForm(parent, course, this, assessment, onSaveCallback).setVisible(true);
    }
  
    public void addAssessmentToGradebook(String courseID, String columnHeader) {
      File file = new File("database/grades/" + courseID + "_gradebook.txt");
      if (!file.exists()) return;
  
      List<String[]> rows = new ArrayList<>();
  
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
          String line;
          while ((line = reader.readLine()) != null) {
              rows.add(line.split(",", -1));
          }
      } catch (IOException e) {
          e.printStackTrace();
          return;
      }
  
      if (!rows.isEmpty()) {
          String[] headerRow = rows.get(0);
          String[] newHeader = Arrays.copyOf(headerRow, headerRow.length + 1);
          newHeader[newHeader.length - 1] = columnHeader;
          rows.set(0, newHeader);
      }
  
      for (int i = 1; i < rows.size(); i++) {
          String[] row = rows.get(i);
          String[] newRow = Arrays.copyOf(row, row.length + 1);
          newRow[newRow.length - 1] = ""; 
          rows.set(i, newRow);
      }
  
      try (PrintWriter writer = new PrintWriter(file)) {
          for (String[] row : rows) {
              writer.println(String.join(",", row));
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

  private String getAssessmentNameByID(String id) {
    for (Assessment a : new AssessmentController().getAllAssessments()) {
        if (a.getId().equals(id)) return a.getName();
    }
    return "";
  }

  public Map<String, String> getGradesForAssessment(Course course, String assessmentID) {
    Map<String, String> grades = new HashMap<>();
    File file = new File("database/grades/" + course.getCourseID() + "_gradebook.txt");

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String[] headers = reader.readLine().split(",", -1);

        String displayColumn = assessmentID + " - " + getAssessmentNameByID(assessmentID);
        int index = Arrays.asList(headers).indexOf(displayColumn);
        if (index == -1) return grades;

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",", -1);
            if (parts.length > index) {
                grades.put(parts[0], parts[index]); 
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    return grades;
    }

    public void updateAssessmentNameInGradebook(String courseID, Assessment updatedAssessment) {
        File file = new File("database/grades/" + courseID + "_gradebook.txt");
    
        if (!file.exists()) return;
    
        List<String> lines = new ArrayList<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String[] headers = reader.readLine().split(",", -1);
    
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].startsWith(updatedAssessment.getId())) {
                    headers[i] = updatedAssessment.getId() + " - " + updatedAssessment.getName();
                    break;
                }
            }
    
            lines.add(String.join(",", headers));
    
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
    
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    



  
}
