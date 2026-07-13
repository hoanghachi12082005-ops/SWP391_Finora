package dao.system;

import model.VatSetting;
import util.database.DBContext;
import java.sql.*;

public class VatSettingDAO {

    public VatSetting getSetting() {
        String sql = "SELECT TOP 1 setting_id, vat_percentage, updated_by, updated_at FROM vat_setting ORDER BY setting_id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                VatSetting s = new VatSetting();
                s.setSettingId(rs.getInt("setting_id"));
                s.setVatPercentage(rs.getDouble("vat_percentage"));
                int ub = rs.getInt("updated_by");
                if (!rs.wasNull()) s.setUpdatedBy(ub);
                return s;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new VatSetting(); // trả về mặc định 8%
    }

    /** Lấy nhanh vatPercentage, trả về mặc định 8 nếu lỗi */
    public static double getVatPercentage() {
        String sql = "SELECT TOP 1 vat_percentage FROM vat_setting ORDER BY setting_id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("vat_percentage");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 8;
    }

    /** Lấy nhanh vatRate (hệ số), trả về 0.08 nếu lỗi */
    public static double getVatRate() {
        return getVatPercentage() / 100.0;
    }

    public boolean update(VatSetting s) {
        String sql = "UPDATE vat_setting SET vat_percentage = ?, updated_by = ?, updated_at = GETDATE() WHERE setting_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, s.getVatPercentage());
            if (s.getUpdatedBy() != null) {
                ps.setInt(2, s.getUpdatedBy());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, s.getSettingId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
