package dao.supplier;

import util.database.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplierProductDAO {

    /**
     * Lấy toàn bộ bản đồ quan hệ: SupplierID -> { ProductID: ImportPrice }
     */
    public Map<Integer, Map<Integer, Double>> getSupplierProductMap() {
        Map<Integer, Map<Integer, Double>> map = new HashMap<>();
        String sql = "SELECT SupplierID, ProductID, ImportPrice FROM SupplierProduct";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int supplierId = rs.getInt("SupplierID");
                int productId = rs.getInt("ProductID");
                double importPrice = rs.getDouble("ImportPrice");

                map.computeIfAbsent(supplierId, k -> new HashMap<>()).put(productId, importPrice);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * Lấy danh sách ID sản phẩm được cung cấp bởi 1 nhà cung cấp
     */
    public List<Integer> getLinkedProductIds(int supplierId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT ProductID FROM SupplierProduct WHERE SupplierID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("ProductID"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy bản đồ sản phẩm liên kết kèm đơn giá nhập của 1 nhà cung cấp cụ thể
     */
    public Map<Integer, Double> getLinkedProductsWithPrices(int supplierId) {
        Map<Integer, Double> map = new HashMap<>();
        String sql = "SELECT ProductID, ImportPrice FROM SupplierProduct WHERE SupplierID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("ProductID"), rs.getDouble("ImportPrice"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * Ghi nhận các liên kết sản phẩm và giá đàm phán của nhà cung cấp (Sử dụng Transaction)
     */
    public boolean saveAssociations(int supplierId, List<Integer> productIds, List<Double> prices) {
        String deleteSql = "DELETE FROM SupplierProduct WHERE SupplierID = ?";
        String insertSql = "INSERT INTO SupplierProduct (SupplierID, ProductID, ImportPrice) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Xóa các liên kết cũ
            try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                deletePs.setInt(1, supplierId);
                deletePs.executeUpdate();
            }

            // 2. Thêm các liên kết mới
            if (productIds != null && !productIds.isEmpty()) {
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    for (int i = 0; i < productIds.size(); i++) {
                        insertPs.setInt(1, supplierId);
                        insertPs.setInt(2, productIds.get(i));
                        insertPs.setDouble(3, prices.get(i));
                        insertPs.addBatch();
                    }
                    insertPs.executeBatch();
                }
            }

            conn.commit(); // Hoàn thành transaction
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback nếu gặp lỗi
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return false;
    }
}
