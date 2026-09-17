package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import org.sqlite.SQLiteConfig;

public class DatabaseHandler {

    // ====== ( Database File ) =========
    // The whole database lives in one SQLite file, so the app works offline with no server.
    // Default: staysmart.db in the working directory. Override with -Dstaysmart.db=<path>.
    private static final File DB_FILE = new File(System.getProperty("staysmart.db", "staysmart.db")).getAbsoluteFile();
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE.getPath();

    private static final SQLiteConfig CONFIG = new SQLiteConfig();
    static {
        CONFIG.enforceForeignKeys(true);            // ON DELETE CASCADE only works with this on
        CONFIG.setBusyTimeout(5000);                // wait instead of failing if another connection holds a lock
        CONFIG.setDateStringFormat("yyyy-MM-dd HH:mm:ss"); // matches datetime('now','localtime') for getTimestamp()
    }

    private static boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        ensureInitialized();
        return DriverManager.getConnection(DB_URL, CONFIG.toProperties());
    }

    public static File getDatabaseFile() {
        return DB_FILE;
    }

    /** Creates the schema (and loads the sample data) the first time the database file is used. */
    private static synchronized void ensureInitialized() throws SQLException {
        if (initialized) return;
        try (Connection conn = DriverManager.getConnection(DB_URL, CONFIG.toProperties());
                Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='users'");
            boolean hasSchema = rs.next() && rs.getInt(1) > 0;
            if (!hasSchema) {
                runScript(conn, "/application/db/schema.sql");
            }
            rs = st.executeQuery("SELECT COUNT(*) FROM users");
            boolean empty = rs.next() && rs.getInt(1) == 0;
            if (empty) {
                runScript(conn, "/application/db/seed.sql");
            }
            migrate(conn);
        }
        initialized = true;
    }

    /** Adds columns introduced after a database file was first created. */
    private static void migrate(Connection conn) throws SQLException {
        if (!columnExists(conn, "properties", "created_at")) {
            try (Statement st = conn.createStatement()) {
                // SQLite can't add a column with a non-constant default, so existing rows stay NULL
                // (treated as "date unknown") and inserts set the value explicitly.
                st.executeUpdate("ALTER TABLE properties ADD COLUMN created_at TEXT");
            }
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) return true;
            }
        }
        return false;
    }

    private static void runScript(Connection conn, String resource) throws SQLException {
        try (InputStream in = DatabaseHandler.class.getResourceAsStream(resource)) {
            if (in == null) throw new SQLException("Missing resource " + resource);
            String script = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().filter(l -> !l.trim().startsWith("--")).collect(Collectors.joining("\n"));
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                // Check foreign keys once at commit so the script's statement order doesn't matter
                st.executeUpdate("PRAGMA defer_foreign_keys = ON");
                for (String sql : script.split(";\n")) {
                    if (!sql.trim().isEmpty()) st.executeUpdate(sql);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (java.io.IOException e) {
            throw new SQLException("Could not read " + resource, e);
        }
    }

    /** Quick check so the UI can explain an unusable database file instead of failing silently. */
    public static boolean isDatabaseReachable() {
        try (Connection conn = getConnection()) {
            return conn != null && conn.isValid(2);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static final String DB_UNREACHABLE_MESSAGE =
            "Cannot open the StaySmart database file:\n" + DB_FILE.getPath() + "\n\n" +
            "Make sure the folder is writable and the file is not locked by another program.";

    // ====== ( Application Handling ) =========

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
            boolean updated = pstmt.executeUpdate() > 0;

            // An approved booking means the room is now taken: keep the room status in sync
            // so the Tenants page, the house view and the dashboard revenue all agree.
            if (updated && "Approved".equalsIgnoreCase(status)) {
                String roomSql = "UPDATE rooms SET status = 'Occupied' WHERE id = " +
                        "(SELECT room_id FROM applications WHERE id = ? AND application_type = 'Booking')";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(roomSql)) {
                    ps.setInt(1, applicationId);
                    ps.executeUpdate();
                }
            }
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ====== ( Messaging ) =========

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

    public static java.util.List<application.model.Application> getTenantConversations(int tenantId) {
        java.util.List<application.model.Application> conversations = new java.util.ArrayList<>();
        String sql = "SELECT a.*, p.name as property_name, p.landlord_id, u.fullname as landlord_name, r.room_number " +
                "FROM applications a " +
                "JOIN properties p ON a.property_id = p.id " +
                "LEFT JOIN users u ON p.landlord_id = u.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "WHERE a.tenant_id = ? " +
                "ORDER BY a.apply_date DESC";

        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tenantId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                application.model.Application app = new application.model.Application(
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

    public static java.util.List<application.model.Application> getLandlordConversations(int landlordId) {
        java.util.List<application.model.Application> conversations = new java.util.ArrayList<>();
        String sql = "SELECT a.*, p.name as property_name, u.fullname as tenant_name, r.room_number, a.tenant_id " +
                "FROM applications a " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN users u ON a.tenant_id = u.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "WHERE p.landlord_id = ? " +
                "ORDER BY a.apply_date DESC";

        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, landlordId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                application.model.Application app = new application.model.Application(
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
    public static java.util.Map<String, Integer> getDashboardStats(int userId, String role) {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        try (Connection conn = getConnection()) {
            if ("landlord".equalsIgnoreCase(role)) {
                // Total Properties
                String sql1 = "SELECT COUNT(*) FROM properties WHERE landlord_id = ?";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql1)) {
                    ps.setInt(1, userId);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next())
                        stats.put("totalProperties", rs.getInt(1));
                }

                // Active Tenants
                String sql2 = "SELECT COUNT(DISTINCT tenant_id) FROM applications a JOIN properties p ON a.property_id = p.id WHERE p.landlord_id = ? AND a.status = 'Approved'";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql2)) {
                    ps.setInt(1, userId);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next())
                        stats.put("activeTenants", rs.getInt(1));
                }

                // Monthly Revenue
                String sql3 = "SELECT SUM(r.price) FROM rooms r JOIN properties p ON r.property_id = p.id WHERE p.landlord_id = ? AND r.status = 'Occupied'";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql3)) {
                    ps.setInt(1, userId);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next())
                        stats.put("monthlyRevenue", rs.getInt(1));
                }

                // Pending Applications
                String sql4 = "SELECT COUNT(*) FROM applications a JOIN properties p ON a.property_id = p.id WHERE p.landlord_id = ? AND a.status = 'Pending'";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql4)) {
                    ps.setInt(1, userId);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next())
                        stats.put("pendingApps", rs.getInt(1));
                }

                // Properties added this calendar month
                String sql5 = "SELECT COUNT(*) FROM properties WHERE landlord_id = ? AND created_at IS NOT NULL " +
                        "AND strftime('%Y-%m', created_at) = strftime('%Y-%m', 'now', 'localtime')";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql5)) {
                    ps.setInt(1, userId);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next())
                        stats.put("propertiesThisMonth", rs.getInt(1));
                }

                // Room occupancy and payment state
                String sql6 = "SELECT COUNT(*), " +
                        "SUM(CASE WHEN r.status = 'Occupied' THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN r.status = 'Occupied' AND r.payment_status = 'Paid' THEN 1 ELSE 0 END) " +
                        "FROM rooms r JOIN properties p ON r.property_id = p.id WHERE p.landlord_id = ?";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql6)) {
                    ps.setInt(1, userId);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        stats.put("totalRooms", rs.getInt(1));
                        stats.put("occupiedRooms", rs.getInt(2));
                        stats.put("paidRooms", rs.getInt(3));
                    }
                }

                // Pending applications that arrived today
                String sql7 = "SELECT COUNT(*) FROM applications a JOIN properties p ON a.property_id = p.id " +
                        "WHERE p.landlord_id = ? AND a.status = 'Pending' AND date(a.apply_date) = date('now', 'localtime')";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql7)) {
                    ps.setInt(1, userId);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next())
                        stats.put("pendingToday", rs.getInt(1));
                }
            } else {
                // Tenant Stats
                String sql1 = "SELECT COUNT(*) FROM applications WHERE tenant_id = ? AND status IN ('Pending', 'Approved')";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql1)) {
                    ps.setInt(1, userId);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next())
                        stats.put("activeApps", rs.getInt(1));
                }

                String sql2 = "SELECT COUNT(*) FROM applications WHERE tenant_id = ? AND application_type = 'Tour' AND status = 'Approved'";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql2)) {
                    ps.setInt(1, userId);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next())
                        stats.put("activeTours", rs.getInt(1));
                }

                String sql3 = "SELECT COUNT(DISTINCT property_id) FROM saved_properties WHERE tenant_id = ?";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql3)) {
                    ps.setInt(1, userId);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next())
                        stats.put("savedProperties", rs.getInt(1));
                }

                stats.put("viewedProperties", 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /** Latest applications and incoming messages for a landlord, newest first. */
    public static java.util.List<application.model.Activity> getLandlordRecentActivity(int landlordId, int limit) {
        java.util.List<application.model.Activity> items = new java.util.ArrayList<>();
        String sql = "SELECT kind, who, what, at FROM (" +
                "  SELECT CASE WHEN a.application_type = 'Tour' THEN 'TOUR' ELSE 'APPLICATION' END AS kind, " +
                "         u.fullname AS who, p.name || ' - Room ' || r.room_number AS what, a.apply_date AS at " +
                "  FROM applications a JOIN properties p ON a.property_id = p.id " +
                "  JOIN rooms r ON a.room_id = r.id JOIN users u ON a.tenant_id = u.id " +
                "  WHERE p.landlord_id = ? " +
                "  UNION ALL " +
                "  SELECT 'MESSAGE', u.fullname, m.message_text, m.timestamp " +
                "  FROM messages m JOIN users u ON m.sender_id = u.id WHERE m.receiver_id = ? " +
                ") ORDER BY at DESC LIMIT ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, landlordId);
            pstmt.setInt(2, landlordId);
            pstmt.setInt(3, limit);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                application.model.Activity.Kind kind = application.model.Activity.Kind.valueOf(rs.getString("kind"));
                String who = rs.getString("who") != null ? rs.getString("who") : "Someone";
                String title;
                switch (kind) {
                    case TOUR: title = "Tour request from " + who; break;
                    case MESSAGE: title = "Message from " + who; break;
                    default: title = "New application from " + who;
                }
                items.add(new application.model.Activity(kind, title, rs.getString("what"), rs.getTimestamp("at")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /** One row per room for the dashboard's "Property Status" panel; configured/occupied rooms first. */
    public static java.util.List<java.util.Map<String, Object>> getLandlordRoomStatus(int landlordId, int limit) {
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        String sql = "SELECT p.name AS property_name, r.room_number, r.price, r.status, " +
                "  (SELECT u.fullname FROM applications a JOIN users u ON u.id = a.tenant_id " +
                "   WHERE a.room_id = r.id AND a.status = 'Approved' AND a.application_type = 'Booking' " +
                "   ORDER BY a.apply_date DESC LIMIT 1) AS tenant_name " +
                "FROM rooms r JOIN properties p ON p.id = r.property_id " +
                "WHERE p.landlord_id = ? " +
                "ORDER BY CASE r.status WHEN 'Occupied' THEN 0 WHEN 'Maintenance' THEN 1 ELSE 2 END, " +
                "         (r.price > 0) DESC, r.price DESC, p.name, CAST(r.room_number AS INTEGER) " +
                "LIMIT ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, landlordId);
            pstmt.setInt(2, limit);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                row.put("propertyName", rs.getString("property_name"));
                row.put("roomNumber", rs.getString("room_number"));
                row.put("price", rs.getDouble("price"));
                row.put("status", rs.getString("status") != null ? rs.getString("status") : "Available");
                row.put("tenantName", rs.getString("tenant_name"));
                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public static java.util.List<application.model.Application> getUpcomingTours(int tenantId) {
        java.util.List<application.model.Application> tours = new java.util.ArrayList<>();
        String sql = "SELECT a.*, p.name as property_name, r.room_number " +
                "FROM applications a " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "WHERE a.tenant_id = ? AND a.application_type = 'Tour' AND a.status = 'Approved' " +
                "ORDER BY a.apply_date ASC";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tenantId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                application.model.Application app = new application.model.Application(
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
                app.setPropertyName(rs.getString("property_name"));
                app.setRoomNumber(rs.getString("room_number"));
                tours.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tours;
    }

    public static java.util.List<application.model.Application> getAllTenants(int landlordId) {
        java.util.List<application.model.Application> tenants = new java.util.ArrayList<>();
        String sql = "SELECT a.*, u.fullname as tenant_name, u.email, u.contact_number as tenant_contact, p.name as property_name, r.room_number, r.payment_status "
                +
                "FROM applications a " +
                "JOIN users u ON a.tenant_id = u.id " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "WHERE p.landlord_id = ? AND a.status = 'Approved'";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, landlordId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                application.model.Application app = new application.model.Application(
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
                app.setTenantName(rs.getString("tenant_name"));
                app.setTenantEmail(rs.getString("email"));
                app.setTenantContact(rs.getString("tenant_contact"));
                app.setPropertyName(rs.getString("property_name"));
                app.setRoomNumber(rs.getString("room_number"));
                app.setRoomPaymentStatus(rs.getString("payment_status"));
                tenants.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tenants;
    }
}