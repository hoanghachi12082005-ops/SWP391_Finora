package controller.user;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

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
/**
 *
 * @author PCQN
 */
import dao.user.UserManagementDao;
import jakarta.servlet.http.HttpSession;
import model.Employee;

@WebServlet(name = "ManagerEmployeeServlet", urlPatterns = {"/manager/emp"})
public class ManagerEmployeeServlet extends HttpServlet {

    private UserManagementDao  managerEmployeeDao;

    @Override
    public void init() throws ServletException {
        managerEmployeeDao = new  UserManagementDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isStoreManager(request, response)) {
            return;
        }

        int branchID = getLoggedInBranchID(request);

        if (branchID <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your account is not assigned to any branch.");
            return;
        }

        String action = getParam(request, "action", "list");

            if ("detail".equals(action)) {
            viewEmployeeProfile(request, response, branchID);
            return;
        }else {
            request.setAttribute("formMode", "list");
        }

        loadPageData(request, branchID);

        request.setAttribute("pageTitle", "Branch Employee List");
        request.setAttribute("pageSubtitle", "Store Manager views employee accounts in the assigned branch");
        request.setAttribute("addButtonText", "");
        request.setAttribute("baseUrl", request.getContextPath() + "/manager/emp");

        request.setAttribute("showBranch", true);
        request.setAttribute("canCreate", false);
        request.setAttribute("canEdit", false);
        request.setAttribute("canLock", false);
        request.setAttribute("canResetPassword", false);

        // FIX: JSP nằm tại webapp/views/users/ không phải /view/user/
        request.getRequestDispatcher("/views/users/user-list.jsp")
                .forward(request, response);
    }

    private void loadPageData(HttpServletRequest request, int branchID) {
        String keyword = request.getParameter("keyword");
        String roleID = request.getParameter("roleId");
        String status = request.getParameter("status");

        String pageSizeOption = getParam(request, "pageSize", "5");

        int totalUsers = managerEmployeeDao.countEmployeesByBranch(branchID, keyword, roleID, status);
        int pageSize = resolvePageSize(pageSizeOption, totalUsers);

        int currentPage = parseInt(request.getParameter("page"), 1);

        if (currentPage < 1) {
            currentPage = 1;
        }

        int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

        if (totalPages < 1) {
            totalPages = 1;
        }

        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        request.setAttribute(
                "users",
                managerEmployeeDao.getEmployeesByBranch(branchID, keyword, roleID, status, currentPage, pageSize)
        );

        request.setAttribute("roles", managerEmployeeDao.getEmployeeRoles());

        request.setAttribute("keyword", keyword);
        request.setAttribute("roleFilter", parseInt(roleID, -1));
        request.setAttribute("statusFilter", status);

        request.setAttribute("currentPage", currentPage);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("pageSizeOption", pageSizeOption);
        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("totalPages", totalPages);

        request.setAttribute("employeeOverview", managerEmployeeDao.getManagerEmployeeOverview(branchID));
    }
    private int resolvePageSize(String pageSizeOption, int totalUsers) {
        if (isBlank(pageSizeOption)) {
            return 5;
        }

        String option = pageSizeOption.trim().toLowerCase();

        if ("30p".equals(option) || "30%".equals(option) || "30".equals(option)) {
            return Math.max(1, (int) Math.ceil(totalUsers * 0.3));
        }

        if ("50p".equals(option) || "50%".equals(option) || "50".equals(option)) {
            return Math.max(1, (int) Math.ceil(totalUsers * 0.5));
        }

        int size = parseInt(option, 5);

        if (size != 5 && size != 10) {
            size = 5;
        }

        return size;
    }

    private void viewEmployeeProfile(HttpServletRequest request, HttpServletResponse response, int branchID)
        throws ServletException, IOException {

        int employeeID = parseInt(request.getParameter("id"), -1);

        if (employeeID <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid employee ID.");
            return;
        }

        Employee profile = managerEmployeeDao.getEmployeeByIdInBranch(employeeID, branchID);

        if (profile == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You can only view employees in your branch.");
            return;
        }

        ProfileDao profileDao = new ProfileDao();

        request.setAttribute("profile", profile);
        request.setAttribute("salesSummary", profileDao.getEmployeeSalesSummaryInBranch(employeeID, branchID));

        boolean isSalesStaff = profile.getRoleID() == 4
            || (profile.getRoleName() != null && profile.getRoleName().toLowerCase().contains("sales"));
        if (isSalesStaff) {
            request.setAttribute("orderHistory", new OrderDAO().findByEmployeeId(employeeID));
        }
        request.setAttribute("showSalesSection", isSalesStaff);

        request.setAttribute("readOnlyProfile", true);
        request.setAttribute("profileTitle", "Employee Profile");
        request.setAttribute("profileSubtitle", "Store Manager views employee information and sales performance");
        request.setAttribute("backUrl", request.getContextPath() + "/manager/emp");

        // FIX: Đường dẫn đúng: /views/profile/ (có chữ 's', không phải /view/)
        request.getRequestDispatcher("/views/profile/profile.jsp")
                .forward(request, response);
    }
    private boolean isStoreManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        // FIX: Đọc đúng key "currentUser" — nhất quán với AuthServlet
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        // FIX: Lấy roleName từ Employee object, không phải từ session attribute riêng lẻ
        Employee currentUser = (Employee) session.getAttribute("currentUser");
        String roleName = currentUser.getRoleName();

        // FIX: So sánh đúng tên role ("Store Manager" trong DB, không phải "StoreManager")
        if (!"Store Manager".equalsIgnoreCase(roleName)
                && !"Admin".equalsIgnoreCase(roleName)
                && !"Owner".equalsIgnoreCase(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. StoreManager only.");
            return false;
        }

        return true;
    }

    private int getLoggedInBranchID(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return -1;
        }

        // FIX: Lấy branchID từ Employee object trong session với key "currentUser"
        Object employeeObj = session.getAttribute("currentUser");

        if (employeeObj instanceof Employee) {
            Employee employee = (Employee) employeeObj;

            if (employee.getBranchID() != null) {
                return employee.getBranchID();
            }
        }

        return -1;
    }

    private String getParam(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return isBlank(value) ? defaultValue : value.trim();
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}