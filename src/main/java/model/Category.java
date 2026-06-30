package model;

import java.time.LocalDateTime;

public class Category {
    private int categoryId;
    private String categoryName;
    private String description;
    private Integer parentCategoryId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String parentCategoryName;

    public Category() { this.status = "ACTIVE"; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(Integer parentCategoryId) { this.parentCategoryId = parentCategoryId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getParentCategoryName() { return parentCategoryName; }
    public void setParentCategoryName(String parentCategoryName) { this.parentCategoryName = parentCategoryName; }
}
