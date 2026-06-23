package model;

    public class PurchaseOrder {
    private int supplierId;
private double totalAmount;

        public PurchaseOrder() {}

    public int getSupplierId() { return supplierId; }
public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
public double getTotalAmount() { return totalAmount; }
public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    }
