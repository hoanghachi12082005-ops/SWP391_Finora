package test;

import java.sql.*;

public class TestDB {
    public static void main(String[] args) throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String url = "jdbc:sqlserver://localhost:1433;databaseName=DBFinoraV2;encrypt=true;trustServerCertificate=true;";
        Connection conn = DriverManager.getConnection(url, "sa", "123");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM inventory_ticket");
        if(rs.next()){
            System.out.println("TOTAL TICKETS: " + rs.getInt(1));
        }
        
        rs = stmt.executeQuery("SELECT TOP 5 ticket_id, ticket_code, status FROM inventory_ticket ORDER BY ticket_id DESC");
        while(rs.next()){
            System.out.println("TICKET: " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3));
        }
        conn.close();
    }
}
