package controller.inventory;

import dao.sales.OrderDAO;
import dao.inventory.StockTransferDAO;
import model.Employee;
import model.Order;
import model.StockTransfer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import service.inventory.InventoryExecutionService;

@WebServlet(name = "ApprovalController", urlPatterns = {"/approval"})
public class ApprovalController extends HttpServlet {

    private OrderDAO orderDAO = new OrderDAO();
    private StockTransferDAO transferDAO = new StockTransferDAO();
    private InventoryExecutionService executionService = new InventoryExecutionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String roleName = currentUser.getRoleName();
        if (!"Owner".equals(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }

        int branchId = currentUser.getBranchId() != null ? currentUser.getBranchId() : 0;

        // Fetch pending transfers
        List<StockTransfer> pendingTransfers = null;
        try {
            pendingTransfers = transferDAO.findAllByStatus(0, "PENDING_DISPATCH");
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setAttribute("pendingTransfers", pendingTransfers);
        request.getRequestDispatcher("/views/inventory/approval.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
        if (currentUser == null || !"Owner".equals(currentUser.getRoleName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("approveOrder".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                executionService.executeOrder(orderId, currentUser.getEmployeeId());
                request.getSession().setAttribute("message", "Duyệt đơn hàng và cập nhật tồn kho thành công.");
            } else if ("rejectOrder".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                orderDAO.updateStatus(orderId, "CANCELLED");
                request.getSession().setAttribute("message", "Đã từ chối đơn hàng.");
            } else if ("approveTransfer".equals(action)) {
                int transferId = Integer.parseInt(request.getParameter("transferId"));
                transferDAO.updateStatus(transferId, "APPROVED_DISPATCH");
                request.getSession().setAttribute("message", "Duyệt phiếu điều chuyển thành công. (Chờ xuất kho)");
            } else if ("rejectTransfer".equals(action)) {
                int transferId = Integer.parseInt(request.getParameter("transferId"));
                transferDAO.updateStatus(transferId, "CANCELLED");
                request.getSession().setAttribute("message", "Đã từ chối phiếu điều chuyển.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        response.sendRedirect(request.getContextPath() + "/approval");
    }
}
