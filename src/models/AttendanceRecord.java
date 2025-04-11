package models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AttendanceRecord {
    private String studentID;
    private String studentName;

    private Map<String, String> attendanceByDate;

    public AttendanceRecord(String studentID, String studentName) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.attendanceByDate = new LinkedHashMap<>(); 
    }

    /** 
     * ============================
     *       GETTER METHODS
     * ============================
     */

    public String getStudentID() {
        return studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public Map<String, String> getAttendanceByDate() {
        return attendanceByDate;
    }

    public void setAttendanceStatus(String date, String status) {
        attendanceByDate.put(date, status);
    }

    public String getAttendanceStatus(String date) {
        return attendanceByDate.getOrDefault(date, "");
    }

    public String[] toRow(List<String> allDates) {
        String[] row = new String[2 + allDates.size()];
        row[0] = studentID;
        row[1] = studentName;
        for (int i = 0; i < allDates.size(); i++) {
            String date = allDates.get(i);
            row[2 + i] = attendanceByDate.getOrDefault(date, "");
        }
        return row;
    }

    public static AttendanceRecord fromRow(String[] row, List<String> dateHeaders) {
        AttendanceRecord record = new AttendanceRecord(row[0], row[1]);
        for (int i = 0; i < dateHeaders.size(); i++) {
            String status = (2 + i < row.length) ? row[2 + i] : "";
            record.setAttendanceStatus(dateHeaders.get(i), status);
        }
        return record;
    }
}
