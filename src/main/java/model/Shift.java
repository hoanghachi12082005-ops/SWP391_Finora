package model;

import java.math.BigDecimal;

public class Shift {
    private int shiftId;
    private int empId;
    private int branchId;
    private BigDecimal openingCash;
    private BigDecimal closingCash;
    private BigDecimal expectedCash;
    private String status; // 'OPEN' or 'CLOSED'
    private String openedAt;
    private String closedAt;

    // Transient fields
    private String employeeName;
    private String branchName;

    public Shift() {}

    public Shift(int shiftId, int empId, int branchId, BigDecimal openingCash, BigDecimal closingCash,
                 BigDecimal expectedCash, String status, String openedAt, String closedAt) {
        this.shiftId = shiftId;
        this.empId = empId;
        this.branchId = branchId;
        this.openingCash = openingCash;
        this.closingCash = closingCash;
        this.expectedCash = expectedCash;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }

    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public BigDecimal getOpeningCash() {
        return openingCash;
    }

    public void setOpeningCash(BigDecimal openingCash) {
        this.openingCash = openingCash;
    }

    public BigDecimal getClosingCash() {
        return closingCash;
    }

    public void setClosingCash(BigDecimal closingCash) {
        this.closingCash = closingCash;
    }

    public BigDecimal getExpectedCash() {
        return expectedCash;
    }

    public void setExpectedCash(BigDecimal expectedCash) {
        this.expectedCash = expectedCash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(String openedAt) {
        this.openedAt = openedAt;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
