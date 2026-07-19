package util.inventory;

/**
 * Lớp tiện ích (Utility Class) chứa các hàm bổ trợ cho quản lý kho hàng (Inventory).
 * Lớp này được thiết kế dưới dạng final class và private constructor để ngăn chặn việc khởi tạo đối tượng.
 */
public final class InventoryUtil {
    
    // Private constructor để tránh việc tạo instance của class tiện ích này
    private InventoryUtil() {}

    /**
     * Kiểm tra xem số lượng sản phẩm trong kho có rơi vào trạng thái sắp hết hàng hay không.
     * Trạng thái "sắp hết hàng" (Low Stock) xảy ra khi số lượng tồn thực tế nhỏ hơn hoặc bằng số lượng tối thiểu quy định.
     *
     * @param quantity Số lượng tồn kho thực tế hiện tại
     * @param minQuantity Ngưỡng số lượng tối thiểu để cảnh báo hết hàng
     * @return true nếu số lượng tồn thực tế nhỏ hơn hoặc bằng ngưỡng tối thiểu, ngược lại là false
     */
    public static boolean isLowStock(int quantity, int minQuantity) {
        return quantity <= minQuantity;
    }
}
