package util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {
    private static final String SERVER = "localhost";
    private static final String PORT = "1433";
    private static final String DATABASE = "StoreManagementDB";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "12345";

    private static final String URL = "jdbc:sqlserver://" + SERVER + ":" + PORT
            + ";databaseName=" + DATABASE
            + ";encrypt=true;trustServerCertificate=true";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQL Server JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
