package common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import jakarta.servlet.ServletContext;

/** Central JDBC helper for DBFinora. TODO: Move credentials to environment/JNDI before production. */
public final class DatabaseUtil {
    private static final String DEFAULT_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String DEFAULT_URL = "jdbc:sqlserver://localhost:1433;databaseName=DBFinora;encrypt=true;trustServerCertificate=true";
    private static final String DEFAULT_USERNAME = "sa";
    private static final String DEFAULT_PASSWORD = "";
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
}
