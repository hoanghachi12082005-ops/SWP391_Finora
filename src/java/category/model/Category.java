package category.model;

import java.util.Objects;

/**
 * Category Model
 * Entity cho bảng Category trong DBFinora.
 * Hỗ trợ hierarchical category với ParentID.
 * 
 * @author Finora Team
 * @version 1.0
 */
public class Category {
    
    private int categoryId;
    private String name;
    private String description;
    private Integer parentId;
    private String parentName;
    private String status;
    private int productCount;

    /**
     * Constructor mặc định.
     */
    public Category() {
    }

    /**
     * Constructor với các tham số cơ bản.
     * 
     * @param name        Tên nhóm hàng
     * @param description Mô tả nhóm hàng
     * @param status      Trạng thái (active/inactive)
     */
    public Category(String name, String description, String status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    /**
     * Kiểm tra category có đang hoạt động hay không.
     * 
     * @return true nếu status là "active"
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    /**
     * Kiểm tra category có phải là nhóm gốc (không có parent) hay không.
     * 
     * @return true nếu parentId là null
     */
    public boolean isRootCategory() {
        return parentId == null;
    }

    /**
     * Kiểm tra category có sản phẩm liên kết hay không.
     * 
     * @return true nếu productCount > 0
     */
    public boolean hasProducts() {
        return productCount > 0;
    }

    // ==================== Getters and Setters ====================

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    // ==================== Override Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return categoryId == category.categoryId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", status='" + status + '\'' +
                '}';
    }
}
