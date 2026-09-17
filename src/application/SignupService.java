package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignupService {

    /** True when an account already uses this email (the users table has no unique key on it). */
    public static boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            java.sql.ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean registerUser(String fullname, String email, String password, String role, String contactNumber) {
        String username = email.contains("@") ? email.split("@")[0] : email;

        // SQL: Make sure 'role' is included here!
        String sql = "INSERT INTO users (username, fullname, email, password, role, contact_number) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, fullname);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setString(5, role); // <--- THIS SAVES 'owner' OR 'tenant'
            pstmt.setString(6, contactNumber);

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}