package controller.user;

import java.io.IOException;
import dao.sales.OrderDAO;
import dao.user.ProfileDao;
import jakarta.servlet.ServletException;
import java.util.List;
import model.Order;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import dao.user.UserManagementDao;
import model.Employee;
import util.auth.AuthUtil;
import util.pagination.PaginationHelper;
import util.pagination.PaginationHelper.PageResult;

@WebServlet(name = "ManagerEmployeeServlet", urlPatterns = {"/manager/emp"})
public class ManagerEmployeeServlet extends HttpServlet {

    private UserManagementDao managerEmployeeDao;

    @Override
    public void init() throws ServletException {
        managerEmployeeDao = new UserManagementDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!AuthUtil.requireManager(request, response)) {
            return;
        }

        Integer branchID = AuthUtil.getBranchId(request);
        if (branchID == null || branchID <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài khoản của bạn chưa được gán cho chi nhánh nào.");
            return;
        }

        String action = AuthUtil.getParam(request, "action", "list");

        if ("detail".equals(action)) {
            viewEmployeeProfile(request, response, branchID);
            return;
        } else {
            request.setAttribute("formMode", "list");
        }

        loadPageData(request, branchID);

        request.setAttribute("pageTitle", "Nhân viên chi nhánh");
        request.setAttribute("pageSubtitle", "Danh sách nhân viên tại chi nhánh");
        request.setAttribute("addButtonText", "");
        request.setAttribute("baseUrl", request.getContextPath() + "/manager/emp");

        request.setAttribute("showBranch", false);
        request.setAttribute("canCreate", false);
        request.setAttribute("canEdit", false);
        request.setAttribute("canLock", false);
        request.setAttribute("canResetPassword", false);

        request.getRequestDispatcher("/views/users/user-list.jsp")
                .forward(request, response);
    }

    private void loadPageData(HttpServletRequest request, int branchID) {
        String keyword = request.getParameter("keyword");
        String roleID = request.getParameter("roleId");
        String status = request.getParameter("status");

        int page = AuthUtil.parseInt(request.getParameter("page"), 1);
        int sizeValue = AuthUtil.parseInt(request.getParameter("sizeValue"), 30);

        int totalUsers = managerEmployeeDao.countEmployeesByBranch(branchID, keyword, roleID, status);
        PageResult pr = PaginationHelper.compute(totalUsers, page, sizeValue);
        pr.setAttributes(request);

        request.setAttribute(
                "users",
                managerEmployeeDao.getEmployeesByBranch(branchID, keyword, roleID, status, pr.getCurrentPage(), pr.getPageSize())
        );

        request.setAttribute("roles", managerEmployeeDao.getEmployeeRoles());

        request.setAttribute("keyword", keyword);
        request.setAttribute("roleFilter", AuthUtil.parseInt(roleID, -1));
        request.setAttribute("statusFilter", status);
        request.setAttribute("totalUsers", totalUsers);

        request.setAttribute("employeeOverview", managerEmployeeDao.getManagerEmployeeOverview(branchID));
    }

    private void viewEmployeeProfile(HttpServletRequest request, HttpServletResponse response, int branchID)
        throws ServletException, IOException {

        int employeeID = AuthUtil.parseInt(request.getParameter("id"), -1);

        if (employeeID <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã nhân viên không hợp lệ.");
            return;
        }

        Employee profile = managerEmployeeDao.getEmployeeByIdInBranch(employeeID, branchID);

        if (profile == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn chỉ có thể xem nhân viên trong chi nhánh của mình.");
            return;
        }

        ProfileDao profileDao = new ProfileDao();

        request.setAttribute("profile", profile);
        request.setAttribute("salesSummary", profileDao.getEmployeeSalesSummaryInBranch(employeeID, branchID));

        boolean isSalesStaff = profile.getRoleID() == 4
            || (profile.getRoleName() != null && profile.getRoleName().toLowerCase().contains("sales"));
        if (isSalesStaff) {
            int page = AuthUtil.parseInt(request.getParameter("page"), 1);
            int sizeValue = AuthUtil.parseInt(request.getParameter("sizeValue"), 10);
            int totalRecords = new OrderDAO().countByEmployeeId(employeeID);
            PageResult pr = PaginationHelper.compute(totalRecords, page, sizeValue);
            pr.setAttributes(request);

            request.setAttribute("orderHistory", new OrderDAO().findByEmployeeIdPaged(
                    employeeID, (pr.getCurrentPage() - 1) * pr.getPageSize(), pr.getPageSize()));
            request.setAttribute("baseUrl", request.getContextPath() + "/manager/emp");
            request.setAttribute("queryString", "&action=detail&id=" + employeeID);
        }
        request.setAttribute("showSalesSection", isSalesStaff);

        request.setAttribute("readOnlyProfile", true);
        request.setAttribute("profileTitle", "Hồ sơ nhân viên");
        request.setAttribute("profileSubtitle", "Xem thông tin nhân viên và hiệu suất bán hàng tại chi nhánh");
        request.setAttribute("backUrl", request.getContextPath() + "/manager/emp");

        request.getRequestDispatcher("/views/profile/profile.jsp")
                .forward(request, response);
    }
}