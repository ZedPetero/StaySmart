package application;

public class User {
    private int id;
    private String username;
    private String role;
    private String contactNumber;
    
    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
    
    public User(int id, String username, String role, String contactNumber) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.contactNumber = contactNumber;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}