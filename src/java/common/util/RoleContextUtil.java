package common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Single source of truth for the current role during development. TODO: later read logged-in Employee role. */
public final class RoleContextUtil {
    public static final String CURRENT_ROLE_SESSION_KEY = "currentRole";
    public static final String DEFAULT_ROLE = "Admin";
    private RoleContextUtil() {}
    public static String getCurrentRole(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object role = session.getAttribute(CURRENT_ROLE_SESSION_KEY);
        if (role == null || role.toString().isBlank()) {
            session.setAttribute(CURRENT_ROLE_SESSION_KEY, DEFAULT_ROLE);
            return DEFAULT_ROLE;
        }
        return role.toString();
    }
    public static void setCurrentRole(HttpServletRequest request, String roleName) { request.getSession(true).setAttribute(CURRENT_ROLE_SESSION_KEY, normalizeRole(roleName)); }
    public static String normalizeRole(String roleName) {
        if (roleName == null || roleName.isBlank()) return DEFAULT_ROLE;
        return switch (roleName.trim()) {
            case "Owner", "Shop Owner" -> "Owner";
            case "StoreManager", "Store Manager" -> "Store Manager";
            case "SalesStaff", "Sales Staff" -> "Sales Staff";
            case "WarehouseStaff", "Warehouse Staff" -> "Warehouse Staff";
            case "Guest" -> "Guest";
            default -> "Admin";
        };
    }
}
