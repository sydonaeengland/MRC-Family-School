package controllers;

import models.ContactInfo;
import models.EmergencyContact;
import models.Staff;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class StaffController {
    private static final String STAFF_FILE = "database/staff.txt";
    private List<Staff> staffList;

    public StaffController() {
        staffList = new ArrayList<>();
        loadStaffFromFile();
    }

    /** 
     * SECTION: CRUD OPERATIONS
     * - Handles adding, updating, and deleting staff records.
     */

    /** 
     * ADDORUPDATESTAFF
     * - Removes existing staff with same ID (if any)
     * - Adds the new/updated staff to the list
     * - Saves all staff to file
     */

    public void addOrUpdateStaff(Staff staff) {
        staffList.removeIf(s -> s.getStaffID().equals(staff.getStaffID()));
        staffList.add(staff);
        saveStaffToFile();
    }


    /** 
     * DELETESTAFF
     * - Removes a staff member by ID
     * - Saves updated list to file if removal occurred
     * - Returns true if deleted, false if not found
     */

    public boolean deleteStaff(String staffID) {
        boolean removed = staffList.removeIf(s -> s.getStaffID().equals(staffID));
        if (removed) {
            saveStaffToFile();
        }
        return removed;
    }

    /** 
     * SECTION: SEARCH OPERATIONS
     * - Supports searching staff by ID, name, or full name.
     */

    /** 
     * GETSTAFFBYID
     * - Searches staff list by ID (case-insensitive)
     * - Returns matching staff or null if not found
     */

    public Staff getStaffByID(String staffID) {
        return staffList.stream()
                .filter(s -> s.getStaffID().equalsIgnoreCase(staffID))
                .findFirst()
                .orElse(null);
    }

    /** 
     * SEARCHSTAFFBYNAME
     * - Returns staff whose first or last name matches given name
     * - Sorts results alphabetically by last name then first name
     */

    public List<Staff> searchStaffByName(String name) {
        List<Staff> results = new ArrayList<>();
        for (Staff staff : staffList) {
            if (staff.getFirstName().equalsIgnoreCase(name) ||
                staff.getLastName().equalsIgnoreCase(name)) {
                results.add(staff);
            }
        }
        results.sort(Comparator.comparing(Staff::getLastName)
                               .thenComparing(Staff::getFirstName));
        return results;
    }

    /** 
     * SEARCHSTAFFBYFULLNAME
     * - Returns staff with matching first and last name
     * - Case-insensitive search
     */

    public List<Staff> searchStaffByFullName(String firstName, String lastName) {
        List<Staff> results = new ArrayList<>();
        for (Staff staff : staffList) {
            if (staff.getFirstName().equalsIgnoreCase(firstName) &&
                staff.getLastName().equalsIgnoreCase(lastName)) {
                results.add(staff);
            }
        }
        return results;
    }

    /** 
     * SECTION: ID GENERATION
     * - Generates a unique staff ID in the format SXXXX.
     */

     /** 
     * GENERATESTAFFID
     * - Generates a unique staff ID in format 'SXXXX'
     * - Ensures no duplicates in current staff list
     */

    public String generateStaffID() {
        Set<String> existingIDs = new HashSet<>();
        for (Staff s : staffList) {
            existingIDs.add(s.getStaffID());
        }

        String id;
        Random rand = new Random();
        do {
            id = "S" + String.format("%04d", rand.nextInt(10000));
        } while (existingIDs.contains(id));
        return id;
    }

    /** 
     * SECTION: FILE HANDLING
     * - Loads and saves staff records to 'staff.txt'.
     */


    /** 
     * LOADSTAFFFROMFILE
     * - Loads staff data from 'staff.txt'
     */

    private void loadStaffFromFile() {
        staffList.clear();
        File file = new File(STAFF_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Staff staff = Staff.fromCSV(line);
                if (staff != null) staffList.add(staff);
            }
        } catch (IOException e) {
            System.err.println("Error loading staff data: " + e.getMessage());
        }
    }

    /** 
     * SAVESTAFFTOFILE
     * - Writes all staff records to 'staff.txt'
     * - Uses Staff.toCSV for formatting
     */

    private void saveStaffToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STAFF_FILE))) {
            for (Staff staff : staffList) {
                writer.write(staff.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving staff data: " + e.getMessage());
        }
    }

    /** 
     * GETALLSTAFF
     * - Returns all staff sorted by last name then first name
     */

    public List<Staff> getAllStaff() {
        staffList.sort(Comparator.comparing(Staff::getLastName)
                                 .thenComparing(Staff::getFirstName));
        return staffList;
    }

    /** 
     * SECTION: VALIDATION
     * - Validates format and required fields in staff and contact info.
     */

     /** 
     * VALIDATESTAFFINFO
     * - Validates phone/email format and required emergency info
     * - Returns false and builds error messages if validation fails
     */


    public boolean validateStaffInfo(Staff staff, StringBuilder errorMessage) {
      ContactInfo contact = staff.getContactInfo();
      EmergencyContact emergency = staff.getEmergencyContact();
  
      boolean valid = true;
  
      if (!staff.getDateOfBirth().matches("\\d{4}/\\d{2}/\\d{2}")) {
          errorMessage.append("Date of birth must be in format YYYY/MM/DD.\n");
          valid = false;
      }
  
      if (!Pattern.matches("\\(876\\)\\d{3}-\\d{4}", contact.getPhoneNumber())) {
          errorMessage.append("Main phone number must match (876)XXX-XXXX format.\n");
          valid = false;
      }
  
      if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", contact.getEmail())) {
          errorMessage.append("Email address is not valid.\n");
          valid = false;
      }
  
      if (!Pattern.matches("\\(876\\)\\d{3}-\\d{4}", emergency.getPhone())) {
          errorMessage.append("Emergency phone number must match (876)XXX-XXXX format.\n");
          valid = false;
      }
  
      if (emergency.getName().isEmpty() || emergency.getRelationship().isEmpty()) {
          errorMessage.append("Emergency contact name and relationship are required.\n");
          valid = false;
      }
  
      return valid;
    }

    /** 
     * SECTION: TEACHER FILTERING
     * - Filters staff by 'Teacher' role and subject expertise.
     */

     /** 
     * GETTEACHERSBYSUBJECT
     * - Returns staff with 'Teacher' role who can teach a specific subject
     * - Matches subject name exactly or partially (case-insensitive)
     */
  

    public List<String> getTeachersBySubject(String subject) {
        List<String> teachers = new ArrayList<>();
        for (Staff staff : staffList) {
            if (staff.getRoles().contains("Teacher")) {
                for (String s : staff.getSubjects()) {
                    if (s.equalsIgnoreCase(subject) || s.toLowerCase().contains(subject.toLowerCase())) {
                        teachers.add(staff.getStaffID() + " - " + staff.getFirstName() + " " + staff.getLastName());
                        break;
                    }
                }
            }
        }
        return teachers;
    }
}
