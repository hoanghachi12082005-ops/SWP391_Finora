package category.service;

import category.dao.CategoryDAO;
import category.model.Category;

import java.util.List;

/**
 * Category Management Service
 * Service layer cho nghiệp vụ quản lý nhóm hàng.
 * Chứa business logic và validation.
 * 
 * @author Finora Team
 * @version 1.0
 */
public class CategoryManagementService {

    private final CategoryDAO categoryDAO;

    public CategoryManagementService() {
        this.categoryDAO = new CategoryDAO();
    }

    // ==================== PAGINATION & LISTING ====================
    
    /**
     * Lấy danh sách category với phân trang.
     * 
     * @param keyword    Từ khóa tìm kiếm
     * @param status     Trạng thái lọc
     * @param parentName Tên nhóm cha
     * @param page       Trang hiện tại
     * @param limit      Số bản ghi mỗi trang
     * @return Danh sách category
     */
    public List<Category> getCategoriesWithPagination(String keyword, String status, String parentName, int page, int limit) {
        return categoryDAO.findAllWithPagination(keyword, status, parentName, page, limit);
    }

    /**
     * Đếm tổng số category theo bộ lọc.
     * 
     * @param keyword    Từ khóa tìm kiếm
     * @param status     Trạng thái
     * @param parentName Tên nhóm cha
     * @return Tổng số category
     */
    public int getTotalCount(String keyword, String status, String parentName) {
        return categoryDAO.count(keyword, status, parentName);
    }

    /**
     * Đếm số nhóm gốc.
     */
    public int getRootCategoryCount(String keyword, String status) {
        return categoryDAO.countRootCategories(keyword, status);
    }

    /**
     * Đếm số sản phẩm liên kết.
     */
    public int getLinkedProductCount(String keyword, String status, String parentName) {
        return categoryDAO.countLinkedProducts(keyword, status, parentName);
    }

    /**
     * Lấy danh sách parent options.
     */
    public List<Category> getParentOptions() {
        return categoryDAO.findParentOptions();
    }

    // ==================== CRUD OPERATIONS ====================
    
    /**
     * Thêm mới category.
     * 
     * @param name        Tên nhóm hàng
     * @param description Mô tả
     * @param parentName  Tên nhóm cha (null nếu là nhóm gốc)
     * @param status      Trạng thái
     * @return Result chứa success/failure và message
     */
    public ServiceResult createCategory(String name, String description, String parentName, String status) {
        // Validate required fields
        if (!isValidCategoryName(name)) {
            return ServiceResult.failure("Tên nhóm hàng không hợp lệ!");
        }

        // Check duplicate name
        if (categoryDAO.existsByName(name, null)) {
            return ServiceResult.failure("Tên nhóm hàng '" + name + "' đã tồn tại!");
        }

        // Build category
        Category category = new Category();
        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : null);
        category.setStatus(status != null && !status.trim().isEmpty() ? status.trim() : "active");

        // Set parent ID if specified
        if (parentName != null && !parentName.trim().isEmpty() && !isRootParentFilter(parentName)) {
            Integer parentId = categoryDAO.findIdByName(parentName.trim());
            if (parentId == null) {
                return ServiceResult.failure("Nhóm cha '" + parentName + "' không tồn tại!");
            }
            category.setParentId(parentId);
        }

        // Insert
        boolean success = categoryDAO.insert(category);
        
        if (success) {
            return ServiceResult.success("Thêm nhóm hàng thành công!");
        } else {
            return ServiceResult.failure("Thêm nhóm hàng thất bại!");
        }
    }

    /**
     * Cập nhật category.
     * 
     * @param categoryId  ID category cần cập nhật
     * @param name       Tên mới
     * @param description Mô tả mới
     * @param parentName  Tên nhóm cha mới
     * @param status      Trạng thái mới
     * @return Result chứa success/failure và message
     */
    public ServiceResult updateCategory(int categoryId, String name, String description, String parentName, String status) {
        // Validate required fields
        if (!isValidCategoryName(name)) {
            return ServiceResult.failure("Tên nhóm hàng không hợp lệ!");
        }

        // Check exists
        Category existingCategory = categoryDAO.findById(categoryId);
        if (existingCategory == null) {
            return ServiceResult.failure("Nhóm hàng không tồn tại!");
        }

        // Check duplicate name (excluding current category)
        if (categoryDAO.existsByName(name, categoryId)) {
            return ServiceResult.failure("Tên nhóm hàng '" + name + "' đã tồn tại!");
        }

        // Validate parent (prevent circular reference)
        Integer parentId = null;
        if (parentName != null && !parentName.trim().isEmpty() && !isRootParentFilter(parentName)) {
            parentId = categoryDAO.findIdByName(parentName.trim());
            if (parentId == null) {
                return ServiceResult.failure("Nhóm cha '" + parentName + "' không tồn tại!");
            }
            
            // Check circular reference - cannot set self as parent
            if (parentId == categoryId) {
                return ServiceResult.failure("Không thể đặt nhóm hàng làm cha của chính nó!");
            }
            
            // Check if new parent is a descendant of this category
            if (categoryDAO.isAncestorOf(categoryId, parentId)) {
                return ServiceResult.failure("Nhóm cha không hợp lệ - tạo vòng lặp phân cấp!");
            }
        }

        // Build updated category
        existingCategory.setName(name.trim());
        existingCategory.setDescription(description != null ? description.trim() : null);
        existingCategory.setParentId(parentId);
        existingCategory.setStatus(status != null && !status.trim().isEmpty() ? status.trim() : "active");

        // Update
        boolean success = categoryDAO.update(existingCategory);
        
        if (success) {
            return ServiceResult.success("Cập nhật nhóm hàng thành công!");
        } else {
            return ServiceResult.failure("Cập nhật nhóm hàng thất bại!");
        }
    }

    /**
     * Xóa category.
     * 
     * @param categoryId ID category cần xóa
     * @return Result chứa success/failure và message
     */
    public ServiceResult deleteCategory(int categoryId) {
        // Check exists
        Category category = categoryDAO.findById(categoryId);
        if (category == null) {
            return ServiceResult.failure("Nhóm hàng không tồn tại!");
        }

        // Check has linked products
        if (categoryDAO.hasLinkedProducts(categoryId)) {
            return ServiceResult.failure("Không thể xóa nhóm hàng đang có sản phẩm liên kết!");
        }

        // Check has children
        List<Category> all = categoryDAO.findAll();
        boolean hasChildren = all.stream()
                .anyMatch(c -> categoryId == c.getParentId());
        
        if (hasChildren) {
            return ServiceResult.failure("Không thể xóa nhóm hàng đang có nhóm con!");
        }

        // Delete
        boolean success = categoryDAO.delete(categoryId);
        
        if (success) {
            return ServiceResult.success("Xóa nhóm hàng thành công!");
        } else {
            return ServiceResult.failure("Xóa nhóm hàng thất bại!");
        }
    }

    // ==================== FIND BY ID ====================
    
    /**
     * Lấy category theo ID.
     */
    public Category getCategoryById(int categoryId) {
        return categoryDAO.findById(categoryId);
    }

    /**
     * Lấy tất cả category active.
     */
    public List<Category> getAllActiveCategories() {
        return categoryDAO.findAllActive();
    }

    // ==================== VALIDATION ====================
    
    /**
     * Validate tên category.
     */
    private boolean isValidCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 255;
    }

    /**
     * Kiểm tra có phải lọc nhóm gốc không.
     */
    private boolean isRootParentFilter(String parentName) {
        String trimmed = parentName == null ? "" : parentName.trim();
        return trimmed.isEmpty() || "goc".equalsIgnoreCase(trimmed) || "gốc".equalsIgnoreCase(trimmed);
    }

    // ==================== INNER CLASSES ====================
    
    /**
     * Result class cho service operations.
     */
    public static class ServiceResult {
        private final boolean success;
        private final String message;

        private ServiceResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ServiceResult success(String message) {
            return new ServiceResult(true, message);
        }

        public static ServiceResult failure(String message) {
            return new ServiceResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
