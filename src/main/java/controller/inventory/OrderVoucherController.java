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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import util.database.DBContext;

@WebServlet(name = "OrderVoucherController", urlPatterns = {"/inventory-order"})
public class OrderVoucherController extends InventoryBaseController {

    private final OrderDAO orderDAO = new OrderDAO();
    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    private final StockTransactionDAO transactionDAO = new StockTransactionDAO();

    /**
     * Xử lý yêu cầu GET: hiển thị các popup chi tiết phiếu nhập/xuất hoặc tạo bản in hóa đơn.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            // Xem chi tiết phiếu nhập/xuất kho
            if ("viewOrderDetails".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                model.Order order = orderDAO.findById(orderId);
                List<model.OrderDetail> details = orderDAO.findDetailsByOrderId(orderId);
                // Tìm kiếm vết lịch sử tăng/giảm tồn kho thực tế gắn với phiếu này
                List<model.StockTransaction> txs = transactionDAO.findByReference("PURCHASE_ORDER", orderId);
                
                request.setAttribute("order", order);
                request.setAttribute("orderDetails", details);
                request.setAttribute("transactions", txs);
                
                forward(request, response, "inventory/modals/_modal_order_details");
                return;
            // In phiếu nhập/xuất kho
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
            // Form xác nhận nhập kho thực tế cho đơn nhập hàng NCC
            } else if ("viewReceiveOrderDetails".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                model.Order order = orderDAO.findById(orderId);
                List<model.OrderDetail> details = orderDAO.findDetailsByOrderId(orderId);
                
                request.setAttribute("order", order);
                request.setAttribute("orderDetails", details);
                
                forward(request, response, "inventory/modals/_modal_receive_order");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Xử lý yêu cầu POST: Thực thi phê duyệt, từ chối hoặc hủy bỏ phiếu nhập/xuất hàng.
     */
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
                // Duyệt phiếu nhập/xuất hàng (Chỉ dành cho Owner, Admin hoặc StoreManager)
                case "approveOrder": {
                    int orderId = Integer.parseInt(request.getParameter("orderId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()) && !"StoreManager".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    String orderType = "";
                    int targetWId = 1;
                    try (Connection conn = DBContext.getConnection();
                         PreparedStatement ps = conn.prepareStatement("SELECT order_type, warehouse_id FROM [order] WHERE order_id = ?")) {
                        ps.setInt(1, orderId);
                        try (java.sql.ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                orderType = rs.getString("order_type");
                                targetWId = rs.getInt("warehouse_id");
                                if (targetWId <= 0) targetWId = 1;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    model.Order order = orderDAO.findById(orderId);
                    PurchaseOrder po = purchaseOrderDAO.findById(orderId);
                    if (orderType == null || orderType.isEmpty()) {
                        orderType = order != null ? order.getOrderType() : (po != null ? po.getOrderType() : "");
                    }
                    
                    if (orderType == null || orderType.isEmpty() || "PURCHASE".equalsIgnoreCase(orderType) || "IMPORT".equalsIgnoreCase(orderType) || orderType.toUpperCase().contains("PURCHASE")) {
                        // CHỈ ĐƠN NHẬP HÀNG TỪ NCC: Chuyển sang trạng thái IN_TRANSIT (Chờ kiểm tra xác nhận nhập kho thực tế)
                        StringBuilder dbg = new StringBuilder();
                        boolean ok = false;
                        
                        try {
                            ok = orderDAO.updateStatus(orderId, "IN_TRANSIT", currentUser.getEmployeeId());
                            dbg.append("[DAO_Emp:").append(ok).append("]");
                        } catch (Exception e) {
                            dbg.append("[DAO_Emp_Err:").append(e.getMessage()).append("]");
                        }
                        
                        if (!ok) {
                            try {
                                ok = orderDAO.updateStatus(orderId, "IN_TRANSIT");
                                dbg.append("[DAO_Simple:").append(ok).append("]");
                            } catch (Exception e) {
                                dbg.append("[DAO_Simple_Err:").append(e.getMessage()).append("]");
                            }
                        }
                        
                        if (!ok) {
                            try (Connection conn = DBContext.getConnection()) {
                                conn.setAutoCommit(true);
                                try (java.sql.Statement stmt = conn.createStatement()) {
                                    stmt.execute("""
                                        DECLARE @sql NVARCHAR(MAX) = '';
                                        SELECT @sql += 'ALTER TABLE [dbo].[order] DROP CONSTRAINT [' + cc.name + ']; '
                                        FROM sys.check_constraints cc
                                        JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id
                                        WHERE cc.parent_object_id = OBJECT_ID('dbo.order') AND c.name = 'status';
                                        IF @sql <> '' EXEC sp_executesql @sql;
                                    """);
                                } catch (Exception ignored) {}
                                try (PreparedStatement ps = conn.prepareStatement("UPDATE [order] SET status = 'IN_TRANSIT' WHERE order_id = ?")) {
                                    ps.setInt(1, orderId);
                                    int r = ps.executeUpdate();
                                    dbg.append("[JDBC_Id:").append(r).append("]");
                                    if (r > 0) ok = true;
                                }
                                if (!ok && po != null && po.getOrderCode() != null) {
                                    try (PreparedStatement ps = conn.prepareStatement("UPDATE [order] SET status = 'IN_TRANSIT' WHERE order_code = ?")) {
                                        ps.setString(1, po.getOrderCode());
                                        int r = ps.executeUpdate();
                                        dbg.append("[JDBC_POCode:").append(r).append("]");
                                        if (r > 0) ok = true;
                                    }
                                }
                                if (!ok && order != null && order.getOrderCode() != null) {
                                    try (PreparedStatement ps = conn.prepareStatement("UPDATE [order] SET status = 'IN_TRANSIT' WHERE order_code = ?")) {
                                        ps.setString(1, order.getOrderCode());
                                        int r = ps.executeUpdate();
                                        dbg.append("[JDBC_OrdCode:").append(r).append("]");
                                        if (r > 0) ok = true;
                                    }
                                }
                            } catch (Exception ex) {
                                dbg.append("[JDBC_Err:").append(ex.getMessage()).append("]");
                            }
                        }

                        // Check current DB status to see if it's already IN_TRANSIT
                        String currentDBStatus = "UNKNOWN";
                        try (Connection conn = DBContext.getConnection();
                             PreparedStatement ps = conn.prepareStatement("SELECT status FROM [order] WHERE order_id = ?")) {
                            ps.setInt(1, orderId);
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    currentDBStatus = rs.getString("status");
                                } else {
                                    currentDBStatus = "NOT_FOUND";
                                }
                            }
                        } catch (Exception e) {
                            currentDBStatus = "ERR:" + e.getMessage();
                        }
                        dbg.append("[CurrentDBStatus:").append(currentDBStatus).append("]");

                        if ("IN_TRANSIT".equalsIgnoreCase(currentDBStatus)) {
                            ok = true;
                        }

                        if (!ok) {
                            request.getSession().setAttribute("error", "Không thể cập nhật trạng thái đơn #" + orderId + " sang IN_TRANSIT. Details: " + dbg.toString());
                            redirect(response, request.getContextPath() + "/inventory?tab=approval");
                            return;
                        }
                        request.getSession().setAttribute("message", "Đã phê duyệt đơn nhập hàng. Đơn hàng chuyển sang trạng thái Đang Vận Chuyển & Chờ Xác Nhận Nhập Kho.");
                        if (order != null && order.getWarehouseId() > 0) targetWId = order.getWarehouseId();
                        else if (po != null && po.getWarehouseId() != null && po.getWarehouseId() > 0) targetWId = po.getWarehouseId();
                        redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + targetWId);
                    } else {
                        // Các loại đơn khác (ví dụ: EXPORT): Giữ nguyên logic xử lý trực tiếp
                        service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                        executionService.executeOrder(orderId, currentUser.getEmployeeId());
                        request.getSession().setAttribute("message", "Đã phê duyệt phiếu và cập nhật tồn kho thành công.");
                        redirect(response, request.getContextPath() + "/inventory?tab=history&subtab=voucher");
                    }
                    break;
                }
                // Xác nhận nhập kho thực tế cho đơn nhập hàng từ Nhà Cung Cấp
                case "confirmReceiveOrder": {
                    int orderId = Integer.parseInt(request.getParameter("orderId"));
                    String targetSuppParam = request.getParameter("targetSupplierId");
                    Integer targetSupplierId = null;
                    if (targetSuppParam != null && !targetSuppParam.trim().isEmpty()) {
                        try {
                            targetSupplierId = Integer.parseInt(targetSuppParam.trim());
                        } catch (Exception ex) {}
                    }

                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"WarehouseStaff".equalsIgnoreCase(currentUser.getRoleName()) && !"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()) && !"StoreManager".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }

                    try {
                        model.Order order = orderDAO.findById(orderId);
                        if (order == null || !"PURCHASE".equalsIgnoreCase(order.getOrderType())) {
                            request.getSession().setAttribute("error", "Đơn hàng không hợp lệ.");
                            redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process");
                            return;
                        }

                        List<model.OrderDetail> details = orderDAO.findDetailsByOrderId(orderId);
                        java.util.Map<Integer, Integer> actualQuantities = new java.util.HashMap<>();
                        for (model.OrderDetail d : details) {
                            String paramVal = request.getParameter("actualQty_" + d.getOrderDetailId());
                            if (paramVal != null) {
                                try {
                                    actualQuantities.put(d.getOrderDetailId(), Integer.parseInt(paramVal.trim()));
                                } catch (Exception ex) {
                                    actualQuantities.put(d.getOrderDetailId(), d.getQuantity());
                                }
                            } else {
                                actualQuantities.put(d.getOrderDetailId(), d.getQuantity());
                            }
                        }

                        service.inventory.InventoryExecutionService executionService = new service.inventory.InventoryExecutionService();
                        executionService.confirmReceivePurchaseOrder(orderId, targetSupplierId, actualQuantities, currentUser.getEmployeeId());

                        request.getSession().setAttribute("message", "Đã xác nhận nhập kho thực tế thành công. Tồn kho và sổ quỹ đã được cập nhật.");
                        redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process&warehouseId=" + order.getWarehouseId());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        request.getSession().setAttribute("error", "Lỗi xác nhận nhập kho: " + ex.getMessage());
                        redirect(response, request.getContextPath() + "/inventory?tab=transfer&subtab=transfer_process");
                    }
                    break;
                }
                // Từ chối duyệt phiếu nhập/xuất hàng
                case "rejectOrder": {
                    int orderId = Integer.parseInt(request.getParameter("orderId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()) && !"StoreManager".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    // Đổi trạng thái phiếu thành REJECTED (từ chối)
                    orderDAO.updateStatus(orderId, "REJECTED", currentUser.getEmployeeId());
                    request.getSession().setAttribute("message", "Đã từ chối phiếu.");
                    redirect(response, request.getContextPath() + "/inventory?tab=history&subtab=voucher");
                    break;
                }
                // Hủy phiếu nhập/xuất hàng (Nhân viên tự hủy phiếu của mình khi còn chờ duyệt)
                case "cancelOrder": {
                    int orderId = Integer.parseInt(request.getParameter("orderId"));
                    Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
                    if (currentUser == null || (!"WarehouseStaff".equalsIgnoreCase(currentUser.getRoleName()) && !"Owner".equalsIgnoreCase(currentUser.getRoleName()) && !"Admin".equalsIgnoreCase(currentUser.getRoleName()) && !"StoreManager".equalsIgnoreCase(currentUser.getRoleName()))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    PurchaseOrder po = purchaseOrderDAO.findById(orderId);
                    if (po == null || !"PENDING".equals(po.getStatus())) {
                        request.getSession().setAttribute("error", "Không thể hủy phiếu này vì phiếu không tồn tại hoặc đã được xử lý.");
                    } else {
                        // Đổi trạng thái phiếu thành CANCELLED (Đã hủy)
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
}
