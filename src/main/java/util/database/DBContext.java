package util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {

    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";


    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
            + "databaseName=DBFinoraV2;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";
 
    private static final String USERNAME = "sa";
 
    private static final String PASSWORD = "123456";

    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "Không tìm thấy SQL Server JDBC Driver. "
                    + "Hãy thêm mssql-jdbc-*.jar vào thư viện dự án. "
                    + "Chi tiết: " + e.getMessage()
            );
        }
    }
    // Ngăn không cho khởi tạo instance của class tiện ích này
    private DBContext() {
    }
}
