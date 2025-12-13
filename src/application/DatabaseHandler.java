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
}