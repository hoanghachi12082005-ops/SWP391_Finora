package model;

import java.time.LocalDate;

public class OrderReportFilter {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Integer empId;
    private Integer branchId;
    private Integer customerId;
    private Integer orderId;
    private String orderStatus;
    private String paymentMethod;
    private String keyword;
    private String sortBy;      // created_at, total_amount
    private String sortDir;     // ASC, DESC

    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }
    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }
    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }

    public String getSortColumn() {
        if ("total_amount".equals(sortBy)) return "o.total_amount";
        return "o.created_at";
    }

    public String getSortDirection() {
        if ("ASC".equalsIgnoreCase(sortDir)) return "ASC";
        return "DESC";
    }
}
