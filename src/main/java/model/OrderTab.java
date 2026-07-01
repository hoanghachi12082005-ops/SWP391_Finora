package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderTab implements Serializable {
    private static final long serialVersionUID = 1L;

    private int tabId;
    private List<CartItem> items = new ArrayList<>();
    private Customer selectedCustomer;
    private String note = "";
    private Voucher appliedVoucher;
    private String status = "ACTIVE"; // ACTIVE, HOLD

    public OrderTab() {}

    public OrderTab(int tabId) {
        this.tabId = tabId;
    }

    // ── Computed fields for tab summary ──
    public double getSubtotal() {
        double subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getLineTotal();
        }
        return subtotal;
    }

    public int getTotalQuantity() {
        int qty = 0;
        for (CartItem item : items) {
            qty += item.getQuantity();
        }
        return qty;
    }

    public double getDiscountAmount() {
        double subtotal = getSubtotal();
        if (appliedVoucher == null) return 0;
        double discount = 0;
        if ("PERCENT".equalsIgnoreCase(appliedVoucher.getDiscountType()) || "PERCENTAGE".equalsIgnoreCase(appliedVoucher.getDiscountType())) {
            discount = subtotal * appliedVoucher.getDiscountValue() / 100.0;
        } else {
            discount = appliedVoucher.getDiscountValue();
        }
        return Math.min(discount, subtotal);
    }

    public double getVatAmount() {
        double subtotal = getSubtotal();
        double discount = getDiscountAmount();
        return (subtotal - discount) * 0.08;
    }

    public double getTotalAmount() {
        double subtotal = getSubtotal();
        double discount = getDiscountAmount();
        double vat = getVatAmount();
        return subtotal - discount + vat;
    }

    // ── Getters and Setters ──
    public int getTabId() { return tabId; }
    public void setTabId(int tabId) { this.tabId = tabId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public Customer getSelectedCustomer() { return selectedCustomer; }
    public void setSelectedCustomer(Customer selectedCustomer) { this.selectedCustomer = selectedCustomer; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Voucher getAppliedVoucher() { return appliedVoucher; }
    public void setAppliedVoucher(Voucher appliedVoucher) { this.appliedVoucher = appliedVoucher; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
