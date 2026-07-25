package util.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;

import java.io.IOException;
import java.util.Set;

public final class AuthUtil {

    private static final Set<String> MANAGER_ROLES = Set.of("storemanager", "admin", "owner");
    private static final Set<String> ADMIN_ROLES = Set.of("admin", "owner");

    private AuthUtil() {}

    public static Employee getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object obj = session.getAttribute("currentUser");
        return obj instanceof Employee ? (Employee) obj : null;
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    public static boolean requireLoggedIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    public static Integer getBranchId(HttpServletRequest request) {
        Employee emp = getCurrentUser(request);
        return emp != null ? emp.getBranchID() : null;
    }

    public static int getEmployeeId(HttpServletRequest request) {
        Employee emp = getCurrentUser(request);
        return emp != null ? emp.getEmployeeID() : -1;
    }

    public static String getRoleName(HttpServletRequest request) {
        Employee emp = getCurrentUser(request);
        if (emp == null || emp.getRoleName() == null) return "";
        return emp.getRoleName().trim().toLowerCase();
    }

    public static boolean hasRole(HttpServletRequest request, String role) {
        return getRoleName(request).equals(role.toLowerCase());
    }

    public static boolean hasAnyRole(HttpServletRequest request, String... roles) {
        String current = getRoleName(request);
        for (String r : roles) {
            if (current.equals(r.toLowerCase())) return true;
        }
        return false;
    }

    public static boolean isManager(HttpServletRequest request) {
        return MANAGER_ROLES.contains(getRoleName(request));
    }

    public static boolean isAdminOrOwner(HttpServletRequest request) {
        return ADMIN_ROLES.contains(getRoleName(request));
    }

    public static boolean requireRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws IOException {
        if (!requireLoggedIn(request, response)) return false;
        if (!hasRole(request, role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. " + role + " only.");
            return false;
        }
        return true;
    }

    public static boolean requireAnyRole(HttpServletRequest request, HttpServletResponse response, String... roles)
            throws IOException {
        if (!requireLoggedIn(request, response)) return false;
        if (!hasAnyRole(request, roles)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
            return false;
        }
        return true;
    }

    public static boolean requireManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        return requireAnyRole(request, response, "storemanager", "admin", "owner");
    }

    public static boolean requireAdminOrOwner(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        return requireAnyRole(request, response, "admin", "owner");
    }

    public static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getParam(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return isBlank(value) ? defaultValue : value.trim();
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }
}