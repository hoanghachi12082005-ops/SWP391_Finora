package model;

/**
 * Model đại diện cho Voucher giảm giá (Voucher) trong hệ thống Finora.
 * Khớp hoàn toàn với sơ đồ ERD.
 *
 * @author Finora Team
 */
public class Voucher {

    private int voucherId;
    private String voucherCode;
    private String voucherName;
    private String discountType;   // "PERCENTAGE" / "FIXED_AMOUNT"
    private double discountValue;
    private int usedQuantity;
    private String startDate;       // yyyy-MM-dd HH:mm:ss
    private String endDate;         // yyyy-MM-dd HH:mm:ss
    private String status;          // "ACTIVE" / "EXPIRED" / "DISABLED"
    private String createdAt;       // yyyy-MM-dd HH:mm:ss

    // ── Constructors ─────────────────────────────────────────

    public Voucher() {}

    public Voucher(int voucherId, String voucherCode, String voucherName, String discountType, double discountValue, int usedQuantity, String startDate, String endDate, String status, String createdAt) {
        this.voucherId = voucherId;
        this.voucherCode = voucherCode;
        this.voucherName = voucherName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.usedQuantity = usedQuantity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(int voucherId) {
        this.voucherId = voucherId;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getVoucherName() {
        return voucherName;
    }

    public void setVoucherName(String voucherName) {
        this.voucherName = voucherName;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public int getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(int usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Voucher{voucherId=" + voucherId + ", voucherCode='" + voucherCode + "', discountValue=" + discountValue + ", status='" + status + "'}";
    }
}
