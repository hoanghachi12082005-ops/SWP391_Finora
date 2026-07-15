package controller.inventory;

import dao.inventory.InventoryDAO;
import dao.inventory.StockTransferDAO;
import dao.inventory.StockTransactionDAO;
import dao.purchase.PurchaseOrderDAO;
import model.Inventory;
import model.StockTransfer;
import model.StockTransaction;
import model.PurchaseOrder;
import model.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@WebServlet(name = "HistoryController", urlPatterns = {"/inventory-history"})
public class HistoryController extends InventoryBaseController {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final StockTransferDAO transferDAO = new StockTransferDAO();
    private final StockTransactionDAO transactionDAO = new StockTransactionDAO();
    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // History tab is loaded by InventoryController during tab rendering.
        // This controller has no standalone doGet endpoints.
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // History is read-only. No doPost actions.
        doGet(request, response);
    }

    // [MOVED FROM InventoryController] - Original lines 862-1108
    void handleHistoryTab(HttpServletRequest request, Integer warehouseId, List<Integer> allowedWarehouseIds) throws Exception {
        String typeFilter = request.getParameter("typeFilter");
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");
        String productNameQuery = request.getParameter("productNameQuery");
        
        int page = 1;
        if (request.getParameter("page") != null) page = Integer.parseInt(request.getParameter("page"));
        int offset = (page - 1) * 20;

        // Clean & validate text filter
        if (productNameQuery != null) {
            productNameQuery = productNameQuery.trim();
        }

        // Validate date years and ranges
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            try {
                String[] parts = fromDate.split("-");
                if (parts.length > 0) {
                    int year = Integer.parseInt(parts[0]);
                    if (year < 1000 || year > 9999) {
                        request.setAttribute("error", "Từ ngày không hợp lệ! Năm phải nằm trong khoảng từ 1000 đến 9999.");
                        request.setAttribute("history", new ArrayList<StockTransaction>());
                        request.setAttribute("typeFilter", typeFilter);
                        request.setAttribute("fromDate", fromDate);
                        request.setAttribute("toDate", toDate);
                        request.setAttribute("productNameQuery", productNameQuery);
                        return;
                    }
                }
            } catch (Exception e) {
                // Ignore format exception here
            }
        }
        
        if (toDate != null && !toDate.trim().isEmpty()) {
            try {
                String[] parts = toDate.split("-");
                if (parts.length > 0) {
                    int year = Integer.parseInt(parts[0]);
                    if (year < 1000 || year > 9999) {
                        request.setAttribute("error", "Đến ngày không hợp lệ! Năm phải nằm trong khoảng từ 1000 đến 9999.");
                        request.setAttribute("history", new ArrayList<StockTransaction>());
                        request.setAttribute("typeFilter", typeFilter);
                        request.setAttribute("fromDate", fromDate);
                        request.setAttribute("toDate", toDate);
                        request.setAttribute("productNameQuery", productNameQuery);
                        return;
                    }
                }
            } catch (Exception e) {
                // Ignore format exception here
            }
        }

        if (fromDate != null && !fromDate.trim().isEmpty() && toDate != null && !toDate.trim().isEmpty()) {
            if (fromDate.compareTo(toDate) > 0) {
                request.setAttribute("error", "Khoảng ngày lọc không hợp lệ! Từ ngày phải trước hoặc bằng Đến ngày.");
                request.setAttribute("history", new ArrayList<StockTransaction>());
                request.setAttribute("typeFilter", typeFilter);
                request.setAttribute("fromDate", fromDate);
                request.setAttribute("toDate", toDate);
                request.setAttribute("productNameQuery", productNameQuery);
                return;
            }
        }

        List<StockTransaction> history = transactionDAO.findAllFiltered(
            warehouseId != null ? warehouseId : 0,
            allowedWarehouseIds,
            offset,
            20,
            typeFilter,
            fromDate,
            toDate,
            productNameQuery
        );
        
        request.setAttribute("typeFilter", typeFilter);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);
        request.setAttribute("productNameQuery", productNameQuery);
        request.setAttribute("history", history);

        List<PurchaseOrder> completedImports = purchaseOrderDAO.findAllByWarehouseAndType(warehouseId != null ? warehouseId : 0, "PURCHASE", null);
        List<PurchaseOrder> completedExports = purchaseOrderDAO.findAllByWarehouseAndType(warehouseId != null ? warehouseId : 0, "EXPORT", null);
        List<StockTransfer> completedTransfers = new ArrayList<>();
        try {
            completedTransfers = transferDAO.findAllGrouped(warehouseId != null ? warehouseId : 0, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        List<Map<String, Object>> unifiedVoucherHistory = new ArrayList<>();
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        java.text.SimpleDateFormat inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (PurchaseOrder po : completedImports) {
            if (!"PENDING".equalsIgnoreCase(po.getStatus())) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", po.getOrderId());
                map.put("code", po.getOrderCode());
                map.put("type", "IMPORT");
                map.put("typeLabel", "Nhập");
                map.put("partner", po.getSupplierName() != null ? po.getSupplierName() : "Nhà cung cấp");
                map.put("createdBy", po.getEmpName());
                map.put("approvedBy", po.getApprovedByName() != null ? po.getApprovedByName() : "-");
                map.put("amount", po.getTotalAmount());
                
                String dateStr = po.getCreatedAt() != null ? po.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
                java.util.Date parsedDate = null;
                if (dateStr != null) {
                    try { parsedDate = inputSdf.parse(dateStr); } catch (Exception e) {}
                }
                map.put("createdAt", parsedDate != null ? sdf.format(parsedDate) : (po.getCreatedAt() != null ? po.getCreatedAtFormatted() : ""));
                map.put("rawDate", parsedDate != null ? parsedDate : new java.util.Date(0));
                map.put("status", po.getStatus());
                map.put("statusLabel", "COMPLETED".equalsIgnoreCase(po.getStatus()) ? "Đã hoàn thành" : ("REJECTED".equalsIgnoreCase(po.getStatus()) ? "Bị từ chối" : "Đã hủy"));
                map.put("statusColor", "COMPLETED".equalsIgnoreCase(po.getStatus()) ? "bg-success" : ("REJECTED".equalsIgnoreCase(po.getStatus()) ? "bg-danger" : "bg-secondary"));
                map.put("detailCallback", "viewOrderDetails(" + po.getOrderId() + ")");
                unifiedVoucherHistory.add(map);
            }
        }

        for (PurchaseOrder po : completedExports) {
            if (!"PENDING".equalsIgnoreCase(po.getStatus())) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", po.getOrderId());
                map.put("code", po.getOrderCode());
                map.put("type", "EXPORT");
                map.put("typeLabel", "Xuất");
                map.put("partner", po.getSupplierName() != null ? po.getSupplierName() : "Khách hàng");
                map.put("createdBy", po.getEmpName());
                map.put("approvedBy", po.getApprovedByName() != null ? po.getApprovedByName() : "-");
                map.put("amount", po.getTotalAmount());
                
                String dateStr = po.getCreatedAt() != null ? po.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
                java.util.Date parsedDate = null;
                if (dateStr != null) {
                    try { parsedDate = inputSdf.parse(dateStr); } catch (Exception e) {}
                }
                map.put("createdAt", parsedDate != null ? sdf.format(parsedDate) : (po.getCreatedAt() != null ? po.getCreatedAtFormatted() : ""));
                map.put("rawDate", parsedDate != null ? parsedDate : new java.util.Date(0));
                map.put("status", po.getStatus());
                map.put("statusLabel", "COMPLETED".equalsIgnoreCase(po.getStatus()) ? "Đã hoàn thành" : ("REJECTED".equalsIgnoreCase(po.getStatus()) ? "Bị từ chối" : "Đã hủy"));
                map.put("statusColor", "COMPLETED".equalsIgnoreCase(po.getStatus()) ? "bg-success" : ("REJECTED".equalsIgnoreCase(po.getStatus()) ? "bg-danger" : "bg-secondary"));
                map.put("detailCallback", "viewOrderDetails(" + po.getOrderId() + ")");
                unifiedVoucherHistory.add(map);
            }
        }

        for (StockTransfer st : completedTransfers) {
            String s = st.getDisplayStatus();
            if ("COMPLETED".equals(s) || "CANCELLED".equals(s) || "PARTIAL_COMPLETE".equals(s)) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", st.getStockTransferId());
                map.put("code", st.getTransferCode());
                map.put("type", "TRANSFER");
                map.put("typeLabel", "Điều chuyển");
                
                String partnerName = "Nhiều đối tác";
                if (st.getSubTransfers() != null && st.getSubTransfers().size() == 1) {
                    partnerName = st.getSubTransfers().get(0).getToWarehouseName();
                }
                map.put("partner", partnerName);
                map.put("createdBy", st.getCreatedByName());
                map.put("approvedBy", st.getApprovedByName() != null ? st.getApprovedByName() : "-");
                map.put("amount", null);
                map.put("createdAt", st.getTransferDate() != null ? sdf.format(st.getTransferDate()) : "");
                map.put("rawDate", st.getTransferDate() != null ? st.getTransferDate() : new java.util.Date(0));
                map.put("status", s);
                
                if ("COMPLETED".equals(s)) {
                    map.put("statusLabel", "Đã hoàn thành");
                    map.put("statusColor", "bg-success");
                } else if ("PARTIAL_COMPLETE".equals(s)) {
                    map.put("statusLabel", "Hoàn thành (Có lỗi)");
                    map.put("statusColor", "bg-warning text-dark");
                } else {
                    map.put("statusLabel", "Đã hủy / Bị từ chối");
                    map.put("statusColor", "bg-danger");
                }
                map.put("detailCallback", "viewTicketDetails(" + st.getStockTransferId() + ")");
                unifiedVoucherHistory.add(map);
            }
        }
        
        try {
            dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
            List<model.InventoryCheck> checks = checkDAO.findAllByWarehouseFiltered(warehouseId != null ? warehouseId : 0, null, null, null);
            if (checks != null) {
                for (model.InventoryCheck c : checks) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getCheckId());
                    map.put("code", c.getCheckCode());
                    map.put("type", "CHECK");
                    map.put("typeLabel", "Kiểm Kho");
                    map.put("partner", c.getWarehouseName());
                    map.put("createdBy", c.getCreatedByName());
                    map.put("approvedBy", c.getApprovedByName() != null && !c.getApprovedByName().isEmpty() ? c.getApprovedByName() : "-");
                    map.put("amount", null);
                    
                    java.util.Date parsedDate = null;
                    if (c.getCreatedAt() != null) {
                        try {
                            parsedDate = java.sql.Timestamp.valueOf(c.getCreatedAt());
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                    map.put("createdAt", c.getFormattedCreatedAt());
                    map.put("rawDate", parsedDate != null ? parsedDate : new java.util.Date(0));
                    map.put("status", c.getStatus());
                    map.put("statusLabel", "APPROVED".equalsIgnoreCase(c.getStatus()) ? "Đã duyệt" : ("PENDING".equalsIgnoreCase(c.getStatus()) ? "Chờ duyệt" : "Đã hủy"));
                    map.put("statusColor", "APPROVED".equalsIgnoreCase(c.getStatus()) ? "bg-success" : ("PENDING".equalsIgnoreCase(c.getStatus()) ? "bg-warning text-dark" : "bg-danger"));
                    map.put("detailCallback", "viewCheckDetails(" + c.getCheckId() + ")");
                    unifiedVoucherHistory.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Filter by dates if present
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            try {
                java.util.Date fd = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(fromDate);
                unifiedVoucherHistory.removeIf(map -> ((java.util.Date) map.get("rawDate")).before(fd));
            } catch (Exception e) {}
        }
        if (toDate != null && !toDate.trim().isEmpty()) {
            try {
                java.util.Date td = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(toDate);
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(td);
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
                java.util.Date nextDay = cal.getTime();
                unifiedVoucherHistory.removeIf(map -> ((java.util.Date) map.get("rawDate")).after(nextDay));
            } catch (Exception e) {}
        }
        
        // Sort by created time descending
        unifiedVoucherHistory.sort((m1, m2) -> ((java.util.Date) m2.get("rawDate")).compareTo((java.util.Date) m1.get("rawDate")));
        request.setAttribute("voucherHistory", unifiedVoucherHistory);
    }
}
