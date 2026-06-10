package model;

    public class Order extends BaseModel {
    private int customerId;
private int storeId;
private double totalAmount;

        public Order() {}

    public int getCustomerId() { return customerId; }
public void setCustomerId(int customerId) { this.customerId = customerId; }
public int getStoreId() { return storeId; }
public void setStoreId(int storeId) { this.storeId = storeId; }
public double getTotalAmount() { return totalAmount; }
public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    }
