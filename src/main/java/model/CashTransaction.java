package model;

import java.math.BigDecimal;

public class CashTransaction {
    private int cashTransactionId;
    private int shiftId;
    private String type; // 'WITHDRAW' or 'DEPOSIT'
    private BigDecimal amount;
    private String note;
    private String createdAt;

    public CashTransaction() {}

    public CashTransaction(int cashTransactionId, int shiftId, String type, BigDecimal amount, String note, String createdAt) {
        this.cashTransactionId = cashTransactionId;
        this.shiftId = shiftId;
        this.type = type;
        this.amount = amount;
        this.note = note;
        this.createdAt = createdAt;
    }

    public int getCashTransactionId() {
        return cashTransactionId;
    }

    public void setCashTransactionId(int cashTransactionId) {
        this.cashTransactionId = cashTransactionId;
    }

    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
