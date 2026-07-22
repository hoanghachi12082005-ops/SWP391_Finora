package constant;

public final class AppConstants {

    private AppConstants() {}

    // Session keys
    public static final String SESSION_USER = "currentUser";
    public static final String SESSION_EMPLOYEE = "employee";
    public static final String SESSION_CSRF_TOKEN = "csrfToken";
    public static final String SESSION_CART = "cart";
    public static final String SESSION_CART_TABS = "cartTabs";
    public static final String SESSION_ACTIVE_TAB = "activeTab";

    // Flash message keys
    public static final String FLASH_SUCCESS = "successMessage";
    public static final String FLASH_ERROR = "errorMessage";
    public static final String FLASH_MESSAGE = "message";

    // Role names
    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_OWNER = "Owner";
    public static final String ROLE_STORE_MANAGER = "StoreManager";
    public static final String ROLE_SALES_STAFF = "SalesStaff";
    public static final String ROLE_WAREHOUSE_STAFF = "WarehouseStaff";

    // Employee status
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final int MAX_FAILED_LOGIN = 5;

    // Order status
    public static final String ORDER_PENDING = "PENDING";
    public static final String ORDER_COMPLETED = "COMPLETED";
    public static final String ORDER_CANCELLED = "CANCELLED";

    // Order type constants
    public static final String ORDER_TYPE_SALE = "SALE";
    public static final String ORDER_TYPE_PURCHASE = "PURCHASE";
    public static final String ORDER_TYPE_OTHER = "OTHER";

    // Payment type constants
    public static final String PAYMENT_TYPE_INCOME = "INCOME";
    public static final String PAYMENT_TYPE_EXPENSE = "EXPENSE";

    // Payment status constants
    public static final String PAYMENT_STATUS_COMPLETED = "COMPLETED";
    public static final String PAYMENT_STATUS_PAID = "PAID";
    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    public static final String PAYMENT_STATUS_CANCELLED = "CANCELLED";

    // Inventory ticket status
    public static final String TICKET_PENDING = "PENDING";
    public static final String TICKET_IN_TRANSIT = "IN_TRANSIT";
    public static final String TICKET_COMPLETED = "COMPLETED";
    public static final String TICKET_REJECTED = "REJECTED";
    public static final String TICKET_CANCELLED = "CANCELLED";
    public static final String TICKET_COMPLETED_WITH_ERROR = "COMPLETED_WITH_ERROR";

    // Pagination defaults
    public static final int PAGE_DEFAULT = 1;
    public static final int PAGE_SIZE_DEFAULT = 20;

    // Application
    public static final String APP_CONTEXT_PATH = "FinoraRetail";
    public static final String ENCODING_UTF8 = "UTF-8";
}
