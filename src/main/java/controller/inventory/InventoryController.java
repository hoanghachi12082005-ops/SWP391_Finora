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
import model.Warehouse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Controller trung tâm quản lý giao diện Kho hàng.
 * Tiếp nhận tất cả các yêu cầu gửi đến "/inventory", phân phối xử lý theo tab và action.
 */
@WebServlet(name = "InventoryController", urlPatterns = {"/inventory"})
public class InventoryController extends BaseController {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final StockTransferDAO transferDAO = new StockTransferDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    private final StockTransactionDAO transactionDAO = new StockTransactionDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    /**
     * Xử lý yêu cầu GET: hiển thị giao diện kho hàng, xác định tab đang hiển thị 
     * (Tồn kho, Chuyển kho, Kiểm kho, Phê duyệt, Lịch sử...) và gọi Controller con tương ứng.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();

        if (session == null || session.getAttribute("currentUser") == null) {
            redirect(response, request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        
        // Delegate API actions and View actions BEFORE flash message extraction
        if ("searchProductsApi".equals(action)) {
            new TransferController().doGet(request, response);
            return;
        } else if ("searchAllProductsApi".equals(action)) {
            new TransferController().doGet(request, response);
            return;
        } else if ("getProductStockApi".equals(action)) {
            new StockController().doGet(request, response);
            return;
        } else if ("searchImportProductsApi".equals(action)) {
            new StockController().doGet(request, response);
            return;
        } else if ("getImportTemplateDataApi".equals(action)) {
            new StockController().doGet(request, response);
            return;
        } else if ("exportStockExcel".equals(action) || "exportExcel".equals(action)) {
            new StockController().doGet(request, response);
            return;
        } else if ("viewTicket".equals(action)) {
            new TransferController().doGet(request, response);
            return;
        } else if ("viewOrderDetails".equals(action) || "viewReceiveOrderDetails".equals(action)) {
            new OrderVoucherController().doGet(request, response);
            return;
        } else if ("printTicket".equals(action)) {
            new TransferController().doGet(request, response);
            return;
        } else if ("printOrder".equals(action)) {
            new OrderVoucherController().doGet(request, response);
            return;
        } else if ("searchStockCheckProductsApi".equals(action)) {
            new InventoryCheckController().doGet(request, response);
            return;
        } else if ("viewCheckDetails".equals(action)) {
            new InventoryCheckController().doGet(request, response);
            return;
        }

        // Extract Flash messages from session into request attributes and clear them from session
        String[] flashKeys = {"message", "error", "errorMessage", "successMessage", "warning", "warningMessage"};
        for (String key : flashKeys) {
            String val = (String) session.getAttribute(key);
            if (val != null) {
                request.setAttribute(key, val);
                session.removeAttribute(key);
            }
        }

        Employee currentUser = (Employee) session.getAttribute("currentUser");
        String role = currentUser.getRoleName() != null ? currentUser.getRoleName().toLowerCase() : "";

        if (!role.equals("owner") && !role.equals("admin") && !role.equals("warehousestaff") && !role.equals("storemanager")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này.");
            return;
        }

        // Determine accessible warehouse(s)
        int branchId = currentUser.getBranchId() != null ? currentUser.getBranchId() : 0;
        Integer selectedWarehouseId = null;

        try {
            List<Warehouse> myWarehouses = warehouseDAO.findByBranch(branchId);

            List<Warehouse> allowedWarehouses;
            // Global access for Owner & Admin
            if (role.equals("owner") || role.equals("admin")) {
                allowedWarehouses = warehouseDAO.findAll();
                if (allowedWarehouses.isEmpty()) {
                    forward(request, response, "inventory/setup_warehouse");
                    return;
                }
            } else {
                allowedWarehouses = myWarehouses; // Only own warehouses
                if (allowedWarehouses.isEmpty()) {
                    if (role.equals("storemanager") || role.equals("admin")) {
                        forward(request, response, "inventory/setup_warehouse");
                    } else {
                        forward(request, response, "inventory/no_warehouse_notice");
                    }
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
            if (filterWarehouse != null && !filterWarehouse.trim().isEmpty() && !"null".equalsIgnoreCase(filterWarehouse.trim())) {
                try {
                    selectedWarehouseId = Integer.parseInt(filterWarehouse.trim());
                    session.setAttribute("selectedWarehouseId", selectedWarehouseId);
                } catch (NumberFormatException ignored) {}
            } else {
                if (role.equals("owner") || role.equals("admin") || "true".equals(clearSelected)) {
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

            action = request.getParameter("action");

            // Calculate KPIs
            dao.inventory.InventoryDAO.DashboardKPI kpi = inventoryDAO.getDashboardKPI(allowedWarehouseIds, selectedWarehouseId);
            request.setAttribute("totalProducts", kpi.totalProducts);
            request.setAttribute("totalCategories", kpi.totalCategories);
            request.setAttribute("lowStockCount", kpi.lowStockCount);

            int pendingTransferCount = transferDAO.getPendingCount(selectedWarehouseId != null ? selectedWarehouseId : 0);
            request.setAttribute("pendingTransferCount", pendingTransferCount);

            // Calculate total pending approvals across all types (Transfers, Orders, Checks)
            new ApprovalTabController().handleApprovalTab(request, role);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> unifiedApprovals = (List<Map<String, Object>>) request.getAttribute("unifiedApprovals");
            int pendingApprovalCount = (unifiedApprovals != null) ? unifiedApprovals.size() : 0;
            request.setAttribute("pendingApprovalCount", pendingApprovalCount);

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

            dao.inventory.InventoryCheckDAO inventoryCheckDAO = new dao.inventory.InventoryCheckDAO();
            model.InventoryCheck pendingCheck = inventoryCheckDAO.getPendingCheckByWarehouse(selectedWarehouseId != null ? selectedWarehouseId : 0);
            request.setAttribute("pendingCheck", pendingCheck);

            switch (tab) {
                case "stock":
                    new StockController().handleStockTab(request, selectedWarehouseId);
                    break;
                case "transfer":
                    new TransferController().handleTransferTab(request, selectedWarehouseId, role);
                    break;
                case "check":
                    new InventoryCheckController().handleCheckTab(request, selectedWarehouseId, role);
                    break;
                case "approval":
                    new ApprovalTabController().handleApprovalTab(request, role);
                    break;
                case "history":
                    new HistoryController().handleHistoryTab(request, selectedWarehouseId, allowedWarehouseIds);
                    break;

                case "pending_vouchers":
                    new PendingVouchersController().handlePendingVouchersTab(request, selectedWarehouseId, role);
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
                    if (pendingCheck != null) {
                        request.getSession().setAttribute("error", "Kho hàng đang có phiếu kiểm kho chưa được duyệt (Mã phiếu: " + pendingCheck.getCheckCode() + "). Không thể tạo phiếu mới cho đến khi phiếu cũ được duyệt hoặc bị hủy!");
                        redirect(response, request.getContextPath() + "/inventory?tab=check&warehouseId=" + selectedWarehouseId);
                        return;
                    }
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
                    new StockController().handleStockTab(request, selectedWarehouseId);
                    request.setAttribute("activeTab", "stock");
                    break;
            }

            forward(request, response, "inventory/inventory");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Lỗi server: " + e.getMessage());
        }
    }

    /**
     * Xử lý yêu cầu POST: Tiếp nhận các hành động thay đổi dữ liệu (Action) từ client
     * và ủy quyền (delegate) cho các Controller phụ tương ứng xử lý.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            doGet(request, response);
            return;
        }

        switch (action) {
            // Setup/Update Warehouse -> Handled by WarehouseController
            case "setupWarehouse":
            case "updateWarehouse":
                new WarehouseController().doPost(request, response);
                break;

            // Import/Export/Excel -> Handled by StockController
            case "checkImportExcel":
            case "saveImport":
            case "saveExport":
            case "updateStockDirectly":
                new StockController().doPost(request, response);
                break;

            // Stock Transfer -> Handled by TransferController
            case "saveTransfer":
            case "confirmDispatch":
            case "rejectDispatch":
            case "confirmReceive":
            case "rejectReceive":
            case "cancelTransfer":
            case "confirmReceiveWithDiscrepancy":
                new TransferController().doPost(request, response);
                break;

            // Approvals -> Handled by ApprovalTabController
            case "approveTransfer":
            case "rejectTransfer":
            case "partnerApproveTransfer":
            case "partnerRejectTransfer":
            case "partnerApproveTransferAll":
            case "partnerRejectTransferAll":
                new ApprovalTabController().doPost(request, response);
                break;

            // Orders -> Handled by OrderVoucherController
            case "approveOrder":
            case "rejectOrder":
            case "cancelOrder":
            case "confirmReceiveOrder":
                new OrderVoucherController().doPost(request, response);
                break;

            // Inventory Check -> Handled by InventoryCheckController
            case "saveCheck":
            case "updateCheck":
            case "approveCheck":
            case "rejectCheck":
            case "cancelCheck":
                new InventoryCheckController().doPost(request, response);
                break;

            default:
                doGet(request, response);
                break;
        }
    }

    /*
     * =========================================================================
     * CODE ĐÃ ĐƯỢC CHUYỂN SANG CÁC CONTROLLER CHUYÊN BIỆT (COMMENT LẠI THEO YÊU CẦU)
     * =========================================================================
     *
     * 1. handleStockTab(...), handleSearchImportProductsApi(...), handleGetImportTemplateDataApi(...) 
     *    -> Chuyển sang StockController.java
     * 
     * 2. handleTransferTab(...), handleSearchProductsApi(...), handleSearchAllProductsApi(...)
     *    -> Chuyển sang TransferController.java
     * 
     * 3. handleCheckTab(...), handleSearchStockCheckProductsApi(...)
     *    -> Chuyển sang InventoryCheckController.java
     * 
     * 4. handleImportTab(...), handleExportTab(...)
     *    -> Chuyển sang OrderVoucherController.java
     * 
     * 5. handleHistoryTab(...)
     *    -> Chuyển sang HistoryController.java
     * 
     * 6. handleApprovalTab(...)
     *    -> Chuyển sang ApprovalTabController.java
     * 
     * 7. handlePendingVouchersTab(...)
     *    -> Chuyển sang PendingVouchersController.java
     */
}
