package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import util.database.DBContext;
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
        ROLE_MAP.put("/owner/",          Set.of("owner"));
        ROLE_MAP.put("/admin/",          Set.of("admin", "owner"));
        ROLE_MAP.put("/manager/",        Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/branch/",         Set.of("admin", "owner"));
        ROLE_MAP.put("/supplier",        Set.of("admin", "owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/purchase/",       Set.of("admin", "owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/finance/",        Set.of("admin", "owner"));
        ROLE_MAP.put("/activity/",       Set.of("admin", "owner"));
        ROLE_MAP.put("/settings",        Set.of("admin", "owner"));
        ROLE_MAP.put("/report/",         Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/inventory/",      Set.of("owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/warehouse/",      Set.of("owner", "storemanager", "warehousestaff"));
        ROLE_MAP.put("/product/",        Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/category/",       Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/customer/",       Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/sales/",          Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/cart/",           Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/checkout/",       Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/orders/",         Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/print/",          Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/search-product",  Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/cash-transaction",Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/dashboard/",      Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/revenue/",        Set.of("admin", "owner", "storemanager"));
        ROLE_MAP.put("/shift/",          Set.of("admin", "owner", "storemanager", "salesstaff"));
        ROLE_MAP.put("/profile/",        Set.of("admin", "owner", "storemanager", "salesstaff", "warehousestaff"));
    }

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

        if (allowedRoles != null && !allowedRoles.contains(role)) {
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
