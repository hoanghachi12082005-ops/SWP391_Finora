package dao.system;

import model.VatSetting;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VatSettingDAO {

    public VatSetting getSetting() {
        String sql = "SELECT TOP 1 s.setting_id, s.vat_percentage, s.category_id, s.updated_by, s.updated_at, "
                   + "c.category_name FROM vat_setting s "
                   + "LEFT JOIN category c ON s.category_id = c.category_id "
                   + "WHERE s.category_id IS NULL ORDER BY s.setting_id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return extractVatSetting(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new VatSetting(); // trả về mặc định 8%
    }

    /** Lấy VAT setting cho một category cụ thể, fallback về default (NULL category_id) nếu không có */
    public VatSetting getSettingByCategory(int categoryId) {
        String sql = "SELECT s.setting_id, s.vat_percentage, s.category_id, s.updated_by, s.updated_at, "
                   + "c.category_name FROM vat_setting s "
                   + "LEFT JOIN category c ON s.category_id = c.category_id "
                   + "WHERE s.category_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractVatSetting(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Fallback về default setting
        return getSetting();
    }

    /** Lấy tất cả VAT settings (cho trang cấu hình) */
    public List<VatSetting> getAllSettings() {
        List<VatSetting> list = new ArrayList<>();
        String sql = "SELECT s.setting_id, s.vat_percentage, s.category_id, s.updated_by, s.updated_at, "
                   + "c.category_name FROM vat_setting s "
                   + "LEFT JOIN category c ON s.category_id = c.category_id "
                   + "ORDER BY CASE WHEN s.category_id IS NULL THEN 0 ELSE 1 END, c.category_name";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractVatSetting(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy nhanh vatPercentage cho một category, fallback về default */
    public static double getVatPercentageByCategory(int categoryId) {
        String sql = "SELECT vat_percentage FROM vat_setting WHERE category_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("vat_percentage");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Fallback về default
        return getVatPercentage();
    }

    /** Lấy nhanh vatRate (hệ số) cho một category, fallback về default */
    public static double getVatRateByCategory(int categoryId) {
        return getVatPercentageByCategory(categoryId) / 100.0;
    }

    /** Lấy nhanh vatPercentage, trả về mặc định 8 nếu lỗi (dùng cho default VAT) */
    public static double getVatPercentage() {
        String sql = "SELECT TOP 1 vat_percentage FROM vat_setting WHERE category_id IS NULL ORDER BY setting_id DESC";
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

    /** Lấy nhanh vatRate (hệ số), trả về 0.08 nếu lỗi (dùng cho default VAT) */
    public static double getVatRate() {
        return getVatPercentage() / 100.0;
    }

    /** Upsert VAT setting: nếu có setting_id thì update, nếu không thì insert */
    public boolean upsert(VatSetting s) {
        if (s.getSettingId() > 0) {
            return update(s);
        } else {
            return insert(s);
        }
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

    public boolean insert(VatSetting s) {
        String sql = "INSERT INTO vat_setting (vat_percentage, category_id, updated_by, updated_at) VALUES (?, ?, ?, GETDATE())";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, s.getVatPercentage());
            if (s.getCategoryId() != null) {
                ps.setInt(2, s.getCategoryId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (s.getUpdatedBy() != null) {
                ps.setInt(3, s.getUpdatedBy());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        s.setSettingId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBySettingId(int settingId) {
        String sql = "DELETE FROM vat_setting WHERE setting_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, settingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Xóa VAT setting theo category_id */
    public boolean deleteByCategoryId(int categoryId) {
        String sql = "DELETE FROM vat_setting WHERE category_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private VatSetting extractVatSetting(ResultSet rs) throws SQLException {
        VatSetting s = new VatSetting();
        s.setSettingId(rs.getInt("setting_id"));
        s.setVatPercentage(rs.getDouble("vat_percentage"));
        int catId = rs.getInt("category_id");
        if (!rs.wasNull()) {
            s.setCategoryId(catId);
        }
        s.setCategoryName(rs.getString("category_name"));
        int ub = rs.getInt("updated_by");
        if (!rs.wasNull()) s.setUpdatedBy(ub);
        return s;
    }
}
