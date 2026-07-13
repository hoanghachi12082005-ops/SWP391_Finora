package controller.customer;

import dao.customer.CustomerDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import model.Customer;
import model.Employee;
import service.system.ActivityLogService;
import util.pagination.PaginationHelper;
import util.pagination.PaginationHelper.PageResult;

/**
 * CustomerController - Refactored according to role permissions and dynamic rules.
 * Handles normal Web UI flows and POS API search/create/edit endpoints.
 */
@WebServlet(name = "CustomerController", urlPatterns = {"/customers"})
public class CustomerController extends HttpServlet {

    private CustomerDAO customerDAO;
    private ActivityLogService activityLogService;

    @Override
    public void init() throws ServletException {
        customerDAO = new CustomerDAO();
        activityLogService = new ActivityLogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAuthorized(request, response)) {
            return;
        }

        String action = getParam(request, "action", "list");

        // AJAX/API Search for POS
        if ("search-api".equals(action)) {
            handleSearchApi(request, response);
            return;
        }

        switch (action) {
            case "add":
                request.setAttribute("formMode", "add");
                break;

            case "edit":
                loadSelectedCustomer(request, "editingCustomer");
                request.setAttribute("formMode", "edit");
                break;

            case "detail":
                loadCustomerDetails(request);
                request.setAttribute("formMode", "detail");
                break;

            default:
                request.setAttribute("formMode", "list");
                break;
        }

        loadPageData(request);

        request.setAttribute("pageTitle", "Quản lý khách hàng");
        request.setAttribute("pageSubtitle", "Quản lý thông tin khách hàng, điểm tích lũy và lịch sử mua hàng");
        request.setAttribute("addButtonText", "Thêm khách hàng");
        request.setAttribute("baseUrl", request.getContextPath() + "/customers");

        // Permissions for UI
        Employee user = getLoggedInUser(request);
        String role = user != null ? user.getRoleName() : "";
        boolean isOwner = "Owner".equalsIgnoreCase(role);
        boolean isStoreManager = "StoreManager".equalsIgnoreCase(role);

        request.setAttribute("canCreate", isStoreManager);
        request.setAttribute("canEdit", isStoreManager);
        request.setAttribute("canDelete", isOwner || isStoreManager);
        request.setAttribute("canRedeem", isOwner || isStoreManager);
        // ponytail: isAdmin kept for modal readonly compat; only StoreManager reaches modal
        request.setAttribute("isAdmin", isStoreManager);

        request.getRequestDispatcher("/views/customers/customer-list.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAuthorized(request, response)) {
            return;
        }

        String action = getParam(request, "action", "list");

        if ("create-api".equals(action)) {
            handleCreateApi(request, response);
            return;
        }

        if ("update-api".equals(action)) {
            handleUpdateApi(request, response);
            return;
        }

        switch (action) {
            case "create":
            case "update":
                saveCustomer(request, action);
                break;

            case "delete":
                deleteCustomer(request);
                break;

            case "sync-loyalty":
                syncLoyalty(request);
                break;

            case "redeem-points":
                redeemPoints(request);
                break;

            default:
                setFlash(request, "errorMessage", "Thao tác không hợp lệ.");
                break;
        }

        if ("sync-loyalty".equals(action) || "redeem-points".equals(action)) {
            int customerId = parseInt(request.getParameter("customerId"), -1);
            response.sendRedirect(request.getContextPath() + "/customers?action=detail&id=" + customerId);
        } else {
            response.sendRedirect(request.getContextPath() + "/customers");
        }
    }

    // =====================================================
    // POS API HANDLERS (AJAX Support)
    // =====================================================

    private void handleSearchApi(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keyword = request.getParameter("keyword");
        List<Customer> list = customerDAO.searchCustomersForPOS(keyword);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Customer c = list.get(i);
            json.append(String.format(
                "{\"customerId\":%d,\"fullName\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\",\"totalSpent\":%s,\"loyaltyPoint\":%d,\"lifetimePoints\":%d}",
                c.getCustomerId(),
                escapeJson(c.getFullName()),
                escapeJson(c.getPhone()),
                escapeJson(c.getEmail()),
                c.getTotalSpent().toString(),
                c.getLoyaltyPoint(),
                c.getLifetimePoints()
            ));
            if (i < list.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        sendJsonResponse(response, json.toString());
    }

    private void handleCreateApi(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fullName = trim(request.getParameter("fullName"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));

        if (isBlank(fullName) || isBlank(phone)) {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Please enter full name and phone number.\"}");
            return;
        }

        if (customerDAO.isEmailOrPhoneExists(email, phone, null)) {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Email hoặc số điện thoại đã tồn tại.\"}");
            return;
        }

        Customer customer = new Customer();
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setEmail(email);

        boolean success = customerDAO.insert(customer);
        if (success) {
            Employee user = getLoggedInUser(request);
            if (user != null) {
                activityLogService.log(user.getEmployeeID(), "CREATE", "Customer", null, null, phone);
            }
            // Re-fetch to get auto-generated ID and default point status
            List<Customer> found = customerDAO.searchCustomersForPOS(phone);
            if (!found.isEmpty()) {
                Customer created = found.get(0);
                sendJsonResponse(response, String.format(
                    "{\"status\":\"success\",\"customerId\":%d,\"fullName\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\",\"totalSpent\":%s,\"loyaltyPoint\":%d}",
                    created.getCustomerId(),
                    escapeJson(created.getFullName()),
                    escapeJson(created.getPhone()),
                    escapeJson(created.getEmail()),
                    created.getTotalSpent().toString(),
                    created.getLoyaltyPoint()
                ));
            } else {
                sendJsonResponse(response, "{\"status\":\"success\",\"message\":\"Thêm khách hàng thành công.\"}");
            }
        } else {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Không thể thêm khách hàng.\"}");
        }
    }

    private void handleUpdateApi(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int customerId = parseInt(request.getParameter("customerId"), -1);
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));

        if (customerId <= 0 || isBlank(phone)) {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Dữ liệu khách hàng không hợp lệ.\"}");
            return;
        }

        if (customerDAO.isEmailOrPhoneExists(email, phone, customerId)) {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Email hoặc số điện thoại đã tồn tại.\"}");
            return;
        }

        Customer existing = customerDAO.findById(customerId);
        if (existing == null) {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Không tìm thấy khách hàng.\"}");
            return;
        }

        Employee user = getLoggedInUser(request);
        boolean isSales = user != null && user.getRoleName().toLowerCase().contains("sales");

        if (isSales) {
            // Sales Staff can ONLY edit phone and email
            existing.setPhone(phone);
            existing.setEmail(email);
            boolean ok = customerDAO.update(existing, false, 0, 0);
            if (ok) {
                Employee user2 = getLoggedInUser(request);
                if (user2 != null) activityLogService.log(user2.getEmployeeID(), "UPDATE", "Customer", customerId, null, phone);
                sendJsonResponse(response, "{\"status\":\"success\",\"message\":\"Cập nhật khách hàng thành công (Chỉ điện thoại và email).\"}");
            } else {
                sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Không thể cập nhật khách hàng.\"}");
            }
        } else {
            // Admin, Owner, Manager can edit everything in update-api too
            existing.setPhone(phone);
            existing.setEmail(email);
            String fullName = trim(request.getParameter("fullName"));
            if (!isBlank(fullName)) {
                existing.setFullName(fullName);
            }
            boolean ok = customerDAO.update(existing, false, 0, 0);
            if (ok) {
                Employee user2 = getLoggedInUser(request);
                if (user2 != null) activityLogService.log(user2.getEmployeeID(), "UPDATE", "Customer", customerId, null, phone);
                sendJsonResponse(response, "{\"status\":\"success\",\"message\":\"Cập nhật khách hàng thành công.\"}");
            } else {
                sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Không thể cập nhật khách hàng.\"}");
            }
        }
    }

    // =====================================================
    // LOAD PAGE DATA (Filter + Pagination + Overview)
    // =====================================================

    private void loadPageData(HttpServletRequest request) {
        String keyword = request.getParameter("keyword");
        String branchIdFilter = request.getParameter("branchId");

        int page = parseInt(request.getParameter("page"), 1);
        int sizeValue = parseInt(request.getParameter("sizeValue"), 30);

        int totalRecords = customerDAO.countCustomers(keyword, branchIdFilter);
        PageResult pr = PaginationHelper.compute(totalRecords, page, sizeValue);
        pr.setAttributes(request);

        request.setAttribute(
                "customers",
                customerDAO.getCustomers(keyword, branchIdFilter, pr.getCurrentPage(), pr.getPageSize())
        );

        request.setAttribute("keyword", keyword);
        request.setAttribute("branchFilter", parseInt(branchIdFilter, -1));
        request.setAttribute("totalCustomers", totalRecords);

        request.setAttribute("customerOverview", customerDAO.getCustomerOverview());
        request.setAttribute("branches", customerDAO.getAllBranches());
    }

    // =====================================================
    // LOAD SELECTED CUSTOMER DETAILS (Includes TX and Orders)
    // =====================================================

    private void loadSelectedCustomer(HttpServletRequest request, String attributeName) {
        int customerId = parseInt(request.getParameter("id"), -1);
        if (customerId > 0) {
            request.setAttribute(attributeName, customerDAO.findById(customerId));
        }
    }

    private void loadCustomerDetails(HttpServletRequest request) {
        int customerId = parseInt(request.getParameter("id"), -1);
        if (customerId > 0) {
            Customer cust = customerDAO.findById(customerId);
            request.setAttribute("detailCustomer", cust);
            request.setAttribute("detailCustomerTransactions", customerDAO.getPointTransactions(customerId));
            request.setAttribute("detailCustomerOrders", customerDAO.getOrderHistory(customerId));
        }
    }

    // =====================================================
    // SAVE CUSTOMER (Normal Web UI: Create / Update)
    // =====================================================

    private void saveCustomer(HttpServletRequest request, String action) {
        boolean isUpdate = "update".equals(action);
        int customerId = parseInt(request.getParameter("customerId"), -1);

        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));
        String gender = trim(request.getParameter("gender"));
        String address = trim(request.getParameter("address"));
        String dateOfBirthStr = trim(request.getParameter("dateOfBirth"));

        if (isUpdate && customerId <= 0) {
            setFlash(request, "errorMessage", "ID khách hàng không hợp lệ.");
            return;
        }

        if (isBlank(fullName) || isBlank(phone)) {
            setFlash(request, "errorMessage", "Vui lòng nhập họ tên và số điện thoại.");
            return;
        }

        Integer excludeCustomerId = isUpdate ? customerId : null;
        if (customerDAO.isEmailOrPhoneExists(email, phone, excludeCustomerId)) {
            setFlash(request, "errorMessage", "Email hoặc số điện thoại đã tồn tại.");
            return;
        }

        Customer customer = new Customer();
        if (isUpdate) {
            customer.setCustomerId(customerId);
            Customer existing = customerDAO.findById(customerId);
            if (existing != null) {
                customer.setTotalSpent(existing.getTotalSpent());
            }
        }

        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setGender(gender);
        customer.setAddress(address);
        customer.setDateOfBirth(parseDate(dateOfBirthStr));

        Employee user = getLoggedInUser(request);
        boolean isAdmin = user != null && ("Admin".equalsIgnoreCase(user.getRoleName()) || "Owner".equalsIgnoreCase(user.getRoleName()) || "StoreManager".equalsIgnoreCase(user.getRoleName()));

        boolean success;
        if (isUpdate) {
            if (isAdmin) {
                String totalSpentStr = trim(request.getParameter("totalSpent"));
                int currentPoints = parseInt(request.getParameter("loyaltyPoint"), 0);
                int lifetimePoints = parseInt(request.getParameter("lifetimePoints"), 0);
                customer.setTotalSpent(parseBigDecimal(totalSpentStr));
                success = customerDAO.update(customer, true, currentPoints, lifetimePoints);
            } else {
                success = customerDAO.update(customer, false, 0, 0);
            }
        } else {
            if (isAdmin) {
                String totalSpentStr = trim(request.getParameter("totalSpent"));
                customer.setTotalSpent(parseBigDecimal(totalSpentStr));
            }
            success = customerDAO.insert(customer);
        }

        if (success && user != null) {
            activityLogService.log(user.getEmployeeID(), isUpdate ? "UPDATE" : "CREATE", "Customer", isUpdate ? customerId : null, null, phone);
        }

        setFlash(
                request,
                success ? "successMessage" : "errorMessage",
                success
                        ? (isUpdate ? "Cập nhật khách hàng thành công." : "Thêm khách hàng thành công.")
                        : (isUpdate ? "Không thể cập nhật khách hàng." : "Không thể thêm khách hàng.")
        );
    }

    // =====================================================
    // SOFT DELETE
    // =====================================================

    private void deleteCustomer(HttpServletRequest request) {
        int customerId = parseInt(request.getParameter("customerId"), -1);
        if (customerId <= 0) {
            setFlash(request, "errorMessage", "ID khách hàng không hợp lệ.");
            return;
        }

        boolean success = customerDAO.softDelete(customerId);
        if (success) {
            Employee user = getLoggedInUser(request);
            if (user != null) activityLogService.log(user.getEmployeeID(), "DELETE", "Customer", customerId, null, null);
        }
        setFlash(
                request,
                success ? "successMessage" : "errorMessage",
                success ? "Xóa khách hàng thành công." : "Không thể xóa khách hàng."
        );
    }

    // =====================================================
    // LOYALTY ACTIONS
    // =====================================================

    private void syncLoyalty(HttpServletRequest request) {
        int customerId = parseInt(request.getParameter("customerId"), -1);
        if (customerId <= 0) {
            setFlash(request, "errorMessage", "ID khách hàng không hợp lệ.");
            return;
        }

        customerDAO.syncLoyaltyFromPaidOrders(customerId);
        setFlash(request, "successMessage", "Đã đồng bộ điểm từ đơn hàng đã thanh toán.");
    }

    private void redeemPoints(HttpServletRequest request) {
        int customerId = parseInt(request.getParameter("customerId"), -1);
        int redeemPointsValue = parseInt(request.getParameter("redeemPoints"), 0);

        if (customerId <= 0) {
            setFlash(request, "errorMessage", "ID khách hàng không hợp lệ.");
            return;
        }

        boolean ok = customerDAO.redeemPoints(customerId, redeemPointsValue, "Đổi điểm khách hàng");

        if (ok) {
            Employee user = getLoggedInUser(request);
            if (user != null) activityLogService.log(user.getEmployeeID(), "REDEEM_POINTS", "Customer", customerId, null, String.valueOf(redeemPointsValue));
        }

        setFlash(
                request,
                ok ? "successMessage" : "errorMessage",
                ok ? "Points redeemed successfully." : "Cannot redeem points. Please check available balance."
        );
    }

    // =====================================================
    // AUTHORIZATION
    // =====================================================

    private boolean isAuthorized(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        Employee currentUser = (Employee) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String roleName = currentUser.getRoleName();
        if (roleName == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
            return false;
        }

        String action = getParam(request, "action", "list");
        boolean isPost = "POST".equalsIgnoreCase(request.getMethod());
        String roleLower = roleName.trim().toLowerCase();

        // Allowed roles for Customer Management: Admin, Owner, StoreManager, SalesStaff
        if (!"admin".equals(roleLower)
                && !"owner".equals(roleLower)
                && !"storemanager".equals(roleLower)
                && !"salesstaff".equals(roleLower)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
            return false;
        }

        // Sales staff is authorized ONLY for the API search/create/edit endpoints used in POS
        boolean isApiCall = "search-api".equals(action) || "create-api".equals(action) || "update-api".equals(action);
        boolean isSales = roleLower.contains("sales");

        if (isSales) {
            if (isApiCall) {
                return true;
            } else {
                response.sendRedirect(request.getContextPath() + "/pos/sale");
                return false;
            }
        }

        // Admin: redeem/sync POST only, no customer UI access
        if ("admin".equals(roleLower)) {
            if (isPost && ("sync-loyalty".equals(action) || "redeem-points".equals(action))) {
                return true;
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                return false;
            }
        }

        // Owner: view, search, filter, soft-delete, detail, redeem/sync. No create/edit.
        if ("Owner".equalsIgnoreCase(roleName)) {
            boolean isWriteAction = "create".equals(action) || "update".equals(action)
                    || "create-api".equals(action) || "update-api".equals(action);
            if (isWriteAction) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Owner cannot create or edit customers.");
                return false;
            }
            return true;
        }

        // StoreManager: full access
        if ("StoreManager".equalsIgnoreCase(roleName)) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
        return false;
    }

    private Employee getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Employee) session.getAttribute("currentUser");
        }
        return null;
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    private void sendJsonResponse(HttpServletResponse response, String jsonString) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(jsonString);
        out.flush();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void setFlash(HttpServletRequest request, String key, String message) {
        request.getSession().setAttribute(key, message);
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
