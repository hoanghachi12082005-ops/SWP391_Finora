package controller.inventory;

import controller.common.BaseController;
import dao.inventory.InventoryDAO;
import dao.inventory.WarehouseDAO;
import dao.inventory.StockTransferDAO;
import dao.inventory.StockCheckDAO;
import dao.inventory.StockTransactionDAO;
import model.Employee;
import model.Inventory;
import model.Warehouse;
import model.StockTransfer;
import model.StockCheck;
import model.StockTransaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "InventoryController", urlPatterns = {"/inventory"})
public class InventoryController extends BaseController {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final StockTransferDAO transferDAO = new StockTransferDAO();
    private final StockCheckDAO checkDAO = new StockCheckDAO();
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
            if (!myWarehouses.isEmpty()) {
                selectedWarehouseId = myWarehouses.get(0).getWarehouseId();
            }

            // Global access for Admin/Owner
            if (role.equals("admin") || role.equals("owner")) {
                List<Warehouse> allWarehouses = warehouseDAO.findAll();
                request.setAttribute("warehouses", allWarehouses);
                
                String filterWarehouse = request.getParameter("warehouseId");
                if (filterWarehouse != null && !filterWarehouse.isEmpty()) {
                    selectedWarehouseId = Integer.parseInt(filterWarehouse);
                } else {
                    selectedWarehouseId = null; // null means all
                }
            } else {
                request.setAttribute("warehouses", myWarehouses); // Only own warehouses
            }

            String tab = request.getParameter("tab");
            if (tab == null || tab.isEmpty()) {
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
                    handleHistoryTab(request, selectedWarehouseId);
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
        List<StockTransfer> transfers;
        if (role.equals("admin") || role.equals("owner")) {
            transfers = transferDAO.findAllGlobal();
        } else {
            transfers = transferDAO.findAll(warehouseId != null ? warehouseId : 0);
        }
        request.setAttribute("transfers", transfers);
    }

    private void handleCheckTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        List<StockCheck> checks;
        if (role.equals("admin") || role.equals("owner")) {
            checks = checkDAO.findAllGlobal();
        } else {
            checks = checkDAO.findAll(warehouseId != null ? warehouseId : 0);
        }
        request.setAttribute("checks", checks);
    }

    private void handleHistoryTab(HttpServletRequest request, Integer warehouseId) throws Exception {
        String typeFilter = request.getParameter("typeFilter");
        String dateFilter = request.getParameter("dateFilter");

        int page = 1;
        int limit = 50;
        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }
        int offset = (page - 1) * limit;

        List<StockTransaction> history = transactionDAO.findAll(warehouseId != null ? warehouseId : 0, offset, limit, typeFilter, dateFilter);
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
                // To be implemented: actual form processing
                case "createTransfer":
                    request.getSession().setAttribute("message", "Đã tạo phiếu chuyển kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer");
                    break;
                case "confirmExport":
                    int exportId = Integer.parseInt(request.getParameter("transferId"));
                    transferDAO.updateStatus(exportId, "IN_TRANSIT");
                    // TODO: Decrease source inventory
                    request.getSession().setAttribute("message", "Đã xác nhận xuất hàng.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer");
                    break;
                case "confirmReceive":
                    int receiveId = Integer.parseInt(request.getParameter("transferId"));
                    transferDAO.updateStatus(receiveId, "COMPLETED");
                    // TODO: Increase destination inventory
                    request.getSession().setAttribute("message", "Đã xác nhận nhận hàng.");
                    redirect(response, request.getContextPath() + "/inventory?tab=transfer");
                    break;
                case "createCheck":
                    request.getSession().setAttribute("message", "Đã tạo phiếu kiểm kho thành công.");
                    redirect(response, request.getContextPath() + "/inventory?tab=check");
                    break;
                case "approveCheck":
                    int checkId = Integer.parseInt(request.getParameter("checkId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    checkDAO.approveCheck(checkId, currentUser.getEmployeeId());
                    // TODO: Adjust inventory based on check detail difference
                    request.getSession().setAttribute("message", "Đã duyệt phiếu kiểm kho.");
                    redirect(response, request.getContextPath() + "/inventory?tab=check");
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
}
