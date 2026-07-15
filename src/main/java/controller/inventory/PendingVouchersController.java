package controller.inventory;

import dao.purchase.PurchaseOrderDAO;
import dao.inventory.StockTransferDAO;
import dao.inventory.InventoryCheckDAO;
import model.PurchaseOrder;
import model.StockTransfer;
import model.InventoryCheck;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@WebServlet(name = "PendingVouchersController", urlPatterns = {"/inventory-pending"})
public class PendingVouchersController extends InventoryBaseController {

    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    private final StockTransferDAO transferDAO = new StockTransferDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Pending vouchers tab is loaded by InventoryController during tab rendering.
        // This controller has no standalone doGet endpoints.
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Pending vouchers tab only displays data. No doPost actions.
        doGet(request, response);
    }

    // [MOVED FROM InventoryController] - Original lines 421-560
    void handlePendingVouchersTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        List<PurchaseOrder> pendingImports = purchaseOrderDAO.findAllByWarehouseAndType(warehouseId != null ? warehouseId : 0, "PURCHASE", "PENDING");
        List<PurchaseOrder> pendingExports = purchaseOrderDAO.findAllByWarehouseAndType(warehouseId != null ? warehouseId : 0, "EXPORT", "PENDING");
        
        List<Map<String, Object>> pendingVouchers = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        java.text.SimpleDateFormat inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (pendingImports != null) {
            for (PurchaseOrder po : pendingImports) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", po.getOrderId());
                map.put("code", po.getOrderCode());
                map.put("type", "IMPORT");
                map.put("typeLabel", "Nhập");
                map.put("partner", po.getSupplierName() != null ? po.getSupplierName() : "Nhà cung cấp");
                map.put("createdBy", po.getEmpName());
                map.put("amount", po.getTotalAmount());
                
                String dateStr = po.getCreatedAt() != null ? po.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
                java.util.Date parsedDate = null;
                if (dateStr != null) {
                    try { parsedDate = inputSdf.parse(dateStr); } catch (Exception e) {}
                }
                map.put("createdAt", parsedDate != null ? sdf.format(parsedDate) : (po.getCreatedAt() != null ? po.getCreatedAtFormatted() : ""));
                map.put("rawDate", parsedDate != null ? parsedDate : new java.util.Date(0));
                map.put("actionCancel", "cancelOrder");
                map.put("idParamName", "orderId");
                map.put("detailCallback", "viewOrderDetails(" + po.getOrderId() + ")");
                pendingVouchers.add(map);
            }
        }

        if (pendingExports != null) {
            for (PurchaseOrder po : pendingExports) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", po.getOrderId());
                map.put("code", po.getOrderCode());
                map.put("type", "EXPORT");
                map.put("typeLabel", "Xuất");
                map.put("partner", po.getSupplierName() != null ? po.getSupplierName() : "Khách hàng");
                map.put("createdBy", po.getEmpName());
                map.put("amount", po.getTotalAmount());
                
                String dateStr = po.getCreatedAt() != null ? po.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
                java.util.Date parsedDate = null;
                if (dateStr != null) {
                    try { parsedDate = inputSdf.parse(dateStr); } catch (Exception e) {}
                }
                map.put("createdAt", parsedDate != null ? sdf.format(parsedDate) : (po.getCreatedAt() != null ? po.getCreatedAtFormatted() : ""));
                map.put("rawDate", parsedDate != null ? parsedDate : new java.util.Date(0));
                map.put("actionCancel", "cancelOrder");
                map.put("idParamName", "orderId");
                map.put("detailCallback", "viewOrderDetails(" + po.getOrderId() + ")");
                pendingVouchers.add(map);
            }
        }

        try {
            dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
            List<model.InventoryCheck> pendingChecks = checkDAO.findAllByWarehouseFiltered(warehouseId != null ? warehouseId : 0, null, "PENDING", null);
            if (pendingChecks != null) {
                for (model.InventoryCheck check : pendingChecks) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", check.getCheckId());
                    map.put("code", check.getCheckCode());
                    map.put("type", "CHECK");
                    map.put("typeLabel", "Kiểm Kho");
                    map.put("partner", check.getWarehouseName());
                    map.put("createdBy", check.getCreatedByName());
                    map.put("amount", null);
                    
                    java.util.Date parsedDate = null;
                    if (check.getCreatedAt() != null) {
                        try {
                            parsedDate = java.sql.Timestamp.valueOf(check.getCreatedAt());
                        } catch (Exception e) {}
                    }
                    map.put("createdAt", check.getFormattedCreatedAt());
                    map.put("rawDate", parsedDate != null ? parsedDate : new java.util.Date(0));
                    map.put("actionCancel", "cancelCheck");
                    map.put("idParamName", "checkId");
                    map.put("detailCallback", "viewCheckDetails(" + check.getCheckId() + ")");
                    pendingVouchers.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<model.StockTransfer> transfers = transferDAO.findAllGrouped(warehouseId != null ? warehouseId : 0, null, null, null);
            if (transfers != null) {
                for (model.StockTransfer st : transfers) {
                    String status = st.getDisplayStatus();
                    if ("COMPLETED".equals(status) || "PARTIAL_COMPLETE".equals(status) || "CANCELLED".equals(status)) {
                        continue;
                    }
                    
                    String partnerName = "Nhiều đối tác";
                    if (st.getSubTransfers() != null) {
                        if (st.getSubTransfers().size() == 1) {
                            model.StockTransfer sub = st.getSubTransfers().get(0);
                            boolean isExp = (sub.getFromBranchId() == st.getCreatorBranchId());
                            partnerName = isExp ? sub.getToWarehouseName() : sub.getFromWarehouseName();
                        } else {
                            java.util.Set<String> partners = new java.util.LinkedHashSet<>();
                            for (model.StockTransfer sub : st.getSubTransfers()) {
                                boolean isExp = (sub.getFromBranchId() == st.getCreatorBranchId());
                                partners.add(isExp ? sub.getToWarehouseName() : sub.getFromWarehouseName());
                            }
                            partnerName = String.join(", ", partners);
                        }
                    }
                    
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", st.getStockTransferId());
                    map.put("code", st.getTransferCode());
                    map.put("type", "TRANSFER");
                    map.put("typeLabel", "Điều chuyển");
                    map.put("partner", partnerName);
                    map.put("createdBy", st.getCreatedByName());
                    map.put("amount", null);
                    
                    map.put("createdAt", st.getTransferDate() != null ? sdf.format(st.getTransferDate()) : "");
                    map.put("rawDate", st.getTransferDate() != null ? st.getTransferDate() : new java.util.Date(0));
                    map.put("actionCancel", "cancelTransfer");
                    map.put("idParamName", "transferId");
                    map.put("detailCallback", "viewTicketDetails(" + st.getStockTransferId() + ")");
                    pendingVouchers.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Sort by created time descending
        pendingVouchers.sort((m1, m2) -> ((java.util.Date) m2.get("rawDate")).compareTo((java.util.Date) m1.get("rawDate")));
        request.setAttribute("pendingVouchers", pendingVouchers);
    }
}
