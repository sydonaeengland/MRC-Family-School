package models;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Student {
    private String studentID;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private int age;
    private String currentGrade;
    private String currentSchool;
    private ContactInfo contactInfo;
    private List<GuardianInfo> guardians;

    /** 
     * Constructor for creating a new Student 
     */
    public Student(String studentID, String firstName, String lastName, String dateOfBirth, 
                   String currentGrade, String currentSchool, ContactInfo contactInfo) {
        this.studentID = studentID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.age = calculateAge(dateOfBirth);
        this.currentGrade = currentGrade;
        this.currentSchool = currentSchool;
        this.contactInfo = contactInfo;
        this.guardians = new ArrayList<>();
    }

    /** 
     * Calculates the student's age based on the date of birth 
     */
    private int calculateAge(String dob) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate birthDate = LocalDate.parse(dob, formatter);
            LocalDate currentDate = LocalDate.now();
            return Period.between(birthDate, currentDate).getYears();
        } catch (Exception e) {
            System.err.println("Invalid date format for DOB: " + dob);
            return 0; 
        }
    }

    /** 
     * ============================
     *       GETTER METHODS
     * ============================
     */

    public String getStudentID() {
        return studentID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public int getAge() {
        return age;
    }

    public String getCurrentGrade() {
        return currentGrade;
    }

    public String getCurrentSchool() {
        return currentSchool;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public List<GuardianInfo> getGuardians() {
        return guardians;
    }

    /** 
     * ============================
     *       SETTER METHODS
     * ============================
     */

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        this.age = calculateAge(dateOfBirth); 
    }

    public void setCurrentGrade(String currentGrade) {
        this.currentGrade = currentGrade;
    }

    public void setCurrentSchool(String currentSchool) {
        this.currentSchool = currentSchool;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    /** 
     * ============================
     *   GUARDIAN MANAGEMENT
     * ============================
     */

    public void addGuardian(GuardianInfo guardian) {
        guardians.add(guardian);
    }

    public void removeGuardian(GuardianInfo guardian) {
        guardians.remove(guardian);
    }

    /** 
     * Converts Student object to CSV format 
     */
    public String toCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append(studentID).append(",")
           .append(firstName).append(",")
           .append(lastName).append(",")
           .append(dateOfBirth).append(",")
           .append(age).append(",")
           .append(currentGrade).append(",")
           .append(currentSchool).append(",")
           .append(contactInfo != null ? contactInfo.toCSV() : "");

        for (GuardianInfo guardian : guardians) {
            csv.append(",").append(guardian.toCSV());
        }

        return csv.toString();
    }

    /** 
     * Converts CSV line back into a Student object 
     */
    public static Student fromCSV(String csv) {
        String[] parts = csv.split(",");
        if (parts.length < 10) {
            System.err.println("Invalid student CSV format: " + csv);
            return null;
        }
    
        ContactInfo contact = new ContactInfo(
                parts[7].trim(),
                parts[8].trim(),
                parts[9].trim()
        );
    
        Student student = new Student(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[5].trim(),
                parts[6].trim(),
                contact
        );
    
        for (int i = 10; i + 4 < parts.length; i += 5) {
            GuardianInfo guardian = new GuardianInfo(
                    parts[i].trim(),
                    parts[i + 1].trim(),
                    parts[i + 2].trim(),
                    parts[i + 3].trim(),
                    parts[i + 4].trim()
            );
            student.addGuardian(guardian);
        }
    
        return student;
    }
    

    /** 
     * Compares two Student objects based on Student ID 
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student other = (Student) obj;
        return studentID != null && studentID.equals(other.studentID);
    }

    @Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Student ID: ").append(studentID).append("\n");
    sb.append("Name: ").append(firstName).append(" ").append(lastName).append("\n");
    sb.append("DOB: ").append(dateOfBirth).append("\n");
    sb.append("Grade: ").append(currentGrade).append("\n");
    sb.append("School: ").append(currentSchool).append("\n");
    
    if (contactInfo != null) {
        sb.append("Contact Number: ").append(contactInfo.getPhoneNumber()).append("\n");
        sb.append("Email: ").append(contactInfo.getEmail()).append("\n");
        sb.append("Address: ").append(contactInfo.getAddress()).append("\n");
    }

    if (!guardians.isEmpty()) {
        for (int i = 0; i < guardians.size(); i++) {
            sb.append("Guardian ").append(i + 1).append(": ").append(guardians.get(i).getGuardianFirstName()).append(" ")
              .append(guardians.get(i).getGuardianLastName()).append(" (").append(guardians.get(i).getRelation()).append(")\n")
              .append("Phone: ").append(guardians.get(i).getPhoneNumber()).append("\n")
              .append("Email: ").append(guardians.get(i).getEmail()).append("\n");
        }
    }
    return sb.toString();
}

}
