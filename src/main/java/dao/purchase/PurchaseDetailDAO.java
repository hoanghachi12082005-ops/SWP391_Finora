package dao.purchase;

import model.PurchaseDetail;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDetailDAO {

    public List<PurchaseDetail> findByOrderId(int orderId) {
        List<PurchaseDetail> list = new ArrayList<>();
        String sql = "SELECT od.order_detail_id, od.order_id, od.product_id, od.quantity, od.unit_price, od.total_price, p.product_name "
                   + "FROM order_detail od "
                   + "LEFT JOIN [product] p ON od.product_id = p.product_id "
                   + "WHERE od.order_id = ? ORDER BY od.order_detail_id";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseDetail d = new PurchaseDetail();
                    d.setOrderDetailId(rs.getInt("order_detail_id"));
                    d.setOrderId(rs.getInt("order_id"));
                    d.setProductId(rs.getInt("product_id"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setUnitPrice(rs.getBigDecimal("unit_price"));
                    d.setTotalPrice(rs.getBigDecimal("total_price"));
                    d.setProductName(rs.getString("product_name"));
                    list.add(d);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
