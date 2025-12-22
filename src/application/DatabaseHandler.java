package application;

import application.model.Application;
import application.model.Message;
import java.sql.*;
import java.util.*;

public class DatabaseHandler {

    // ====== ( Database Credentials ) =========
    private static final String DB_URL = "jdbc:mysql://localhost:3306/staysmart_db";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // ====== ( Application Handling ) =========

    public static boolean saveApplication(int roomId, int tenantId, int propertyId, String type, String message,
                                          String paymentMethod, String contactNumber) {
        String sql = "INSERT INTO applications (room_id, tenant_id, property_id, application_type, message, payment_method, contact_number, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, applicationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ====== ( Messaging ) =========

    public static boolean saveMessage(int applicationId, int senderId, int receiverId, String messageText) {
        String sql = "INSERT INTO messages (application_id, sender_id, receiver_id, message_text) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

    public static List<Message> getMessages(int applicationId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE application_id = ? ORDER BY timestamp ASC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, applicationId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
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

    // ====== ( Conversation Handling ) =========

    public static List<Application> getTenantConversations(int tenantId) {
        List<Application> conversations = new ArrayList<>();
        String sql = "SELECT a.*, p.name as property_name, p.landlord_id, u.fullname as landlord_name, r.room_number " +
                "FROM applications a " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN users u ON p.landlord_id = u.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "WHERE a.tenant_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tenantId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Application app = extractApplicationFromResultSet(rs);
                app.setPropertyName(rs.getString("property_name"));
                app.setLandlordId(rs.getInt("landlord_id"));
                app.setLandlordName(rs.getString("landlord_name"));
                app.setRoomNumber(rs.getString("room_number"));
                conversations.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    public static List<Application> getLandlordConversations(int landlordId) {
        List<Application> conversations = new ArrayList<>();
        String sql = "SELECT a.*, p.name as property_name, u.fullname as tenant_name, r.room_number, a.tenant_id " +
                "FROM applications a " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN users u ON a.tenant_id = u.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "WHERE p.landlord_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, landlordId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Application app = extractApplicationFromResultSet(rs);
                app.setPropertyName(rs.getString("property_name"));
                app.setTenantName(rs.getString("tenant_name"));
                app.setRoomNumber(rs.getString("room_number"));
                conversations.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    // ====== ( Dashboard Stats ) =========
    public static Map<String, Integer> getDashboardStats(int userId, String role) {
        Map<String, Integer> stats = new HashMap<>();
        try (Connection conn = getConnection()) {
            // FIX: Match 'owner' role from SQL
            if ("landlord".equalsIgnoreCase(role) || "owner".equalsIgnoreCase(role)) {
                // Total Properties
                stats.put("totalProperties", getSingleCount(conn, "SELECT COUNT(*) FROM properties WHERE landlord_id = ?", userId));
                // Active Tenants
                stats.put("activeTenants", getSingleCount(conn, "SELECT COUNT(DISTINCT tenant_id) FROM applications a JOIN properties p ON a.property_id = p.id WHERE p.landlord_id = ? AND a.status = 'Approved'", userId));
                // Monthly Revenue
                stats.put("monthlyRevenue", getSingleCount(conn, "SELECT COALESCE(SUM(r.price), 0) FROM rooms r JOIN properties p ON r.property_id = p.id WHERE p.landlord_id = ? AND r.status = 'Occupied'", userId));
                // Pending Applications
                stats.put("pendingApps", getSingleCount(conn, "SELECT COUNT(*) FROM applications a JOIN properties p ON a.property_id = p.id WHERE p.landlord_id = ? AND a.status = 'Pending'", userId));
            } else {
                // Tenant Stats
                stats.put("activeApps", getSingleCount(conn, "SELECT COUNT(*) FROM applications WHERE tenant_id = ? AND application_type = 'Booking' AND status IN ('Pending', 'Approved')", userId));
                stats.put("activeTours", getSingleCount(conn, "SELECT COUNT(*) FROM applications WHERE tenant_id = ? AND application_type = 'Tour' AND status = 'Pending'", userId));
                stats.put("savedProperties", getSingleCount(conn, "SELECT COUNT(DISTINCT property_id) FROM saved_properties WHERE tenant_id = ?", userId));
                stats.put("viewedProperties", getSingleCount(conn, "SELECT COUNT(DISTINCT property_id) FROM applications WHERE tenant_id = ?", userId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public static List<Application> getUpcomingTours(int tenantId) {
        List<Application> tours = new ArrayList<>();
        // JOIN with users (landlord) to get the "Host" name
        String sql = "SELECT a.*, p.name as property_name, r.room_number, p.location, u.fullname as host_name " +
                "FROM applications a " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "JOIN users u ON p.landlord_id = u.id " +
                "WHERE a.tenant_id = ? AND a.application_type = 'Tour' " +
                "ORDER BY a.apply_date ASC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tenantId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Application app = extractApplicationFromResultSet(rs);
                app.setPropertyName(rs.getString("property_name"));
                app.setRoomNumber(rs.getString("room_number"));
                app.setLandlordName(rs.getString("host_name")); // For the "Host" label
                tours.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tours;
    }

    public static List<Application> getAllTenants(int landlordId) {
        List<Application> tenants = new ArrayList<>();
        // FIX: Change contact_info to contact_number
        String sql = "SELECT a.*, u.fullname as tenant_name, u.email, u.contact_number, p.name as property_name, r.room_number " +
                "FROM applications a " +
                "JOIN users u ON a.tenant_id = u.id " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "WHERE p.landlord_id = ? AND a.status = 'Approved'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, landlordId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Application app = extractApplicationFromResultSet(rs);
                app.setTenantName(rs.getString("tenant_name"));
                app.setPropertyName(rs.getString("property_name"));
                app.setRoomNumber(rs.getString("room_number"));
                tenants.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tenants;
    }

    // Helper to reduce code duplication
    private static Application extractApplicationFromResultSet(ResultSet rs) throws SQLException {
        return new Application(
                rs.getInt("id"),
                rs.getInt("room_id"),
                rs.getInt("tenant_id"),
                rs.getInt("property_id"),
                rs.getString("application_type"),
                rs.getString("message"),
                rs.getString("payment_method"),
                rs.getString("contact_number"),
                rs.getString("status"),
                rs.getTimestamp("apply_date"));
    }

    private static int getSingleCount(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}