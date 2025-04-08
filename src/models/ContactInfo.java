package models;

public class ContactInfo {
    private String phoneNumber;
    private String email;
    private String address;

    public ContactInfo(String phoneNumber, String email, String address) {
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }

    /** 
     * ============================
     *       GETTER METHODS
     * ============================
     */

    public String getPhoneNumber() {
        return phoneNumber; 
    }

    public String getEmail() { 
        return email; 
    }

    public String getAddress() { 
        return address; 
    }

    /** 
     * ============================
     *       SETTER METHODS
     * ============================
     */

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber; 
    }

    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public void setAddress(String address) { 
        this.address = address; 
    }

    // Convert to CSV
    public String toCSV() {
        return phoneNumber + "," + email + "," + address;
    }

    // Convert from CSV
    public static ContactInfo fromCSV(String csv) {
        String[] parts = csv.split(",");
        if (parts.length < 3) return null;
        return new ContactInfo(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }
}

