package controller.inventory;

import controller.common.BaseController;
import dao.inventory.InventoryDAO;
import dao.inventory.WarehouseDAO;
import dao.inventory.InventoryTicketDAO;
import dao.inventory.StockTransactionDAO;
import model.Employee;
import model.Inventory;
import model.Warehouse;
import model.InventoryTicket;
import model.StockTransaction;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
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
            } else if ("viewTicket".equals(action)) {
                int ticketId = Integer.parseInt(request.getParameter("ticketId"));
                model.InventoryTicket ticket = ticketDAO.findById(ticketId);
                List<model.InventoryTicketDetail> details = ticketDAO.getTicketDetails(ticketId);
                request.setAttribute("ticket", ticket);
                request.setAttribute("ticketDetails", details);
                request.getRequestDispatcher("/views/inventory/_modal_ticket_details.jsp").forward(request, response);
                return;
            } else if ("printTicket".equals(action)) {
                int ticketId = Integer.parseInt(request.getParameter("ticketId"));
                model.InventoryTicket ticket = ticketDAO.findById(ticketId);
                List<model.InventoryTicketDetail> details = ticketDAO.getTicketDetails(ticketId);
                request.setAttribute("ticket", ticket);
                request.setAttribute("ticketDetails", details);
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
    }

    private void handleTransferTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        List<InventoryTicket> transfers;
        if ((role.equals("Admin") || role.equals("Owner")) && warehouseId == null) {
            transfers = ticketDAO.findAllByType("TRANSFER", null);
        } else {
            transfers = ticketDAO.findAllByType("TRANSFER", warehouseId != null ? warehouseId : 0);
        }
        request.setAttribute("transfers", transfers);
    }

    private void handleCheckTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        List<InventoryTicket> checks;
        if ((role.equals("Admin") || role.equals("Owner")) && warehouseId == null) {
            checks = ticketDAO.findAllByType("CHECK", null);
        } else {
            checks = ticketDAO.findAllByType("CHECK", warehouseId != null ? warehouseId : 0);
        }
        request.setAttribute("checks", checks);
    }

    private void handleHistoryTab(HttpServletRequest request, Integer warehouseId, List<Integer> allowedWarehouseIds) throws Exception {
        String typeFilter = request.getParameter("typeFilter");
        String dateFilter = request.getParameter("dateFilter");

        int page = 1;
        int limit = 50;
        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }
        int offset = (page - 1) * limit;

        List<StockTransaction> history = transactionDAO.findAll(warehouseId != null ? warehouseId : 0, allowedWarehouseIds, offset, limit, typeFilter, dateFilter);
        request.setAttribute("history", history);
        request.setAttribute("typeFilter", typeFilter);
        request.setAttribute("dateFilter", dateFilter);
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
                            List<model.InventoryTicketDetail> details = entry.getValue();
                            
                            model.InventoryTicket ticket = new model.InventoryTicket();
                            ticket.setTicketCode("TRN-" + System.currentTimeMillis() + "-" + partnerWId);
                            ticket.setTicketType("TRANSFER");
                            ticket.setFromWarehouseId(currentWarehouseId); // Creator
                            ticket.setToWarehouseId(partnerWId); // Partner
                            ticket.setStatus("PENDING");
                            ticket.setCreatedBy(currentUser.getEmployeeId());
                            
                            ticketDAO.createExchangeTicket(ticket, details);
                        }
                    }

                    request.getSession().setAttribute("message", "Đã tạo phiếu chuyển kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + currentWarehouseId);
                    break;
                }
                case "confirmExport": {
                    int transferId = Integer.parseInt(request.getParameter("transferId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    ticketDAO.approveTransferTicket(transferId, currentUser.getEmployeeId());
                    request.getSession().setAttribute("message", "Đã duyệt phiếu chuyển kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer&warehouseId=" + request.getParameter("warehouseId"));
                    break;
                }
                case "confirmReceive":
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
            request.getSession().setAttribute("error", "Lỗi: " + e.getMessage());
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
}
