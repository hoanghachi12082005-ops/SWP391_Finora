package dao.customer;

import model.LoyaltyPointSetting;
import util.database.DBContext;
import java.sql.*;

public class LoyaltyPointSettingDAO {

    public LoyaltyPointSetting getSetting() {
        String sql = "SELECT TOP 1 setting_id, amount_per_point, point_to_currency, updated_by, updated_at FROM loyalty_point_setting ORDER BY setting_id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                LoyaltyPointSetting s = new LoyaltyPointSetting();
                s.setSettingId(rs.getInt("setting_id"));
                s.setAmountPerPoint(rs.getBigDecimal("amount_per_point"));
                s.setPointToCurrency(rs.getBigDecimal("point_to_currency"));
                int ub = rs.getInt("updated_by"); if (!rs.wasNull()) s.setUpdatedBy(ub);
                return s;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new LoyaltyPointSetting();
    }

    public boolean update(LoyaltyPointSetting s) {
        String sql = "UPDATE loyalty_point_setting SET amount_per_point = ?, point_to_currency = ?, updated_by = ?, updated_at = GETDATE() WHERE setting_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, s.getAmountPerPoint());
            ps.setBigDecimal(2, s.getPointToCurrency());
            if (s.getUpdatedBy() != null) ps.setInt(3, s.getUpdatedBy()); else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, s.getSettingId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
