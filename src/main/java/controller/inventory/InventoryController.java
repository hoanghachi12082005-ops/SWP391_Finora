package controller.inventory;

import controller.common.BaseController;
import dao.inventory.InventoryDAO;
import dao.inventory.WarehouseDAO;
import dao.inventory.InventoryTicketDAO;
import dao.inventory.StockTransactionDAO;
import dao.supplier.SupplierDAO;
import model.Employee;
import model.Inventory;
import model.Warehouse;
import model.InventoryTicket;
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
    private final InventoryTicketDAO ticketDAO = new InventoryTicketDAO();
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

        // Determine accessible warehouse(s)
        int branchId = currentUser.getBranchId() != null ? currentUser.getBranchId() : 0;
        Integer selectedWarehouseId = null;

        try {
            List<Warehouse> myWarehouses = warehouseDAO.findByBranch(branchId);

            List<Warehouse> allowedWarehouses;
            // Global access for Admin/Owner
            if (role.equals("admin") || role.equals("owner")) {
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

            String filterWarehouse = request.getParameter("warehouseId");
            if (filterWarehouse != null && !filterWarehouse.isEmpty()) {
                selectedWarehouseId = Integer.parseInt(filterWarehouse);
            } else if (allowedWarehouses.size() == 1) {
                // Auto select if only 1 warehouse is available
                selectedWarehouseId = allowedWarehouses.get(0).getWarehouseId();
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
            } else if ("viewReceiptForm".equals(action)) {
                int ticketId = Integer.parseInt(request.getParameter("ticketId"));
                model.InventoryTicket ticket = ticketDAO.findById(ticketId);
                List<model.InventoryTicketDetail> details = ticketDAO.getTicketDetails(ticketId);
                request.setAttribute("ticket", ticket);
                request.setAttribute("ticketDetails", details);
                request.getRequestDispatcher("/views/inventory/_modal_receipt_form.jsp").forward(request, response);
                return;
            } else if ("viewTicket".equals(action)) {
                int ticketId = Integer.parseInt(request.getParameter("ticketId"));
                model.InventoryTicket ticket = ticketDAO.findById(ticketId);
                List<model.InventoryTicketDetail> details = ticketDAO.getTicketDetails(ticketId);
                request.setAttribute("ticket", ticket);
                request.setAttribute("ticketDetails", details);
                
                if (ticket != null && ("COMPLETED".equals(ticket.getStatus()) || "REJECTED".equals(ticket.getStatus()))) {
                    List<model.StockTransaction> txs = transactionDAO.findByReference("TRANSFER", ticketId);
                    request.setAttribute("transactions", txs);
                }
                
                if (ticket != null && "TRANSFER_REQUEST".equals(ticket.getTicketType())) {
                    dao.inventory.InventoryDAO inventoryDAO = new dao.inventory.InventoryDAO();
                    model.InventoryTicket txTicket = ticketDAO.findByCode("TX-" + ticketId);
                    if (txTicket != null) {
                        request.setAttribute("txTicket", txTicket);
                        List<model.InventoryTicketDetail> txDetails = ticketDAO.getTicketDetails(txTicket.getTicketId());
                        request.setAttribute("txDetails", txDetails);
                        request.setAttribute("txTransactions", transactionDAO.findByReference("TRANSFER", txTicket.getTicketId()));
                        if ("IN_TRANSIT".equals(txTicket.getStatus())) {
                            java.util.Map<Integer, Integer> txCurrentStock = new java.util.HashMap<>();
                            for (model.InventoryTicketDetail d : txDetails) {
                                txCurrentStock.put(d.getProductId(), inventoryDAO.getCurrentStock(txTicket.getFromWarehouseId(), d.getProductId()));
                            }
                            request.setAttribute("txCurrentStock", txCurrentStock);
                        }
                    }
                    model.InventoryTicket tiTicket = ticketDAO.findByCode("TI-" + ticketId);
                    if (tiTicket != null) {
                        request.setAttribute("tiTicket", tiTicket);
                        List<model.InventoryTicketDetail> tiDetails = ticketDAO.getTicketDetails(tiTicket.getTicketId());
                        request.setAttribute("tiDetails", tiDetails);
                        request.setAttribute("tiTransactions", transactionDAO.findByReference("TRANSFER", tiTicket.getTicketId()));
                        if ("IN_TRANSIT".equals(tiTicket.getStatus())) {
                            java.util.Map<Integer, Integer> tiCurrentStock = new java.util.HashMap<>();
                            for (model.InventoryTicketDetail d : tiDetails) {
                                tiCurrentStock.put(d.getProductId(), inventoryDAO.getCurrentStock(tiTicket.getToWarehouseId(), d.getProductId()));
                            }
                            request.setAttribute("tiCurrentStock", tiCurrentStock);
                        }
                    }
                }
                
                request.setAttribute("selectedWarehouseId", selectedWarehouseId);
                request.getRequestDispatcher("/views/inventory/_modal_ticket_details.jsp").forward(request, response);
                return;
            } else if ("printTicket".equals(action)) {
                int ticketId = Integer.parseInt(request.getParameter("ticketId"));
                model.InventoryTicket ticket = ticketDAO.findById(ticketId);
                List<model.InventoryTicketDetail> details = ticketDAO.getTicketDetails(ticketId);
                request.setAttribute("ticket", ticket);
                request.setAttribute("ticketDetails", details);

                dao.inventory.InventoryDAO inventoryDAO = new dao.inventory.InventoryDAO();
                if (ticket != null && ("COMPLETED".equals(ticket.getStatus()) || "REJECTED".equals(ticket.getStatus()))) {
                    List<model.StockTransaction> txs = transactionDAO.findByReference("TRANSFER", ticketId);
                    request.setAttribute("transactions", txs);
                }
                
                if (ticket != null && "TRANSFER_REQUEST".equals(ticket.getTicketType())) {
                    model.InventoryTicket txTicket = ticketDAO.findByCode("TX-" + ticketId);
                    if (txTicket != null) {
                        request.setAttribute("txTicket", txTicket);
                        List<model.InventoryTicketDetail> txDetails = ticketDAO.getTicketDetails(txTicket.getTicketId());
                        request.setAttribute("txDetails", txDetails);
                        request.setAttribute("txTransactions", transactionDAO.findByReference("TRANSFER", txTicket.getTicketId()));
                        if ("IN_TRANSIT".equals(txTicket.getStatus())) {
                            java.util.Map<Integer, Integer> txCurrentStock = new java.util.HashMap<>();
                            for (model.InventoryTicketDetail d : txDetails) {
                                txCurrentStock.put(d.getProductId(), inventoryDAO.getCurrentStock(txTicket.getFromWarehouseId(), d.getProductId()));
                            }
                            request.setAttribute("txCurrentStock", txCurrentStock);
                        }
                    }
                    model.InventoryTicket tiTicket = ticketDAO.findByCode("TI-" + ticketId);
                    if (tiTicket != null) {
                        request.setAttribute("tiTicket", tiTicket);
                        List<model.InventoryTicketDetail> tiDetails = ticketDAO.getTicketDetails(tiTicket.getTicketId());
                        request.setAttribute("tiDetails", tiDetails);
                        request.setAttribute("tiTransactions", transactionDAO.findByReference("TRANSFER", tiTicket.getTicketId()));
                        if ("IN_TRANSIT".equals(tiTicket.getStatus())) {
                            java.util.Map<Integer, Integer> tiCurrentStock = new java.util.HashMap<>();
                            for (model.InventoryTicketDetail d : tiDetails) {
                                tiCurrentStock.put(d.getProductId(), inventoryDAO.getCurrentStock(tiTicket.getToWarehouseId(), d.getProductId()));
                            }
                            request.setAttribute("tiCurrentStock", tiCurrentStock);
                        }
                    }
                }
                
                request.getRequestDispatcher("/views/inventory/_print_ticket.jsp").forward(request, response);
                return;
            }

            // Calculate KPIs
            dao.inventory.InventoryDAO.DashboardKPI kpi = inventoryDAO.getDashboardKPI(allowedWarehouseIds, selectedWarehouseId);
            request.setAttribute("totalProducts", kpi.totalProducts);
            request.setAttribute("totalCategories", kpi.totalCategories);
            request.setAttribute("lowStockCount", kpi.lowStockCount);

            int pendingTransferCount = ticketDAO.getPendingCount("TRANSFER", selectedWarehouseId);
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
                case "history":
                    handleHistoryTab(request, selectedWarehouseId, allowedWarehouseIds);
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
        String status = request.getParameter("status");
        String sortParam = request.getParameter("sort");
        
        int page = 1;
        int limit = 20;
        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }
        int offset = (page - 1) * limit;

        List<Inventory> stockList = inventoryDAO.findAll(offset, limit, keyword, status, null, null, warehouseId, sortParam);
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
        List<InventoryTicket> transfers;
        if ((role.equals("Admin") || role.equals("Owner")) && warehouseId == null) {
            transfers = ticketDAO.findAllByTypeAndStatus("TRANSFER_REQUEST", null, "PENDING_IN_TRANSIT");
        } else {
            transfers = ticketDAO.findAllByTypeAndStatus("TRANSFER_REQUEST", warehouseId != null ? warehouseId : 0, "PENDING_IN_TRANSIT");
        }
        for (InventoryTicket t : transfers) {
            if ("IN_TRANSIT".equals(t.getStatus())) {
                t.setTransferProgress(ticketDAO.getTransferProgress(t.getTicketId()));
            } else {
                t.setTransferProgress("Chờ duyệt");
            }
        }
        request.setAttribute("transfers", transfers);
    }

    private void handleCheckTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        String subtab = request.getParameter("subtab");
        if (subtab == null || subtab.isEmpty()) {
            subtab = "inventory_check";
        }
        request.setAttribute("activeSubtab", subtab);

        if ("transfer_check".equals(subtab)) {
            List<InventoryTicket> transfers;
            if ((role.equals("Admin") || role.equals("Owner")) && warehouseId == null) {
                transfers = ticketDAO.findAllByTypeAndStatus("TRANSFER_CHECK", null, "PENDING_IN_TRANSIT");
            } else {
                transfers = ticketDAO.findAllByTypeAndStatus("TRANSFER_CHECK", warehouseId != null ? warehouseId : 0, "PENDING_IN_TRANSIT");
            }
            request.setAttribute("transferChecks", transfers);
        } else if ("discrepancy".equals(subtab)) {
            List<InventoryTicket> discrepancies;
            if ((role.equals("Admin") || role.equals("Owner")) && warehouseId == null) {
                discrepancies = ticketDAO.findAllByTypeAndStatus("DISCREPANCY", null, null);
            } else {
                discrepancies = ticketDAO.findAllByTypeAndStatus("DISCREPANCY", warehouseId != null ? warehouseId : 0, null);
            }
            request.setAttribute("discrepancies", discrepancies);
        } else {
            List<InventoryTicket> checks;
            if ((role.equals("Admin") || role.equals("Owner")) && warehouseId == null) {
                checks = ticketDAO.findAllByType("CHECK", null);
            } else {
                checks = ticketDAO.findAllByType("CHECK", warehouseId != null ? warehouseId : 0);
            }
            request.setAttribute("checks", checks);
        }
    }

    private void handleHistoryTab(HttpServletRequest request, Integer warehouseId, List<Integer> allowedWarehouseIds) throws Exception {
        // Fetch COMPLETED/REJECTED tickets for history tab (Only TRANSFER_REQUEST)
        List<InventoryTicket> historyTickets = ticketDAO.findAllByTypeAndStatus("TRANSFER_REQUEST", warehouseId != null ? warehouseId : 0, "COMPLETED_REJECTED");
        for (InventoryTicket t : historyTickets) {
            t.setTransferProgress(ticketDAO.getTransferProgress(t.getTicketId()));
        }
        request.setAttribute("history", historyTickets);
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
                    String[] actionTypes = request.getParameterValues("actionType[]");
                    String[] quantities = request.getParameterValues("quantity[]");
                    
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null) {
                        redirect(response, request.getContextPath() + "/login");
                        return;
                    }

                    if (productIds != null && productIds.length > 0) {
                        // Group by partner warehouse
                        java.util.Map<Integer, List<model.InventoryTicketDetail>> groupedDetails = new java.util.HashMap<>();
                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int partnerWId = Integer.parseInt(partnerWarehouseIds[i]);
                            String aType = actionTypes[i];
                            int qty = Integer.parseInt(quantities[i]);
                            
                            model.InventoryTicketDetail detail = new model.InventoryTicketDetail();
                            detail.setProductId(pId);
                            detail.setActionType(aType);
                            detail.setQuantity(qty);
                            
                            groupedDetails.computeIfAbsent(partnerWId, k -> new ArrayList<>()).add(detail);
                        }
                        
                        // Create a ticket for each partner warehouse
                        for (java.util.Map.Entry<Integer, List<model.InventoryTicketDetail>> entry : groupedDetails.entrySet()) {
                            int partnerWId = entry.getKey();
                            List<model.InventoryTicketDetail> allDetails = entry.getValue();
                            
                            List<model.InventoryTicketDetail> exportDetails = new ArrayList<>();
                            model.InventoryTicket requestTicket = new model.InventoryTicket();
                            requestTicket.setTicketCode("TR-" + System.currentTimeMillis());
                            requestTicket.setTicketType("TRANSFER_REQUEST");
                            requestTicket.setFromWarehouseId(currentWarehouseId);
                            requestTicket.setToWarehouseId(partnerWId);
                            requestTicket.setStatus("PENDING");
                            requestTicket.setCreatedBy(currentUser.getEmployeeId());
                            
                            // Lưu tất cả detail vào phiếu này
                            ticketDAO.createExchangeTicket(requestTicket, allDetails);
                        }
                    }

                    request.getSession().setAttribute("message", "Đã tạo phiếu chuyển kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "saveImport": {
                    int currentWarehouseId = Integer.parseInt(request.getParameter("currentWarehouseId"));
                    String note = request.getParameter("note");
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] quantities = request.getParameterValues("quantity[]");
                    String[] supplierIds = request.getParameterValues("supplierId[]");
                    
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    if (productIds != null && productIds.length > 0) {
                        java.util.Map<Integer, List<model.InventoryTicketDetail>> groupedDetails = new java.util.HashMap<>();
                        for (int i = 0; i < productIds.length; i++) {
                            int pId = Integer.parseInt(productIds[i]);
                            int qty = Integer.parseInt(quantities[i]);
                            int sId = Integer.parseInt(supplierIds[i]);
                            if (qty > 0) {
                                model.InventoryTicketDetail detail = new model.InventoryTicketDetail();
                                detail.setProductId(pId);
                                detail.setActionType("RECEIVE");
                                detail.setQuantity(qty);
                                groupedDetails.computeIfAbsent(sId, k -> new ArrayList<>()).add(detail);
                            }
                        }
                        
                        for (java.util.Map.Entry<Integer, List<model.InventoryTicketDetail>> entry : groupedDetails.entrySet()) {
                            int sId = entry.getKey();
                            List<model.InventoryTicketDetail> importDetails = entry.getValue();
                            
                            model.InventoryTicket importTicket = new model.InventoryTicket();
                            importTicket.setTicketCode("IMP-" + System.currentTimeMillis() + "-" + sId);
                            importTicket.setTicketType("IMPORT");
                            importTicket.setFromWarehouseId(sId); // SupplierID
                            importTicket.setToWarehouseId(currentWarehouseId);
                            importTicket.setStatus("COMPLETED");
                            importTicket.setNote(note);
                            importTicket.setCreatedBy(currentUser.getEmployeeId());
                            
                            ticketDAO.createExchangeTicket(importTicket, importDetails);
                            
                            // Calculate total import cost
                            double totalImportCost = 0.0;
                            
                            for (model.InventoryTicketDetail d : importDetails) {
                                inventoryDAO.increaseStock(currentWarehouseId, d.getProductId(), d.getQuantity());
                                
                                double price = 0.0;
                                String priceStr = request.getParameter("importPrice_" + d.getProductId());
                                if (priceStr != null && !priceStr.isBlank()) {
                                    price = Double.parseDouble(priceStr);
                                }
                                totalImportCost += d.getQuantity() * price;
                                
                                StockTransaction tx = new StockTransaction();
                                tx.setWarehouseId(currentWarehouseId);
                                tx.setProductId(d.getProductId());
                                tx.setTransactionType("IMPORT");
                                tx.setQuantity(d.getQuantity());
                                tx.setReferenceType("IMPORT_TICKET");
                                tx.setReferenceId(0); // Cannot easily get ticketId here without DAO changes
                                tx.setCreatedBy(currentUser.getEmployeeId());
                                transactionDAO.insert(tx);
                            }
                            
                            // Record payment expense in cash book
                            if (totalImportCost > 0) {
                                model.Payment payment = new model.Payment();
                                payment.setPaymentAmount(totalImportCost);
                                payment.setPaymentType("EXPENSE");
                                payment.setMethod("CASH");
                                payment.setPaymentStatus("PAID");
                                
                                dao.supplier.SupplierDAO sDao = new dao.supplier.SupplierDAO();
                                model.Supplier supplier = sDao.getById(sId);
                                String supplierName = (supplier != null) ? supplier.getName() : ("ID " + sId);
                                
                                payment.setDescription("Nhập hàng từ nhà cung cấp: " + supplierName + " (Phiếu: " + importTicket.getTicketCode() + ")");
                                payment.setTransactionCode("IMP-" + System.currentTimeMillis() + "-" + sId);
                                
                                if (currentUser != null) {
                                    payment.setEmployeeId(currentUser.getEmployeeId());
                                    payment.setBranchId(currentUser.getBranchId());
                                }
                                
                                service.finance.PaymentService paymentService = new service.finance.PaymentService();
                                paymentService.insert(payment);
                            }
                        }
                    }

                    request.getSession().setAttribute("message", "Đã nhập kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=stock&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "confirmExport": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    
                    // Đánh dấu gốc là IN_TRANSIT
                    ticketDAO.approveTransferTicket(transferId, currentUser.getEmployeeId());
                    
                    // Tách phiếu con cho Kiểm Kho
                    model.InventoryTicket original = ticketDAO.getTicketById(transferId);
                    if (original != null) {
                        List<model.InventoryTicketDetail> allDetails = ticketDAO.getTicketDetails(transferId);
                        List<model.InventoryTicketDetail> exportDetails = new ArrayList<>();
                        List<model.InventoryTicketDetail> importDetails = new ArrayList<>();
                        for (model.InventoryTicketDetail d : allDetails) {
                            if ("SEND".equalsIgnoreCase(d.getActionType()) || "EXPORT".equalsIgnoreCase(d.getActionType())) {
                                exportDetails.add(d);
                            } else if ("RECEIVE".equalsIgnoreCase(d.getActionType()) || "IMPORT".equalsIgnoreCase(d.getActionType())) {
                                importDetails.add(d);
                            }
                        }
                        
                        if (!exportDetails.isEmpty()) {
                            model.InventoryTicket exportTicket = new model.InventoryTicket();
                            exportTicket.setTicketCode("TX-" + original.getTicketId());
                            exportTicket.setTicketType("TRANSFER_CHECK");
                            exportTicket.setFromWarehouseId(original.getFromWarehouseId());
                            exportTicket.setToWarehouseId(original.getToWarehouseId());
                            exportTicket.setStatus("IN_TRANSIT");
                            exportTicket.setNote("Phiếu gốc: " + original.getTicketCode());
                            exportTicket.setCreatedBy(currentUser.getEmployeeId());
                            ticketDAO.createExchangeTicket(exportTicket, exportDetails);
                        }
                        
                        if (!importDetails.isEmpty()) {
                            model.InventoryTicket importTicket = new model.InventoryTicket();
                            importTicket.setTicketCode("TI-" + original.getTicketId());
                            importTicket.setTicketType("TRANSFER_CHECK");
                            importTicket.setFromWarehouseId(original.getToWarehouseId());
                            importTicket.setToWarehouseId(original.getFromWarehouseId());
                            importTicket.setStatus("IN_TRANSIT");
                            importTicket.setNote("Phiếu gốc: " + original.getTicketCode());
                            importTicket.setCreatedBy(currentUser.getEmployeeId());
                            ticketDAO.createExchangeTicket(importTicket, importDetails);
                        }
                    }

                    request.getSession().setAttribute("message", "Đã duyệt phiếu. Hàng đang trong trạng thái trung chuyển và chuyển sang Kiểm Kho.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + request.getParameter("warehouseId"));
                    break;
                }
                case "cancelTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    ticketDAO.cancelTicket(transferId, currentUser.getEmployeeId(), "");
                    request.getSession().setAttribute("message", "Đã hủy phiếu điều chuyển thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + request.getParameter("warehouseId"));
                    break;
                }
                case "rejectTransfer": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    ticketDAO.rejectTicket(transferId, currentUser.getEmployeeId(), "");
                    request.getSession().setAttribute("message", "Đã từ chối phiếu điều chuyển thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + request.getParameter("warehouseId"));
                    break;
                }
                case "confirmDispatch": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    ticketDAO.confirmDispatch(transferId, currentUser.getEmployeeId());
                    request.getSession().setAttribute("message", "Đã xác nhận xuất kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=check&subtab=transfer_check&warehouseId=" + request.getParameter("warehouseId"));
                    break;
                }
                case "rejectDispatch": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    String note = request.getParameter("note");
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    ticketDAO.rejectDispatch(transferId, currentUser.getEmployeeId(), note);
                    request.getSession().setAttribute("message", "Đã từ chối phiếu xuất kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=check&subtab=transfer_check&warehouseId=" + request.getParameter("warehouseId"));
                    break;
                }
                case "confirmReceiveWithDiscrepancy": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    int currentWarehouseId = Integer.parseInt(request.getParameter("warehouseId"));
                    String note = request.getParameter("note");
                    
                    String[] productIds = request.getParameterValues("productId[]");
                    String[] actualQtys = request.getParameterValues("actualQty[]");
                    java.util.Map<Integer, Integer> actualQtyMap = new java.util.HashMap<>();
                    
                    if (productIds != null && actualQtys != null) {
                        for (int i = 0; i < productIds.length; i++) {
                            actualQtyMap.put(Integer.parseInt(productIds[i]), Integer.parseInt(actualQtys[i]));
                        }
                    }
                    
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    ticketDAO.confirmReceiptWithDiscrepancy(transferId, currentUser.getEmployeeId(), note, actualQtyMap, currentWarehouseId);
                    request.getSession().setAttribute("message", "Đã ghi nhận kiểm đếm nhập kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=check&subtab=transfer_check&warehouseId=" + request.getParameter("warehouseId"));
                    break;
                }
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
        if (keyword == null) keyword = "";

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
        if (keyword == null) keyword = "";

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
        if (keyword == null) keyword = "";
        
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
}
