package com.storemanagement.model;

    public class Payment extends BaseModel {
    private int orderId;
private double amount;
private String method;

        public Payment() {}

    public int getOrderId() { return orderId; }
public void setOrderId(int orderId) { this.orderId = orderId; }
public double getAmount() { return amount; }
public void setAmount(double amount) { this.amount = amount; }
public String getMethod() { return method; }
public void setMethod(String method) { this.method = method; }
    }
