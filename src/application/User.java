package application;

public class User {
    private int id;
    private String username;
    private String fullname;      // Added
    private String role;
    private String contactNumber;
    private String email;         // Added

    // Constructor used by AuthService
    public User(int id, String username, String fullname, String role, String contactNumber, String email) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.role = role;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    // Keep your old constructor just in case other parts of the app use it (optional)
    public User(int id, String username, String role, String contactNumber) {
        this(id, username, null, role, contactNumber, null);
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // Smart Getter for Fullname: Returns username if fullname is empty
    public String getFullname() {
        return (fullname == null || fullname.isEmpty()) ? username : fullname;
    }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}