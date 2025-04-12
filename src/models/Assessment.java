package models;

public class Assessment {
    private String id;
    private String name;
    private String type; 
    private String dateString; 
    private String courseID;

    public Assessment(String id, String name, String type, String dateString, String courseID) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.dateString = dateString;
        this.courseID = courseID;
    }
    

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDateString() {
        return dateString;
    }

    public String getCourseID() {
        return courseID;
    }


    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDate(String dateString) {
        this.dateString = dateString;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }
}
