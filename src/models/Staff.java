package models;

import java.util.*;
import java.util.stream.Collectors;

public class Staff {
    private String staffID;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private ContactInfo contactInfo;
    private Set<String> roles;
    private List<String> subjects;
    private EmergencyContact emergencyContact;

    public Staff(String staffID, String firstName, String lastName, String dateOfBirth,
                     ContactInfo contactInfo, Set<String> roles, List<String> subjects,
                     EmergencyContact emergencyContact) {
        this.staffID = staffID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.contactInfo = contactInfo;
        this.roles = roles;
        this.subjects = subjects;
        this.emergencyContact = emergencyContact;
    }

    /** 
     * ============================
     *       GETTER METHODS
     * ============================
     */

    public String getStaffID() {
        return staffID;
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

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public EmergencyContact getEmergencyContact() {
        return emergencyContact;
    }

    /** 
     * ============================
     *       SETTER METHODS
     * ============================
     */

    public void setStaffID(String staffID) {
        this.staffID = staffID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public void setEmergencyContact(EmergencyContact emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    // Convert to CSV
    public String toCSV() {
        String rolesCSV = String.join(";", roles);
        String subjectsCSV = String.join(";", subjects);
        return String.join(",",
                staffID,
                firstName,
                lastName,
                dateOfBirth,
                contactInfo.toCSV(),
                rolesCSV,
                subjectsCSV,
                emergencyContact.toCSV()
        );
    }

    // Parse from CSV
    public static Staff fromCSV(String csv) {
        String[] parts = csv.split(",", 12);
        if (parts.length < 12) return null;

        String staffID = parts[0].trim();
        String firstName = parts[1].trim();
        String lastName = parts[2].trim();
        String dob = parts[3].trim();

        String contactCSV = String.join(",", parts[4], parts[5], parts[6]);
        ContactInfo contactInfo = ContactInfo.fromCSV(contactCSV);

        Set<String> roles = Arrays.stream(parts[7].split(";"))
                .map(String::trim)
                .collect(Collectors.toSet());

        List<String> subjects = Arrays.stream(parts[8].split(";"))
                .map(String::trim)
                .collect(Collectors.toList());

        String emergencyCSV = String.join(",", parts[9], parts[10], parts[11]);
        EmergencyContact emergencyContact = EmergencyContact.fromCSV(emergencyCSV);

        return new Staff(staffID, firstName, lastName, dob, contactInfo, roles, subjects, emergencyContact);
    }
}
