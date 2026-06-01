package util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Central RDS authorization matrix. Modules must not implement local role rules. */
public final class RolePermissionUtil {
    private static final Set<String> PUBLIC_SCREENS = Set.of("Login", "Forgot Password", "Product List", "Product Detail", "Category List", "Homepage", "About Us", "Product Showcase", "Contact Page", "Notification Center");
    private static final Map<String, Set<String>> ROLE_SCREENS = createRoleScreens();
    private RolePermissionUtil() {}
    public static boolean canAccess(String role, String screenName) {
        String normalizedRole = RoleContextUtil.normalizeRole(role);
        if (PUBLIC_SCREENS.contains(screenName)) return true;
        Set<String> screens = ROLE_SCREENS.getOrDefault(normalizedRole, Collections.emptySet());
        return screens.contains("*") || screens.contains(screenName);
    }
    public static Map<String, Set<String>> getRoleScreens() { return ROLE_SCREENS; }
    private static Map<String, Set<String>> createRoleScreens() {
        Map<String, Set<String>> map = new LinkedHashMap<>();
        map.put("Admin", Set.of("*"));
        map.put("Owner", Set.of("*"));
        map.put("Store Manager", Set.of("Change Password", "Customer List", "Edit Customer", "Customer Detail", "Loyal Customer Ranking", "Product List", "Product Detail", "Add Product", "Edit Product", "Category List", "Add Category", "Edit Category", "Supplier List", "Add Supplier", "Edit Supplier", "Purchase Order", "Purchase Detail", "Import Receipt", "Inventory Dashboard", "Stock Adjustment", "Inventory Transfer", "Create Order", "Order Detail", "Update Order", "Cancel Order", "Payment", "Invoice Management", "Add Expense", "Sales Report by Store", "Employee Sales Report", "Inventory Report", "Export Report", "Dashboard Overview", "Notification Center"));
        map.put("Sales Staff", Set.of("Change Password", "Customer List", "Add Customer", "Edit Customer", "Customer Detail", "Product List", "Product Detail", "Category List", "Create Order", "Order Detail", "Update Order", "Cancel Order", "Payment", "Invoice Management", "Add Expense", "Export Report", "Notification Center"));
        map.put("Warehouse Staff", Set.of("Change Password", "Product List", "Product Detail", "Category List", "Supplier List", "Purchase Detail", "Import Receipt", "Inventory Dashboard", "Stock Adjustment", "Inventory Transfer", "Inventory Report", "Export Report", "Notification Center"));
        map.put("Guest", PUBLIC_SCREENS);
        return Collections.unmodifiableMap(map);
    }
}
