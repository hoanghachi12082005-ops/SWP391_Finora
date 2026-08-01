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
/**
 * Base controller chung cho tất cả các controller trong phân hệ Kho hàng (Inventory).
 * Cung cấp các hàm tiện ích được kế thừa để sử dụng lại trên nhiều Servlet khác nhau.
 */
public abstract class InventoryBaseController extends BaseController {

    /**
     * Hàm định dạng và xử lý chuỗi ký tự thô để đảm bảo an toàn dữ liệu khi xuất ra JSON (JSON Escape).
     * Loại bỏ các ký tự đặc biệt như nháy kép, dấu xuyệt ngược, ký tự xuống dòng... để tránh lỗi cú pháp JSON ở Client.
     *
     * @param input Chuỗi ký tự thô
     * @return Chuỗi ký tự đã được chuyển đổi an toàn (ví dụ: " chuyển thành \")
     */
    protected String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // Duyệt qua từng ký tự của chuỗi đầu vào
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\""); // Thay đổi dấu nháy kép thành nháy kép có xuyệt
                    break;
                case '\\':
                    sb.append("\\\\"); // Thay đổi dấu xuyệt ngược thành hai xuyệt
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n"); // Thay đổi ký tự xuống dòng thành \n
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t"); // Thay đổi ký tự tab thành \t
                    break;
                default:
                    // Các ký tự điều khiển ASCII không hiển thị
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
     * Hàm quét thư mục chứa ảnh sản phẩm trong server vật lý để tự động gán đường dẫn hình ảnh 
     * vào danh sách sản phẩm tồn kho hiển thị ở Client.
     *
     * @param request HttpServletRequest để lấy đường dẫn ngữ cảnh thực tế của ứng dụng web
     * @param stockList Danh sách hàng tồn kho cần quét gán ảnh
     */
    protected void attachImageUrls(HttpServletRequest request, List<Inventory> stockList) {
        if (stockList == null || stockList.isEmpty()) return;
        
        String ctx = request.getContextPath();
        String real = request.getServletContext().getRealPath("/assets/images/product/");
        java.io.File dir = (real != null) ? new java.io.File(real) : null;
        java.io.File[] files = (dir != null && dir.exists()) ? dir.listFiles() : null;

        for (Inventory item : stockList) {
            if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
                item.setImageUrl(model.Product.formatDisplayUrl(item.getImageUrl(), ctx));
            } else if (files != null) {
                String prefix = "product_" + item.getProductId();
                for (java.io.File f : files) {
                    if (f.isFile() && f.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                        item.setImageUrl(ctx + "/assets/images/product/" + f.getName() + "?v=" + f.lastModified());
                        break;
                    }
                }
            }
        }
    }
}
