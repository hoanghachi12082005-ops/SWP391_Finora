package       controller.dashboard;

import       controller.common.BaseController;
import dao.dashboard.DashboardDAO;
import dao.system.ActivityLogDAO;
import model.ActivityLog;
import model.DashboardOverview;
import model.Employee;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "DashboardController", urlPatterns = {"/dashboard/owner", "/dashboard/inventory", "/dashboard/financial"})
public class DashboardController extends BaseController {

    private ActivityLogDAO activityLogDAO;
    private DashboardDAO dashboardDAO;

    @Override
    public void init() throws ServletException {
        activityLogDAO = new ActivityLogDAO();
        dashboardDAO = new DashboardDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Card "Hoạt động gần đây" chỉ load dữ liệu khi user là Owner.
        // Vai trò khác sẽ không thấy card này (xem owner.jsp).
        if (isOwner(request)) {
            try {
                List<ActivityLog> recentActivities = activityLogDAO.findRecent(5);
                request.setAttribute("recentActivities", recentActivities);
            } catch (SQLException ex) {
                ex.printStackTrace();
                request.setAttribute("recentActivities", Collections.emptyList());
            }
        } else {
            request.setAttribute("recentActivities", Collections.emptyList());
        }

        // Nạp dữ liệu Overview cho Owner Dashboard
        if ("/dashboard/owner".equals(path)) {
            try {
                DashboardOverview overview = dashboardDAO.getOwnerOverview();
                request.setAttribute("overview", overview);
            } catch (SQLException ex) {
                ex.printStackTrace();
                request.setAttribute("overview", new DashboardOverview());
                request.setAttribute("overviewError",
                        "Không thể tải dữ liệu tổng quan. Vui lòng thử lại sau.");
            }
        }

        // Nạp dữ liệu Overview cho Financial Dashboard
        if ("/dashboard/financial".equals(path)) {
            String range = request.getParameter("range");
            if (range == null || range.trim().isEmpty()) {
                range = "month";
            }
            try {
                DashboardDAO.FinancialData financialData = dashboardDAO.getFinancialData(range);
                request.setAttribute("totalRevenue", financialData.totalRevenue);
                request.setAttribute("totalExpenses", financialData.totalExpenses);
                request.setAttribute("netProfit", financialData.netProfit);
                request.setAttribute("totalInvoices", financialData.totalInvoices);
                request.setAttribute("branchRevenues", financialData.branchRevenues);
                request.setAttribute("selectedRange", range);
                
                // Nạp danh sách phát sinh chi tiết toàn hệ thống
                List<model.Payment> globalPayments = dashboardDAO.getBranchPayments(range, null);
                request.setAttribute("globalPayments", globalPayments);
            } catch (SQLException ex) {
                ex.printStackTrace();
                request.setAttribute("financialError", "Không thể tải dữ liệu tài chính. Vui lòng thử lại sau.");
            }
        }

        switch (path) {
        case "/dashboard/owner": forward(request, response, "dashboard/owner"); break;
        case "/dashboard/inventory": forward(request, response, "dashboard/inventory"); break;
        case "/dashboard/financial": forward(request, response, "dashboard/financial"); break;
            default: forward(request, response, "dashboard/owner"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }

    private boolean isOwner(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = (session == null) ? null : session.getAttribute("currentUser");
        if (!(user instanceof Employee)) return false;
        String role = ((Employee) user).getRoleName();
        return role != null && "Owner".equalsIgnoreCase(role.trim());
    }
}
