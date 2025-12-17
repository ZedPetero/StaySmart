package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    // Changed parameter 'username' to 'email'
    public static User authenticate(String email, String password) {
        // SQL: Checks 'email' instead of 'username'
        String sql = "SELECT id, username, fullname, role, contact_number, email FROM users WHERE email = ? AND password = ?";

        // Connects using DatabaseHandler (the unified connection class)
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String fullname = rs.getString("fullname"); // Fetch Full Name
                String role = rs.getString("role");
                String contact = rs.getString("contact_number");
                String dbEmail = rs.getString("email");

                // Return updated User object
                return new User(id, username, fullname, role, contact, dbEmail);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Authentication failed
    }
}