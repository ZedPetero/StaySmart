package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {

    // UPDATE THESE WITH YOUR DB CREDENTIALS
    private static final String DB_URL = "jdbc:mysql://localhost:3306/staysmart_db";
    private static final String USER = "root";
    private static final String PASS = ""; // Put your real password here

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // --- APPLICATION HANDLING ---

    public static boolean saveApplication(int roomId, int tenantId, int propertyId, String type, String message,
            String paymentMethod, String contactNumber) {
        String sql = "INSERT INTO applications (room_id, tenant_id, property_id, application_type, message, payment_method, contact_number, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            pstmt.setInt(2, tenantId);
            pstmt.setInt(3, propertyId);
            pstmt.setString(4, type);
            pstmt.setString(5, message);
            pstmt.setString(6, paymentMethod);
            pstmt.setString(7, contactNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateApplicationStatus(int applicationId, String status) {
        String sql = "UPDATE applications SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, applicationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- MESSAGING ---

    public static boolean saveMessage(int applicationId, int senderId, int receiverId, String messageText) {
        String sql = "INSERT INTO messages (application_id, sender_id, receiver_id, message_text) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, applicationId);
            pstmt.setInt(2, senderId);
            pstmt.setInt(3, receiverId);
            pstmt.setString(4, messageText);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static java.util.List<application.model.Message> getMessages(int applicationId) {
        java.util.List<application.model.Message> messages = new java.util.ArrayList<>();
        String sql = "SELECT * FROM messages WHERE application_id = ? ORDER BY timestamp ASC";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, applicationId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new application.model.Message(
                        rs.getInt("id"),
                        rs.getInt("application_id"),
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        rs.getString("message_text"),
                        rs.getTimestamp("timestamp")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}