package common.util;

import common.dto.ModuleActionDTO;
import common.dto.ModuleDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central registry of RDS-defined modules grouped by real development workspace.
 * Dashboard shows modules; each module page shows its RDS actions/screens.
 */
public final class ModuleRegistry {
    private static final List<ModuleDefinition> DEFINITIONS = createDefinitions();

    private ModuleRegistry() {
    }

    public static List<ModuleDTO> getModules(String role) {
        return DEFINITIONS.stream().map(definition -> toDto(definition, role)).toList();
    }

    public static ModuleDTO findModuleByRoute(String route, String role) {
        ModuleDefinition definition = DEFINITIONS.stream()
                .filter(module -> module.route().equals(route))
                .findFirst()
                .orElse(new ModuleDefinition(
                        "Unknown Module",
                        route,
                        "Skeleton page for an unmapped module route.",
                        "No database mapping yet.",
                        "Unassigned",
                        List.of(new ActionDefinition("Unknown Action", "Route is not registered.", "No database mapping yet.", "TODO: Register this module in ModuleRegistry."))));
        return toDto(definition, role);
    }

    private static ModuleDTO toDto(ModuleDefinition definition, String role) {
        List<ModuleActionDTO> actions = definition.actions().stream()
                .map(action -> new ModuleActionDTO(
                        action.name(),
                        action.description(),
                        action.databaseMapping(),
                        action.todo(),
                        RolePermissionUtil.canAccess(role, action.name())))
                .toList();
        boolean moduleAllowed = actions.stream().anyMatch(ModuleActionDTO::isAllowed);
        return new ModuleDTO(
                definition.name(),
                definition.route(),
                definition.description(),
                definition.databaseMapping(),
                definition.ownerSuggestion(),
                actions,
                moduleAllowed);
    }

    private static List<ModuleDefinition> createDefinitions() {
        List<ModuleDefinition> modules = new ArrayList<>();

        add(modules, "Authentication", "/login", "Login, register, forgot password, and logout flows.", "Employee, Role, Branch", "Member 1",
                action("Login", "Users enter username/email and password.", "Employee, Role, Branch", "TODO: Implement Login workflow"),
                action("Register Account", "New users create an account.", "Employee, Role, Branch", "TODO: Implement Register workflow"),
                action("Forgot Password", "Users recover account password.", "Employee", "TODO: Implement password reset workflow"),
                action("Logout", "Users securely log out.", "Employee session", "TODO: Implement Logout workflow"));

        add(modules, "User & Role Management", "/employee-management", "Employee account, role, permission, and password workspace.", "Employee, Role, Branch", "Member 1",
                action("User List", "Admin views and manages user accounts.", "Employee, Role, Branch", "TODO: Implement staff account list"),
                action("Add User", "Admin creates employee accounts.", "Employee, Role, Branch", "TODO: Implement Add User"),
                action("Edit User", "Admin updates employee information.", "Employee, Role, Branch", "TODO: Implement Edit User"),
                action("Lock / Unlock User", "Admin locks/unlocks accounts.", "Employee.Status", "TODO: Implement Lock / Unlock User"),
                action("Change Password", "Users change passwords.", "Employee.PasswordHash", "TODO: Implement Change Password"),
                action("Role Management", "Admin assigns roles and permissions.", "Role + RDS permission matrix", "TODO: Implement Role Management"));

        add(modules, "Branch Management", "/branch-management", "Store branch list, create, update, and close workflow.", "Branch", "Member 1",
                action("Store List", "Admin views all stores.", "Branch", "TODO: Implement Store List"),
                action("Add Store", "Admin adds a branch.", "Branch", "TODO: Implement Add Store"),
                action("Edit Store", "Admin updates branch information.", "Branch", "TODO: Implement Edit Store"),
                action("Delete Store", "Admin closes inactive branches.", "Branch.Status", "TODO: Implement Delete Store"));

        add(modules, "Product Management", "/product-management", "One product workspace for list, detail, search, add, edit, pricing, and delete/status changes.", "Product, Category, Warehouse", "Member 2",
                action("Product List", "Users view products.", "Product, Category, Warehouse", "TODO: Implement Product list/search"),
                action("Product Detail", "Users view product details.", "Product, Category, Warehouse", "TODO: Implement Product Detail"),
                action("Add Product", "Admin/manager adds products.", "Product, Category", "TODO: Implement Add Product"),
                action("Edit Product", "Admin/manager updates products and pricing.", "Product", "TODO: Implement Edit Product and Product Pricing"),
                action("Delete Product", "Admin removes inactive products.", "Product.Status", "TODO: Implement Delete/Deactivate Product"),
                action("Search Products", "Search products and view stock quantity.", "Product, Warehouse", "TODO: Implement Product Search"),
                action("Manage Units", "Manage product unit conversions.", "Skeleton only", "TODO: Implement Units when database support exists"));

        add(modules, "Category Management", "/category-management", "One category workspace for tree/list, add, edit, and deactivate.", "Category", "Member 2",
                action("Category List", "Users view categories.", "Category", "TODO: Implement Category tree/list"),
                action("Add Category", "Admin creates categories.", "Category", "TODO: Implement Add Category"),
                action("Edit Category", "Admin updates categories.", "Category", "TODO: Implement Edit Category"));

        add(modules, "Supplier Management", "/supplier-management", "Supplier/provider CRUD workspace.", "Supplier", "Member 2",
                action("Supplier List", "Staff views supplier information.", "Supplier", "TODO: Implement Supplier list"),
                action("Add Supplier", "Staff adds suppliers.", "Supplier", "TODO: Implement Add Supplier"),
                action("Edit Supplier", "Staff updates suppliers.", "Supplier", "TODO: Implement Edit Supplier"));

        add(modules, "Purchase Management", "/purchase-management", "Purchase order, receiving, supplier return, debt, and history workspace.", "Orders, OrderDetail, Supplier, WarehouseTransaction", "Member 4",
                action("Purchase Order", "Staff creates purchase orders.", "Orders, OrderDetail, Supplier", "TODO: Implement Purchase Order Workflow"),
                action("Purchase Detail", "Staff views purchase details.", "Orders, OrderDetail, Supplier", "TODO: Implement Purchase Detail"),
                action("Import Receipt", "Warehouse confirms imported products.", "WarehouseTransaction, Warehouse", "TODO: Implement Import Receipt"),
                action("Receive Goods", "Receive goods and update inventory.", "Warehouse, WarehouseTransaction", "TODO: Implement Receive Goods"),
                action("Return Goods to Supplier", "Return damaged or expired products.", "Orders, OrderDetail, WarehouseTransaction", "TODO: Implement Supplier Return"),
                action("Pay Supplier Debt", "Approve and pay supplier debts.", "FinanceTransaction, Payments", "TODO: Implement Supplier Debt Payment"),
                action("View Purchase History", "View inventory import history.", "Orders, WarehouseTransaction", "TODO: Implement Purchase History"));

        add(modules, "Inventory & Warehouse Management", "/inventory-management", "Inventory dashboard, warehouse stock, adjustment, transfer, and alerts workspace.", "Warehouse, WarehouseTransaction, StockTransfer, Product, Branch", "Member 4",
                action("Inventory Dashboard", "Warehouse monitors stock.", "Warehouse, Product, Branch", "TODO: Implement Inventory Dashboard"),
                action("View Inventory", "View stock quantity by product or warehouse.", "Warehouse, Product", "TODO: Implement View Inventory"),
                action("Stock Checking", "Perform inventory stock checking.", "Warehouse, WarehouseTransaction", "TODO: Implement Stock Checking"),
                action("Stock Adjustment", "Adjust inventory discrepancies.", "Warehouse, WarehouseTransaction", "TODO: Implement Stock Adjustment"),
                action("Inventory Transfer", "Transfer products between warehouses.", "StockTransfer, Warehouse", "TODO: Implement Inventory Transfer"),
                action("Low Stock Alert", "Receive low stock notifications.", "Product.StockAlertQty, Warehouse.Quantity", "TODO: Implement Low Stock Alert"));

        add(modules, "Sales Management", "/sales-management", "Sales order workspace covering order CRUD, discount, return, payment, and invoice flow.", "Orders, OrderDetail, Customer, Product, Payments, FinanceTransaction", "Member 3",
                action("Create Order", "Sales staff creates orders.", "Orders, OrderDetail, Customer, Product", "TODO: Implement Create Sales Order"),
                action("Order Detail", "Staff views order details.", "Orders, OrderDetail", "TODO: Implement Order Detail"),
                action("Update Order", "Staff updates order information.", "Orders, OrderDetail", "TODO: Implement Update Order"),
                action("Cancel Order", "Staff cancels invalid orders.", "Orders.Status", "TODO: Implement Cancel Order"),
                action("Apply Discount", "Apply discounts to sales orders.", "Orders.DiscountAmount", "TODO: Implement Discount"),
                action("Payment", "Staff processes payments.", "Payments, Orders, FinanceTransaction", "TODO: Implement Payment"),
                action("Invoice Management", "Staff generates invoices.", "Orders, OrderDetail, Payments", "TODO: Implement Invoice Workflow"),
                action("Process Return/Exchange", "Handle product returns and exchanges.", "Orders, OrderDetail, WarehouseTransaction", "TODO: Implement Return/Exchange"),
                action("Create Credit Sales Order", "Create debt orders for VIP customers.", "Orders, Customer", "TODO: Implement Credit Sales"),
                action("View Sales History", "View completed sales orders.", "Orders, Payments", "TODO: Implement Sales History"));

        add(modules, "Customer & Loyalty Management", "/customer-management", "Customer profile, loyalty tier, points, and debt workspace.", "Customer, Orders, Payments", "Member 5",
                action("Customer List", "Staff views customer information.", "Customer, Orders", "TODO: Implement Customer List"),
                action("Add Customer", "Staff adds customer information.", "Customer", "TODO: Implement Add Customer"),
                action("Edit Customer", "Staff updates customer details.", "Customer", "TODO: Implement Edit Customer"),
                action("Customer Detail", "Staff views customer profile and points.", "Customer, Orders, Payments", "TODO: Implement Customer Detail"),
                action("Loyal Customer Ranking", "Admin views loyalty ranking.", "Customer.MembershipTier, Customer.Points", "TODO: Implement Loyal Customer Ranking"),
                action("Manage Customer Levels", "Manage Silver, Gold and VIP levels.", "Customer.MembershipTier", "TODO: Implement Customer Levels"),
                action("Apply Loyalty Discount", "Apply discounts by membership level.", "Customer.MembershipTier, Orders.DiscountAmount", "TODO: Implement Loyalty Discount"),
                action("View Customer Debt", "Monitor customer debt information.", "Orders, Payments, Customer", "TODO: Implement Customer Debt"),
                action("Collect Customer Debt", "Record customer debt payments.", "Payments, FinanceTransaction", "TODO: Implement Debt Collection"));

        add(modules, "Finance Management", "/financial-management", "Income, expense, cash book, reconciliation, and finance dashboard workspace.", "FinanceTransaction, Payments, Orders", "Member 5",
                action("Financial Dashboard", "Admin monitors revenue/expense/profit.", "FinanceTransaction, Payments, Orders", "TODO: Implement Financial Dashboard"),
                action("Income List", "Admin views income transactions.", "FinanceTransaction, Payments", "TODO: Implement Income List"),
                action("Expense List", "Admin views expenses.", "FinanceTransaction", "TODO: Implement Expense Management"),
                action("Add Expense", "Staff records expenses.", "FinanceTransaction", "TODO: Implement Add Expense"),
                action("Create Income Receipt", "Record income transactions.", "FinanceTransaction", "TODO: Implement Income Receipt"),
                action("Create Expense Receipt", "Record expense transactions.", "FinanceTransaction", "TODO: Implement Expense Receipt"),
                action("Manage Income & Expense Types", "Manage income and expense categories.", "Skeleton only", "TODO: Implement Income/Expense Types"),
                action("Daily Cash Reconciliation", "Compare actual cash with system records.", "FinanceTransaction, Payments", "TODO: Implement Cash Reconciliation"),
                action("View Cash Book", "Monitor cash flow.", "FinanceTransaction", "TODO: Implement Cash Book"));

        add(modules, "Report Center", "/reports", "All sales, inventory, customer, debt, finance, and export reports.", "Orders, Payments, Warehouse, Customer, FinanceTransaction", "Member 5",
                action("Sales Report by Store", "Revenue reports by branch.", "Orders, Payments, Branch", "TODO: Implement Sales Report by Store"),
                action("Employee Sales Report", "Employee sales performance.", "Orders, Payments, Employee", "TODO: Implement Employee Sales Report"),
                action("Inventory Report", "Inventory reports.", "Warehouse, Product, Branch", "TODO: Implement Inventory Report"),
                action("Loyal Customer Report", "Loyal customer statistics.", "Customer, Orders", "TODO: Implement Loyal Customer Report"),
                action("View Revenue Reports by Time", "View revenue by day/month/year.", "Orders, Payments", "TODO: Implement Revenue by Time"),
                action("View Profit & Loss Reports", "Analyze revenue, expenses and profits.", "FinanceTransaction, Payments", "TODO: Implement Profit & Loss"),
                action("View Debt Reports", "Monitor customer and supplier debts.", "Orders, Payments", "TODO: Implement Debt Reports"),
                action("View Best-selling Product Reports", "Analyze best and slow-selling products.", "OrderDetail, Product", "TODO: Implement Product Reports"),
                action("Export Report", "Export reports to files.", "Read-only report datasets", "TODO: Implement Export Report"));

        add(modules, "Website & SEO Management", "/website", "Public website pages plus content and SEO management workspace.", "Product, Category; skeleton-only for content/SEO", "Member 5",
                action("Homepage", "Public homepage.", "Product, Category", "TODO: Implement Website Homepage"),
                action("About Us", "Company information.", "Skeleton only", "TODO: Implement About Us content"),
                action("Product Showcase", "Public product catalog.", "Product, Category", "TODO: Implement Product Showcase"),
                action("Contact Page", "Customer contact requests.", "Skeleton only", "TODO: Implement Contact Page"),
                action("Manage Website Content", "Manage banners/articles/content.", "Skeleton only", "TODO: Implement Website Content"),
                action("SEO Settings", "Configure SEO metadata.", "Skeleton only", "TODO: Implement SEO Settings"));

        add(modules, "System Dashboard & Operations", "/system-configuration", "Overview, activity log, notifications, and business configuration workspace.", "AuditLog, Product, Warehouse; skeleton-only for settings", "Member 1",
                action("Dashboard Overview", "Overview statistics.", "Orders, Customer, Warehouse, FinanceTransaction", "TODO: Implement Dashboard metrics"),
                action("Activity Log", "Admin tracks activity.", "AuditLog, Employee", "TODO: Implement Activity Log"),
                action("Notification Center", "Low stock and activity notifications.", "Product.StockAlertQty, Warehouse.Quantity", "TODO: Implement Notification Center"),
                action("Business Configuration", "VAT/company/logo/currency policies.", "Skeleton only", "TODO: Implement Business Configuration"));

        return Collections.unmodifiableList(modules);
    }

    private static void add(List<ModuleDefinition> modules, String name, String route, String description,
                            String databaseMapping, String ownerSuggestion, ActionDefinition... actions) {
        modules.add(new ModuleDefinition(name, route, description, databaseMapping, ownerSuggestion, List.of(actions)));
    }

    private static ActionDefinition action(String name, String description, String databaseMapping, String todo) {
        return new ActionDefinition(name, description, databaseMapping, todo);
    }

    private record ModuleDefinition(String name, String route, String description, String databaseMapping,
                                    String ownerSuggestion, List<ActionDefinition> actions) {
    }

    private record ActionDefinition(String name, String description, String databaseMapping, String todo) {
    }
}
