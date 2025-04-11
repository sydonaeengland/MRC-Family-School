package controllers;

import java.io.*;
import java.util.*;
import models.Student;

public class AttendanceController {
    private static final String ATTENDANCE_DIR = "database/attendance/";

    // =============================
    // ATTENDANCE DATA MANAGEMENT
    // =============================
    // - Load, save, and update attendance data for courses
    // - Supports adding/removing attendance columns
    // - Manages student attendance status 

    /**
     * GETATTENDANCEDATAFORCOURSE
     * - Loads attendance data for a given course
     */
    public List<String[]> getAttendanceDataForCourse(String courseID) {
        List<String[]> data = new ArrayList<>();
        String path = ATTENDANCE_DIR + courseID + "_attendance.txt";
        File file = new File(path);

        if (!file.exists()) {
            data.add(new String[]{"Student ID", "Student Name"});
            return data;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",", -1);
                for (int i = 0; i < columns.length; i++) {
                    columns[i] = columns[i].trim();
                }
                data.add(columns);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * SAVEATTENDANCEDATA
     * - Writes updated attendance data to file
     */
    public void saveAttendanceData(String courseID, List<String[]> rows) throws IOException {
        String path = ATTENDANCE_DIR + courseID + "_attendance.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    if (row[i] == null) row[i] = "";
                    row[i] = row[i].trim();
                }
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
    }

    /**
     * DELETEATTENDANCECOLUMN
     * - Deletes a single date column from attendance file
     * - Skips if date is not found
     */

    public void deleteAttendanceColumn(String courseID, String date) {
        String path = ATTENDANCE_DIR + courseID + "_attendance.txt";
        File file = new File(path);
        if (!file.exists()) return;

        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }

            if (lines.isEmpty()) return;

            List<String> updatedLines = new ArrayList<>();
            int dateColIndex = -1;

            String[] headers = lines.get(0).split(",");
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equals(date)) {
                    dateColIndex = i;
                    break;
                }
            }

            if (dateColIndex == -1) return;

            for (String line : lines) {
                String[] cols = line.split(",", -1);
                List<String> newCols = new ArrayList<>();
                for (int i = 0; i < cols.length; i++) {
                    if (i != dateColIndex) {
                        newCols.add(cols[i].trim());
                    }
                }
                updatedLines.add(String.join(",", newCols));
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String updated : updatedLines) {
                    writer.write(updated);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // STUDENT MANAGEMENT
    // =============================
    // - Fetches assigned students from attendance file
    // - Maps student ID to Student objects using StudentController

    /**
     * GETASSIGNEDSTUDENTSFORCOURSE
     * - Retrieves student objects assigned to a course
     * - Uses attendance file and StudentController lookup
     */

    public List<Student> getAssignedStudentsForCourse(String courseID) {
        List<Student> students = new ArrayList<>();
        StudentController studentController = new StudentController();
        List<String[]> data = getAttendanceDataForCourse(courseID);
    
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 2) {
                String studentID = row[0].trim();
                Student student = studentController.getStudentByID(studentID);
                if (student != null) {
                    students.add(student);
                }
            }
        }
    
        return students;
    }
    

    /**
     * GETSTUDENTLISTFROMATTENDANCEFILE
     * - Extracts [ID, Name] of each student from attendance file
     */

    public List<String[]> getStudentListFromAttendanceFile(String courseID) {
        List<String[]> data = getAttendanceDataForCourse(courseID);
        List<String[]> students = new ArrayList<>();

        if (data.size() > 1) {
            for (int i = 1; i < data.size(); i++) {
                String[] row = data.get(i);
                if (row.length >= 2) {
                    students.add(new String[]{row[0], row[1]});
                }
            }
        }

        return students;
    }

     /**
     * BUILDUPDATEDATTENDANCEDATA
     * - Builds full attendance table with updated date column
     * - Ensures all students have a status for the given date
     */

    public List<String[]> buildUpdatedAttendanceData(String courseID, String selectedDate, Map<String, String> studentStatusMap) {
        List<String[]> data = getAttendanceDataForCourse(courseID);
        Map<String, String[]> existingRows = new LinkedHashMap<>();
        Set<String> existingDates = new TreeSet<>();
    
        if (!data.isEmpty()) {
            String[] headers = data.get(0);
            for (String h : headers) {
                if (!h.equalsIgnoreCase("Student ID") && !h.equalsIgnoreCase("Student Name")) {
                    existingDates.add(h);
                }
            }
        }
    
        existingDates.add(selectedDate);
        List<String> sortedDates = new ArrayList<>(existingDates);
    
        List<String> finalHeader = new ArrayList<>();
        finalHeader.add("Student ID");
        finalHeader.add("Student Name");
        finalHeader.addAll(sortedDates);
    
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            String key = row[0] + " - " + row[1];
            existingRows.put(key, row);
        }
    
        List<String[]> updatedData = new ArrayList<>();
        updatedData.add(finalHeader.toArray(new String[0]));
    
        for (Map.Entry<String, String> entry : studentStatusMap.entrySet()) {
            String[] parts = entry.getKey().split(" - ", 2);
            String id = parts[0];
            String name = parts[1];
            String status = entry.getValue();
    
            String[] row = existingRows.getOrDefault(entry.getKey(), new String[2 + sortedDates.size()]);
            if (row[0] == null || row[0].isEmpty()) {
                row[0] = id;
                row[1] = name;
            }
    
            int index = sortedDates.indexOf(selectedDate);
            if (index >= 0) {
                if (row.length < 2 + sortedDates.size()) {
                    row = Arrays.copyOf(row, 2 + sortedDates.size());
                }
                row[2 + index] = status;
            }
    
            updatedData.add(row);
        }
    
        return updatedData;
    }

    
    
}
