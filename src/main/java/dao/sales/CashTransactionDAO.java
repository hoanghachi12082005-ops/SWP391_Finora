package dao.sales;

import model.CashTransaction;
import util.database.DBContext;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CashTransactionDAO {

    public boolean insert(int shiftId, String type, BigDecimal amount, String note) {
        String sql = """
            INSERT INTO cash_transaction (shift_id, type, amount, note, created_at)
            VALUES (?, ?, ?, ?, GETDATE())
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            ps.setString(4, note);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CashTransaction> getByShiftId(int shiftId) {
        List<CashTransaction> list = new ArrayList<>();
        String sql = """
            SELECT * FROM cash_transaction
            WHERE shift_id = ?
            ORDER BY cash_transaction_id ASC
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CashTransaction tx = new CashTransaction();
                    tx.setCashTransactionId(rs.getInt("cash_transaction_id"));
                    tx.setShiftId(rs.getInt("shift_id"));
                    tx.setType(rs.getString("type"));
                    tx.setAmount(rs.getBigDecimal("amount"));
                    tx.setNote(rs.getString("note"));
                    
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        tx.setCreatedAt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts));
                    }
                    list.add(tx);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
