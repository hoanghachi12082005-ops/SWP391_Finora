package category.controller;

import category.model.Category;
import category.service.CategoryManagementService;
import category.service.CategoryManagementService.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Category Management Servlet
 * Controller xử lý các request cho module quản lý nhóm hàng.
 * Điều phối request, gọi service, và forward đến JSP view.
 * 
 * URL Patterns: /category-management, /category-management/*
 * 
 * @author Finora Team
 * @version 1.0
 */
public class CategoryManagementServlet extends HttpServlet {

    private CategoryManagementService categoryService;

    // ==================== CONSTANTS ====================
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 10;
    
    // Permission check - Chỉ Store Manager và Warehouse Staff được phép quản lý category
    private static final Set<String> MANAGER_ROLES = Set.of("Store Manager", "Warehouse Staff");

    // ==================== LIFECYCLE ====================
    
    @Override
    public void init() throws ServletException {
        super.init();
        categoryService = new CategoryManagementService();
    }

    // ==================== HTTP METHODS ====================
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Handle print mode
        boolean printMode = "true".equals(request.getParameter("printMode"));
        request.setAttribute("printMode", printMode);
        
        if (printMode) {
            // Print mode - show all categories without pagination
            handlePrintMode(request, response);
            return;
        }
        
        // Normal mode - list with pagination
        handleList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Set character encoding
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        if (action == null) {
            sendRedirectWithMessage(request, response, "danger", "Hành động không hợp lệ!");
            return;
        }

        switch (action) {
            case "add":
                handleAdd(request, response);
                break;
            case "update":
                handleUpdate(request, response);
                break;
            case "delete":
                handleDelete(request, response);
                break;
            default:
                sendRedirectWithMessage(request, response, "warning", "Hành động không được hỗ trợ: " + action);
        }
    }

    // ==================== LIST HANDLERS ====================
    
    /**
     * Xử lý hiển thị danh sách category với phân trang.
     */
    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get filter parameters
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String parentName = request.getParameter("parentName");
        int page = parsePositiveInt(request.getParameter("page"), DEFAULT_PAGE);
        int limit = DEFAULT_LIMIT;

        // Get data
        List<Category> categories = categoryService.getCategoriesWithPagination(
                keyword, status, parentName, page, limit);
        
        int totalItems = categoryService.getTotalCount(keyword, status, parentName);
        int totalPages = (int) Math.ceil((double) totalItems / limit);
        
        List<Category> parentOptions = categoryService.getParentOptions();
        
        // Statistics
        int totalRootCategories = categoryService.getRootCategoryCount(keyword, status);
        int totalLinkedProducts = categoryService.getLinkedProductCount(keyword, status, parentName);

        // Set attributes for view
        request.setAttribute("categories", categories);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("parentNameFilter", parentName);
        request.setAttribute("parentOptions", parentOptions);
        
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("totalRootCategories", totalRootCategories);
        request.setAttribute("totalLinkedProducts", totalLinkedProducts);
        
        // Check permission - dựa trên role của user
        boolean canManageCategory = canManageCategory(request);
        request.setAttribute("canManageCategory", canManageCategory);

        // Forward to view
        request.getRequestDispatcher("/WEB-INF/views/category-management/index.jsp").forward(request, response);
    }

    /**
     * Xử lý chế độ in ấn.
     */
    private void handlePrintMode(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String parentName = request.getParameter("parentName");

        // Get all categories for printing (no pagination)
        List<Category> categories = categoryService.getCategoriesWithPagination(
                keyword, status, parentName, 1, Integer.MAX_VALUE);
        
        List<Category> parentOptions = categoryService.getParentOptions();

        request.setAttribute("categories", categories);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("parentNameFilter", parentName);
        request.setAttribute("parentOptions", parentOptions);
        request.setAttribute("totalItems", categories.size());
        request.setAttribute("totalPages", 1);
        request.setAttribute("canManageCategory", false);

        request.getRequestDispatcher("/WEB-INF/views/category-management/index.jsp").forward(request, response);
    }

    // ==================== CRUD HANDLERS ====================
    
    /**
     * Xử lý thêm mới category.
     */
    private void handleAdd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        // Check permission
        if (!canManageCategory(request)) {
            sendRedirectWithMessage(request, response, "danger", 
                    "Bạn không có quyền thêm nhóm hàng!");
            return;
        }

        // Get parameters
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String parentName = request.getParameter("parentName");
        String status = request.getParameter("status");

        // Create category
        ServiceResult result = categoryService.createCategory(name, description, parentName, status);

        // Set session message
        String messageType = result.isSuccess() ? "success" : "danger";
        sendRedirectWithMessage(request, response, messageType, result.getMessage());
    }

    /**
     * Xử lý cập nhật category.
     */
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        // Check permission
        if (!canManageCategory(request)) {
            sendRedirectWithMessage(request, response, "danger", 
                    "Bạn không có quyền cập nhật nhóm hàng!");
            return;
        }

        // Get parameters
        String categoryIdStr = request.getParameter("categoryId");
        if (categoryIdStr == null || categoryIdStr.trim().isEmpty()) {
            sendRedirectWithMessage(request, response, "danger", 
                    "ID nhóm hàng không hợp lệ!");
            return;
        }

        int categoryId;
        try {
            categoryId = Integer.parseInt(categoryIdStr.trim());
        } catch (NumberFormatException e) {
            sendRedirectWithMessage(request, response, "danger", 
                    "ID nhóm hàng không hợp lệ!");
            return;
        }

        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String parentName = request.getParameter("parentName");
        String status = request.getParameter("status");

        // Update category
        ServiceResult result = categoryService.updateCategory(
                categoryId, name, description, parentName, status);

        // Set session message
        String messageType = result.isSuccess() ? "success" : "danger";
        sendRedirectWithMessage(request, response, messageType, result.getMessage());
    }

    /**
     * Xử lý xóa category.
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        // Check permission
        if (!canManageCategory(request)) {
            sendRedirectWithMessage(request, response, "danger", 
                    "Bạn không có quyền xóa nhóm hàng!");
            return;
        }

        // Get parameters
        String categoryIdStr = request.getParameter("categoryId");
        if (categoryIdStr == null || categoryIdStr.trim().isEmpty()) {
            sendRedirectWithMessage(request, response, "danger", 
                    "ID nhóm hàng không hợp lệ!");
            return;
        }

        int categoryId;
        try {
            categoryId = Integer.parseInt(categoryIdStr.trim());
        } catch (NumberFormatException e) {
            sendRedirectWithMessage(request, response, "danger", 
                    "ID nhóm hàng không hợp lệ!");
            return;
        }

        // Delete category
        ServiceResult result = categoryService.deleteCategory(categoryId);

        // Set session message
        String messageType = result.isSuccess() ? "success" : "danger";
        sendRedirectWithMessage(request, response, messageType, result.getMessage());
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Kiểm tra role hiện tại có quyền quản lý category không.
     * Chỉ Admin, Owner, Store Manager được phép.
     */
    private boolean canManageCategory(HttpServletRequest request) {
        String currentRole = getCurrentRole(request);
        return MANAGER_ROLES.contains(currentRole);
    }
    
    /**
     * Lấy current role từ session.
     */
    private String getCurrentRole(HttpServletRequest request) {
        Object role = request.getSession().getAttribute("currentRole");
        if (role == null || role.toString().isBlank()) {
            return "Admin"; // Default role
        }
        return normalizeRole(role.toString());
    }
    
    /**
     * Normalize role name.
     */
    private String normalizeRole(String roleName) {
        if (roleName == null || roleName.isBlank()) return "Admin";
        return switch (roleName.trim()) {
            case "Owner", "Shop Owner" -> "Owner";
            case "StoreManager", "Store Manager" -> "Store Manager";
            case "SalesStaff", "Sales Staff" -> "Sales Staff";
            case "WarehouseStaff", "Warehouse Staff" -> "Warehouse Staff";
            case "Guest" -> "Guest";
            default -> "Admin";
        };
    }

    /**
     * Parse positive integer từ string.
     */
    private int parsePositiveInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gửi redirect kèm message qua session.
     */
    private void sendRedirectWithMessage(HttpServletRequest request, HttpServletResponse response,
                                         String messageType, String message) throws IOException {
        request.getSession().setAttribute("message", message);
        request.getSession().setAttribute("messageType", messageType);
        response.sendRedirect(request.getContextPath() + "/category-management");
    }
}
