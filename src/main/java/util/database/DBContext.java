package util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Connection Utility - Microsoft SQL Server.
 */
public class DBContext {

    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String DEFAULT_URL
        = "jdbc:sqlserver://160.191.242.124:1433;"
        + "databaseName=DBFinoraV3;"
        + "encrypt=false;trustServerCertificate=true;";

    private static final String JDBC_URL = envOr("DB_URL", DEFAULT_URL);
    private static final String DB_USER = envOr("DB_USER", "sa");
    private static final String DB_SECRET = envOr("DB_PASSWORD", "a12345A@");

    private static String url = JDBC_URL;
    private static String user = DB_USER;
    private static String password = DB_SECRET;

    private static final ThreadLocal<Integer> currentEmployeeId = new ThreadLocal<>();

    public static void setCurrentEmployeeId(Integer empId) {
        currentEmployeeId.set(empId);
    }

    public static Integer getCurrentEmployeeId() {
        return currentEmployeeId.get();
    }

    public static void clearCurrentEmployeeId() {
        currentEmployeeId.remove();
    }

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Khong tim thay driver SQL Server!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, user, password);
        try {
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        } catch (SQLException ignored) {}
        
        // Set session context cho trigger audit log
        Integer empId = currentEmployeeId.get();
        if (empId != null) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("EXEC sp_set_session_context N'EmployeeID', " + empId);
            } catch (SQLException e) {
                // Khong anh huong den luong chinh, chi canh bao
                System.err.println("WARN: Khong the set session context: " + e.getMessage());
            }
        }
        return conn;
    }

    public static void setConnection(String dbUrl, String dbUser, String dbPassword) {
        url = dbUrl;
        user = dbUser;
        password = dbPassword;
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void main(String[] args) {
        if (testConnection()) {
            System.out.println("Connection to SQL Server successful.");
        } else {
            System.err.println("Kết nối tới SQL Server thất bại. Vui lòng kiểm tra lại tài khoản/mật khẩu hoặc cổng 1433!");
        }
    }

    private static String envOr(String key, String fallback) {
        String val = System.getenv(key);
        return val != null ? val : fallback;
    }
}
