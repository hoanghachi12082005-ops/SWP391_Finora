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

    /**
     * Xử lý và chuẩn bị dữ liệu cho Tab Lịch sử.
     * Hàm này thực hiện:
     * 1. Lọc và phân trang lịch sử biến động số lượng (Stock Transactions - thẻ kho).
     * 2. Lấy toàn bộ phiếu nhập/xuất/điều chuyển/kiểm kho đã hoàn thành, lọc theo ngày và gộp chung thành một danh sách duy nhất (Unified Voucher History).
     */
    void handleHistoryTab(HttpServletRequest request, Integer warehouseId, List<Integer> allowedWarehouseIds) throws Exception {
        String typeFilter = request.getParameter("typeFilter");
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");
        String productNameQuery = request.getParameter("productNameQuery");
        
        int page = 1;
        if (request.getParameter("page") != null) page = Integer.parseInt(request.getParameter("page"));
        int offset = (page - 1) * 20;

        // Clean & trim productNameQuery
        if (productNameQuery != null) {
            productNameQuery = productNameQuery.trim();
        }

        // Lấy danh sách giao dịch tồn kho thực tế (Stock Transactions)
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

        // BẮT ĐẦU: Gom nhóm và xây dựng danh sách phiếu gộp (Unified Voucher History)
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
                map.put("statusLabel", "COMPLETED".equalsIgnoreCase(po.getStatus()) ? "Đã hoàn thành" : ("IN_TRANSIT".equalsIgnoreCase(po.getStatus()) ? "Đang vận chuyển (Chờ nhập kho)" : ("REJECTED".equalsIgnoreCase(po.getStatus()) ? "Bị từ chối" : "Đã hủy")));
                map.put("statusColor", "COMPLETED".equalsIgnoreCase(po.getStatus()) ? "bg-success" : ("IN_TRANSIT".equalsIgnoreCase(po.getStatus()) ? "bg-info text-dark" : ("REJECTED".equalsIgnoreCase(po.getStatus()) ? "bg-danger" : "bg-secondary")));
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
        
        // 1. Filter by dates (fromDate & toDate)
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            java.util.Date fd = parseFlexibleDate(fromDate);
            if (fd != null) {
                final java.util.Date finalFd = fd;
                unifiedVoucherHistory.removeIf(map -> {
                    java.util.Date d = (java.util.Date) map.get("rawDate");
                    return d != null && d.before(finalFd);
                });
            }
        }
        if (toDate != null && !toDate.trim().isEmpty()) {
            java.util.Date td = parseFlexibleDate(toDate);
            if (td != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(td);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                final java.util.Date endOfDay = cal.getTime();
                unifiedVoucherHistory.removeIf(map -> {
                    java.util.Date d = (java.util.Date) map.get("rawDate");
                    return d != null && d.after(endOfDay);
                });
            }
        }

        // 2. Filter by productNameQuery if present
        if (productNameQuery != null && !productNameQuery.trim().isEmpty()) {
            String q = productNameQuery.trim().toLowerCase();
            unifiedVoucherHistory.removeIf(map -> {
                String code = map.get("code") != null ? map.get("code").toString().toLowerCase() : "";
                String cb = map.get("createdBy") != null ? map.get("createdBy").toString().toLowerCase() : "";
                String partner = map.get("partner") != null ? map.get("partner").toString().toLowerCase() : "";
                String approvedBy = map.get("approvedBy") != null ? map.get("approvedBy").toString().toLowerCase() : "";
                String note = map.get("note") != null ? map.get("note").toString().toLowerCase() : "";
                String typeLabel = map.get("typeLabel") != null ? map.get("typeLabel").toString().toLowerCase() : "";
                return !code.contains(q) && !cb.contains(q) && !partner.contains(q) && !approvedBy.contains(q) && !note.contains(q) && !typeLabel.contains(q);
            });
        }

        // 3. Filter by typeFilter
        if (typeFilter != null && !typeFilter.trim().isEmpty()) {
            String filter = typeFilter.trim().toUpperCase();
            unifiedVoucherHistory.removeIf(map -> {
                String type = map.get("type") != null ? map.get("type").toString().toUpperCase() : "";
                if (filter.startsWith("TRANSFER") && type.startsWith("TRANSFER")) {
                    return false;
                }
                return !filter.equalsIgnoreCase(type);
            });
        }

        // Sort by created time descending
        unifiedVoucherHistory.sort((m1, m2) -> ((java.util.Date) m2.get("rawDate")).compareTo((java.util.Date) m1.get("rawDate")));

        try {
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
        } catch (Exception ignored) {}

        int sizeValue = 10;
        try {
            if (request.getParameter("sizeValue") != null) {
                sizeValue = Integer.parseInt(request.getParameter("sizeValue"));
            }
        } catch (Exception ignored) {}

        int totalRecords = unifiedVoucherHistory.size();
        util.pagination.PaginationHelper.PageResult pr = util.pagination.PaginationHelper.compute(totalRecords, page, sizeValue);
        pr.setAttributes(request);

        int fromIndex = (pr.getCurrentPage() - 1) * pr.getPageSize();
        int toIndex = Math.min(fromIndex + pr.getPageSize(), totalRecords);
        List<Map<String, Object>> pagedHistory = (fromIndex < totalRecords) ? unifiedVoucherHistory.subList(fromIndex, toIndex) : new ArrayList<>();

        request.setAttribute("voucherHistory", pagedHistory);
        request.setAttribute("productNameQuery", productNameQuery);
        request.setAttribute("typeFilter", typeFilter);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);

        request.setAttribute("baseUrl", request.getContextPath() + "/inventory");
        StringBuilder qs = new StringBuilder();
        qs.append("&tab=history");
        if (warehouseId != null) qs.append("&warehouseId=").append(warehouseId);
        if (productNameQuery != null && !productNameQuery.isEmpty()) qs.append("&productNameQuery=").append(java.net.URLEncoder.encode(productNameQuery, "UTF-8"));
        if (typeFilter != null && !typeFilter.isEmpty()) qs.append("&typeFilter=").append(typeFilter);
        if (fromDate != null && !fromDate.isEmpty()) qs.append("&fromDate=").append(fromDate);
        request.setAttribute("queryString", qs.toString());
    }

    private java.util.Date parseFlexibleDate(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String s = input.trim();
        String[] formats = {"yyyy-MM-dd", "dd/MM/yyyy", "d/M/yyyy", "yyyy/MM/dd"};
        for (String fmt : formats) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(fmt);
                sdf.setLenient(false);
                return sdf.parse(s);
            } catch (Exception ignored) {}
        }
        return null;
    }
}
