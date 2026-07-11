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
            } else if ("searchImportProductsApi".equals(action)) {
                handleSearchImportProductsApi(request, response);
                return;
            } else if ("viewTicket".equals(action)) {
                int ticketId = Integer.parseInt(request.getParameter("ticketId"));
                model.StockTransfer transfer = transferDAO.findById(ticketId);
                List<model.StockTransferDetail> details = transferDAO.getTransferDetails(ticketId);
                List<model.StockTransaction> txs = transactionDAO.findByReference("STOCK_TRANSFER", ticketId);
                
                request.setAttribute("ticket", transfer);
                request.setAttribute("ticketDetails", details);
                request.setAttribute("transactions", txs);
                
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
                List<model.StockTransferDetail> details = transferDAO.getTransferDetails(ticketId);
                List<model.StockTransaction> txs = transactionDAO.findByReference("STOCK_TRANSFER", ticketId);
                
                request.setAttribute("ticket", transfer);
                request.setAttribute("ticketDetails", details);
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
                case "createTransfer":
                    // No extra data fetching needed for createTransfer view itself
                    // It uses API for data
                    break;
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

        List<StockTransfer> transfers = transferDAO.findAllByStatus(warehouseId != null ? warehouseId : 0, null);
        request.setAttribute("transfers", transfers);
    }

    private void handleImportTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        List<PurchaseOrder> imports = purchaseOrderDAO.findAllByWarehouseAndType(warehouseId != null ? warehouseId : 0, "PURCHASE", null);
        request.setAttribute("imports", imports);
    }

    private void handleExportTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        List<PurchaseOrder> exports = purchaseOrderDAO.findAllByWarehouseAndType(warehouseId != null ? warehouseId : 0, "EXPORT", null);
        request.setAttribute("exports", exports);
    }

    private void handleCheckTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        String subtab = "inventory_check";
        request.setAttribute("activeSubtab", subtab);

        List<Order> checks = orderDAO.getAllSaleOrders("", warehouseId != null ? warehouseId : 0); // Need specialized check method in OrderDAO later
        request.setAttribute("checks", checks);
    }

    private void handleApprovalTab(HttpServletRequest request, String role) throws Exception {
        if (!"Owner".equalsIgnoreCase(role)) {
            request.setAttribute("error", "Bạn không có quyền truy cập tab này.");
            return;
        }
        List<model.StockTransfer> pendingTransfers = transferDAO.findAllByStatus(0, "PENDING_DISPATCH");
        request.setAttribute("pendingTransfers", pendingTransfers);
    }

    private void handleHistoryTab(HttpServletRequest request, Integer warehouseId, List<Integer> allowedWarehouseIds) throws Exception {
        String typeFilter = request.getParameter("typeFilter");
        String dateFilter = request.getParameter("dateFilter");
        
        int page = 1;
        if (request.getParameter("page") != null) page = Integer.parseInt(request.getParameter("page"));
        int offset = (page - 1) * 20;

        List<StockTransaction> history = transactionDAO.findAll(warehouseId != null ? warehouseId : 0, allowedWarehouseIds, offset, 20, typeFilter, dateFilter);
        
        request.setAttribute("typeFilter", typeFilter);
        request.setAttribute("dateFilter", dateFilter);
        request.setAttribute("history", history);
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
                    
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    boolean isOwner = "Owner".equals(currentUser.getRoleName());
                    
                    if (productIds != null && productIds.length > 0) {
                        java.util.Map<Integer, List<model.StockTransferDetail>> groupedDetails = new java.util.HashMap<>();
                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int partnerWId = Integer.parseInt(partnerWarehouseIds[i]);
                            int qty = Integer.parseInt(quantities[i]);
                            if (qty > 0) {
                                model.StockTransferDetail detail = new model.StockTransferDetail();
                                detail.setProductId(pId);
                                detail.setQuantity(qty);
                                groupedDetails.computeIfAbsent(partnerWId, k -> new ArrayList<>()).add(detail);
                            }
                        }
                        
                        for (java.util.Map.Entry<Integer, List<model.StockTransferDetail>> entry : groupedDetails.entrySet()) {
                            int partnerWId = entry.getKey();
                            List<model.StockTransferDetail> details = entry.getValue();
                            
                            model.StockTransfer transfer = new model.StockTransfer();
                            transfer.setTransferCode("TX-" + System.currentTimeMillis() + "-" + partnerWId);
                            transfer.setFromWarehouseId(currentWarehouseId);
                            transfer.setToWarehouseId(partnerWId);
                            // Owner tạo -> tự động duyệt, nhân viên tạo -> chờ Owner duyệt
                            transfer.setStatus(isOwner ? "APPROVED_DISPATCH" : "PENDING_DISPATCH");
                            transfer.setCreatedBy(currentUser.getEmployeeId());
                            
                            transferDAO.createTransfer(transfer, details);
                        }
                    }

                    if (isOwner) {
                        request.getSession().setAttribute("message", "Đã tạo và duyệt phiếu điều chuyển. Nhân viên kho xuất có thể xác nhận xuất hàng.");
                    } else {
                        request.getSession().setAttribute("message", "Đã tạo phiếu điều chuyển (Chờ Owner duyệt).");
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
                    boolean isOwner = "Owner".equals(currentUser.getRoleName());
                    
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
                        request.getSession().setAttribute("message", "Đã tạo phiếu nhập hàng (Chờ Owner duyệt).");
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
                    
                    service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                    executionService.dispatchTransfer(transferId, currentUser.getEmployeeId());
                    
                    request.getSession().setAttribute("message", "Đã xuất kho thành công. Hàng đang trên đường vận chuyển.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "confirmReceive": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                    executionService.receiveTransfer(transferId, currentUser.getEmployeeId());
                    
                    request.getSession().setAttribute("message", "Đã nhập kho thành công. Phiếu điều chuyển hoàn tất.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "cancelTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    
                    transferDAO.updateStatus(transferId, "CANCELLED");
                    
                    request.getSession().setAttribute("message", "Đã hủy phiếu điều chuyển.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "confirmReceiveWithDiscrepancy":
                case "createCheck":
                case "approveCheck":
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

    private void handleSearchProductsApi(HttpServletRequest request, HttpServletResponse response, Integer warehouseId) throws Exception {
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

        List<dto.inventory.ExchangeProductDTO> list = inventoryDAO.searchExchangeProducts(warehouseId, keyword);
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            dto.inventory.ExchangeProductDTO dto = list.get(i);
            json.append("{");
            json.append("\"productId\":").append(dto.getProductId()).append(",");
            json.append("\"productName\":\"").append(dto.getProductName().replace("\"", "\\\"")).append("\",");
            json.append("\"myStock\":").append(dto.getMyStock()).append(",");
            json.append("\"partnerWarehouseId\":").append(dto.getPartnerWarehouseId()).append(",");
            json.append("\"partnerWarehouseName\":\"").append(dto.getPartnerWarehouseName().replace("\"", "\\\"")).append("\",");
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

        Integer supplierId = null;
        String supplierIdParam = request.getParameter("supplierId");
        if (supplierIdParam != null && !supplierIdParam.trim().isEmpty()) {
            try {
                supplierId = Integer.parseInt(supplierIdParam.trim());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        
        dao.product.ProductDAO productDAO = new dao.product.ProductDAO();
        List<model.Product> list = productDAO.findAll(0, 50, keyword, "ACTIVE", null, null, supplierId);
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            model.Product p = list.get(i);
            json.append("{");
            json.append("\"productId\":").append(p.getProductID()).append(",");
            json.append("\"productName\":\"").append(p.getName().replace("\"", "\\\"")).append("\",");
            json.append("\"importPrice\":").append(p.getImportPrice() != null ? p.getImportPrice() : 0);
            json.append("}");
            if (i < list.size() - 1) json.append(",");
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
            json.append("\"productName\":\"").append(p.getProductName().replace("\"", "\\\"")).append("\",");
            json.append("\"myStock\":").append(p.getMyStock()).append(",");
            json.append("\"suppliers\":[");
            if (p.getSuppliers() != null) {
                for (int j = 0; j < p.getSuppliers().size(); j++) {
                    dto.inventory.ImportProductDTO.SupplierInfo s = p.getSuppliers().get(j);
                    json.append("{");
                    json.append("\"supplierId\":").append(s.getSupplierId()).append(",");
                    json.append("\"supplierName\":\"").append(s.getSupplierName().replace("\"", "\\\"")).append("\",");
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
}
