package models;

public class GuardianInfo {
    private String guardianFirstName;
    private String guardianLastName;
    private String relation;
    private String phoneNumber;
    private String email;

    /** 
     * Constructor for Guardian Information 
     */
    public GuardianInfo(String guardianFirstName, String guardianLastName, String relation, String phoneNumber, String email) {
        this.guardianFirstName = guardianFirstName;
        this.guardianLastName = guardianLastName;
        this.relation = relation;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    /** 
     * ============================
     *       GETTER METHODS
     * ============================
     */

    public String getGuardianFirstName() {
        return guardianFirstName;
    }

    public String getGuardianLastName() {
        return guardianLastName;
    }

    public String getRelation() {
        return relation;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    /** 
     * ============================
     *       SETTER METHODS
     * ============================
     */

    public void setGuardianFirstName(String guardianFirstName) {
        this.guardianFirstName = guardianFirstName;
    }

    public void setGuardianLastName(String guardianLastName) {
        this.guardianLastName = guardianLastName;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /** 
     * ============================
     *     CSV FILE OPERATIONS
     * ============================
     */

    /** 
     * Converts Guardian object to CSV format 
     */
    public String toCSV() {
        return guardianFirstName + "," + guardianLastName + "," + relation + "," + phoneNumber + "," + email;
    }

    /** 
     * Converts CSV line back into a GuardianInfo object 
     */
    public static GuardianInfo fromCSV(String csv) {
        String[] parts = csv.split(",");
        if (parts.length < 5) {
            System.err.println("Invalid guardian CSV format: " + csv);
            return null;
        }
        return new GuardianInfo(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim());
    }

    /** 
     * Compares two GuardianInfo objects based on name and relation 
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GuardianInfo other = (GuardianInfo) obj;
        return guardianFirstName.equals(other.guardianFirstName) 
            && guardianLastName.equals(other.guardianLastName) 
            && relation.equals(other.relation);
    }
}
