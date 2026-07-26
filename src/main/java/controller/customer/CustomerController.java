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

        // POS phone-only search
        if ("search-pos".equals(action)) {
            handleSearchPos(request, response);
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
        boolean isOwner = "Owner".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);

        request.setAttribute("canCreate", isOwner);
        request.setAttribute("canEdit", isOwner);
        request.setAttribute("canDelete", isOwner);
        request.setAttribute("canRedeem", isOwner);
        request.setAttribute("isAdmin", isOwner);

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

    private void handleSearchPos(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String phone = trim(request.getParameter("phone"));
        List<Customer> list = new java.util.ArrayList<>();
        if (!isBlank(phone)) {
            list = customerDAO.searchByPhone(phone);
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Customer c = list.get(i);
            json.append(String.format(
                "{\"customerId\":%d,\"fullName\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\",\"totalSpent\":%s,\"loyaltyPoint\":%d}",
                c.getCustomerId(),
                escapeJson(c.getFullName()),
                escapeJson(c.getPhone()),
                escapeJson(c.getEmail() != null ? c.getEmail() : ""),
                c.getTotalSpent().toString(),
                c.getLoyaltyPoint()
            ));
            if (i < list.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        sendJsonResponse(response, json.toString());
    }

    private void handleSearchApi(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keyword = request.getParameter("keyword");
        List<Customer> list = customerDAO.searchCustomersForPOS(keyword);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Customer c = list.get(i);
            json.append(String.format(
                "{\"customerId\":%d,\"fullName\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\",\"totalSpent\":%s,\"loyaltyPoint\":%d}",
                c.getCustomerId(),
                escapeJson(c.getFullName()),
                escapeJson(c.getPhone()),
                escapeJson(c.getEmail()),
                c.getTotalSpent().toString(),
                c.getLoyaltyPoint()
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
        String fullName = trim(request.getParameter("fullName"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));
        String address = trim(request.getParameter("address"));
        String dateOfBirthStr = trim(request.getParameter("dateOfBirth"));
        String gender = trim(request.getParameter("gender"));

        if (customerId <= 0) {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Dữ liệu khách hàng không hợp lệ.\"}");
            return;
        }

        if (isBlank(phone)) {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Số điện thoại không được để trống.\",\"field\":\"phone\"}");
            return;
        }
        if (!phone.matches("^0[0-9]{9,10}$")) {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và 10-11 số).\",\"field\":\"phone\"}");
            return;
        }
        if (email != null && !email.isBlank() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Email không hợp lệ.\",\"field\":\"email\"}");
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
            if (!isBlank(fullName)) existing.setFullName(fullName);
            existing.setPhone(phone);
            existing.setEmail(email);
        } else {
            if (!isBlank(fullName)) existing.setFullName(fullName);
            existing.setPhone(phone);
            existing.setEmail(email);
            existing.setAddress(address);
            existing.setDateOfBirth(parseDate(dateOfBirthStr));
            if (!isBlank(gender)) existing.setGender(gender);
        }

        boolean ok = customerDAO.update(existing, false, 0);
        if (ok) {
            if (user != null) activityLogService.log(user.getEmployeeID(), "UPDATE", "Customer", customerId, null, phone);

            Customer updated = customerDAO.findById(customerId);
            sendJsonResponse(response, String.format(
                "{\"status\":\"success\",\"message\":\"Cập nhật khách hàng thành công.\"," +
                "\"customer\":{\"customerId\":%d,\"fullName\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\"," +
                "\"address\":\"%s\",\"gender\":\"%s\",\"totalSpent\":%s,\"loyaltyPoint\":%d}}",
                updated.getCustomerId(),
                escapeJson(updated.getFullName()),
                escapeJson(updated.getPhone()),
                escapeJson(updated.getEmail() != null ? updated.getEmail() : ""),
                escapeJson(updated.getAddress() != null ? updated.getAddress() : ""),
                escapeJson(updated.getGender() != null ? updated.getGender() : ""),
                updated.getTotalSpent().toString(),
                updated.getLoyaltyPoint()
            ));
        } else {
            sendJsonResponse(response, "{\"status\":\"error\",\"message\":\"Không thể cập nhật khách hàng.\"}");
        }
    }

    // =====================================================
    // LOAD PAGE DATA (Filter + Pagination + Overview)
    // =====================================================

    private void loadPageData(HttpServletRequest request) {
        String keyword = request.getParameter("keyword");
        String branchIdFilter = getEffectiveBranchId(request);

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

        request.setAttribute("customerOverview", customerDAO.getCustomerOverview(getBranchIdFromSession(request)));
        request.setAttribute("branches", getAvailableBranches(request));
    }

    /**
     * For Store Manager, force branch filter from session (never trust browser).
     * For Owner/Admin, allow branch filter from request.
     */
    private String getEffectiveBranchId(HttpServletRequest request) {
        Employee user = getLoggedInUser(request);
        if (user != null && "StoreManager".equalsIgnoreCase(user.getRoleName())) {
            Integer bid = user.getBranchID();
            return bid != null ? String.valueOf(bid) : null;
        }
        return request.getParameter("branchId");
    }

    private Integer getBranchIdFromSession(HttpServletRequest request) {
        Employee user = getLoggedInUser(request);
        if (user != null && "StoreManager".equalsIgnoreCase(user.getRoleName())) {
            return user.getBranchID();
        }
        return null;
    }

    private List<model.Branch> getAvailableBranches(HttpServletRequest request) {
        Employee user = getLoggedInUser(request);
        if (user != null && "StoreManager".equalsIgnoreCase(user.getRoleName())) {
            Integer bid = user.getBranchID();
            if (bid != null) {
                model.Branch b = customerDAO.getBranchById(bid);
                if (b != null) return java.util.Collections.singletonList(b);
            }
            return java.util.Collections.emptyList();
        }
        return customerDAO.getAllBranches();
    }

    // =====================================================
    // LOAD SELECTED CUSTOMER DETAILS (Includes TX and Orders)
    // =====================================================

    private void loadSelectedCustomer(HttpServletRequest request, String attributeName) {
        int customerId = parseInt(request.getParameter("id"), -1);
        if (customerId > 0) {
            Customer c = customerDAO.findById(customerId);
            if (c != null && isStoreManagerDenied(request, c)) return;
            request.setAttribute(attributeName, c);
        }
    }

    /**
     * Check if a Store Manager is denied access to a customer (wrong branch).
     * Returns true if denied (response error already sent), false if allowed.
     */
    private boolean isStoreManagerDenied(HttpServletRequest request, Customer customer) {
        Employee user = getLoggedInUser(request);
        if (user == null || !"StoreManager".equalsIgnoreCase(user.getRoleName())) return false;
        if (user.getBranchID() == null) return false;
        // Check if the customer has any orders in the manager's branch
        if (!customerDAO.customerBelongsToBranch(customer.getCustomerId(), user.getBranchID())) {
            try {
                HttpServletResponse resp = (HttpServletResponse) request.getAttribute("jakarta.servlet.http.response");
                if (resp != null) resp.sendError(403, "Không có quyền truy cập khách hàng này.");
            } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

    private void loadCustomerDetails(HttpServletRequest request) {
        int customerId = parseInt(request.getParameter("id"), -1);
        if (customerId > 0) {
            Customer cust = customerDAO.findById(customerId);
            if (cust != null && isStoreManagerDenied(request, cust)) return;
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

        // Store Manager can only edit customers belonging to their branch
        if (isUpdate) {
            Employee user = getLoggedInUser(request);
            if (user != null && "StoreManager".equalsIgnoreCase(user.getRoleName()) && user.getBranchID() != null) {
                Customer existing = customerDAO.findById(customerId);
                if (existing == null || !customerDAO.customerBelongsToBranch(customerId, user.getBranchID())) {
                    setFlash(request, "errorMessage", "Không có quyền chỉnh sửa khách hàng này.");
                    return;
                }
            }
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
                 customer.setTotalSpent(parseBigDecimal(totalSpentStr));
                 success = customerDAO.update(customer, true, currentPoints);
             } else {
                 success = customerDAO.update(customer, false, 0);
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

        // Store Manager can only delete customers belonging to their branch
        Employee user = getLoggedInUser(request);
        if (user != null && "StoreManager".equalsIgnoreCase(user.getRoleName()) && user.getBranchID() != null) {
            if (!customerDAO.customerBelongsToBranch(customerId, user.getBranchID())) {
                setFlash(request, "errorMessage", "Không có quyền xóa khách hàng này.");
                return;
            }
        }

        boolean success = customerDAO.softDelete(customerId);
        if (success) {
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
        boolean isApiCall = "search-pos".equals(action) || "search-api".equals(action) || "create-api".equals(action) || "update-api".equals(action);
        boolean isSales = roleLower.contains("sales");

        if (isSales) {
            if (isApiCall) {
                return true;
            } else {
                response.sendRedirect(request.getContextPath() + "/pos/sale");
                return false;
            }
        }

        // Owner/Admin: Full access
        if ("Owner".equalsIgnoreCase(roleName) || "Admin".equalsIgnoreCase(roleName)) {
            return true;
        }

        // StoreManager: Read-only access (list, detail, search-api)
        if ("StoreManager".equalsIgnoreCase(roleName)) {
            boolean isWriteAction = isPost || "create".equals(action) || "update".equals(action) || "delete".equals(action)
                    || "sync-loyalty".equals(action) || "redeem-points".equals(action) || "add".equals(action) || "edit".equals(action);
            if (isWriteAction) {
                setFlash(request, "errorMessage", "Bạn không có quyền thực hiện thao tác này.");
                response.sendRedirect(request.getContextPath() + "/customers");
                return false;
            }
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
