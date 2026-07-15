package controller.inventory;

import controller.common.BaseController;
import dao.inventory.InventoryDAO;
import dao.inventory.WarehouseDAO;
import dao.sales.OrderDAO;
import dao.purchase.PurchaseOrderDAO;
import dao.inventory.StockTransferDAO;
import dao.inventory.StockTransactionDAO;
import dao.supplier.SupplierDAO;
import model.Employee;
import model.Inventory;
import model.Warehouse;
import model.StockTransfer;
import model.StockTransferDetail;
import model.Order;
import model.PurchaseOrder;
import model.StockTransaction;
import model.Supplier;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@WebServlet(name = "InventoryController", urlPatterns = {"/inventory"})
public class InventoryController extends BaseController {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final StockTransferDAO transferDAO = new StockTransferDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    private final StockTransactionDAO transactionDAO = new StockTransactionDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        
        // Flash messages
        String msg = (String) session.getAttribute("message");
        if (msg != null) {
            request.setAttribute("message", msg);
            session.removeAttribute("message");
        }
        
        String errorMsg = (String) session.getAttribute("error");
        if (errorMsg != null) {
            request.setAttribute("error", errorMsg);
            session.removeAttribute("error");
        }
        
        String successMsg = (String) session.getAttribute("successMessage");
        if (successMsg != null) {
            request.setAttribute("successMessage", successMsg);
            session.removeAttribute("successMessage");
        }

        if (session == null || session.getAttribute("currentUser") == null) {
            redirect(response, request.getContextPath() + "/login");
            return;
        }

        Employee currentUser = (Employee) session.getAttribute("currentUser");
        String role = currentUser.getRoleName() != null ? currentUser.getRoleName().toLowerCase() : "";

        if (!role.equals("owner") && !role.equals("warehousestaff") && !role.equals("storemanager")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này.");
            return;
        }

        // Determine accessible warehouse(s)
        int branchId = currentUser.getBranchId() != null ? currentUser.getBranchId() : 0;
        Integer selectedWarehouseId = null;

        try {
            List<Warehouse> myWarehouses = warehouseDAO.findByBranch(branchId);

            List<Warehouse> allowedWarehouses;
            // Global access for Owner
            if (role.equals("owner")) {
                allowedWarehouses = warehouseDAO.findAll();
            } else {
                allowedWarehouses = myWarehouses; // Only own warehouses
                if (allowedWarehouses.isEmpty()) {
                    forward(request, response, "inventory/setup_warehouse");
                    return;
                }
            }
            request.setAttribute("warehouses", allowedWarehouses);
            
            List<Integer> allowedWarehouseIds = new ArrayList<>();
            for (Warehouse w : allowedWarehouses) {
                allowedWarehouseIds.add(w.getWarehouseId());
            }

            String clearSelected = request.getParameter("clearSelected");
            if ("true".equals(clearSelected)) {
                session.removeAttribute("selectedWarehouseId");
            }

            String filterWarehouse = request.getParameter("warehouseId");
            if (filterWarehouse != null && !filterWarehouse.isEmpty()) {
                selectedWarehouseId = Integer.parseInt(filterWarehouse);
                session.setAttribute("selectedWarehouseId", selectedWarehouseId);
            } else {
                if (role.equals("owner") || "true".equals(clearSelected)) {
                    selectedWarehouseId = null;
                    session.removeAttribute("selectedWarehouseId");
                } else {
                    Integer sessionWarehouseId = (Integer) session.getAttribute("selectedWarehouseId");
                    if (sessionWarehouseId != null && allowedWarehouseIds.contains(sessionWarehouseId)) {
                        selectedWarehouseId = sessionWarehouseId;
                    } else if ((role.equals("warehousestaff") || role.equals("storemanager")) && !allowedWarehouses.isEmpty()) {
                        selectedWarehouseId = allowedWarehouses.get(0).getWarehouseId();
                        session.setAttribute("selectedWarehouseId", selectedWarehouseId);
                    } else if (allowedWarehouses.size() == 1) {
                        selectedWarehouseId = allowedWarehouses.get(0).getWarehouseId();
                        session.setAttribute("selectedWarehouseId", selectedWarehouseId);
                    }
                }
            }

            String action = request.getParameter("action");
            if ("searchProductsApi".equals(action)) {
                handleSearchProductsApi(request, response, selectedWarehouseId);
                return;
            } else if ("searchAllProductsApi".equals(action)) {
                handleSearchAllProductsApi(request, response);
                return;
            } else if ("getProductStockApi".equals(action)) {
                int pId = Integer.parseInt(request.getParameter("productId"));
                int wId = Integer.parseInt(request.getParameter("warehouseId"));
                int stock = 0;
                try (java.sql.Connection conn = util.database.DBContext.getConnection()) {
                    stock = inventoryDAO.getStockInTransaction(conn, pId, wId);
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
                sendJsonResponse(response, "{\"stock\":" + stock + "}");
                return;
            } else if ("searchImportProductsApi".equals(action)) {
                handleSearchImportProductsApi(request, response);
                return;
            } else if ("getImportTemplateDataApi".equals(action)) {
                handleGetImportTemplateDataApi(request, response);
                return;
            } else if ("viewTicket".equals(action)) {
                int ticketId = Integer.parseInt(request.getParameter("ticketId"));
                String showAll = request.getParameter("showAll");
                model.StockTransfer transfer = transferDAO.findById(ticketId);
                if (transfer != null) {
                    List<model.StockTransfer> sub = transferDAO.findSubTransfersByCode(transfer.getTransferCode());
                    transfer.setSubTransfers(sub);
                    transfer.setDisplayStatus(transferDAO.calculateDisplayStatus(sub));
                    request.setAttribute("ticket", transfer);
                    request.setAttribute("subTransfers", sub);
                    request.setAttribute("showAll", "true".equals(showAll));
                }
                
                forward(request, response, "inventory/_modal_ticket_details");
                return;
            } else if ("viewOrderDetails".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                model.Order order = orderDAO.findById(orderId);
                List<model.OrderDetail> details = orderDAO.findDetailsByOrderId(orderId);
                List<model.StockTransaction> txs = transactionDAO.findByReference("PURCHASE_ORDER", orderId);
                
                request.setAttribute("order", order);
                request.setAttribute("orderDetails", details);
                request.setAttribute("transactions", txs);
                
                forward(request, response, "inventory/_modal_order_details");
                return;
            } else if ("printTicket".equals(action)) {
                int ticketId = Integer.parseInt(request.getParameter("ticketId"));
                model.StockTransfer transfer = transferDAO.findById(ticketId);
                List<model.StockTransfer> sub = new ArrayList<>();
                if (transfer != null) {
                    sub = transferDAO.findSubTransfersByCode(transfer.getTransferCode());
                    transfer.setSubTransfers(sub);
                    transfer.setDisplayStatus(transferDAO.calculateDisplayStatus(sub));
                }
                List<model.StockTransaction> txs = transactionDAO.findByReference("STOCK_TRANSFER", ticketId);
                
                request.setAttribute("ticket", transfer);
                request.setAttribute("subTransfers", sub);
                request.setAttribute("transactions", txs);
                
                forward(request, response, "inventory/_print_ticket");
                return;
            } else if ("printOrder".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                model.Order order = orderDAO.findById(orderId);
                List<model.OrderDetail> details = orderDAO.findDetailsByOrderId(orderId);
                List<model.StockTransaction> txs = transactionDAO.findByReference("PURCHASE_ORDER", orderId);
                
                request.setAttribute("order", order);
                request.setAttribute("orderDetails", details);
                request.setAttribute("transactions", txs);
                
                forward(request, response, "inventory/_print_order");
                return;
            } else if ("searchStockCheckProductsApi".equals(action)) {
                handleSearchStockCheckProductsApi(request, response, selectedWarehouseId);
                return;
            } else if ("viewCheckDetails".equals(action)) {
                int checkId = Integer.parseInt(request.getParameter("checkId"));
                dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                model.InventoryCheck check = checkDAO.findById(checkId);
                List<model.InventoryCheckDetail> details = checkDAO.getCheckDetails(checkId);
                
                request.setAttribute("check", check);
                request.setAttribute("checkDetails", details);
                
                forward(request, response, "inventory/_modal_check_details");
                return;
            }

            // Calculate KPIs
            dao.inventory.InventoryDAO.DashboardKPI kpi = inventoryDAO.getDashboardKPI(allowedWarehouseIds, selectedWarehouseId);
            request.setAttribute("totalProducts", kpi.totalProducts);
            request.setAttribute("totalCategories", kpi.totalCategories);
            request.setAttribute("lowStockCount", kpi.lowStockCount);

            int pendingTransferCount = transferDAO.getPendingCount(selectedWarehouseId != null ? selectedWarehouseId : 0);
            request.setAttribute("pendingTransferCount", pendingTransferCount);

            String tab = request.getParameter("tab");
            if ("createTransfer".equals(action)) {
                tab = "createTransfer";
            } else if ("createCheck".equals(action)) {
                tab = "createCheck";
            } else if ("editCheck".equals(action)) {
                tab = "editCheck";
            } else if (tab == null || tab.isEmpty()) {
                tab = "stock";
            }
            request.setAttribute("activeTab", tab);
            request.setAttribute("selectedWarehouseId", selectedWarehouseId);

            switch (tab) {
                case "stock":
                    handleStockTab(request, selectedWarehouseId);
                    break;
                case "transfer":
                    handleTransferTab(request, selectedWarehouseId, role);
                    break;
                case "check":
                    handleCheckTab(request, selectedWarehouseId, role);
                    break;
                case "approval":
                    handleApprovalTab(request, role);
                    break;
                case "history":
                    handleHistoryTab(request, selectedWarehouseId, allowedWarehouseIds);
                    break;
                case "import":
                    handleImportTab(request, selectedWarehouseId, role);
                    break;
                case "export":
                    handleExportTab(request, selectedWarehouseId, role);
                    break;
                case "pending_vouchers":
                    handlePendingVouchersTab(request, selectedWarehouseId, role);
                    break;
                case "createTransfer": {
                    String type = request.getParameter("type");
                    if (type == null) {
                        type = "RECEIVE";
                    }
                    request.setAttribute("transferType", type);
                    try {
                        List<model.Warehouse> allWarehouses = warehouseDAO.findAll();
                        request.setAttribute("allWarehouses", allWarehouses);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "createCheck":
                    break;
                case "editCheck": {
                    int checkId = Integer.parseInt(request.getParameter("checkId"));
                    dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                    model.InventoryCheck check = checkDAO.findById(checkId);
                    List<model.InventoryCheckDetail> checkDetails = checkDAO.getCheckDetails(checkId);
                    request.setAttribute("check", check);
                    request.setAttribute("checkDetails", checkDetails);
                    break;
                }
                default:
                    handleStockTab(request, selectedWarehouseId);
                    request.setAttribute("activeTab", "stock");
                    break;
            }

            forward(request, response, "inventory/inventory");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Lỗi server: " + e.getMessage());
        }
    }

    private void handleStockTab(HttpServletRequest request, Integer warehouseId) throws Exception {
        if (warehouseId == null) {
            // Dashboard mode: don't query products, skip to save DB calls.
            return;
        }

        String keyword = request.getParameter("keyword");
        if (keyword != null) {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }
        String status = request.getParameter("status");
        String sortParam = request.getParameter("sort");
        
        int page = 1;
        int limit = 20;
        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }
        int offset = (page - 1) * limit;

        List<Inventory> stockList = inventoryDAO.findAll(offset, limit, keyword, status, null, null, warehouseId, sortParam);
        attachImageUrls(request, stockList);
        int totalCount = inventoryDAO.getTotalCount(keyword, status, null, null, warehouseId);
        int totalPages = (int) Math.ceil((double) totalCount / limit);

        request.setAttribute("stockList", stockList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("keyword", keyword);
        request.setAttribute("statusFilter", status);
        request.setAttribute("sortParam", sortParam);
        
        // Setup KPI Summary manually for simplicity
        request.setAttribute("totalItems", totalCount);
        int lowStockCount = inventoryDAO.getTotalCount(keyword, "LOW_STOCK", null, null, warehouseId);
        request.setAttribute("lowStockCount", lowStockCount);

        // Fetch Suppliers for Import Modal
        List<Supplier> suppliers = supplierDAO.getSuppliersPaging("", "active", 1, 1000);
        request.setAttribute("suppliers", suppliers);
    }

    private void handleTransferTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        String subtab = request.getParameter("subtab");
        if (subtab == null || subtab.isEmpty()) {
            subtab = "transfer_list";
        }
        request.setAttribute("activeSubtab", subtab);

        // Filters
        String transferCodeQuery = request.getParameter("transferCodeQuery");
        String partnerWQueryStr = request.getParameter("partnerWarehouseQuery");
        String statusQuery = request.getParameter("statusQuery");

        Integer partnerWarehouseQuery = null;
        if (partnerWQueryStr != null && !partnerWQueryStr.trim().isEmpty()) {
            try {
                partnerWarehouseQuery = Integer.parseInt(partnerWQueryStr);
            } catch (NumberFormatException e) {
                // Ignore invalid format
            }
        }

        // Clean & validate text filters
        if (transferCodeQuery != null) {
            transferCodeQuery = transferCodeQuery.trim();
        }

        List<StockTransfer> transfers;
        if ("transfer_process".equals(subtab)) {
            transfers = transferDAO.findAllByStatusFiltered(
                warehouseId != null ? warehouseId : 0,
                null,
                transferCodeQuery,
                partnerWarehouseQuery
            );
        } else {
            transfers = transferDAO.findAllGrouped(
                warehouseId != null ? warehouseId : 0,
                statusQuery,
                transferCodeQuery,
                partnerWarehouseQuery
            );
        }
        request.setAttribute("transfers", transfers);
        request.setAttribute("transferCodeQuery", transferCodeQuery);
        request.setAttribute("partnerWarehouseQuery", partnerWarehouseQuery);
        request.setAttribute("statusQuery", statusQuery);
    }

    private void handleImportTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        List<PurchaseOrder> imports = purchaseOrderDAO.findAllByWarehouseAndType(warehouseId != null ? warehouseId : 0, "PURCHASE", null);
        if ("WarehouseStaff".equalsIgnoreCase(role)) {
            imports.removeIf(po -> !"PENDING".equals(po.getStatus()));
        }
        request.setAttribute("imports", imports);
    }

    private void handleExportTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        List<PurchaseOrder> exports = purchaseOrderDAO.findAllByWarehouseAndType(warehouseId != null ? warehouseId : 0, "EXPORT", null);
        if ("WarehouseStaff".equalsIgnoreCase(role)) {
            exports.removeIf(po -> !"PENDING".equals(po.getStatus()));
        }
        request.setAttribute("exports", exports);
    }

    private void handlePendingVouchersTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
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

    private void handleCheckTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        String subtab = "inventory_check";
        request.setAttribute("activeSubtab", subtab);

        String checkCodeQuery = request.getParameter("checkCodeQuery");
        String statusQuery = request.getParameter("statusQuery");
        String discrepancyQuery = request.getParameter("discrepancyQuery");

        dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
        List<model.InventoryCheck> checks = checkDAO.findAllByWarehouseFiltered(
            warehouseId != null ? warehouseId : 0,
            checkCodeQuery,
            statusQuery,
            discrepancyQuery
        );
        request.setAttribute("checks", checks);
    }

    private void handleApprovalTab(HttpServletRequest request, String role) throws Exception {
        if (!"Owner".equalsIgnoreCase(role) && !"StoreManager".equalsIgnoreCase(role)) {
            request.setAttribute("error", "Bạn không có quyền truy cập tab này.");
            return;
        }

        String transferCodeQuery = request.getParameter("transferCodeQuery");
        String fromWarehouseQueryStr = request.getParameter("fromWarehouseQuery");
        String toWarehouseQueryStr = request.getParameter("toWarehouseQuery");

        Integer fromWarehouseQuery = null;
        Integer toWarehouseQuery = null;
        try {
            if (fromWarehouseQueryStr != null && !fromWarehouseQueryStr.trim().isEmpty()) {
                fromWarehouseQuery = Integer.parseInt(fromWarehouseQueryStr);
            }
            if (toWarehouseQueryStr != null && !toWarehouseQueryStr.trim().isEmpty()) {
                toWarehouseQuery = Integer.parseInt(toWarehouseQueryStr);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        if (transferCodeQuery != null) {
            transferCodeQuery = transferCodeQuery.trim();
        }

        List<model.StockTransfer> pendingTransfers = transferDAO.findPendingTransfersFiltered(
            transferCodeQuery,
            fromWarehouseQuery,
            toWarehouseQuery
        );
        
        Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
        int branchId = currentUser != null && currentUser.getBranchId() != null ? currentUser.getBranchId() : 0;
        List<model.Order> pendingOrders = orderDAO.getPendingInventoryOrders(branchId);
        
        List<model.InventoryCheck> pendingChecks = null;
        try {
            dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
            pendingChecks = checkDAO.findAllByWarehouseFiltered(0, null, "PENDING", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Build unified list of pending approvals
        List<Map<String, Object>> unifiedApprovals = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        java.text.SimpleDateFormat inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (pendingTransfers != null) {
            Integer selectedWarehouseId = (Integer) request.getSession().getAttribute("selectedWarehouseId");
            boolean isSystemOwner = "Owner".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);
            
            // Group pendingTransfers by transfer_code
            Map<String, List<model.StockTransfer>> groupedByCode = new java.util.LinkedHashMap<>();
            for (model.StockTransfer item : pendingTransfers) {
                groupedByCode.computeIfAbsent(item.getTransferCode(), k -> new ArrayList<>()).add(item);
            }
            
            for (Map.Entry<String, List<model.StockTransfer>> entry : groupedByCode.entrySet()) {
                String transferCode = entry.getKey();
                List<model.StockTransfer> subList = entry.getValue();
                if (subList.isEmpty()) continue;
                
                model.StockTransfer representative = subList.get(0);
                String status = representative.getStatus();
                
                if (isSystemOwner) {
                    // Owner/Admin view: 1 row per transfer_code
                    java.util.Set<String> partners = new java.util.LinkedHashSet<>();
                    String creatorWName = "";
                    for (model.StockTransfer item : subList) {
                        boolean fromIsCreator = (item.getCreatorBranchId() == item.getFromBranchId());
                        if (fromIsCreator) {
                            creatorWName = item.getFromWarehouseName();
                            partners.add(item.getToWarehouseName());
                        } else {
                            creatorWName = item.getToWarehouseName();
                            partners.add(item.getFromWarehouseName());
                        }
                    }
                    
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", representative.getStockTransferId());
                    map.put("code", transferCode);
                    map.put("type", "TRANSFER");
                    map.put("typeLabel", "Điều chuyển");
                    map.put("createdBy", representative.getCreatedByName());
                    map.put("createdAt", representative.getTransferDate() != null ? sdf.format(representative.getTransferDate()) : "");
                    map.put("amount", null);
                    map.put("idParamName", "transferId");
                    map.put("detailCallback", "viewTicketDetails(" + representative.getStockTransferId() + ", true)");
                    map.put("rawDate", representative.getTransferDate() != null ? representative.getTransferDate() : new java.util.Date(0));
                    map.put("warehouseName", creatorWName + " ➔ " + String.join(", ", partners));
                    
                    if ("PENDING_OWNER".equals(status)) {
                        map.put("actionApprove", "approveTransfer");
                        map.put("actionReject", "rejectTransfer");
                        unifiedApprovals.add(map);
                    } else if ("PENDING_PARTNER".equals(status)) {
                        // Approving on behalf of partner warehouses must be done inside the ticket details modal,
                        // so we add it to the list without quick actions.
                        unifiedApprovals.add(map);
                    } else if ("PENDING_DISPATCH".equals(status)) {
                        map.put("actionApprove", "approveTransfer");
                        map.put("actionReject", "rejectTransfer");
                        unifiedApprovals.add(map);
                    }
                } else {
                    // StoreManager view: 1 row per partner relation involving selectedWarehouseId
                    if ("PENDING_OWNER".equals(status)) {
                        int creatorBranchId = representative.getCreatorBranchId();
                        int currentBranchId = 0;
                        if (selectedWarehouseId != null) {
                            for (model.StockTransfer item : subList) {
                                if (selectedWarehouseId == item.getFromWarehouseId()) {
                                    currentBranchId = item.getFromBranchId();
                                    break;
                                } else if (selectedWarehouseId == item.getToWarehouseId()) {
                                    currentBranchId = item.getToBranchId();
                                    break;
                                }
                            }
                        }
                        
                        if (selectedWarehouseId != null && currentBranchId == creatorBranchId) {
                            java.util.Set<String> partners = new java.util.LinkedHashSet<>();
                            String creatorWName = "";
                            for (model.StockTransfer item : subList) {
                                boolean fromIsCreator = (item.getCreatorBranchId() == item.getFromBranchId());
                                if (fromIsCreator) {
                                    creatorWName = item.getFromWarehouseName();
                                    partners.add(item.getToWarehouseName());
                                } else {
                                    creatorWName = item.getToWarehouseName();
                                    partners.add(item.getFromWarehouseName());
                                }
                            }
                            
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", representative.getStockTransferId());
                            map.put("code", transferCode);
                            map.put("type", "TRANSFER");
                            map.put("typeLabel", "Điều chuyển");
                            map.put("createdBy", representative.getCreatedByName());
                            map.put("createdAt", representative.getTransferDate() != null ? sdf.format(representative.getTransferDate()) : "");
                            map.put("amount", null);
                            map.put("idParamName", "transferId");
                            map.put("detailCallback", "viewTicketDetails(" + representative.getStockTransferId() + ")");
                            map.put("rawDate", representative.getTransferDate() != null ? representative.getTransferDate() : new java.util.Date(0));
                            map.put("warehouseName", creatorWName + " ➔ " + String.join(", ", partners));
                            map.put("actionApprove", "approveTransfer");
                            map.put("actionReject", "rejectTransfer");
                            unifiedApprovals.add(map);
                        }
                    } else if ("PENDING_PARTNER".equals(status)) {
                        List<model.StockTransfer> partnerSubList = new ArrayList<>();
                        for (model.StockTransfer item : subList) {
                            if (selectedWarehouseId != null && (item.getFromWarehouseId() == selectedWarehouseId || item.getToWarehouseId() == selectedWarehouseId)) {
                                partnerSubList.add(item);
                            }
                        }
                        
                        if (!partnerSubList.isEmpty()) {
                            model.StockTransfer partnerRep = partnerSubList.get(0);
                            int currentBranchId = (selectedWarehouseId == partnerRep.getFromWarehouseId()) ? partnerRep.getFromBranchId() : partnerRep.getToBranchId();
                            boolean fromIsCreator = (partnerRep.getCreatorBranchId() == partnerRep.getFromBranchId());
                            int partnerBranchId = fromIsCreator ? partnerRep.getToBranchId() : partnerRep.getFromBranchId();
                            
                            if (currentBranchId == partnerBranchId) {
                                String creatorWName = fromIsCreator ? partnerRep.getFromWarehouseName() : partnerRep.getToWarehouseName();
                                String partnerWName = fromIsCreator ? partnerRep.getToWarehouseName() : partnerRep.getFromWarehouseName();
                                
                                Map<String, Object> map = new HashMap<>();
                                map.put("id", partnerRep.getStockTransferId());
                                map.put("code", transferCode);
                                map.put("type", "TRANSFER");
                                map.put("typeLabel", "Điều chuyển");
                                map.put("createdBy", partnerRep.getCreatedByName());
                                map.put("createdAt", partnerRep.getTransferDate() != null ? sdf.format(partnerRep.getTransferDate()) : "");
                                map.put("amount", null);
                                map.put("idParamName", "transferId");
                                map.put("detailCallback", "viewTicketDetails(" + partnerRep.getStockTransferId() + ")");
                                map.put("rawDate", partnerRep.getTransferDate() != null ? partnerRep.getTransferDate() : new java.util.Date(0));
                                map.put("warehouseName", creatorWName + " ➔ " + partnerWName);
                                map.put("actionApprove", "partnerApproveTransfer");
                                map.put("actionReject", "partnerRejectTransfer");
                                unifiedApprovals.add(map);
                            }
                        }
                    } else if ("PENDING_DISPATCH".equals(status)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", representative.getStockTransferId());
                        map.put("code", transferCode);
                        map.put("type", "TRANSFER");
                        map.put("typeLabel", "Điều chuyển");
                        map.put("createdBy", representative.getCreatedByName());
                        map.put("createdAt", representative.getTransferDate() != null ? sdf.format(representative.getTransferDate()) : "");
                        map.put("amount", null);
                        map.put("idParamName", "transferId");
                        map.put("detailCallback", "viewTicketDetails(" + representative.getStockTransferId() + ")");
                        map.put("rawDate", representative.getTransferDate() != null ? representative.getTransferDate() : new java.util.Date(0));
                        map.put("warehouseName", representative.getFromWarehouseName() + " ➔ " + representative.getToWarehouseName());
                        map.put("actionApprove", "approveTransfer");
                        map.put("actionReject", "rejectTransfer");
                        unifiedApprovals.add(map);
                    }
                }
            }
        }

        if (pendingOrders != null) {
            for (model.Order order : pendingOrders) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", order.getOrderId());
                map.put("code", order.getOrderCode());
                map.put("type", order.getOrderType());
                map.put("typeLabel", "PURCHASE".equalsIgnoreCase(order.getOrderType()) ? "Nhập" : "Xuất");
                map.put("warehouseName", order.getCustomerName()); // customerName stores warehouseName
                map.put("createdBy", order.getEmployeeName());
                
                String dateStr = order.getCreatedAt();
                java.util.Date parsedDate = null;
                if (dateStr != null) {
                    try {
                        parsedDate = inputSdf.parse(dateStr);
                    } catch (Exception e) {
                        // ignore
                    }
                }
                map.put("createdAt", parsedDate != null ? sdf.format(parsedDate) : dateStr);
                map.put("amount", order.getTotalAmount());
                map.put("actionApprove", "approveOrder");
                map.put("actionReject", "rejectOrder");
                map.put("idParamName", "orderId");
                map.put("detailCallback", "viewOrderDetails(" + order.getOrderId() + ")");
                map.put("rawDate", parsedDate != null ? parsedDate : new java.util.Date(0));
                unifiedApprovals.add(map);
            }
        }
        
        if (pendingChecks != null) {
            for (model.InventoryCheck check : pendingChecks) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", check.getCheckId());
                map.put("code", check.getCheckCode());
                map.put("type", "CHECK");
                map.put("typeLabel", "Kiểm Kho");
                map.put("warehouseName", check.getWarehouseName());
                map.put("createdBy", check.getCreatedByName());
                
                java.util.Date parsedDate = null;
                if (check.getCreatedAt() != null) {
                    try {
                        parsedDate = java.sql.Timestamp.valueOf(check.getCreatedAt());
                    } catch (Exception e) {
                        // ignore
                    }
                }
                map.put("createdAt", check.getFormattedCreatedAt());
                map.put("amount", null);
                map.put("actionApprove", "approveCheck");
                map.put("actionReject", "rejectCheck");
                map.put("idParamName", "checkId");
                map.put("detailCallback", "viewCheckDetails(" + check.getCheckId() + ")");
                map.put("rawDate", parsedDate != null ? parsedDate : new java.util.Date(0));
                unifiedApprovals.add(map);
            }
        }

        // Sort by rawDate descending
        unifiedApprovals.sort((m1, m2) -> ((java.util.Date) m2.get("rawDate")).compareTo((java.util.Date) m1.get("rawDate")));

        request.setAttribute("pendingTransfers", pendingTransfers);
        request.setAttribute("pendingOrders", pendingOrders);
        request.setAttribute("unifiedApprovals", unifiedApprovals);
        request.setAttribute("transferCodeQuery", transferCodeQuery);
        request.setAttribute("fromWarehouseQuery", fromWarehouseQuery);
        request.setAttribute("toWarehouseQuery", toWarehouseQuery);
    }

    private void handleHistoryTab(HttpServletRequest request, Integer warehouseId, List<Integer> allowedWarehouseIds) throws Exception {
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            doGet(request, response);
            return;
        }

        try {
            switch (action) {
                case "checkImportExcel": {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    String data = request.getParameter("data");
                    int warehouseId = Integer.parseInt(request.getParameter("warehouseId"));
                    dao.inventory.InventoryDAO dao = new dao.inventory.InventoryDAO();

                    List<String> errors = new ArrayList<>();
                    List<String> rowJsons = new ArrayList<>();

                    if (data != null && !data.trim().isEmpty()) {
                        String[] lines = data.split("\\|");
                        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
                            String line = lines[lineIdx].trim();
                            if (line.isEmpty()) continue;
                            String[] parts = line.split("\\t", 3);
                            int excelRow = lineIdx + 2; // Excel row number (header=1, data starts at 2)

                            if (parts.length < 3) {
                                errors.add("Dòng " + excelRow + ": Thiếu dữ liệu (cần Tên sản phẩm, Mã NCC, Số lượng).");
                                continue;
                            }

                            String productName = parts[0].trim();
                            String supplierIdStr = parts[1].trim();
                            String quantityStr = parts[2].trim();

                            // Product name empty → skip (can't recover)
                            if (productName.isEmpty()) {
                                errors.add("Dòng " + excelRow + ": Tên sản phẩm trống.");
                                continue;
                            }

                            // Lookup product by exact name first
                            dto.inventory.ImportProductDTO product = dao.getImportProductByName(warehouseId, productName);
                            if (product == null) {
                                errors.add("Dòng " + excelRow + ": Không tìm thấy sản phẩm '" + productName + "'.");
                                continue;
                            }

                            // --- From here, product exists → row ALWAYS goes into table ---
                            boolean isErrorRow = false;
                            List<String> rowErrors = new ArrayList<>();

                            // Validate supplierId
                            int supplierId = -1;
                            boolean supplierIdValid = false;
                            try {
                                supplierId = Integer.parseInt(supplierIdStr);
                                if (supplierId <= 0) throw new NumberFormatException();
                                supplierIdValid = true;
                            } catch (NumberFormatException e) {
                                isErrorRow = true;
                                rowErrors.add("Mã NCC '" + supplierIdStr + "' không hợp lệ");
                            }

                            // Validate quantity
                            int quantity = 1;
                            boolean quantityValid = false;
                            try {
                                quantity = Integer.parseInt(quantityStr);
                                if (quantity <= 0) throw new NumberFormatException();
                                quantityValid = true;
                            } catch (NumberFormatException e) {
                                isErrorRow = true;
                                quantity = 1; // default
                                rowErrors.add("Số lượng '" + quantityStr + "' không hợp lệ");
                            }

                            // Check supplier linkage
                            dto.inventory.ImportProductDTO.SupplierInfo matchedSupplier = null;
                            boolean supplierLinked = false;
                            if (supplierIdValid) {
                                boolean supplierExists = false;
                                for (dto.inventory.ImportProductDTO.SupplierInfo si : product.getSuppliers()) {
                                    if (si.getSupplierId() == supplierId) {
                                        supplierExists = true;
                                        matchedSupplier = si;
                                        if (si.getImportPrice() != null && si.getImportPrice().doubleValue() > 0) {
                                            supplierLinked = true;
                                        }
                                        break;
                                    }
                                }
                                if (!supplierExists) {
                                    isErrorRow = true;
                                    String sName = dao.getSupplierName(supplierId);
                                    if (sName == null) {
                                        rowErrors.add("Mã NCC " + supplierId + " không tồn tại");
                                    } else {
                                        rowErrors.add("NCC '" + sName + "' (ID:" + supplierId + ") không hoạt động");
                                    }
                                } else if (!supplierLinked) {
                                    isErrorRow = true;
                                    rowErrors.add("Nhà cung cấp không có sản phẩm.");
                                }
                            }

                            // Use first supplier as fallback if supplier ID invalid or not found
                            if (matchedSupplier == null && !product.getSuppliers().isEmpty()) {
                                matchedSupplier = product.getSuppliers().get(0);
                            }

                            double price = (matchedSupplier != null && matchedSupplier.getImportPrice() != null && supplierLinked) 
                                ? matchedSupplier.getImportPrice().doubleValue() : 0;
                            String rowErrorMsg = String.join("; ", rowErrors);

                            // Build row JSON
                            StringBuilder rowJson = new StringBuilder("{");
                            rowJson.append("\"product\":{");
                            rowJson.append("\"productId\":").append(product.getProductId()).append(",");
                            rowJson.append("\"productName\":\"").append(escapeJson(product.getProductName())).append("\",");
                            rowJson.append("\"myStock\":").append(product.getMyStock());
                            rowJson.append("},");
                            rowJson.append("\"supplier\":{");
                            if (matchedSupplier != null) {
                                rowJson.append("\"supplierId\":").append(matchedSupplier.getSupplierId()).append(",");
                                rowJson.append("\"supplierName\":\"").append(escapeJson(matchedSupplier.getSupplierName())).append("\",");
                                rowJson.append("\"importPrice\":").append(price);
                            } else {
                                rowJson.append("\"supplierId\":0,\"supplierName\":\"\",\"importPrice\":0");
                            }
                            rowJson.append("},");
                            // All active suppliers for the dropdown
                            rowJson.append("\"allSuppliers\":[");
                            for (int s = 0; s < product.getSuppliers().size(); s++) {
                                dto.inventory.ImportProductDTO.SupplierInfo si = product.getSuppliers().get(s);
                                rowJson.append("{\"supplierId\":").append(si.getSupplierId());
                                rowJson.append(",\"supplierName\":\"").append(escapeJson(si.getSupplierName())).append("\"");
                                rowJson.append(",\"importPrice\":").append(si.getImportPrice() != null ? si.getImportPrice() : 0);
                                rowJson.append("}");
                                if (s < product.getSuppliers().size() - 1) rowJson.append(",");
                            }
                            rowJson.append("],");
                            rowJson.append("\"price\":").append(price).append(",");
                            rowJson.append("\"quantity\":").append(quantity).append(",");
                            rowJson.append("\"isErrorRow\":").append(isErrorRow).append(",");
                            rowJson.append("\"rowError\":\"").append(escapeJson(rowErrorMsg)).append("\"");
                            rowJson.append("}");

                            rowJsons.add(rowJson.toString());
                        }
                    }

                    // Build final response JSON
                    StringBuilder result = new StringBuilder("{");
                    result.append("\"success\":true,");
                    result.append("\"errors\":[");
                    for (int i = 0; i < errors.size(); i++) {
                        result.append("\"").append(escapeJson(errors.get(i))).append("\"");
                        if (i < errors.size() - 1) result.append(",");
                    }
                    result.append("],");
                    result.append("\"rows\":[");
                    for (int i = 0; i < rowJsons.size(); i++) {
                        result.append(rowJsons.get(i));
                        if (i < rowJsons.size() - 1) result.append(",");
                    }
                    result.append("]");
                    result.append("}");

                    response.getWriter().write(result.toString());
                    return;
                }
                case "setupWarehouse": {

                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    Warehouse newW = new Warehouse();
                    newW.setWarehouseName(request.getParameter("warehouseName"));
                    newW.setAddress(request.getParameter("address"));
                    newW.setBranchId(currentUser.getBranchId() != null ? currentUser.getBranchId() : 0);
                    warehouseDAO.createWarehouse(newW);
                    request.getSession().setAttribute("message", "Khởi tạo kho đầu tiên thành công!");
                    redirect(response, request.getContextPath() + "/inventory");
                    break;
                }
                case "updateWarehouse": {
                    Warehouse updateW = new Warehouse();
                    int wId = Integer.parseInt(request.getParameter("warehouseId"));
                    updateW.setWarehouseId(wId);
                    updateW.setWarehouseName(request.getParameter("warehouseName"));
                    updateW.setAddress(request.getParameter("address"));
                    warehouseDAO.updateWarehouse(updateW);
                    request.getSession().setAttribute("message", "Cập nhật thông tin kho thành công!");
                    redirect(response, request.getContextPath() + "/inventory?warehouseId=" + wId);
                    break;
                }
                case "saveTransfer": {
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] partnerWarehouseIds = request.getParameterValues("partnerWarehouseId[]");
                    String[] quantities = request.getParameterValues("quantity[]");
                    String[] actionTypes = request.getParameterValues("actionType[]");
                    
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    boolean isOwner = "Owner".equals(currentUser.getRoleName());
                    
                    if (productIds != null && productIds.length > 0) {
                        java.util.Map<String, List<model.StockTransferDetail>> groupedDetails = new java.util.HashMap<>();
                        java.util.Map<String, int[]> groupInfo = new java.util.HashMap<>();
                        
                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int partnerWId = Integer.parseInt(partnerWarehouseIds[i]);
                            int qty = Integer.parseInt(quantities[i]);
                            String actType = (actionTypes != null && actionTypes.length > i) ? actionTypes[i] : "RECEIVE";
                            
                            if (qty > 0) {
                                int fromWId = "RECEIVE".equals(actType) ? partnerWId : currentWarehouseId;
                                int toWId = "RECEIVE".equals(actType) ? currentWarehouseId : partnerWId;
                                String key = fromWId + "_" + toWId;
                                
                                model.StockTransferDetail detail = new model.StockTransferDetail();
                                detail.setProductId(pId);
                                detail.setQuantity(qty);
                                
                                groupedDetails.computeIfAbsent(key, k -> new ArrayList<>()).add(detail);
                                groupInfo.put(key, new int[]{fromWId, toWId});
                            }
                        }
                        
                        String transferCode = "TO-" + System.currentTimeMillis();
                        List<model.StockTransfer> transferList = new ArrayList<>();
                        for (java.util.Map.Entry<String, List<model.StockTransferDetail>> entry : groupedDetails.entrySet()) {
                            String key = entry.getKey();
                            List<model.StockTransferDetail> details = entry.getValue();
                            int[] info = groupInfo.get(key);
                            int fromWId = info[0];
                            int toWId = info[1];
                            
                            model.StockTransfer transfer = new model.StockTransfer();
                            transfer.setTransferCode(transferCode);
                            transfer.setFromWarehouseId(fromWId);
                            transfer.setToWarehouseId(toWId);
                            // Owner tạo -> tự động duyệt (PENDING_PARTNER), nhân viên tạo -> PENDING_OWNER
                            transfer.setStatus(isOwner ? "PENDING_PARTNER" : "PENDING_OWNER");
                            transfer.setCreatedBy(currentUser.getEmployeeId());
                            transfer.setDetails(details);
                            transferList.add(transfer);
                        }
                        transferDAO.createTransfers(transferList);
                    }

                    if (isOwner) {
                        request.getSession().setAttribute("message", "Đã tạo phiếu điều chuyển (Đã tự động duyệt, đang chờ các kho đối tác duyệt).");
                    } else {
                        request.getSession().setAttribute("message", "Đã tạo phiếu điều chuyển (Chờ duyệt).");
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "saveImport": {
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    String note = request.getParameter("note");
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] quantities = request.getParameterValues("quantity[]");
                    String[] supplierIds = request.getParameterValues("supplierId[]");
                    String[] importPrices = request.getParameterValues("importPrice[]");
                    
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    boolean isOwner = "Owner".equals(currentUser.getRoleName()) || "StoreManager".equals(currentUser.getRoleName());
                    
                    if (productIds != null && productIds.length > 0) {
                        List<model.OrderDetail> allDetails = new ArrayList<>();
                        double totalCost = 0.0;
                        
                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int qty = Integer.parseInt(quantities[i]);
                            int sId = Integer.parseInt(supplierIds[i]);
                            double price = 0.0;
                            if (importPrices != null && importPrices.length > i) {
                                try { price = Double.parseDouble(importPrices[i]); } catch (Exception e) {}
                            }
                            if (qty > 0) {
                                model.OrderDetail detail = new model.OrderDetail();
                                detail.setProductId(pId);
                                detail.setQuantity(qty);
                                detail.setUnitPrice(price);
                                detail.setTotalPrice(qty * price);
                                detail.setImportPrice(price);
                                detail.setSupplierId(sId);
                                detail.setSupplierStatus("PENDING");
                                allDetails.add(detail);
                                totalCost += qty * price;
                            }
                        }
                        
                        // Tạo DUY NHẤT 1 Order cho tất cả NCC
                        model.Order purchaseOrder = new model.Order();
                        purchaseOrder.setOrderCode("PO-" + System.currentTimeMillis());
                        purchaseOrder.setOrderType("PURCHASE");
                        purchaseOrder.setSupplierId(null); // Nhiều NCC -> không gán 1 NCC cụ thể
                        purchaseOrder.setEmpId(currentUser.getEmployeeId());
                        purchaseOrder.setBranchId(currentUser.getBranchId() != null ? currentUser.getBranchId() : 1);
                        purchaseOrder.setWarehouseId(currentWarehouseId);
                        purchaseOrder.setSubtotal(totalCost);
                        purchaseOrder.setDiscountAmount(0.0);
                        purchaseOrder.setTotalAmount(totalCost);
                        purchaseOrder.setPaymentMethod("BANK_TRANSFER");
                        // Owner tạo -> PENDING (chờ NCC xác nhận), Staff tạo -> PENDING (chờ Owner duyệt)
                        purchaseOrder.setStatus(model.Order.OrderStatus.PENDING);
                        
                        try (java.sql.Connection conn = util.database.DBContext.getConnection()) {
                            conn.setAutoCommit(false);
                            try {
                                int orderId = orderDAO.createOrderInTransaction(conn, purchaseOrder);
                                dao.sales.OrderDetailDAO detailDao = new dao.sales.OrderDetailDAO();
                                detailDao.insertBatchPurchase(conn, orderId, allDetails);
                                
                                // Owner tạo -> tự động duyệt và cộng tồn kho luôn
                                if (isOwner) {
                                    orderDAO.updateStatus(conn, orderId, "COMPLETED");
                                    dao.inventory.InventoryDAO invDAO = new dao.inventory.InventoryDAO();
                                    for (model.OrderDetail d : allDetails) {
                                        int beforeQty = invDAO.getStockInTransaction(conn, d.getProductId(), currentWarehouseId);
                                        invDAO.increaseStock(conn, currentWarehouseId, d.getProductId(), d.getQuantity());
                                        invDAO.logCustomStockTransaction(conn, currentWarehouseId, d.getProductId(),
                                                "PURCHASE_ORDER", orderId, "IMPORT",
                                                d.getQuantity(), beforeQty, beforeQty + d.getQuantity(),
                                                "Nhập hàng từ phiếu PO-" + orderId, currentUser.getEmployeeId());
                                    }
                                }
                                
                                conn.commit();
                            } catch (Exception ex) {
                                conn.rollback();
                                throw ex;
                            }
                        }
                    }

                    if (isOwner) {
                        request.getSession().setAttribute("message", "Đã nhập hàng và cập nhật tồn kho thành công!");
                    } else {
                        request.getSession().setAttribute("message", "Đã tạo phiếu nhập hàng (Chờ duyệt).");
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=stock&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "saveExport": {
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    String note = request.getParameter("note");
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] quantities = request.getParameterValues("quantity[]");
                    String[] importPrices = request.getParameterValues("importPrice[]"); // Used for value estimation
                    
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    if (productIds != null && productIds.length > 0) {
                        List<model.OrderDetail> exportDetails = new ArrayList<>();
                        double totalValue = 0.0;
                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int qty = Integer.parseInt(quantities[i]);
                            double price = 0.0;
                            if (importPrices != null && importPrices.length > i) {
                                try { price = Double.parseDouble(importPrices[i]); } catch (Exception e) {}
                            }
                            if (qty > 0) {
                                model.OrderDetail detail = new model.OrderDetail();
                                detail.setProductId(pId);
                                detail.setQuantity(qty);
                                detail.setUnitPrice(price);
                                detail.setTotalPrice(qty * price);
                                detail.setImportPrice(price);
                                exportDetails.add(detail);
                                totalValue += (qty * price);
                            }
                        }
                        
                        if (!exportDetails.isEmpty()) {
                            model.Order exportOrder = new model.Order();
                            exportOrder.setOrderCode("EX-" + System.currentTimeMillis());
                            exportOrder.setOrderType("EXPORT");
                            exportOrder.setEmpId(currentUser.getEmployeeId());
                            exportOrder.setBranchId(currentUser.getBranchId() != null ? currentUser.getBranchId() : 1);
                            exportOrder.setWarehouseId(currentWarehouseId);
                            exportOrder.setSubtotal(totalValue);
                            exportOrder.setDiscountAmount(0.0);
                            exportOrder.setTotalAmount(totalValue);
                            exportOrder.setPaymentMethod("NONE");
                            exportOrder.setStatus(model.Order.OrderStatus.PENDING); // Staff creates as PENDING
                            
                            try (java.sql.Connection conn = util.database.DBContext.getConnection()) {
                                conn.setAutoCommit(false);
                                try {
                                    int orderId = orderDAO.createOrderInTransaction(conn, exportOrder);
                                    dao.sales.OrderDetailDAO detailDao = new dao.sales.OrderDetailDAO();
                                    detailDao.insertBatchPurchase(conn, orderId, exportDetails); // Reuse method since schema is identical
                                    conn.commit();
                                } catch (Exception ex) {
                                    conn.rollback();
                                    throw ex;
                                }
                            }
                        }
                    }

                    request.getSession().setAttribute("message", "Đã tạo phiếu xuất hàng (Chờ duyệt).");
                    redirect(response, request.getContextPath() + "/inventory?tab=export&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "confirmDispatch": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    new service.inventory.TransferService().dispatchTransfer(transferId, currentUser.getEmployeeId());
                    
                    request.getSession().setAttribute("message", "Đã xuất kho thành công. Hàng đang trên đường vận chuyển.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "rejectDispatch": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    new service.inventory.TransferService().rejectDispatch(transferId, currentUser.getEmployeeId());
                    
                    request.getSession().setAttribute("message", "Đã từ chối xuất kho trung chuyển.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "confirmReceive": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    new service.inventory.TransferService().receiveTransfer(transferId, currentUser.getEmployeeId());
                    
                    request.getSession().setAttribute("message", "Đã nhập kho thành công. Phiếu điều chuyển hoàn tất.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "rejectReceive": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    new service.inventory.TransferService().rejectReceive(transferId, currentUser.getEmployeeId());
                    
                    request.getSession().setAttribute("message", "Đã từ chối nhận kho trung chuyển.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "cancelTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        transferDAO.updateStatusByCode(st.getTransferCode(), "CANCELLED", null);
                    }
                    
                    request.getSession().setAttribute("message", "Đã hủy phiếu điều chuyển.");
                    String redirectTab = request.getParameter("tab");
                    if (redirectTab == null || redirectTab.isEmpty()) {
                        redirectTab = "transfer";
                    }
                    String whIdParam = request.getParameter("warehouseId");
                    if (whIdParam == null || whIdParam.isEmpty()) {
                        whIdParam = request.getParameter("currentWarehouseId");
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=" + redirectTab + "&warehouseId=" + whIdParam);
                    break;
                }
                case "approveTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"StoreManager".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        new service.inventory.TransferService().approveMasterTransfer(st.getTransferCode(), currentUser.getEmployeeId());
                    }
                    request.getSession().setAttribute("message", "Đã duyệt phiếu tổng điều chuyển thành công. (Chờ các đối tác duyệt)");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                case "rejectTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"StoreManager".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        new service.inventory.TransferService().rejectMasterTransfer(st.getTransferCode(), currentUser.getEmployeeId());
                    }
                    request.getSession().setAttribute("message", "Đã từ chối phiếu điều chuyển.");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                case "partnerApproveTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()) && !"StoreManager".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    int currentWarehouseId = 0;
                    String cwIdStr = request.getParameter("currentWarehouseId");
                    if (cwIdStr != null && !cwIdStr.trim().isEmpty()) {
                        try { currentWarehouseId = Integer.parseInt(cwIdStr); } catch (Exception e) {}
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        if (currentWarehouseId == 0) {
                            boolean fromIsCreator = (st.getCreatorBranchId() == st.getFromBranchId());
                            currentWarehouseId = fromIsCreator ? st.getToWarehouseId() : st.getFromWarehouseId();
                        }
                        new service.inventory.TransferService().partnerApprove(st.getTransferCode(), currentWarehouseId);
                    }
                    request.getSession().setAttribute("message", "Đối tác đã phê duyệt phiếu thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                case "partnerRejectTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()) && !"StoreManager".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    int currentWarehouseId = 0;
                    String cwIdStr = request.getParameter("currentWarehouseId");
                    if (cwIdStr != null && !cwIdStr.trim().isEmpty()) {
                        try { currentWarehouseId = Integer.parseInt(cwIdStr); } catch (Exception e) {}
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        if (currentWarehouseId == 0) {
                            boolean fromIsCreator = (st.getCreatorBranchId() == st.getFromBranchId());
                            currentWarehouseId = fromIsCreator ? st.getToWarehouseId() : st.getFromWarehouseId();
                        }
                        new service.inventory.TransferService().partnerReject(st.getTransferCode(), currentWarehouseId);
                    }
                    request.getSession().setAttribute("message", "Đối tác đã từ chối phiếu thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                case "partnerApproveTransferAll": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        new service.inventory.TransferService().partnerApproveAll(st.getTransferCode());
                    }
                    request.getSession().setAttribute("message", "Đối tác đã phê duyệt toàn bộ phiếu thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                case "partnerRejectTransferAll": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    model.StockTransfer st = transferDAO.findById(transferId);
                    if (st != null) {
                        new service.inventory.TransferService().partnerRejectAll(st.getTransferCode());
                    }
                    request.getSession().setAttribute("message", "Đối tác đã từ chối toàn bộ phiếu thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=approval");
                    break;
                }
                case "approveOrder": {
                    int orderId = Integer.parseInt(request.getParameter("orderId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"StoreManager".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                    executionService.executeOrder(orderId, currentUser.getEmployeeId());
                    request.getSession().setAttribute("message", "Đã phê duyệt phiếu và cập nhật tồn kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=history&subtab=voucher");
                    break;
                }
                case "rejectOrder": {
                    int orderId = Integer.parseInt(request.getParameter("orderId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"StoreManager".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    orderDAO.updateStatus(orderId, "REJECTED", currentUser.getEmployeeId());
                    request.getSession().setAttribute("message", "Đã từ chối phiếu.");
                    redirect(response, request.getContextPath() + "/inventory?tab=history&subtab=voucher");
                    break;
                }
                case "cancelOrder": {
                    int orderId = Integer.parseInt(request.getParameter("orderId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"WarehouseStaff".equals(currentUser.getRoleName()) && !"Owner".equals(currentUser.getRoleName()) && !"StoreManager".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    PurchaseOrder po = purchaseOrderDAO.findById(orderId);
                    if (po == null || !"PENDING".equals(po.getStatus())) {
                        request.getSession().setAttribute("error", "Không thể hủy phiếu này vì phiếu không tồn tại hoặc đã được xử lý.");
                    } else {
                        orderDAO.updateStatus(orderId, "CANCELLED");
                        request.getSession().setAttribute("message", "Đã hủy phiếu thành công.");
                    }
                    String redirectTab = request.getParameter("tab");
                    if (redirectTab == null || redirectTab.isEmpty()) {
                        redirectTab = "WarehouseStaff".equals(currentUser.getRoleName()) ? "pending_vouchers" : "import";
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=" + redirectTab + "&warehouseId=" + request.getParameter("warehouseId"));
                    break;
                }
                case "saveCheck": {
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] systemQtys = request.getParameterValues("systemQty[]");
                    String[] actualQtys = request.getParameterValues("actualQty[]");
                    String[] notes = request.getParameterValues("note[]");

                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");

                    if (productIds != null && productIds.length > 0) {
                        if (actualQtys == null || actualQtys.length != productIds.length) {
                            request.getSession().setAttribute("error", "Lỗi: Dữ liệu kiểm kho không hợp lệ.");
                            redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                            return;
                        }

                        for (String qty : actualQtys) {
                            if (qty == null || qty.trim().isEmpty() || !qty.trim().matches("^\\d+$")) {
                                request.getSession().setAttribute("error", "Lỗi: Số lượng thực tế phải là số nguyên dương hợp lệ và không chứa ký tự khác.");
                                redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                                return;
                            }
                        }

                        List<model.InventoryCheckDetail> details = new ArrayList<>();
                        int totalDiscrepancy = 0;

                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int sysQty = Integer.parseInt(systemQtys[i]);
                            int actQty = Integer.parseInt(actualQtys[i]);
                            String note = (notes != null && notes.length > i) ? notes[i] : "";

                            int disc = actQty - sysQty;
                            totalDiscrepancy += Math.abs(disc);

                            model.InventoryCheckDetail d = new model.InventoryCheckDetail();
                            d.setProductId(pId);
                            d.setSystemQty(sysQty);
                            d.setActualQty(actQty);
                            d.setDiscrepancy(disc);
                            d.setNote(note);
                            details.add(d);
                        }

                        model.InventoryCheck check = new model.InventoryCheck();
                        check.setCheckCode("IC-" + System.currentTimeMillis());
                        check.setWarehouseId(currentWarehouseId);
                        check.setCreatedBy(currentUser.getEmployeeId());
                        
                        boolean isApprover = "Owner".equals(currentUser.getRoleName()) || "StoreManager".equals(currentUser.getRoleName());
                        check.setStatus(isApprover ? "APPROVED" : "PENDING");
                        check.setTotalDiscrepancy(totalDiscrepancy);

                        dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                        int checkId = checkDAO.createCheck(check, details);

                        if (isApprover) {
                            service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                            executionService.executeStockBalance(checkId, currentUser.getEmployeeId());
                            request.getSession().setAttribute("message", "Đã lập và duyệt phiếu kiểm kho, hoàn tất cân bằng tồn kho!");
                        } else {
                            request.getSession().setAttribute("message", "Đã lập phiếu kiểm kho (Chờ quản lý phê duyệt)!");
                        }
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "approveCheck": {
                    int checkId = Integer.parseInt(request.getParameter("checkId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"StoreManager".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                    executionService.approveInventoryCheck(checkId, currentUser.getEmployeeId());

                    request.getSession().setAttribute("message", "Đã phê duyệt phiếu kiểm kho và cân bằng tồn kho thành công!");
                    String redirectTab = request.getParameter("tab");
                    if ("check".equals(redirectTab)) {
                        redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + request.getParameter("warehouseId"));
                    } else {
                        redirect(response, request.getContextPath() + "/inventory?tab=history&subtab=voucher");
                    }
                    break;
                }
                case "rejectCheck": {
                    int checkId = Integer.parseInt(request.getParameter("checkId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"StoreManager".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                    checkDAO.updateStatus(checkId, "REJECTED", currentUser.getEmployeeId());

                    request.getSession().setAttribute("message", "Đã từ chối phiếu kiểm kho.");
                    String redirectTab = request.getParameter("tab");
                    if ("check".equals(redirectTab)) {
                        redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + request.getParameter("warehouseId"));
                    } else {
                        redirect(response, request.getContextPath() + "/inventory?tab=history&subtab=voucher");
                    }
                    break;
                }
                case "cancelCheck": {
                    int checkId = Integer.parseInt(request.getParameter("checkId"));
                    dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                    checkDAO.updateStatus(checkId, "CANCELLED", null);

                    request.getSession().setAttribute("message", "Đã hủy phiếu kiểm kho thành công.");
                    String redirectTab = request.getParameter("tab");
                    if (redirectTab == null || redirectTab.isEmpty()) {
                        redirectTab = "check";
                    }
                    redirect(response, request.getContextPath() + "/inventory?tab=" + redirectTab + "&warehouseId=" + request.getParameter("warehouseId"));
                    break;
                }
                case "updateStockDirectly": {
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"Admin".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    int productId = Integer.parseInt(request.getParameter("productId"));
                    int warehouseId = Integer.parseInt(request.getParameter("warehouseId"));
                    int newQty = Integer.parseInt(request.getParameter("quantity"));

                    inventoryDAO.updateStockQty(warehouseId, productId, newQty);
                    request.getSession().setAttribute("message", "Cập nhật số lượng tồn kho thành công!");
                    redirect(response, request.getContextPath() + "/inventory?tab=stock&warehouseId=" + warehouseId);
                    break;
                }
                case "updateCheck": {
                    int checkId = Integer.parseInt(request.getParameter("checkId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] systemQtys = request.getParameterValues("systemQty[]");
                    String[] actualQtys = request.getParameterValues("actualQty[]");
                    String[] notes = request.getParameterValues("note[]");

                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equals(currentUser.getRoleName()) && !"Admin".equals(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }

                    if (productIds != null && productIds.length > 0) {
                        if (actualQtys == null || actualQtys.length != productIds.length) {
                            request.getSession().setAttribute("error", "Lỗi: Dữ liệu kiểm kho không hợp lệ.");
                            redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                            return;
                        }

                        for (String qty : actualQtys) {
                            if (qty == null || qty.trim().isEmpty() || !qty.trim().matches("^\\d+$")) {
                                request.getSession().setAttribute("error", "Lỗi: Số lượng thực tế phải là số nguyên dương hợp lệ và không chứa ký tự khác.");
                                redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                                return;
                            }
                        }

                        List<model.InventoryCheckDetail> details = new ArrayList<>();
                        int totalDiscrepancy = 0;

                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int sysQty = Integer.parseInt(systemQtys[i]);
                            int actQty = Integer.parseInt(actualQtys[i]);
                            String note = (notes != null && notes.length > i) ? notes[i] : "";

                            int disc = actQty - sysQty;
                            totalDiscrepancy += Math.abs(disc);

                            model.InventoryCheckDetail d = new model.InventoryCheckDetail();
                            d.setProductId(pId);
                            d.setSystemQty(sysQty);
                            d.setActualQty(actQty);
                            d.setDiscrepancy(disc);
                            d.setNote(note);
                            details.add(d);
                        }

                        dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
                        checkDAO.updateCheck(checkId, totalDiscrepancy, details);

                        service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                        executionService.executeStockBalance(checkId, currentUser.getEmployeeId());
                    }

                    request.getSession().setAttribute("message", "Đã cập nhật phiếu nhập kiểm kho và cân bằng tồn kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "confirmReceiveWithDiscrepancy":
                    request.getSession().setAttribute("message", "Tính năng đang bảo trì cấu trúc database.");
                    redirect(response, request.getContextPath() + "/inventory?tab=stock");
                    break;
                default:
                    doGet(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            request.getSession().setAttribute("error", "Lỗi: " + e.getMessage() + " | StackTrace: " + sw.toString().substring(0, Math.min(200, sw.toString().length())));
            redirect(response, request.getContextPath() + "/inventory");
        }
    }

    private void handleSearchProductsApi(HttpServletRequest request, HttpServletResponse response, Integer selectedWarehouseId) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String fromWIdParam = request.getParameter("fromWarehouseId");
        String toWIdParam = request.getParameter("toWarehouseId");
        
        int fromWarehouseId = (fromWIdParam != null && !fromWIdParam.isEmpty()) ? Integer.parseInt(fromWIdParam) : (selectedWarehouseId != null ? selectedWarehouseId : 0);
        int toWarehouseId = (toWIdParam != null && !toWIdParam.isEmpty()) ? Integer.parseInt(toWIdParam) : 0;
        
        if (fromWarehouseId == 0 || toWarehouseId == 0) {
            response.getWriter().write("[]");
            return;
        }

        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        } else {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }

        List<dto.inventory.ExchangeProductDTO> list = inventoryDAO.searchTransferProducts(fromWarehouseId, toWarehouseId, keyword);
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            dto.inventory.ExchangeProductDTO dto = list.get(i);
            json.append("{");
            json.append("\"productId\":").append(dto.getProductId()).append(",");
            json.append("\"productName\":\"").append(escapeJson(dto.getProductName())).append("\",");
            json.append("\"myStock\":").append(dto.getMyStock()).append(",");
            json.append("\"partnerWarehouseId\":").append(dto.getPartnerWarehouseId()).append(",");
            json.append("\"partnerWarehouseName\":\"").append(escapeJson(dto.getPartnerWarehouseName())).append("\",");
            json.append("\"partnerStock\":").append(dto.getPartnerStock());
            json.append("}");
            if (i < list.size() - 1) json.append(",");
        }
        json.append("]");
        
        response.getWriter().write(json.toString());
    }

    private void handleSearchAllProductsApi(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        } else {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }

        int myWarehouseId = 0;
        String wIdParam = request.getParameter("warehouseId");
        if (wIdParam != null && !wIdParam.trim().isEmpty()) {
            try {
                myWarehouseId = Integer.parseInt(wIdParam.trim());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (myWarehouseId == 0) {
            Integer selectedWarehouseId = (Integer) request.getSession().getAttribute("selectedWarehouseId");
            myWarehouseId = selectedWarehouseId != null ? selectedWarehouseId : 0;
        }
        
        List<dto.inventory.ExchangeProductDTO> list = inventoryDAO.searchExchangeProducts(myWarehouseId, keyword);
        
        // Group by productId
        java.util.Map<Integer, java.util.Map<String, Object>> productMap = new java.util.LinkedHashMap<>();
        for (dto.inventory.ExchangeProductDTO p : list) {
            java.util.Map<String, Object> prod = productMap.get(p.getProductId());
            if (prod == null) {
                prod = new java.util.LinkedHashMap<>();
                prod.put("productId", p.getProductId());
                prod.put("productName", p.getProductName());
                prod.put("myStock", p.getMyStock());
                prod.put("partners", new java.util.ArrayList<java.util.Map<String, Object>>());
                productMap.put(p.getProductId(), prod);
            }
            java.util.List<java.util.Map<String, Object>> partners = (java.util.List<java.util.Map<String, Object>>) prod.get("partners");
            java.util.Map<String, Object> partner = new java.util.LinkedHashMap<>();
            partner.put("warehouseId", p.getPartnerWarehouseId());
            partner.put("warehouseName", p.getPartnerWarehouseName());
            partner.put("stock", p.getPartnerStock());
            partners.add(partner);
        }

        // Sort partners of each product by stock descending
        for (java.util.Map<String, Object> prod : productMap.values()) {
            java.util.List<java.util.Map<String, Object>> partners = (java.util.List<java.util.Map<String, Object>>) prod.get("partners");
            partners.sort((a, b) -> Integer.compare((Integer) b.get("stock"), (Integer) a.get("stock")));
        }

        StringBuilder json = new StringBuilder("[");
        int prodIndex = 0;
        for (java.util.Map<String, Object> prod : productMap.values()) {
            json.append("{");
            json.append("\"productId\":").append(prod.get("productId")).append(",");
            json.append("\"productName\":\"").append(escapeJson((String) prod.get("productName"))).append("\",");
            json.append("\"myStock\":").append(prod.get("myStock")).append(",");
            json.append("\"partners\":[");
            java.util.List<java.util.Map<String, Object>> partners = (java.util.List<java.util.Map<String, Object>>) prod.get("partners");
            for (int j = 0; j < partners.size(); j++) {
                java.util.Map<String, Object> part = partners.get(j);
                json.append("{");
                json.append("\"warehouseId\":").append(part.get("warehouseId")).append(",");
                json.append("\"warehouseName\":\"").append(escapeJson((String) part.get("warehouseName"))).append("\",");
                json.append("\"stock\":").append(part.get("stock"));
                json.append("}");
                if (j < partners.size() - 1) json.append(",");
            }
            json.append("]");
            json.append("}");
            if (prodIndex < productMap.size() - 1) json.append(",");
            prodIndex++;
        }
        json.append("]");
        
        response.getWriter().write(json.toString());
    }

    private void handleGetImportTemplateDataApi(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        dao.inventory.InventoryDAO dao = new dao.inventory.InventoryDAO();
        List<String[]> rows = dao.getAllProductsWithSuppliers();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            json.append("[\"").append(escapeJson(r[0])).append("\",");
            json.append(r[1]).append(",");
            json.append("\"").append(escapeJson(r[2])).append("\"]");
            if (i < rows.size() - 1) json.append(",");
        }
        json.append("]");
        response.getWriter().write(json.toString());
    }

    private void handleSearchImportProductsApi(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        } else {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }
        
        int warehouseId = Integer.parseInt(request.getParameter("warehouseId"));
        
        dao.inventory.InventoryDAO dao = new dao.inventory.InventoryDAO();
        List<dto.inventory.ImportProductDTO> list = dao.searchImportProducts(warehouseId, keyword);
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            dto.inventory.ImportProductDTO p = list.get(i);
            json.append("{");
            json.append("\"productId\":").append(p.getProductId()).append(",");
            json.append("\"productName\":\"").append(escapeJson(p.getProductName())).append("\",");
            json.append("\"myStock\":").append(p.getMyStock()).append(",");
            json.append("\"suppliers\":[");
            if (p.getSuppliers() != null) {
                for (int j = 0; j < p.getSuppliers().size(); j++) {
                    dto.inventory.ImportProductDTO.SupplierInfo s = p.getSuppliers().get(j);
                    json.append("{");
                    json.append("\"supplierId\":").append(s.getSupplierId()).append(",");
                    json.append("\"supplierName\":\"").append(escapeJson(s.getSupplierName())).append("\",");
                    json.append("\"importPrice\":").append(s.getImportPrice() != null ? s.getImportPrice() : 0);
                    json.append("}");
                    if (j < p.getSuppliers().size() - 1) json.append(",");
                }
            }
            json.append("]");
            json.append("}");
            if (i < list.size() - 1) json.append(",");
        }
        json.append("]");
        
        response.getWriter().write(json.toString());
    }

    private void attachImageUrls(HttpServletRequest request, List<Inventory> stockList) {
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

    private void handleSearchStockCheckProductsApi(HttpServletRequest request, HttpServletResponse response, Integer warehouseId) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (warehouseId == null) {
            response.getWriter().write("[]");
            return;
        }

        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        } else {
            keyword = keyword.trim().replaceAll("\\s+", " ");
        }

        List<dto.inventory.StockCheckProductDTO> list = inventoryDAO.searchStockCheckProducts(warehouseId, keyword);
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            dto.inventory.StockCheckProductDTO dto = list.get(i);
            json.append("{");
            json.append("\"productId\":").append(dto.getProductId()).append(",");
            json.append("\"productName\":\"").append(escapeJson(dto.getProductName())).append("\",");
            json.append("\"systemStock\":").append(dto.getSystemStock()).append(",");
            json.append("\"productCodebar\":\"").append(escapeJson(dto.getProductCodebar() != null ? dto.getProductCodebar() : "")).append("\",");
            json.append("\"categoryName\":\"").append(escapeJson(dto.getCategoryName())).append("\"");
            json.append("}");
            if (i < list.size() - 1) json.append(",");
        }
        json.append("]");
        
        response.getWriter().write(json.toString());
    }

    private String escapeJson(String input) {
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
}
