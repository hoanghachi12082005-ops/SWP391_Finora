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
                conn.createStatement().executeUpdate("DELETE FROM stock_transaction WHERE reference_type = 'TRANSFER'");
                System.out.println("Deleted transfer stock transactions.");
                // We won't reset inventory quantity to 0 because there might be non-transfer transactions. 
                // But if they want a clean slate for transfer testing, this should be enough to start a new transfer.
            } else {
                System.out.println("Failed!");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
