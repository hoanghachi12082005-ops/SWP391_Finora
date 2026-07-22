package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import util.database.DBContext;
import dao.system.ActivityLogDAO;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.Base64;

/**
 * Central security filter handling authentication, role-based authorization,
 * security headers, and CSRF token validation.
 */
@WebFilter(filterName = "SecurityFilter", urlPatterns = {"/*"})
public class SecurityFilter implements Filter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/login", "/logout", "/forgot-password", "/register", "/role-selection",
        "/assets/", "/css/", "/js/", "/static/",
        "/vnpay/ipn", "/vnpay/return", "/order/status"
    );

    private static final Map<String, Set<String>> ROLE_MAP = new LinkedHashMap<>();
    static {
        ROLE_MAP.put("/system/",         Set.of("admin", "owner"));
        ROLE_MAP.put("/management/",     Set.of("admin", "owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/pos/",            Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/owner/",          Set.of("owner", "storemanager", "salesstaff", "warehousestaff"));
        ROLE_MAP.put("/admin/",          Set.of("admin", "owner"));
        ROLE_MAP.put("/manager/",        Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/branch",          Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/supplier",        Set.of("admin", "owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/purchase/",       Set.of("admin", "owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/cashbook",        Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/finance/",        Set.of("admin", "owner"));
        ROLE_MAP.put("/payments",        Set.of("admin", "owner"));
        ROLE_MAP.put("/invoices",        Set.of("admin", "owner"));
        ROLE_MAP.put("/activity-log",    Set.of("admin", "owner"));
        ROLE_MAP.put("/activity/",       Set.of("admin", "owner"));
        ROLE_MAP.put("/settings",        Set.of("admin", "owner"));
        ROLE_MAP.put("/reports/finance-detail", Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/report/",         Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/reports/",        Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/inventory/",      Set.of("owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/warehouse/",      Set.of("owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/product/",        Set.of("admin", "owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/products",        Set.of("admin", "owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/category/",       Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/customer/",       Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/configuration/",  Set.of("admin", "owner"));
        ROLE_MAP.put("/sales/",          Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/cart/",           Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/checkout/",       Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/orders/",         Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/print/",          Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/search-product",  Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/cash-transaction",Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/dashboard/financial", Set.of("admin", "owner"));
        ROLE_MAP.put("/dashboard/",      Set.of("admin", "owner", "storemanager", "salesstaff", "warehousestaff"));
        ROLE_MAP.put("/revenue/",        Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/shift/",          Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/profile/",        Set.of("admin", "owner", "storemanager", "salesstaff", "warehousestaff"));
    }

    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 1. Public paths — skip all checks
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Employee employee = (session == null) ? null : (Employee) session.getAttribute("currentUser");

        // 2. Authentication check
        if (employee == null) {
            resp.setStatus(401);
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 3. Anti-cache headers for authenticated pages
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        // 4. Security headers
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Referrer-Policy", "same-origin");

        // 5. Ensure CSRF token exists in session (generated on first GET or login)
        if (session.getAttribute("csrfToken") == null) {
            byte[] csrfBytes = new byte[32];
            new SecureRandom().nextBytes(csrfBytes);
            session.setAttribute("csrfToken", Base64.getEncoder().encodeToString(csrfBytes));
        }

        // 6. Role-based authorization
        String role = employee.getRoleName() != null ? employee.getRoleName().trim().toLowerCase() : "";
        Set<String> allowedRoles = findRequiredRoles(path);

        System.out.println("[SecurityFilter] Path: " + path + ", Role: " + role + ", Allowed: " + allowedRoles);

        if (allowedRoles != null && !allowedRoles.contains(role)) {
            // Ghi audit log cho truy cập bất hợp pháp
            String vietPath = mapPathToVietnamese(path);
            String message = "Truy cập trái phép: " + vietPath + " (vai trò: " + role + ")";
            try {
                activityLogDAO.insertLog(employee.getEmployeeId(), "TỪ_CHỐI_TRUY_CẬP", "auth", null, null, message);
            } catch (Exception ignored) {}
            resp.sendError(403, "Bạn không có quyền truy cập chức năng này.");
            return;
        }

        // 7. Set employee context cho trigger audit log
        Integer empId = employee.getEmployeeId();
        if (empId != null) {
            DBContext.setCurrentEmployeeId(empId);
        }

        try {
            // 8. CSRF check for state-changing methods
            if ("POST".equalsIgnoreCase(req.getMethod()) && !isCsrfExempt(path)) {
                String csrfToken = req.getParameter("csrfToken");
                if (csrfToken == null) {
                    csrfToken = req.getHeader("X-CSRF-Token");
                }
                if (csrfToken == null) {
                    csrfToken = req.getHeader("X-Csrf-Token");
                }
                String sessionToken = (String) req.getSession().getAttribute("csrfToken");
                if (csrfToken == null || !csrfToken.equals(sessionToken)) {
                    resp.sendError(403, "CSRF token không hợp lệ. Vui lòng tải lại trang.");
                    return;
                }
            }

            chain.doFilter(request, response);
        } finally {
            DBContext.clearCurrentEmployeeId();
        }
    }

    private boolean isPublicPath(String path) {
        for (String p : PUBLIC_PATHS) {
            if (path.equals(p) || path.startsWith(p)) return true;
        }
        return false;
    }

    /** Chuyển đường dẫn URL thành tên tiếng Việt dễ đọc */
    private String mapPathToVietnamese(String path) {
        if (path.startsWith("/product"))       return "Quản lý sản phẩm";
        if (path.startsWith("/category"))       return "Quản lý danh mục";
        if (path.startsWith("/customer"))       return "Quản lý khách hàng";
        if (path.startsWith("/supplier"))       return "Quản lý nhà cung cấp";
        if (path.startsWith("/warehouse"))      return "Quản lý kho";
        if (path.startsWith("/inventory"))      return "Tồn kho";
        if (path.startsWith("/purchase"))       return "Phiếu nhập hàng";
        if (path.startsWith("/branch"))         return "Chi nhánh";
        if (path.startsWith("/system"))         return "Quản lý hệ thống";
        if (path.startsWith("/configuration"))  return "Cấu hình";
        if (path.startsWith("/admin"))          return "Quản trị";
        if (path.startsWith("/finance"))        return "Tài chính";
        if (path.startsWith("/activity"))       return "Trung tâm hoạt động";
        if (path.startsWith("/settings"))       return "Cài đặt";
        if (path.startsWith("/report"))         return "Báo cáo";
        if (path.startsWith("/manager"))        return "Quản lý";
        if (path.startsWith("/sales"))          return "Bán hàng";
        if (path.startsWith("/cart"))           return "Giỏ hàng";
        if (path.startsWith("/checkout"))       return "Thanh toán";
        if (path.startsWith("/order"))          return "Đơn hàng";
        if (path.startsWith("/dashboard"))      return "Bảng điều khiển";
        if (path.startsWith("/revenue"))        return "Doanh thu";
        if (path.startsWith("/shift"))          return "Ca làm việc";
        if (path.startsWith("/print"))          return "In ấn";
        if (path.startsWith("/profile"))        return "Hồ sơ cá nhân";
        return path; // fallback về đường dẫn gốc nếu không map được
    }

    private Set<String> findRequiredRoles(String path) {
        for (Map.Entry<String, Set<String>> entry : ROLE_MAP.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null; // no role restriction found — allow (default-deny would be safer but breaks existing flows)
    }

    private boolean isCsrfExempt(String path) {
        return path.equals("/login") || path.equals("/logout")
            || path.startsWith("/static/") || path.startsWith("/assets/");
    }
}
