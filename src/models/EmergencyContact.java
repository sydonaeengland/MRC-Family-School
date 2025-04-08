package models;

public class EmergencyContact {
  private String name;
  private String relationship;
  private String phone;
  private String address; 

    public EmergencyContact(String name, String relationship, String phone, String address) {
        this.name = name;
        this.relationship = relationship;
        this.phone = phone;
        this.address = address;
    }

    /** 
     * ============================
     *       GETTER METHODS
     * ============================
     */

    public String getName() {
        return name;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    /** 
     * ============================
     *       SETTER METHODS
     * ============================
     */

    public void setName(String name) {
        this.name = name;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String toCSV() {
      return name + "," + relationship + "," + phone + "," + address;
  }

  // Convert from CSV
  public static EmergencyContact fromCSV(String csv) {
      String[] parts = csv.split(",", -1); 
      if (parts.length < 4) return null;
      return new EmergencyContact(
          parts[0].trim(),
          parts[1].trim(),
          parts[2].trim(),
          parts[3].trim()
      );
  }

  @Override
  public String toString() {
      return "EmergencyContact{" +
              "name='" + name + '\'' +
              ", relationship='" + relationship + '\'' +
              ", phone='" + phone + '\'' +
              ", address='" + address + '\'' +
              '}';
  }
}
