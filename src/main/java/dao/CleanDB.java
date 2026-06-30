package dao;

import java.sql.Connection;
import util.database.DBContext;

public class CleanDB {
    public static void main(String[] args) {
        try {
            Connection conn = DBContext.getConnection();
            if (conn != null) {
                System.out.println("Connected!");
                conn.createStatement().executeUpdate("DELETE FROM inventory_ticket_detail");
                System.out.println("Deleted details.");
                conn.createStatement().executeUpdate("DELETE FROM inventory_ticket");
                System.out.println("Deleted tickets.");
            } else {
                System.out.println("Failed!");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
