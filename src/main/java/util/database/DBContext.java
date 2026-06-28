package util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Utility - Microsoft SQL Server. Cấu hình đồng bộ hóa chạy
 * hệ thống FinoraRetail trên SQL Server.
 */
public class DBContext {
    

    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String DEFAULT_URL
            = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=DBFinoraV2;" 
            + "encrypt=true;"
            + "trustServerCertificate=true;"
            + "characterEncoding=UTF-8";

    private static final String JDBC_URL = envOr("DB_URL", DEFAULT_URL);
    private static final String DB_USER = envOr("DB_USER", "sa"); // Tài khoản SQL Server Authentication
    private static final String DB_SECRET = envOr("DB_PASSWORD", "123"); // Mật khẩu của tài khoản sa

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Microsoft SQL Server JDBC Driver không tìm thấy trong hệ thống!", e);
        }
    }

    private static String envOr(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    /**
     * Tạo kết nối mới tới SQL Server mỗi lần gọi
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_SECRET);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
            System.out.println("Chúc mừng! Kết nối tới cơ sở dữ liệu SQL Server thành công.");
        } else {
            System.err.println("Kết nối tới SQL Server thất bại. Vui lòng kiểm tra lại tài khoản/mật khẩu hoặc cổng 1433!");
        }
    }
}
