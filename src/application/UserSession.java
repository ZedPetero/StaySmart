package application;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private String username;
    private String role; // "Landlord" or "Tenant"

    private UserSession(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public static void getInstance(int userId, String username, String role) {
        if (instance == null) {
            instance = new UserSession(userId, username, role);
        }
    }

    public static UserSession getInstance() {
        return instance;
    }

    public int getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public static void cleanUserSession() {
        instance = null;
    }
}