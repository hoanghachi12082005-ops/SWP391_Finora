package com.storemanagement.util.inventory;

public final class InventoryUtil {
    private InventoryUtil() {}

    public static boolean isLowStock(int quantity, int minQuantity) {
        return quantity <= minQuantity;
    }
}
