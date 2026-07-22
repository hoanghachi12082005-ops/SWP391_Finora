package model;

import java.time.LocalDateTime;

public class VatSetting {
    private int settingId;
    private double vatPercentage; // đơn vị %, vd: 8 = 8%
    private Integer categoryId;   // NULL = mặc định cho tất cả, có giá trị = áp dụng riêng cho category đó
    private Integer updatedBy;
    private LocalDateTime updatedAt;

    // Transient: tên category để hiển thị
    private String categoryName;

    public VatSetting() {
        this.vatPercentage = 8;
    }

    public int getSettingId() { return settingId; }
    public void setSettingId(int settingId) { this.settingId = settingId; }

    public double getVatPercentage() { return vatPercentage; }
    public void setVatPercentage(double vatPercentage) { this.vatPercentage = vatPercentage; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    /** Lấy VAT dưới dạng hệ số (vd: 8% => 0.08) */
    public double getVatRate() { return vatPercentage / 100.0; }

    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
