package com.storemanagement.model;

    public class Expense extends BaseModel {
    private double amount;
private String description;

        public Expense() {}

    public double getAmount() { return amount; }
public void setAmount(double amount) { this.amount = amount; }
public String getDescription() { return description; }
public void setDescription(String description) { this.description = description; }
    }
