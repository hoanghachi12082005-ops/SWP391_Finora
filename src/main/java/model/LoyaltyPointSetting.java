package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoyaltyPointSetting {
    private int settingId;
    private BigDecimal amountPerPoint;
    private BigDecimal pointToCurrency;
    private Integer updatedBy;
    private LocalDateTime updatedAt;

    public LoyaltyPointSetting() {
        this.amountPerPoint = new BigDecimal("100000");
        this.pointToCurrency = BigDecimal.ZERO;
    }

    public int getSettingId() { return settingId; }
    public void setSettingId(int settingId) { this.settingId = settingId; }
    public BigDecimal getAmountPerPoint() { return amountPerPoint; }
    public void setAmountPerPoint(BigDecimal amountPerPoint) { this.amountPerPoint = amountPerPoint; }
    public BigDecimal getPointToCurrency() { return pointToCurrency; }
    public void setPointToCurrency(BigDecimal pointToCurrency) { this.pointToCurrency = pointToCurrency; }
    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
