package util.validation;

import dao.inventory.InventoryDAO;
import model.Employee;
import model.StockTransferDetail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryValidator {

    /**
     * Kiểm tra quyền phê duyệt của tài khoản.
     * Chặn Staff tuyệt đối khỏi các thao tác duyệt/thực thi/hủy phiếu.
     */
    public static ValidationResult validateStaffApprovalPermission(Employee currentUser, String action) {
        ValidationResult result = new ValidationResult();
        if (currentUser == null) {
            result.addError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            return result;
        }

        String role = currentUser.getRoleName();
        if ("Staff".equalsIgnoreCase(role)) {
            if ("approve".equalsIgnoreCase(action) ||
                "approveCheck".equalsIgnoreCase(action) ||
                "rejectCheck".equalsIgnoreCase(action) ||
                "confirmSend".equalsIgnoreCase(action) ||
                "rejectSend".equalsIgnoreCase(action) ||
                "confirmReceive".equalsIgnoreCase(action) ||
                "cancelTransfer".equalsIgnoreCase(action) ||
                "executeTransfer".equalsIgnoreCase(action)) {
                result.addError("Cảnh báo bảo mật: Tài khoản Nhân viên không có quyền phê duyệt hoặc thực thi các thao tác kho.");
            }
        }
        return result;
    }

    /**
     * Validate dữ liệu khi tạo phiếu Nhập kho.
     */
    public static ValidationResult validateImportRequest(int warehouseId, String[] productIds, String[] quantities,
                                                          String[] supplierIds, String[] importPrices, String note) {
        ValidationResult result = new ValidationResult();

        if (warehouseId <= 0) {
            result.addError("Kho hàng không hợp lệ.");
        }

        if (productIds == null || productIds.length == 0) {
            result.addError("Vui lòng chọn ít nhất một sản phẩm để nhập kho.");
            return result;
        }

        if (quantities == null || productIds.length != quantities.length) {
            result.addError("Dữ liệu số lượng không tương thích với danh sách sản phẩm.");
            return result;
        }

        if (supplierIds == null || productIds.length != supplierIds.length) {
            result.addError("Dữ liệu nhà cung cấp không tương thích với danh sách sản phẩm.");
            return result;
        }

        Set<Integer> seenProducts = new HashSet<>();

        for (int i = 0; i < productIds.length; i++) {
            int rowNum = i + 1;

            // Safe parse product ID
            int pId = 0;
            try {
                pId = Integer.parseInt(productIds[i].trim());
                if (pId <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                result.addError("Dòng " + rowNum + ": Mã sản phẩm không hợp lệ.");
                continue;
            }

            if (!seenProducts.add(pId)) {
                result.addError("Dòng " + rowNum + ": Sản phẩm ID " + pId + " bị lặp lại trong phiếu nhập.");
            }

            // Safe parse quantity
            int qty = 0;
            try {
                qty = Integer.parseInt(quantities[i].trim());
                if (qty <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                result.addError("Dòng " + rowNum + ": Số lượng nhập phải là số nguyên dương lớn hơn 0.");
            }

            // Safe parse supplier ID
            try {
                int sId = Integer.parseInt(supplierIds[i].trim());
                if (sId <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                result.addError("Dòng " + rowNum + ": Nhà cung cấp không hợp lệ.");
            }

            // Safe parse import price
            if (importPrices != null && importPrices.length > i) {
                try {
                    double price = Double.parseDouble(importPrices[i].trim());
                    if (price < 0) throw new NumberFormatException();
                } catch (Exception e) {
                    result.addError("Dòng " + rowNum + ": Đơn giá nhập không được là số âm.");
                }
            }
        }

        if (note != null && note.length() > 500) {
            result.addError("Ghi chú nhập kho không được vượt quá 500 ký tự.");
        }

        return result;
    }

    /**
     * Validate khi TẠO phiếu điều chuyển kho (Create Transfer Slip).
     * Quy tắc: Tồn kho không đủ thì đưa ra WARNING (Cảnh báo), không CHẶN tạo đơn.
     */
    public static ValidationResult validateTransferCreation(int fromWarehouseId, String[] productIds,
                                                             String[] partnerWarehouseIds, String[] quantities,
                                                             String note, InventoryDAO inventoryDAO) {
        ValidationResult result = new ValidationResult();

        if (fromWarehouseId <= 0) {
            result.addError("Kho gửi không hợp lệ.");
        }

        if (productIds == null || productIds.length == 0) {
            result.addError("Vui lòng chọn ít nhất 1 sản phẩm để điều chuyển.");
            return result;
        }

        if (partnerWarehouseIds == null || productIds.length != partnerWarehouseIds.length) {
            result.addError("Dữ liệu kho nhận không tương ứng với danh sách sản phẩm.");
            return result;
        }

        if (quantities == null || productIds.length != quantities.length) {
            result.addError("Dữ liệu số lượng không tương ứng với danh sách sản phẩm.");
            return result;
        }

        for (int i = 0; i < productIds.length; i++) {
            int rowNum = i + 1;
            int pId = 0;
            int toWId = 0;
            int qty = 0;

            try {
                pId = Integer.parseInt(productIds[i].trim());
                if (pId <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                result.addError("Dòng " + rowNum + ": Mã sản phẩm không hợp lệ.");
                continue;
            }

            try {
                toWId = Integer.parseInt(partnerWarehouseIds[i].trim());
                if (toWId <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                result.addError("Dòng " + rowNum + ": Kho nhận không hợp lệ.");
                continue;
            }

            if (fromWarehouseId == toWId) {
                result.addError("Dòng " + rowNum + ": Kho nhận không được trùng với Kho gửi.");
            }

            try {
                qty = Integer.parseInt(quantities[i].trim());
                if (qty <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                result.addError("Dòng " + rowNum + ": Số lượng chuyển phải lớn hơn 0.");
                continue;
            }

            // Kiểm tra tồn kho tại Kho gửi -> Nếu thiếu thì cảnh báo WARNING
            if (inventoryDAO != null && fromWarehouseId > 0 && pId > 0) {
                int currentStock = inventoryDAO.getStock(pId, fromWarehouseId);
                if (currentStock < qty) {
                    result.addWarning("Dòng " + rowNum + " [Sản phẩm ID " + pId + "]: Tồn kho hiện tại (" + currentStock +
                            ") nhỏ hơn số lượng yêu cầu chuyển (" + qty + "). Hãy đảm bảo nhập đủ hàng trước khi bấm xuất kho!");
                }
            }
        }

        if (note != null && note.length() > 500) {
            result.addError("Ghi chú phiếu điều chuyển không được vượt quá 500 ký tự.");
        }

        return result;
    }

    /**
     * Validate khi BẤM XUẤT KHO / PHÊ DUYỆT ĐIỀU CHUYỂN (Execute / Confirm Send Transfer).
     * Quy tắc: Tồn kho tại kho gửi ít hơn số lượng xuất -> REJECT HOÀN TOÀN để tránh âm kho.
     */
    public static ValidationResult validateTransferExecution(int fromWarehouseId, List<StockTransferDetail> details,
                                                              InventoryDAO inventoryDAO) {
        ValidationResult result = new ValidationResult();

        if (fromWarehouseId <= 0) {
            result.addError("Kho xuất hàng không hợp lệ.");
            return result;
        }

        if (details == null || details.isEmpty()) {
            result.addError("Phiếu điều chuyển không chứa danh sách sản phẩm.");
            return result;
        }

        for (StockTransferDetail d : details) {
            int currentStock = inventoryDAO.getStock(d.getProductId(), fromWarehouseId);
            if (currentStock < d.getQuantity()) {
                result.addError("Xuất kho thất bại: Sản phẩm ID " + d.getProductId() +
                        " có số lượng tồn khả dụng (" + currentStock + ") không đủ để xuất " + d.getQuantity() +
                        " sản phẩm. Thao tác đã bị hủy để tránh âm kho!");
            }
        }

        return result;
    }

    /**
     * Validate khi TẠO/CẬP NHẬT phiếu kiểm kê kho (Inventory Check).
     */
    public static ValidationResult validateCheckRequest(int warehouseId, String[] productIds, String[] actualQtys, String[] notes) {
        ValidationResult result = new ValidationResult();

        if (warehouseId <= 0) {
            result.addError("Kho kiểm kê không hợp lệ.");
        }

        if (productIds == null || productIds.length == 0) {
            result.addError("Danh sách sản phẩm kiểm kê không được rỗng.");
            return result;
        }

        if (actualQtys == null || productIds.length != actualQtys.length) {
            result.addError("Dữ liệu số lượng thực tế không hợp lệ.");
            return result;
        }

        for (int i = 0; i < productIds.length; i++) {
            int rowNum = i + 1;
            try {
                int pId = Integer.parseInt(productIds[i].trim());
                if (pId <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                result.addError("Dòng " + rowNum + ": Mã sản phẩm không hợp lệ.");
            }

            try {
                int actQty = Integer.parseInt(actualQtys[i].trim());
                if (actQty < 0) throw new NumberFormatException();
            } catch (Exception e) {
                result.addError("Dòng " + rowNum + ": Số lượng thực tế phải là số nguyên không âm (>= 0).");
            }
        }

        return result;
    }

    /**
     * Kiểm tra xem kho hàng có được phép tạo phiếu kiểm kho mới không.
     * Chặn hoàn toàn nếu kho hàng đang có phiếu kiểm chưa được duyệt (status = PENDING).
     */
    public static ValidationResult validateCanCreateCheck(int warehouseId, dao.inventory.InventoryCheckDAO checkDAO) {
        ValidationResult result = new ValidationResult();
        if (warehouseId <= 0) {
            result.addError("Kho kiểm kê không hợp lệ.");
            return result;
        }
        if (checkDAO != null) {
            model.InventoryCheck pendingCheck = checkDAO.getPendingCheckByWarehouse(warehouseId);
            if (pendingCheck != null) {
                result.addError("Kho hàng này đang có phiếu kiểm kho chưa được duyệt (Mã phiếu: " + 
                        pendingCheck.getCheckCode() + "). Không thể tạo phiếu kiểm kho mới cho đến khi phiếu cũ được duyệt hoặc bị hủy!");
            }
        }
        return result;
    }
}
