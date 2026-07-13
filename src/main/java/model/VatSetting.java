package model;

import java.time.LocalDateTime;

public class VatSetting {
    private int settingId;
    private double vatPercentage; // đơn vị %, vd: 8 = 8%
    private Integer updatedBy;
    private LocalDateTime updatedAt;

    public VatSetting() {
        this.vatPercentage = 8;
    }

    public int getSettingId() { return settingId; }
    public void setSettingId(int settingId) { this.settingId = settingId; }

    public double getVatPercentage() { return vatPercentage; }
    public void setVatPercentage(double vatPercentage) { this.vatPercentage = vatPercentage; }

    /** Lấy VAT dưới dạng hệ số (vd: 8% => 0.08) */
    public double getVatRate() { return vatPercentage / 100.0; }

    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
