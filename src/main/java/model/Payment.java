package model;

/**
 * Model đại diện cho Thông tin thanh toán (Payment) trong hệ thống Finora.
 * Khớp hoàn toàn với sơ đồ ERD.
 *
 * @author Finora Team
 */
public class Payment {

    private int paymentId;
    private int orderId;            // FK -> Order.orderId
    private String paymentMethod;   // "CASH", "TRANSFER", "MOMO", v.v.
    private double paymentAmount;
    private String paymentDate;     // yyyy-MM-dd HH:mm:ss
    private String paymentStatus;   // "PENDING", "SUCCESS", "FAILED"
    private String transactionCode; // Mã giao dịch ngân hàng / ví điện tử

    // ── Constructors ─────────────────────────────────────────

    public Payment() {}

    public Payment(int paymentId, int orderId, String paymentMethod, double paymentAmount, String paymentDate, String paymentStatus, String transactionCode) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.paymentStatus = paymentStatus;
        this.transactionCode = transactionCode;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    @Override
    public String toString() {
        return "Payment{paymentId=" + paymentId + ", orderId=" + orderId + ", paymentAmount=" + paymentAmount + ", paymentStatus='" + paymentStatus + "'}";
    }
}
