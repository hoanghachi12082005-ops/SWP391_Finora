package dao.finance;

import model.Invoice;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {
    // ponytail: V3 has no invoice table; reuses [order] table for invoice-like queries
    
    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT order_id, order_code, total_amount, created_at FROM [order] WHERE order_type = 'SALE' ORDER BY created_at DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Invoice inv = new Invoice();
                inv.setInvoiceId(rs.getInt("order_id"));
                inv.setInvoiceCode(rs.getString("order_code"));
                inv.setTotalAmount(rs.getBigDecimal("total_amount"));
                if (rs.getTimestamp("created_at") != null) inv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(inv);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Invoice findById(int id) {
        String sql = "SELECT order_id, order_code, total_amount, created_at FROM [order] WHERE order_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = new Invoice();
                    inv.setInvoiceId(rs.getInt("order_id"));
                    inv.setInvoiceCode(rs.getString("order_code"));
                    inv.setTotalAmount(rs.getBigDecimal("total_amount"));
                    if (rs.getTimestamp("created_at") != null) inv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return inv;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
