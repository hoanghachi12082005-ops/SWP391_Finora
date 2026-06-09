package com.storemanagement.util.product;

public final class ProductCodeUtil {
    private ProductCodeUtil() {}

    public static String buildSku(String categoryCode, int productId) {
        return categoryCode + "-" + String.format("%05d", productId);
    }
}
