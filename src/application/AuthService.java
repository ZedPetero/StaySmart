package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    
	public static User authenticate(String username, String password) {
	    String sql = "SELECT id, username, role, contact_number FROM users WHERE username = ? AND password = ?";
	    
	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, username);
	        pstmt.setString(2, password);
	        
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            int id = rs.getInt("id");
	            String userRole = rs.getString("role");
	            String contactNumber = rs.getString("contact_number");
	            return new User(id, username, userRole, contactNumber);
	        }
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    
	    return null; // Authentication failed
	}
}