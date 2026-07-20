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

    /** Upsert: update dòng đầu tiên, nếu chưa có thì INSERT. */
    public boolean upsert(LoyaltyPointSetting s) {
        LoyaltyPointSetting existing = getSetting();
        if (existing.getSettingId() > 0) {
            // Update
            String sql = "UPDATE loyalty_point_setting SET amount_per_point = ?, point_to_currency = ?, updated_by = ?, updated_at = GETDATE() WHERE setting_id = ?";
            try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, s.getAmountPerPoint());
                ps.setBigDecimal(2, s.getPointToCurrency());
                if (s.getUpdatedBy() != null) ps.setInt(3, s.getUpdatedBy()); else ps.setNull(3, Types.INTEGER);
                ps.setInt(4, existing.getSettingId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); }
            return false;
        } else {
            // Insert
            String sql = "INSERT INTO loyalty_point_setting (amount_per_point, point_to_currency, updated_by, updated_at) VALUES (?, ?, ?, GETDATE())";
            try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, s.getAmountPerPoint());
                ps.setBigDecimal(2, s.getPointToCurrency());
                if (s.getUpdatedBy() != null) ps.setInt(3, s.getUpdatedBy()); else ps.setNull(3, Types.INTEGER);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); }
            return false;
        }
    }
}
