package application;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Legacy entry point kept for compatibility. All database access goes through
 * {@link DatabaseHandler} so there is a single place to configure the connection.
 */
public class DatabaseConnection {
    public static Connection getConnection() throws SQLException {
        return DatabaseHandler.getConnection();
    }
}
