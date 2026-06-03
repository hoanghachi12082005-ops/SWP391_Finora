<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="category.model.Category" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    if (categories == null) categories = new ArrayList<>();
    
    List<Category> parentOptions = (List<Category>) request.getAttribute("parentOptions");
    if (parentOptions == null) parentOptions = new ArrayList<>();
    
    String keyword = (String) request.getAttribute("keyword");
    String selectedStatus = (String) request.getAttribute("selectedStatus");
    String parentNameFilter = (String) request.getAttribute("parentNameFilter");
    
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    Integer totalItems = (Integer) request.getAttribute("totalItems");
    Integer totalRootCategories = (Integer) request.getAttribute("totalRootCategories");
    Integer totalLinkedProducts = (Integer) request.getAttribute("totalLinkedProducts");
    
    Boolean canManageCategory = (Boolean) request.getAttribute("canManageCategory");
    if (canManageCategory == null) canManageCategory = false;
    
    Boolean printMode = (Boolean) request.getAttribute("printMode");
    if (printMode == null) printMode = false;
    
    String message = (String) session.getAttribute("message");
    String messageType = (String) session.getAttribute("messageType");
    session.removeAttribute("message");
    session.removeAttribute("messageType");
    
    if (keyword == null) keyword = "";
    if (selectedStatus == null) selectedStatus = "";
    if (parentNameFilter == null) parentNameFilter = "";
    if (currentPage == null) currentPage = 1;
    if (totalPages == null) totalPages = 1;
    if (totalItems == null) totalItems = 0;
    if (totalRootCategories == null) totalRootCategories = 0;
    if (totalLinkedProducts == null) totalLinkedProducts = 0;
%>

<%!
    // Helper function for JS escaping
    private String escapeJs(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý nhóm hàng | Finora</title>
    <meta name="description" content="Quản lý danh mục nhóm hàng trong hệ thống Finora">
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/category-styles.css">
</head>
<body>
    <!-- Sidebar -->
    <aside class="sidebar">
        <div class="sidebar-brand">
            <div class="sidebar-brand-icon">F</div>
            <div class="sidebar-brand-text"><strong>Finora</strong><small>KiotRetail</small></div>
        </div>
        <nav class="sidebar-nav">
            <a href="${pageContext.request.contextPath}/dashboard" class="sidebar-nav-item">
                <span class="material-icons">dashboard</span> Dashboard
            </a>
            <a href="${pageContext.request.contextPath}/category-management" class="sidebar-nav-item active">
                <span class="material-icons">category</span> Nhóm hàng
            </a>
            <a href="${pageContext.request.contextPath}/product-management" class="sidebar-nav-item">
                <span class="material-icons">inventory_2</span> Hàng hóa
            </a>
            <a href="${pageContext.request.contextPath}/sales-management" class="sidebar-nav-item">
                <span class="material-icons">point_of_sale</span> Bán hàng
            </a>
            <a href="${pageContext.request.contextPath}/warehouse-management" class="sidebar-nav-item">
                <span class="material-icons">warehouse</span> Kho hàng
            </a>
        </nav>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
        <% if (printMode) { %>
        <!-- Print Action Bar -->
        <div class="print-action-bar">
            <div class="print-action-info">
                <span class="material-icons">preview</span>
                Chế độ xem trước in - Danh sách nhóm hàng
            </div>
            <button class="cat-btn-primary print-btn" onclick="window.print()">
                <span class="material-icons">local_printshop</span>
                In ngay
            </button>
        </div>
        <% } %>

        <!-- Hero Header -->
        <div class="cat-hero">
            <div class="cat-hero-content">
                <div class="cat-hero-text">
                    <div class="cat-hero-badge">
                        <span class="material-icons">category</span>
                        Quản lý phân loại
                    </div>
                    <p class="cat-hero-desc">Tổ chức và quản lý danh mục sản phẩm trong hệ thống</p>
                </div>
                <div class="cat-hero-actions">
                    <a href="javascript:void(0)" class="cat-btn-outline" onclick="openPrintView()">
                        <span class="material-icons">print</span>
                        In danh sách
                    </a>
                    <% if (canManageCategory) { %>
                    <button class="cat-btn-primary btn-add" onclick="openAddModal()">
                        <span class="material-icons">add_circle</span>
                        Thêm nhóm hàng
                    </button>
                    <% } %>
                </div>
            </div>
        </div>

        <% if (message != null && !message.isEmpty()) { %>
        <div class="cat-toast alert-<%= messageType %>">
            <div class="cat-toast-icon">
                <span class="material-icons">
                    <% if ("success".equals(messageType)) { %>check_circle<% }
                       else if ("warning".equals(messageType)) { %>warning<% }
                       else { %>error<% } %>
                </span>
            </div>
            <span class="cat-toast-text"><%= message %></span>
            <button type="button" class="cat-toast-close" onclick="this.parentElement.remove()">
                <span class="material-icons">close</span>
            </button>
        </div>
        <% } %>

        <!-- Stats Cards -->
        <div class="cat-stats-grid">
            <div class="cat-stat-card cat-stat-total">
                <div class="cat-stat-icon-wrap"><span class="material-icons">folder_special</span></div>
                <div class="cat-stat-info">
                    <div class="cat-stat-number"><%= totalItems %></div>
                    <div class="cat-stat-text">Tổng nhóm hàng</div>
                </div>
            </div>
            <div class="cat-stat-card cat-stat-root">
                <div class="cat-stat-icon-wrap"><span class="material-icons">account_tree</span></div>
                <div class="cat-stat-info">
                    <div class="cat-stat-number"><%= totalRootCategories %></div>
                    <div class="cat-stat-text">Nhóm gốc</div>
                </div>
            </div>
            <div class="cat-stat-card cat-stat-products">
                <div class="cat-stat-icon-wrap"><span class="material-icons">inventory_2</span></div>
                <div class="cat-stat-info">
                    <div class="cat-stat-number"><%= totalLinkedProducts %></div>
                    <div class="cat-stat-text">Sản phẩm liên kết</div>
                </div>
            </div>
        </div>

        <!-- Filter Bar -->
        <div class="cat-filter-bar">
            <div class="cat-filter-title">
                <span class="material-icons">filter_list</span>
                <span>Bộ lọc</span>
            </div>
            <form method="get" action="${pageContext.request.contextPath}/category-management" class="cat-filter-form">
                <div class="cat-filter-group">
                    <div class="cat-filter-item">
                        <span class="material-icons cat-filter-icon">search</span>
                        <input type="text" class="cat-filter-input" name="keyword" placeholder="Tìm nhóm hàng..." value="<%= keyword %>">
                    </div>
                    <div class="cat-filter-item">
                        <span class="material-icons cat-filter-icon">toggle_on</span>
                        <select class="cat-filter-select" name="status">
                            <option value="">Tất cả trạng thái</option>
                            <option value="active" <%= "active".equals(selectedStatus) ? "selected" : "" %>>Đang sử dụng</option>
                            <option value="inactive" <%= "inactive".equals(selectedStatus) ? "selected" : "" %>>Ngừng sử dụng</option>
                        </select>
                    </div>
                    <div class="cat-filter-item">
                        <span class="material-icons cat-filter-icon">account_tree</span>
                        <input type="text" class="cat-filter-input" name="parentName" list="parentNameList" value="<%= parentNameFilter %>" placeholder="Gõ tên nhóm cha..." autocomplete="off">
                        <datalist id="parentNameList">
                            <option value="gốc">Chỉ nhóm gốc</option>
                            <% for (Category parent : parentOptions) { %>
                            <option value="<%= parent.getName() %>"><%= parent.getName() %></option>
                            <% } %>
                        </datalist>
                    </div>
                </div>
                <div class="cat-filter-actions">
                    <button type="submit" class="cat-btn-filter">
                        <span class="material-icons">filter_alt</span>
                    </button>
                    <a href="${pageContext.request.contextPath}/category-management" class="cat-btn-reset" title="Xóa bộ lọc">
                        <span class="material-icons">refresh</span>
                    </a>
                </div>
            </form>
        </div>

        <!-- Data Table -->
        <div class="cat-table-wrapper">
            <div class="cat-table-header">
                <div class="cat-table-title">
                    <span class="material-icons">list_alt</span>
                    Danh sách nhóm hàng
                </div>
                <div class="cat-table-count">
                    <span class="material-icons" style="font-size:16px;">inventory</span>
                    <%= categories.size() %> nhóm
                </div>
            </div>
            <table class="cat-table" id="categoriesTable">
                <thead>
                    <tr>
                        <th style="width:70px;">Mã</th>
                        <th>Tên nhóm hàng</th>
                        <th>Nhóm cha</th>
                        <th>Mô tả</th>
                        <th style="width:100px;text-align:center;">Sản phẩm</th>
                        <th style="width:150px;">Trạng thái</th>
                        <% if (canManageCategory) { %>
                        <th style="width:90px;text-align:center;" class="action-col">Thao tác</th>
                        <% } %>
                    </tr>
                </thead>
                <tbody>
                    <% if (categories.isEmpty()) { %>
                    <tr>
                        <td colspan="5">
                            <div class="cat-empty">
                                <div class="cat-empty-icon"><span class="material-icons">folder_off</span></div>
                                <h5>Không tìm thấy nhóm hàng</h5>
                                <p>Thử thay đổi bộ lọc hoặc thêm nhóm hàng mới</p>
                                <% if (canManageCategory) { %>
                                <button class="cat-btn-primary btn-add" onclick="openAddModal()">
                                    <span class="material-icons">add</span>
                                    Thêm nhóm hàng
                                </button>
                                <% } %>
                            </div>
                        </td>
                    </tr>
                    <% } else { %>
                    <% for (Category category : categories) { %>
                    <tr class="cat-row">
                        <td><span class="cat-id"><%= category.getCategoryId() %></span></td>
                        <td>
                            <div class="cat-name-cell">
                                <a class="cat-name-icon <%= category.getParentName() == null || category.getParentName().isEmpty() ? "is-root" : "is-child" %>" href="${pageContext.request.contextPath}/product-management?categoryId=<%= category.getCategoryId() %>" title="Xem hàng hóa">
                                    <span class="material-icons"><%= category.getParentName() == null || category.getParentName().isEmpty() ? "widgets" : "segment" %></span>
                                </a>
                                <a class="cat-name-text cat-name-link" href="${pageContext.request.contextPath}/product-management?categoryId=<%= category.getCategoryId() %>" title="Xem hàng hóa">
                                    <%= category.getName() %>
                                </a>
                            </div>
                        </td>
                        <td>
                            <% if (category.getParentName() == null || category.getParentName().isEmpty()) { %>
                            <span class="cat-badge cat-badge-root">
                                <span class="material-icons">hub</span>
                                Nhóm gốc
                            </span>
                            <% } else { %>
                            <span class="cat-badge cat-badge-child">
                                <span class="material-icons">turn_right</span>
                                <%= category.getParentName() %>
                            </span>
                            <% } %>
                        </td>
                        <td>
                            <% if (category.getDescription() == null || category.getDescription().isEmpty()) { %>
                            <span class="cat-no-desc">— Chưa có mô tả</span>
                            <% } else { %>
                            <span class="cat-desc"><%= category.getDescription() %></span>
                            <% } %>
                        </td>
                        <td style="text-align:center;">
                            <span class="cat-product-pill <%= category.getProductCount() > 0 ? "has-items" : "" %>">
                                <%= category.getProductCount() %>
                            </span>
                        </td>
                        <td>
                            <% if ("active".equals(category.getStatus())) { %>
                            <span class="cat-status cat-status-active">
                                <span class="material-icons">check_circle</span>
                                Đang sử dụng
                            </span>
                            <% } else { %>
                            <span class="cat-status cat-status-inactive">
                                <span class="material-icons">do_not_disturb_on</span>
                                Ngừng sử dụng
                            </span>
                            <% } %>
                        </td>
                        <% if (canManageCategory) { %>
                        <td style="text-align:center;" class="action-col">
                            <button type="button" class="cat-action-btn" title="Chỉnh sửa" onclick="openEditModal(<%= category.getCategoryId() %>, '<%= escapeJs(category.getName()) %>', '<%= escapeJs(category.getDescription()) %>', '<%= escapeJs(category.getParentName()) %>', '<%= category.getStatus() %>')">
                                <span class="material-icons">edit</span>
                            </button>
                        </td>
                        <% } %>
                    </tr>
                    <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <% if (totalPages > 1) { %>
        <div class="cat-pagination">
            <div class="cat-pagination-info">
                Hiển thị <strong><%= categories.size() %></strong> / <strong><%= totalItems %></strong> nhóm hàng
            </div>
            <ul class="pagination">
                <li class="page-item <%= currentPage == 1 ? "disabled" : "" %>">
                    <a class="page-link" href="${pageContext.request.contextPath}/category-management?page=<%= currentPage - 1 %>&keyword=<%= keyword %>&status=<%= selectedStatus %>&parentName=<%= parentNameFilter %>">
                        <span class="material-icons" style="font-size:18px;">chevron_left</span>
                    </a>
                </li>
                <% for (int i = 1; i <= totalPages; i++) { %>
                <li class="page-item <%= currentPage == i ? "active" : "" %>">
                    <a class="page-link" href="${pageContext.request.contextPath}/category-management?page=<%= i %>&keyword=<%= keyword %>&status=<%= selectedStatus %>&parentName=<%= parentNameFilter %>"><%= i %></a>
                </li>
                <% } %>
                <li class="page-item <%= currentPage == totalPages ? "disabled" : "" %>">
                    <a class="page-link" href="${pageContext.request.contextPath}/category-management?page=<%= currentPage + 1 %>&keyword=<%= keyword %>&status=<%= selectedStatus %>&parentName=<%= parentNameFilter %>">
                        <span class="material-icons" style="font-size:18px;">chevron_right</span>
                    </a>
                </li>
            </ul>
        </div>
        <% } %>
    </main>

    <!-- Add/Edit Modal -->
    <div class="modal-overlay" id="categoryModal">
        <div class="modal-container">
            <div class="modal-content-bg cat-modal">
                <form method="post" action="${pageContext.request.contextPath}/category-management">
                    <input type="hidden" name="action" id="modalAction" value="add">
                    <input type="hidden" name="categoryId" id="editCategoryId">
                    <div class="cat-modal-header">
                        <div class="modal-header-content">
                            <div class="cat-modal-icon" id="modalIcon"><span class="material-icons">create_new_folder</span></div>
                            <div>
                                <h5 class="modal-title fw-bold mb-0" id="modalTitle">Thêm nhóm hàng mới</h5>
                                <small class="cat-modal-subtitle" id="modalSubtitle">Tạo danh mục phân loại sản phẩm</small>
                            </div>
                        </div>
                        <button type="button" class="btn-close" onclick="closeModal()">&times;</button>
                    </div>
                    <div class="cat-modal-body">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="cat-label">Tên nhóm hàng <span class="text-danger">*</span></label>
                                <input type="text" class="cat-input" name="name" id="inputName" maxlength="255" required placeholder="Nhập tên nhóm hàng">
                            </div>
                            <div class="col-md-6">
                                <label class="cat-label">Nhóm cha</label>
                                <select class="cat-input" name="parentName" id="inputParentName">
                                    <option value="">Không có (nhóm gốc)</option>
                                    <% for (Category parent : parentOptions) { %>
                                    <option value="<%= parent.getName() %>"><%= parent.getName() %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="cat-label">Trạng thái</label>
                                <select class="cat-input" name="status" id="inputStatus">
                                    <option value="active">Đang sử dụng</option>
                                    <option value="inactive">Ngừng sử dụng</option>
                                </select>
                            </div>
                            <div class="col-12">
                                <label class="cat-label">Mô tả</label>
                                <textarea class="cat-input" name="description" id="inputDescription" rows="3" maxlength="1000" placeholder="Mô tả ngắn về nhóm hàng..."></textarea>
                            </div>
                        </div>
                    </div>
                    <div class="cat-modal-footer">
                        <button type="button" class="btn-cancel" onclick="closeModal()">Hủy</button>
                        <button type="submit" class="cat-btn-primary"><span class="material-icons">save</span> Lưu</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/category-scripts.js"></script>
</body>
</html>
