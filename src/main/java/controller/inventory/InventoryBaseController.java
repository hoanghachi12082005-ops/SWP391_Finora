package controller.inventory;

import controller.common.BaseController;
import model.Inventory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Base controller chung cho tất cả các controller trong module Inventory.
 * Chứa các phương thức tiện ích dùng chung (escapeJson, attachImageUrls, ...).
 * 
 * Được tách từ InventoryController gốc để tái sử dụng.
 */
public abstract class InventoryBaseController extends BaseController {

    /**
     * Escape chuỗi cho JSON output.
     * [MOVED FROM InventoryController] - Original lines 2204-2243
     */
    protected String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < ' ') {
                        String t = "000" + Integer.toHexString(ch);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Gắn URL ảnh sản phẩm cho danh sách Inventory.
     * [MOVED FROM InventoryController] - Original lines 2150-2167
     */
    protected void attachImageUrls(HttpServletRequest request, List<Inventory> stockList) {
        if (stockList == null || stockList.isEmpty()) return;
        String real = request.getServletContext().getRealPath("/assets/images/product/");
        java.io.File dir = new java.io.File(real);
        if (!dir.exists()) return;
        java.io.File[] files = dir.listFiles();
        if (files == null) return;
        String ctx = request.getContextPath();
        for (Inventory item : stockList) {
            String prefix = "product_" + item.getProductId() + ".";
            for (java.io.File f : files) {
                if (f.isFile() && f.getName().toLowerCase().startsWith(prefix)) {
                    item.setImageUrl(ctx + "/assets/images/product/" + f.getName() + "?v=" + f.lastModified());
                    break;
                }
            }
        }
    }
}
