package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import jakarta.servlet.ServletContext;

/** Central JDBC helper for DBFinora. TODO: Move credentials to environment/JNDI before production. */
public final class DatabaseUtil {
    private static final String DEFAULT_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String DEFAULT_URL = "jdbc:sqlserver://localhost:1433;databaseName=DBFinoraV2;encrypt=true;trustServerCertificate=true";
    private static final String DEFAULT_USERNAME = "sa";
    private static final String DEFAULT_PASSWORD = "1234";
    private static ServletContext servletContext;
    private DatabaseUtil() {}
    public static void configure(ServletContext context) { servletContext = context; }
    public static Connection getConnection() throws SQLException {
        String driver = getInitParameter("db.driver", DEFAULT_DRIVER);
        String url = getInitParameter("db.url", DEFAULT_URL);
        String username = getInitParameter("db.username", DEFAULT_USERNAME);
        String password = getInitParameter("db.password", DEFAULT_PASSWORD);
        try { Class.forName(driver); } catch (ClassNotFoundException ex) { throw new SQLException("Database driver not found: " + driver, ex); }
        return DriverManager.getConnection(url, username, password);
    }
    private static String getInitParameter(String name, String defaultValue) {
        if (servletContext == null) return defaultValue;
        String value = servletContext.getInitParameter(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * Chạy trực tiếp để kiểm tra kết nối database.
     *
     * Cách chạy (trong cmd, từ thư mục gốc project):
     *   javac -cp "C:/Tomcat 10.1_Tomcat/lib/servlet-api.jar;lib/mssql-jdbc-12.8.1.jre11.jar" -d build src/main/java/util/DatabaseUtil.java
     *   java -cp "build;lib/mssql-jdbc-12.8.1.jre11.jar" util.DatabaseUtil
     */
    public static void main(String[] args) {
        System.out.println("========== KIỂM TRA KẾT NỐI DATABASE ==========");
        System.out.println();

        System.out.print("Kết nối database... ");
        try (Connection connection = DriverManager.getConnection(DEFAULT_URL, DEFAULT_USERNAME, DEFAULT_PASSWORD)) {
            System.out.println("THÀNH CÔNG");
            System.out.println();
            System.out.println("  Database: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("  Version:  " + connection.getMetaData().getDatabaseProductVersion());
            System.out.println("  URL:      " + connection.getMetaData().getURL());
            System.out.println();
            System.out.println("✓ Kết nối database hoạt động bình thường!");
        } catch (SQLException ex) {
            System.out.println("THẤT BẠI");
            System.out.println("    → Lỗi: " + ex.getMessage());
            System.out.println();
            // Hiển thị gợi ý dựa trên mã lỗi SQL Server
            int errorCode = ex.getErrorCode();
            if (errorCode == 4060) {
                System.out.println("    → Database 'DBFinora' không tồn tại.");
            } else if (errorCode == 18456) {
                System.out.println("    → Sai username hoặc password.");
            } else if (errorCode == 18452) {
                System.out.println("    → Không thể xác thực. Kiểm tra chế độ đăng nhập SQL Server.");
            } else if (ex.getMessage().contains("Connection refused")) {
                System.out.println("    → SQL Server chưa chạy hoặc sai cổng (1433).");
            } else if (ex.getMessage().contains("Connection timed out")) {
                System.out.println("    → Không tìm thấy SQL Server. Kiểm tra tên máy/cổng.");
            } else {
                System.out.println("    → Kiểm tra: SQL Server đã chạy? Tên máy đúng? Cổng 1433?");
            }
        }

        System.out.println();
        System.out.println("========== KẾT THÚC ==========");
    }
}
