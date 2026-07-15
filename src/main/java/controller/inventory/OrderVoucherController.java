package controller.inventory;

import dao.sales.OrderDAO;
import dao.purchase.PurchaseOrderDAO;
import dao.inventory.StockTransactionDAO;
import model.Employee;
import model.Order;
import model.PurchaseOrder;
import model.StockTransaction;
import service.inventory.InventoryExecutionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "OrderVoucherController", urlPatterns = {"/inventory-order"})
public class OrderVoucherController extends InventoryBaseController {

    private final OrderDAO orderDAO = new OrderDAO();
    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    private final StockTransactionDAO transactionDAO = new StockTransactionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            // [MOVED FROM InventoryController] - Original lines 172-183
            if ("viewOrderDetails".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                model.Order order = orderDAO.findById(orderId);
                List<model.OrderDetail> details = orderDAO.findDetailsByOrderId(orderId);
                List<model.StockTransaction> txs = transactionDAO.findByReference("PURCHASE_ORDER", orderId);
                
                request.setAttribute("order", order);
                request.setAttribute("orderDetails", details);
                request.setAttribute("transactions", txs);
                
                forward(request, response, "inventory/modals/_modal_order_details");
                return;
            // [MOVED FROM InventoryController] - Original lines 201-212
            } else if ("printOrder".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                model.Order order = orderDAO.findById(orderId);
                List<model.OrderDetail> details = orderDAO.findDetailsByOrderId(orderId);
                List<model.StockTransaction> txs = transactionDAO.findByReference("PURCHASE_ORDER", orderId);
                
                request.setAttribute("order", order);
                request.setAttribute("orderDetails", details);
                request.setAttribute("transactions", txs);
                
                forward(request, response, "inventory/prints/_print_order");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
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
                // [MOVED FROM InventoryController] - Original lines 1706-1718
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
                // [MOVED FROM InventoryController] - Original lines 1719-1729
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
                // [MOVED FROM InventoryController] - Original lines 1731-1751
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

    // [MOVED FROM InventoryController] - Original lines 405-411
    void handleImportTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        List<PurchaseOrder> imports = purchaseOrderDAO.findAllByWarehouseAndType(warehouseId != null ? warehouseId : 0, "PURCHASE", null);
        if ("WarehouseStaff".equalsIgnoreCase(role)) {
            imports.removeIf(po -> !"PENDING".equals(po.getStatus()));
        }
        request.setAttribute("imports", imports);
    }

    // [MOVED FROM InventoryController] - Original lines 413-419
    void handleExportTab(HttpServletRequest request, Integer warehouseId, String role) throws Exception {
        List<PurchaseOrder> exports = purchaseOrderDAO.findAllByWarehouseAndType(warehouseId != null ? warehouseId : 0, "EXPORT", null);
        if ("WarehouseStaff".equalsIgnoreCase(role)) {
            exports.removeIf(po -> !"PENDING".equals(po.getStatus()));
        }
        request.setAttribute("exports", exports);
    }
}
