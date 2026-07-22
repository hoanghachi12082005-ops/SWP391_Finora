package dao.sales;

import dao.customer.LoyaltyPointSettingDAO;
import model.LoyaltyPointSetting;
import util.database.DBContext;
import java.sql.*;

/**
 * DAO thao tác bảng customer_point + point_transaction — quản lý điểm tích lũy.
 */
public class CustomerPointDAO {

    // ponytail: earn rate from loyalty_point_setting.amount_per_point
    public static int getEarnRate() {
        LoyaltyPointSetting setting = new LoyaltyPointSettingDAO().getSetting();
        return setting != null && setting.getAmountPerPoint().intValue() > 0
                ? setting.getAmountPerPoint().intValue() : 100_000;
    }

    // ponytail: redeem rate from loyalty_point_setting.point_to_currency (VND per point)
    public static int getRedeemRate() {
        LoyaltyPointSetting setting = new LoyaltyPointSettingDAO().getSetting();
        return setting != null && setting.getPointToCurrency().intValue() > 0
                ? setting.getPointToCurrency().intValue() : 0;
    }

    /**
     * Lấy cus_point_id theo cus_id. Trả về -1 nếu không tồn tại.
     */
    public int getCusPointId(int cusId) {
        String sql = "SELECT cus_point_id FROM customer_point WHERE cus_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cusId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cus_point_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Lấy current_points theo cus_id. Trả về 0 nếu chưa có.
     */
    public int getCurrentPoints(int cusId) {
        String sql = "SELECT current_points FROM customer_point WHERE cus_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cusId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("current_points");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Trừ điểm khi khách hàng đổi điểm — gọi trong cùng transaction với checkout.
     * Ghi log point_transaction với description "Đổi điểm POS".
     */
    public void deductPoints(Connection conn, int cusId, int pointsToDeduct, int orderId) throws SQLException {
        if (pointsToDeduct <= 0) return;

        int cusPointId = -1;
        int beforePoints = 0;
        String selectSql = "SELECT cus_point_id, current_points FROM customer_point WHERE cus_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, cusId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cusPointId = rs.getInt("cus_point_id");
                    beforePoints = rs.getInt("current_points");
                }
            }
        }
        if (cusPointId == -1) return;

        int afterPoints = Math.max(0, beforePoints - pointsToDeduct);
        String updateSql = "UPDATE customer_point SET current_points = ?, updated_at = GETDATE() WHERE cus_point_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, afterPoints);
            ps.setInt(2, cusPointId);
            ps.executeUpdate();
        }

        String logSql = "INSERT INTO point_transaction (cus_point_id, order_id, before_points, after_points, description, created_at) VALUES (?, ?, ?, ?, N'Đổi điểm POS', GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(logSql)) {
            ps.setInt(1, cusPointId);
            ps.setInt(2, orderId);
            ps.setInt(3, beforePoints);
            ps.setInt(4, afterPoints);
            ps.executeUpdate();
        }
    }

    /**
     * Cộng điểm cho khách hàng sau khi thanh toán.
     * Công thức: 100.000 VND = 1 điểm.
     * Thực hiện trong cùng Connection/Transaction.
     *
     * @param conn       Connection đang mở (autoCommit=false)
     * @param cusId      ID khách hàng
     * @param orderTotal Tổng tiền đơn hàng (VND)
     * @param orderId    ID đơn hàng vừa tạo
     */
    public void addPoints(Connection conn, int cusId, double orderTotal, int orderId) throws SQLException {
        int rate = getEarnRate();
        int pointsEarned = (int) (orderTotal / rate);
        if (pointsEarned <= 0) return;

        // 1. Lấy thông tin điểm hiện tại
        int cusPointId = -1;
        int beforePoints = 0;
        String selectSql = "SELECT cus_point_id, current_points FROM customer_point WHERE cus_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, cusId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cusPointId = rs.getInt("cus_point_id");
                    beforePoints = rs.getInt("current_points");
                }
            }
        }

        if (cusPointId == -1) {
            String insertSql = "INSERT INTO customer_point (cus_id, current_points, updated_at) "
                             + "VALUES (?, ?, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, cusId);
                ps.setInt(2, pointsEarned);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) cusPointId = keys.getInt(1);
                }
            }
            beforePoints = 0;
        } else {
            String updateSql = "UPDATE customer_point SET current_points = current_points + ?, updated_at = GETDATE() "
                             + "WHERE cus_point_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, pointsEarned);
                ps.setInt(2, cusPointId);
                ps.executeUpdate();
            }
        }

        // 2. Ghi log giao dịch điểm
        String logSql = "INSERT INTO point_transaction "
                       + "(cus_point_id, order_id, before_points, after_points, description, created_at) "
                       + "VALUES (?, ?, ?, ?, N'Tích điểm đơn hàng POS', GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(logSql)) {
            ps.setInt(1, cusPointId);
            ps.setInt(2, orderId);
            ps.setInt(3, beforePoints);
            ps.setInt(4, beforePoints + pointsEarned);
            ps.executeUpdate();
        }
    }
}
