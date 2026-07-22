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

@WebServlet(name = "DashboardController", urlPatterns = {"/dashboard", "/dashboard/owner", "/dashboard/inventory", "/dashboard/financial"})
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
        HttpSession session = request.getSession(false);
        Employee currentUser = (session == null) ? null : (Employee) session.getAttribute("currentUser");
        String role = (currentUser != null && currentUser.getRoleName() != null) ? currentUser.getRoleName().trim().toLowerCase() : "";

        // Route cho /dashboard và /dashboard/owner
        if ("/dashboard".equals(path) || "/dashboard/owner".equals(path)) {
            if ("storemanager".equals(role)) {
                int userBranchId = (currentUser != null && currentUser.getBranchId() != null) ? currentUser.getBranchId() : 1;
                try {
                    DashboardOverview overview = dashboardDAO.getBranchOverview(userBranchId);
                    request.setAttribute("overview", overview);
                    
                    List<ActivityLog> recentActivities = activityLogDAO.findRecentByBranch(userBranchId, 5);
                    request.setAttribute("recentActivities", recentActivities);

                    dao.branch.BranchDAO branchDAO = new dao.branch.BranchDAO();
                    model.Branch branch = branchDAO.findById(userBranchId);
                    request.setAttribute("currentBranch", branch);

                    dao.employee.EmployeeDAO empDAO = new dao.employee.EmployeeDAO();
                    List<Employee> branchEmployees = empDAO.getByBranch(userBranchId);
                    request.setAttribute("branchEmployees", branchEmployees);

                    request.setAttribute("isStoreManagerView", true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    request.setAttribute("overview", new DashboardOverview());
                }
                forward(request, response, "dashboard/owner");
                return;
            } else if ("admin".equals(role) || "owner".equals(role)) {
                try {
                    DashboardOverview overview = dashboardDAO.getOwnerOverview();
                    request.setAttribute("overview", overview);
                    List<ActivityLog> recentActivities = activityLogDAO.findRecent(5);
                    request.setAttribute("recentActivities", recentActivities);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    request.setAttribute("overview", new DashboardOverview());
                }
                forward(request, response, "dashboard/owner");
                return;
            } else if ("warehousestaff".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/inventory");
                return;
            } else if ("salesstaff".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/sales");
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        }

        // Nạp dữ liệu Overview cho Financial Dashboard (Chỉ dành cho Owner/Admin)
        if ("/dashboard/financial".equals(path)) {
            if (!isOwner(request)) {
                response.sendError(403, "Bạn không có quyền truy cập chức năng Tài chính.");
                return;
            }
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
        if (role == null) return false;
        String r = role.trim().toLowerCase();
        return "owner".equals(r) || "admin".equals(r);
    }
}
