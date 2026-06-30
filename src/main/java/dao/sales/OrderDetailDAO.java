package dao.sales;

import model.OrderDetail;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDAO {

    public List<OrderDetail> findByOrderId(int orderId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT od.order_detail_id, od.order_id, od.product_id, od.quantity, od.unit_price, od.total_price, "
                   + "p.product_name, p.product_codebar "
                   + "FROM order_detail od "
                   + "LEFT JOIN [product] p ON od.product_id = p.product_id "
                   + "WHERE od.order_id = ? "
                   + "ORDER BY od.order_detail_id";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail d = new OrderDetail();
                    d.setOrderDetailId(rs.getInt("order_detail_id"));
                    d.setOrderId(rs.getInt("order_id"));
                    d.setProductId(rs.getInt("product_id"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setUnitPrice(rs.getBigDecimal("unit_price"));
                    d.setTotalPrice(rs.getBigDecimal("total_price"));
                    d.setProductName(rs.getString("product_name"));
                    d.setProductCodebar(rs.getString("product_codebar"));
                    list.add(d);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void insertBatch(int orderId, List<OrderDetail> details, Connection conn) throws SQLException {
        String sql = "INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderDetail d : details) {
                ps.setInt(1, orderId);
                ps.setInt(2, d.getProductId());
                ps.setInt(3, d.getQuantity());
                ps.setBigDecimal(4, d.getUnitPrice());
                ps.setBigDecimal(5, d.getTotalPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
