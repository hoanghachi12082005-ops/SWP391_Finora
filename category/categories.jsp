<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Quản lý nhóm hàng"/>
</jsp:include>

<div class="d-flex">
    <jsp:include page="../common/sidebar.jsp">
        <jsp:param name="active" value="categories"/>
    </jsp:include>

    <div class="main-content flex-grow-1">

        <!-- ===== Hero Header ===== -->
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
                    <button class="btn cat-btn-outline" onclick="openPrintView()" type="button">
                        <span class="material-icons">print</span>
                        In danh sách
                    </button>
                    <c:if test="${sessionScope.canManageCategory}">
                        <button class="btn cat-btn-primary" data-bs-toggle="modal" data-bs-target="#addCategoryModal">
                            <span class="material-icons">add_circle</span>
                            Thêm nhóm hàng
                        </button>
                    </c:if>
                </div>
            </div>
        </div>

        <!-- ===== Alert Messages ===== -->
        <c:if test="${not empty sessionScope.message}">
            <div class="cat-toast alert-${sessionScope.messageType}">
                <div class="cat-toast-icon">
                    <span class="material-icons">${sessionScope.messageType == 'success' ? 'check_circle' : sessionScope.messageType == 'warning' ? 'warning' : 'error'}</span>
                </div>
                <span class="cat-toast-text">${sessionScope.message}</span>
                <button type="button" class="cat-toast-close" onclick="this.parentElement.remove()">
                    <span class="material-icons">close</span>
                </button>
            </div>
            <c:remove var="message" scope="session"/>
            <c:remove var="messageType" scope="session"/>
        </c:if>

        <!-- ===== Stats Cards ===== -->
        <div class="cat-stats-grid">
            <div class="cat-stat-card cat-stat-total">
                <div class="cat-stat-icon-wrap">
                    <span class="material-icons">folder_special</span>
                </div>
                <div class="cat-stat-info">
                    <div class="cat-stat-number">${totalItems}</div>
                    <div class="cat-stat-text">Tổng nhóm hàng</div>
                </div>
                <div class="cat-stat-decoration"></div>
            </div>
            <div class="cat-stat-card cat-stat-root">
                <div class="cat-stat-icon-wrap">
                    <span class="material-icons">account_tree</span>
                </div>
                <div class="cat-stat-info">
                    <div class="cat-stat-number">${totalRootCategories}</div>
                    <div class="cat-stat-text">Nhóm gốc</div>
                </div>
                <div class="cat-stat-decoration"></div>
            </div>
            <div class="cat-stat-card cat-stat-products">
                <div class="cat-stat-icon-wrap">
                    <span class="material-icons">inventory_2</span>
                </div>
                <div class="cat-stat-info">
                    <div class="cat-stat-number">${totalLinkedProducts}</div>
                    <div class="cat-stat-text">Sản phẩm liên kết</div>
                </div>
                <div class="cat-stat-decoration"></div>
            </div>
        </div>

        <c:if test="${printMode}">
            <div class="cat-print-toolbar">
                <div class="cat-print-info">
                    <div class="cat-print-icon">
                        <span class="material-icons">print</span>
                    </div>
                    <div>
                        <strong>In danh sách nhóm hàng</strong>
                        <span>Tùy chỉnh bộ lọc bên dưới rồi bấm in danh sách</span>
                    </div>
                </div>
                <div class="cat-print-actions">
                    <button type="button" class="cat-print-action-primary" onclick="window.print()">
                        <span class="material-icons">print</span>
                        <span class="cat-print-action-text">In danh sách</span>
                    </button>
                    <button type="button" class="btn cat-print-close" onclick="window.close()" title="Đóng bản in">
                        <span class="material-icons">close</span>
                    </button>
                </div>
            </div>
        </c:if>

        <!-- ===== Filter Bar ===== -->
        <div class="cat-filter-bar">
            <div class="cat-filter-title">
                <span class="material-icons">filter_list</span>
                <span>Bộ lọc</span>
            </div>
            <form method="get" action="${pageContext.request.contextPath}/admin/categories" class="cat-filter-form">
                <div class="cat-filter-group">
                    <div class="cat-filter-item cat-filter-search">
                        <span class="material-icons cat-filter-icon">search</span>
                        <input type="text" class="cat-filter-input" name="keyword" placeholder="Tìm nhóm hàng..." value="${keyword}">
                    </div>
                    <div class="cat-filter-item cat-filter-status-item">
                        <span class="material-icons cat-filter-icon">toggle_on</span>
                        <select class="cat-filter-select" name="status">
                            <option value="" ${empty selectedStatus ? 'selected' : ''}>Tất cả trạng thái</option>
                            <option value="active" ${selectedStatus == 'active' ? 'selected' : ''}>Đang sử dụng</option>
                            <option value="inactive" ${selectedStatus == 'inactive' ? 'selected' : ''}>Ngừng sử dụng</option>
                        </select>
                    </div>
                    <div class="cat-filter-item">
                        <span class="material-icons cat-filter-icon">account_tree</span>
                        <input type="text" class="cat-filter-input" name="parentName" list="parentNameList"
                               value="${parentNameFilter}" placeholder="Gõ tên nhóm cha..." autocomplete="off">
                        <datalist id="parentNameList">
                            <option value="gốc">Chỉ nhóm gốc</option>
                            <c:forEach var="parent" items="${parentOptions}">
                                <option value="${fn:escapeXml(parent.name)}"/>
                            </c:forEach>
                        </datalist>
                    </div>
                </div>
                <div class="cat-filter-actions">
                    <button type="submit" class="btn cat-btn-filter">
                        <span class="material-icons">filter_alt</span>
                        <span class="cat-btn-text">Lọc</span>
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/categories" class="btn cat-btn-reset" title="Xóa bộ lọc">
                        <span class="material-icons">refresh</span>
                    </a>
                </div>
            </form>
        </div>

        <!-- ===== Data Table ===== -->
        <div class="cat-table-wrapper">
            <div class="cat-table-header">
                <div class="cat-table-title">
                    <span class="material-icons">list_alt</span>
                    Danh sách nhóm hàng
                </div>
                <div class="cat-table-count">
                    <span class="material-icons" style="font-size:16px;">inventory</span>
                    ${fn:length(categories)} nhóm
                </div>
            </div>
            <div class="table-responsive">
                <table class="table cat-table" id="categoriesTable">
                    <thead>
                        <tr>
                            <th style="width:70px;">Mã</th>
                            <th>Tên nhóm hàng</th>
                            <th>Nhóm cha</th>
                            <th>Mô tả</th>
                            <th style="width:100px;text-align:center;">Sản phẩm</th>
                            <th style="width:150px;">Trạng thái</th>
                            <c:if test="${sessionScope.canManageCategory}">
                                <th style="width:90px;text-align:center;">Thao tác</th>
                            </c:if>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty categories}">
                                <tr>
                                    <td colspan="7">
                                        <div class="cat-empty">
                                            <div class="cat-empty-icon">
                                                <span class="material-icons">folder_off</span>
                                            </div>
                                            <h5>Không tìm thấy nhóm hàng</h5>
                                            <p>Thử thay đổi bộ lọc hoặc thêm nhóm hàng mới</p>
                                            <c:if test="${sessionScope.canManageCategory}">
                                                <button class="btn cat-btn-primary btn-sm" data-bs-toggle="modal" data-bs-target="#addCategoryModal">
                                                    <span class="material-icons">add</span>
                                                    Thêm nhóm hàng
                                                </button>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="category" items="${categories}" varStatus="loop">
                                    <tr class="cat-row" style="animation-delay: ${loop.index * 0.04}s;">
                                        <td>
                                            <span class="cat-id">${category.categoryId}</span>
                                        </td>
                                        <td>
                                            <div class="cat-name-cell">
                                                <a class="cat-name-icon ${empty category.parentName ? 'is-root' : 'is-child'}"
                                                   href="${pageContext.request.contextPath}/admin/products?categoryId=${category.categoryId}"
                                                   title="Xem hàng hóa trong danh mục này">
                                                    <span class="material-icons">
                                                        ${empty category.parentName ? 'widgets' : 'segment'}
                                                    </span>
                                                </a>
                                                <a class="cat-name-text cat-name-link"
                                                   href="${pageContext.request.contextPath}/admin/products?categoryId=${category.categoryId}"
                                                   title="Xem hàng hóa trong danh mục này">
                                                    <c:out value="${category.name}"/>
                                                </a>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty category.parentName}">
                                                    <span class="cat-badge cat-badge-root">
                                                        <span class="material-icons">hub</span>
                                                        Nhóm gốc
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="cat-badge cat-badge-child">
                                                        <span class="material-icons">turn_right</span>
                                                        <c:out value="${category.parentName}"/>
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty category.description}">
                                                    <span class="cat-no-desc">— Chưa có mô tả</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="cat-desc"><c:out value="${category.description}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align:center;">
                                            <span class="cat-product-pill ${category.productCount > 0 ? 'has-items' : ''}">
                                                ${category.productCount}
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${category.status == 'active'}">
                                                    <span class="cat-status cat-status-active">
                                                        <span class="material-icons cat-status-icon">check_circle</span>
                                                        Đang sử dụng
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="cat-status cat-status-inactive">
                                                        <span class="material-icons cat-status-icon">do_not_disturb_on</span>
                                                        Ngừng sử dụng
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <c:if test="${sessionScope.canManageCategory}">
                                            <td style="text-align:center;">
                                                <button type="button"
                                                        class="cat-action-btn"
                                                        title="Chỉnh sửa"
                                                        data-bs-toggle="modal"
                                                        data-bs-target="#editCategoryModal"
                                                        data-category-id="${category.categoryId}"
                                                        data-category-name="${fn:escapeXml(category.name)}"
                                                        data-category-description="${fn:escapeXml(category.description)}"
                                                        data-category-parent-name="${fn:escapeXml(category.parentName)}"
                                                        data-category-status="${category.status}"
                                                        onclick="prepareEditCategory(this)">
                                                    <span class="material-icons">edit</span>
                                                </button>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- ===== Pagination ===== -->
        <c:if test="${totalPages > 1}">
            <div class="cat-pagination">
                <div class="cat-pagination-info">
                    Hiển thị <strong>${fn:length(categories)}</strong> / <strong>${totalItems}</strong> nhóm hàng
                </div>
                <nav>
                    <ul class="pagination mb-0">
                        <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/admin/categories?page=${currentPage - 1}&keyword=${keyword}&status=${selectedStatus}&parentName=${parentNameFilter}">
                                <span class="material-icons" style="font-size:18px;">chevron_left</span>
                            </a>
                        </li>
                        <c:forEach var="pageIndex" begin="1" end="${totalPages}">
                            <li class="page-item ${currentPage == pageIndex ? 'active' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/admin/categories?page=${pageIndex}&keyword=${keyword}&status=${selectedStatus}&parentName=${parentNameFilter}">
                                    ${pageIndex}
                                </a>
                            </li>
                        </c:forEach>
                        <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/admin/categories?page=${currentPage + 1}&keyword=${keyword}&status=${selectedStatus}&parentName=${parentNameFilter}">
                                <span class="material-icons" style="font-size:18px;">chevron_right</span>
                            </a>
                        </li>
                    </ul>
                </nav>
            </div>
        </c:if>

    </div>
</div>

<!-- ===== Add Category Modal ===== -->
<div class="modal fade" id="addCategoryModal" tabindex="-1">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content cat-modal">
            <form method="post" action="${pageContext.request.contextPath}/admin/categories">
                <input type="hidden" name="action" value="add">
                <div class="modal-header cat-modal-header">
                    <div class="d-flex align-items-center gap-3">
                        <div class="cat-modal-icon add">
                            <span class="material-icons">create_new_folder</span>
                        </div>
                        <div>
                            <h5 class="modal-title fw-bold mb-0">Thêm nhóm hàng mới</h5>
                            <small class="cat-modal-subtitle">Tạo danh mục phân loại sản phẩm</small>
                        </div>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body cat-modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label cat-label">Tên nhóm hàng <span class="text-danger">*</span></label>
                            <input type="text" class="form-control cat-input" name="name" maxlength="255" required placeholder="Nhập tên nhóm hàng">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label cat-label">Nhóm cha</label>
                            <select class="form-select cat-input" name="parentName">
                                <option value="">Không có (nhóm gốc)</option>
                                <c:forEach var="parent" items="${parentOptions}">
                                    <option value="${fn:escapeXml(parent.name)}"><c:out value="${parent.name}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label cat-label">Trạng thái</label>
                            <select class="form-select cat-input" name="status">
                                <option value="active" selected>Đang sử dụng</option>
                                <option value="inactive">Ngừng sử dụng</option>
                            </select>
                        </div>
                        <div class="col-12">
                            <label class="form-label cat-label">Mô tả</label>
                            <textarea class="form-control cat-input" name="description" rows="3" maxlength="1000" placeholder="Mô tả ngắn về nhóm hàng..."></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer cat-modal-footer">
                    <button type="button" class="btn cat-btn-cancel" data-bs-dismiss="modal">
                        <span class="material-icons">close</span>
                        Hủy
                    </button>
                    <button type="submit" class="btn cat-btn-primary">
                        <span class="material-icons">save</span>
                        Lưu nhóm hàng
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- ===== Edit Category Modal ===== -->
<div class="modal fade" id="editCategoryModal" tabindex="-1">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content cat-modal">
            <form method="post" action="${pageContext.request.contextPath}/admin/categories">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="categoryId" id="editCategoryId">
                <div class="modal-header cat-modal-header">
                    <div class="d-flex align-items-center gap-3">
                        <div class="cat-modal-icon edit">
                            <span class="material-icons">edit_note</span>
                        </div>
                        <div>
                            <h5 class="modal-title fw-bold mb-0">Cập nhật nhóm hàng</h5>
                            <small class="cat-modal-subtitle">Chỉnh sửa thông tin danh mục</small>
                        </div>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body cat-modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label cat-label">Tên nhóm hàng <span class="text-danger">*</span></label>
                            <input type="text" class="form-control cat-input" name="name" id="editCategoryName" maxlength="255" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label cat-label">Nhóm cha</label>
                            <select class="form-select cat-input" name="parentName" id="editCategoryParentName">
                                <option value="">Không có (nhóm gốc)</option>
                                <c:forEach var="parent" items="${parentOptions}">
                                    <option value="${fn:escapeXml(parent.name)}"><c:out value="${parent.name}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label cat-label">Trạng thái</label>
                            <select class="form-select cat-input" name="status" id="editCategoryStatus">
                                <option value="active">Đang sử dụng</option>
                                <option value="inactive">Ngừng sử dụng</option>
                            </select>
                        </div>
                        <div class="col-12">
                            <label class="form-label cat-label">Mô tả</label>
                            <textarea class="form-control cat-input" name="description" id="editCategoryDescription" rows="3" maxlength="1000"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer cat-modal-footer">
                    <button type="button" class="btn cat-btn-cancel" data-bs-dismiss="modal">
                        <span class="material-icons">close</span>
                        Hủy
                    </button>
                    <button type="submit" class="btn cat-btn-primary">
                        <span class="material-icons">save</span>
                        Cập nhật
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function prepareEditCategory(button) {
    document.getElementById('editCategoryId').value = button.dataset.categoryId || '';
    document.getElementById('editCategoryName').value = button.dataset.categoryName || '';
    document.getElementById('editCategoryDescription').value = button.dataset.categoryDescription || '';
    document.getElementById('editCategoryStatus').value = button.dataset.categoryStatus || 'active';

    // Set parent name select
    var parentSelect = document.getElementById('editCategoryParentName');
    var parentName = button.dataset.categoryParentName || '';
    var found = false;
    for (var i = 0; i < parentSelect.options.length; i++) {
        if (parentSelect.options[i].value === parentName) {
            parentSelect.selectedIndex = i;
            found = true;
            break;
        }
    }
    if (!found) parentSelect.selectedIndex = 0;
}

function openPrintView() {
    var params = new URLSearchParams(window.location.search);
    params.delete('page');
    params.set('printMode', 'true');
    var printUrl = '${pageContext.request.contextPath}/admin/categories?' + params.toString();
    window.open(printUrl, '_blank');
}

<c:if test="${printMode}">
document.body.classList.add('cat-print-mode');
</c:if>
</script>

<style>
/* ============================================================
   CATEGORIES PAGE — PREMIUM UI STYLES
   ============================================================ */

/* ===== Filter layout fixes ===== */
.cat-filter-status-item {
    min-width: 210px !important;
}

.cat-filter-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
    align-self: stretch;
}

.cat-btn-filter {
    min-width: 104px;
}

/* ===== Hero Header ===== */
.cat-hero {
    background: linear-gradient(135deg, #93000b 0%, #b71c1c 40%, #d32f2f 100%);
    border-radius: 16px;
    padding: 32px 36px;
    margin-bottom: 24px;
    position: relative;
    overflow: hidden;
}

.cat-hero::before {
    content: '';
    position: absolute;
    top: -60%;
    right: -10%;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
    border-radius: 50%;
}

.cat-hero::after {
    content: '';
    position: absolute;
    bottom: -40%;
    left: 10%;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, transparent 70%);
    border-radius: 50%;
}

.cat-hero-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    z-index: 1;
}

.cat-hero-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    background: rgba(255,255,255,0.15);
    backdrop-filter: blur(10px);
    color: rgba(255,255,255,0.9);
    padding: 5px 14px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.3px;
    margin-bottom: 12px;
}

.cat-hero-badge .material-icons {
    font-size: 15px;
}

.cat-hero-title {
    color: #fff;
    font-size: 26px;
    font-weight: 800;
    margin: 0 0 6px 0;
    font-family: var(--font-heading);
    letter-spacing: -0.3px;
}

.cat-hero-desc {
    color: rgba(255,255,255,0.75);
    font-size: 14px;
    margin: 0;
}

.cat-hero-actions {
    display: flex;
    gap: 10px;
    flex-shrink: 0;
}

.cat-btn-outline {
    background: rgba(255,255,255,0.12);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255,255,255,0.25);
    color: #fff;
    border-radius: 10px;
    padding: 9px 18px;
    font-size: 13px;
    font-weight: 600;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    transition: all 0.2s ease;
}

.cat-btn-outline:hover {
    background: rgba(255,255,255,0.22);
    color: #fff;
    transform: translateY(-1px);
}

.cat-btn-outline .material-icons { font-size: 18px; }

.cat-btn-primary {
    background: #fff;
    color: var(--primary-color);
    border: none;
    border-radius: 10px;
    padding: 9px 20px;
    font-size: 13px;
    font-weight: 700;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    transition: all 0.2s ease;
    box-shadow: 0 4px 14px rgba(0,0,0,0.15);
}

.cat-btn-primary:hover {
    background: #f5f5f5;
    color: var(--primary-dark);
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(0,0,0,0.2);
}

.cat-btn-primary .material-icons { font-size: 18px; }

/* ===== Toast Alert ===== */
.cat-toast {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 20px;
    border-radius: 12px;
    margin-bottom: 20px;
    animation: catSlideDown 0.4s cubic-bezier(0.16, 1, 0.3, 1);
    border: 1px solid transparent;
}

.cat-toast.alert-success {
    background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
    border-color: #c8e6c9;
    color: #2e7d32;
}

.cat-toast.alert-danger {
    background: linear-gradient(135deg, #ffebee 0%, #fce4ec 100%);
    border-color: #ffcdd2;
    color: #c62828;
}

.cat-toast.alert-warning {
    background: linear-gradient(135deg, #fff3e0 0%, #fff8e1 100%);
    border-color: #ffe0b2;
    color: #e65100;
}

.cat-toast-icon .material-icons { font-size: 22px; }

.cat-toast-text { flex: 1; font-weight: 500; font-size: 14px; }

.cat-toast-close {
    background: none;
    border: none;
    cursor: pointer;
    opacity: 0.5;
    transition: opacity 0.2s;
    padding: 4px;
    border-radius: 6px;
}

.cat-toast-close:hover { opacity: 1; background: rgba(0,0,0,0.05); }
.cat-toast-close .material-icons { font-size: 18px; }

/* ===== Stats Cards ===== */
.cat-stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 20px;
}

.cat-stat-card {
    background: #fff;
    border-radius: 14px;
    padding: 22px 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    position: relative;
    overflow: hidden;
    border: 1px solid rgba(0,0,0,0.04);
    box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.02);
    transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.cat-stat-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.06), 0 8px 24px rgba(0,0,0,0.04);
}

.cat-stat-icon-wrap {
    width: 50px;
    height: 50px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.cat-stat-icon-wrap .material-icons { font-size: 24px; }

.cat-stat-total .cat-stat-icon-wrap {
    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
    color: #2e7d32;
}

.cat-stat-root .cat-stat-icon-wrap {
    background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
    color: #1565c0;
}

.cat-stat-products .cat-stat-icon-wrap {
    background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
    color: #e65100;
}

.cat-stat-number {
    font-size: 28px;
    font-weight: 800;
    font-family: var(--font-heading);
    line-height: 1;
    color: var(--text-primary);
}

.cat-stat-text {
    font-size: 13px;
    color: var(--text-muted);
    margin-top: 4px;
    font-weight: 500;
}

.cat-stat-decoration {
    position: absolute;
    right: -20px;
    bottom: -20px;
    width: 80px;
    height: 80px;
    border-radius: 50%;
    opacity: 0.04;
}

.cat-stat-total .cat-stat-decoration { background: #2e7d32; }
.cat-stat-root .cat-stat-decoration { background: #1565c0; }
.cat-stat-products .cat-stat-decoration { background: #e65100; }

/* ===== Filter Bar ===== */
.cat-filter-bar {
    background: #fff;
    border-radius: 14px;
    padding: 18px 24px;
    margin-bottom: 20px;
    border: 1px solid rgba(0,0,0,0.04);
    box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.cat-filter-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 700;
    font-size: 14px;
    color: var(--text-primary);
    margin-bottom: 14px;
}

.cat-filter-title .material-icons {
    font-size: 20px;
    color: var(--primary-color);
}

.cat-filter-form {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}

.cat-filter-group {
    display: flex;
    gap: 10px;
    flex: 1;
    flex-wrap: wrap;
}

.cat-filter-item {
    position: relative;
    flex: 1;
    min-width: 180px;
}

.cat-filter-icon {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 20px;
    color: #aaa;
    pointer-events: none;
    z-index: 1;
}

.cat-filter-input,
.cat-filter-select {
    width: 100%;
    padding: 10px 14px 10px 42px;
    border: 1.5px solid #e8e8e8;
    border-radius: 10px;
    font-size: 13px;
    font-family: var(--font-primary);
    color: var(--text-primary);
    background: #fafafa;
    transition: all 0.2s ease;
    appearance: auto;
}

.cat-filter-input:focus,
.cat-filter-select:focus {
    outline: none;
    border-color: var(--primary-color);
    background: #fff;
    box-shadow: 0 0 0 3px rgba(147, 0, 11, 0.08);
}

.cat-filter-input::placeholder {
    color: #bbb;
}

.cat-filter-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
}

.cat-filter-actions .cat-btn-filter,
.cat-filter-actions .cat-btn-filter.btn {
    background: linear-gradient(135deg, #9f0712 0%, #c01824 100%) !important;
    color: #ffffff !important;
    border: 0 !important;
    border-radius: 12px;
    padding: 0 18px;
    min-width: 92px;
    height: 42px;
    font-size: 13px;
    font-weight: 800;
    display: inline-flex !important;
    align-items: center;
    justify-content: center;
    gap: 7px;
    box-shadow: 0 8px 18px rgba(159, 7, 18, 0.22);
    overflow: visible !important;
    white-space: nowrap;
    opacity: 1 !important;
}

.cat-filter-actions .cat-btn-filter:hover,
.cat-filter-actions .cat-btn-filter:focus,
.cat-filter-actions .cat-btn-filter:active,
.cat-filter-actions .cat-btn-filter.btn:hover,
.cat-filter-actions .cat-btn-filter.btn:focus,
.cat-filter-actions .cat-btn-filter.btn:active {
    background: linear-gradient(135deg, #7f0008 0%, #a80f19 100%) !important;
    color: #ffffff !important;
    border: 0 !important;
    transform: translateY(-1px);
    box-shadow: 0 10px 22px rgba(159, 7, 18, 0.28);
}

.cat-filter-actions .cat-btn-filter .material-icons,
.cat-filter-actions .cat-btn-filter:hover .material-icons,
.cat-filter-actions .cat-btn-filter:focus .material-icons,
.cat-filter-actions .cat-btn-filter:active .material-icons {
    font-size: 18px;
    color: #ffffff !important;
    opacity: 1 !important;
    visibility: visible !important;
}

.cat-filter-actions .cat-btn-filter .cat-btn-text,
.cat-filter-actions .cat-btn-filter:hover .cat-btn-text,
.cat-filter-actions .cat-btn-filter:focus .cat-btn-text,
.cat-filter-actions .cat-btn-filter:active .cat-btn-text {
    color: #ffffff !important;
    display: inline-block !important;
    line-height: 1 !important;
    opacity: 1 !important;
    visibility: visible !important;
    text-indent: 0 !important;
    font-size: 13px !important;
}

.cat-btn-reset {
    background: #f0f0f0;
    color: var(--text-secondary);
    border: none;
    border-radius: 10px;
    width: 42px;
    height: 42px;
    padding: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
}

.cat-btn-reset:hover {
    background: #e0e0e0;
    color: var(--text-primary);
    transform: rotate(180deg);
}

.cat-btn-reset .material-icons { font-size: 20px; }

/* ===== Data Table ===== */
.cat-table-wrapper {
    background: #fff;
    border-radius: 14px;
    overflow: hidden;
    margin-bottom: 20px;
    border: 1px solid rgba(0,0,0,0.04);
    box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.02);
}

.cat-table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    border-bottom: 1px solid #f0f0f0;
}

.cat-table-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 700;
    font-size: 15px;
    color: var(--text-primary);
}

.cat-table-title .material-icons {
    font-size: 20px;
    color: var(--primary-color);
}

.cat-table-count {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 13px;
    color: var(--text-muted);
    font-weight: 500;
    background: #f5f5f5;
    padding: 5px 12px;
    border-radius: 20px;
}

.cat-table {
    margin: 0;
}

.cat-table thead th {
    background: #f9fafb;
    font-size: 12px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.6px;
    color: #8a8f98;
    padding: 13px 16px;
    border-bottom: 1px solid #eee;
    white-space: nowrap;
}

.cat-table tbody td {
    padding: 14px 16px;
    vertical-align: middle;
    border-bottom: 1px solid #f5f5f5;
    font-size: 14px;
}

/* Row animation */
@keyframes catSlideDown {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
}

@keyframes catFadeIn {
    from { opacity: 0; transform: translateY(6px); }
    to { opacity: 1; transform: translateY(0); }
}

.cat-row {
    animation: catFadeIn 0.35s ease both;
    transition: background-color 0.15s ease;
}

.cat-row:hover {
    background-color: #fdf5f5 !important;
}

.cat-row:last-child td {
    border-bottom: none;
}

/* ID badge */
.cat-id {
    display: inline-flex;
    align-items: center;
    gap: 1px;
    padding: 4px 10px;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 800;
    background: #eef2ff;
    color: #334155;
    font-family: 'Manrope', monospace;
}



/* Name cell */
.cat-name-cell {
    display: flex;
    align-items: center;
    gap: 10px;
}

.cat-name-icon {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    text-decoration: none;
    border: 1px solid #e2e8f0;
    transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.cat-name-icon.is-root {
    background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
}

.cat-name-icon.is-root .material-icons {
    font-size: 18px;
    color: #475569;
}

.cat-name-icon.is-child {
    background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
}

.cat-name-icon.is-child .material-icons {
    font-size: 18px;
    color: #2563eb;
}

.cat-row:hover .cat-name-icon {
    transform: scale(1.08);
    border-color: #bfdbfe;
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.14);
}

.cat-name-text {
    font-weight: 600;
    color: var(--text-primary);
}

.cat-name-link {
    text-decoration: none;
    text-underline-offset: 4px;
    transition: color 0.2s ease, text-decoration-color 0.2s ease;
}

.cat-name-link:hover {
    color: var(--primary-color);
    text-decoration: underline;
    text-decoration-thickness: 2px;
    text-decoration-color: var(--primary-color);
}

/* Parent badge */
.cat-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 600;
}

.cat-badge .material-icons { font-size: 14px; }

.cat-badge-root {
    background: #e8f5e9;
    color: #2e7d32;
}

.cat-badge-child {
    background: #e3f2fd;
    color: #1565c0;
}

/* Description */
.cat-desc {
    font-size: 13px;
    color: var(--text-secondary);
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    max-width: 220px;
    line-height: 1.5;
}

.cat-no-desc {
    font-size: 13px;
    color: #ccc;
    font-style: italic;
}

/* Product count pill */
.cat-product-pill {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 36px;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 700;
    background: #f5f5f5;
    color: #bbb;
    transition: all 0.2s ease;
}

.cat-product-pill.has-items {
    background: linear-gradient(135deg, #fff3e0, #ffe0b2);
    color: #e65100;
}

/* Status */
.cat-status {
    display: inline-flex;
    align-items: center;
    gap: 7px;
    padding: 7px 13px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 800;
    letter-spacing: 0.1px;
    border: 1px solid transparent;
    box-shadow: inset 0 1px 0 rgba(255,255,255,0.7);
    white-space: nowrap;
}

.cat-status-icon {
    font-size: 16px !important;
    line-height: 1;
}

.cat-status-active {
    background: linear-gradient(135deg, #ecfdf5 0%, #dcfce7 100%);
    color: #047857;
    border-color: #bbf7d0;
}

.cat-status-active .cat-status-icon {
    color: #10b981;
    filter: drop-shadow(0 2px 4px rgba(16, 185, 129, 0.22));
}

.cat-status-inactive {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    color: #64748b;
    border-color: #e2e8f0;
}

.cat-status-inactive .cat-status-icon {
    color: #94a3b8;
}

/* Action button */
.cat-action-btn {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    border: 1.5px solid #e0e0e0;
    background: #fff;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    color: #666;
}

.cat-action-btn .material-icons { font-size: 17px; }

.cat-action-btn:hover {
    border-color: var(--primary-color);
    color: var(--primary-color);
    background: rgba(147, 0, 11, 0.04);
    transform: scale(1.1);
    box-shadow: 0 2px 8px rgba(147, 0, 11, 0.12);
}

/* Empty state */
.cat-empty {
    text-align: center;
    padding: 60px 20px;
}

.cat-empty-icon {
    width: 80px;
    height: 80px;
    border-radius: 20px;
    background: linear-gradient(135deg, #f5f5f5, #eeeeee);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
}

.cat-empty-icon .material-icons {
    font-size: 36px;
    color: #bbb;
}

.cat-empty h5 {
    color: var(--text-secondary);
    font-weight: 700;
    margin-bottom: 6px;
}

.cat-empty p {
    color: var(--text-muted);
    font-size: 14px;
    margin-bottom: 16px;
}

/* ===== Pagination ===== */
.cat-pagination {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 24px;
    background: #fff;
    border-radius: 14px;
    margin-bottom: 24px;
    border: 1px solid rgba(0,0,0,0.04);
    box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.cat-pagination-info {
    font-size: 13px;
    color: var(--text-muted);
    font-weight: 500;
}

/* ===== Modal ===== */
.cat-modal {
    border: none;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}

.cat-modal-header {
    background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
    padding: 24px 28px;
    border-bottom: 1px solid #eee;
}

.cat-modal-icon {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.cat-modal-icon.add {
    background: linear-gradient(135deg, rgba(147, 0, 11, 0.08), rgba(147, 0, 11, 0.14));
}

.cat-modal-icon.add .material-icons {
    color: var(--primary-color);
    font-size: 24px;
}

.cat-modal-icon.edit {
    background: linear-gradient(135deg, rgba(2, 136, 209, 0.08), rgba(2, 136, 209, 0.14));
}

.cat-modal-icon.edit .material-icons {
    color: #0288d1;
    font-size: 24px;
}

.cat-modal-subtitle {
    color: var(--text-muted);
    font-size: 13px;
}

.cat-modal-body {
    padding: 28px;
}

.cat-modal-footer {
    padding: 16px 28px;
    background: #fafafa;
    border-top: 1px solid #eee;
}

.cat-label {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 6px;
}

.cat-input {
    border: 1.5px solid #e0e0e0;
    border-radius: 10px;
    padding: 10px 14px;
    font-size: 14px;
    transition: all 0.2s ease;
}

.cat-input:focus {
    border-color: var(--primary-color);
    box-shadow: 0 0 0 3px rgba(147, 0, 11, 0.08);
}

.cat-btn-cancel {
    background: #f0f0f0;
    color: var(--text-secondary);
    border: none;
    border-radius: 10px;
    padding: 9px 18px;
    font-size: 13px;
    font-weight: 600;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    transition: all 0.2s ease;
}

.cat-btn-cancel:hover {
    background: #e0e0e0;
    color: var(--text-primary);
}

.cat-btn-cancel .material-icons { font-size: 18px; }

/* ===== Print mode screen preview ===== */
body.cat-print-mode .cat-hero,
body.cat-print-mode .cat-toast,
body.cat-print-mode .cat-stats-grid,
body.cat-print-mode .cat-filter-bar,
body.cat-print-mode .cat-pagination,
body.cat-print-mode .sidebar,
body.cat-print-mode .navbar,
body.cat-print-mode .topbar,
body.cat-print-mode header,
body.cat-print-mode aside,
body.cat-print-mode .cat-table th:last-child,
body.cat-print-mode .cat-table td:last-child,
body.cat-print-mode .cat-action-btn {
    display: none !important;
}

body.cat-print-mode .d-flex {
    display: block !important;
}

body.cat-print-mode .main-content {
    width: 100% !important;
    max-width: none !important;
    margin: 0 !important;
    padding: 24px !important;
    background: #f8fafc;
}

.cat-print-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
    padding: 18px 20px;
    margin-bottom: 16px;
    background: #ffffff;
    border: 1px solid #e5e7eb;
    border-left: 5px solid var(--primary-color);
    border-radius: 14px;
    box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.cat-print-info {
    display: flex;
    align-items: center;
    gap: 14px;
    min-width: 0;
}

.cat-print-icon {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    background: #fff1f2;
    color: var(--primary-color);
}

.cat-print-icon .material-icons {
    font-size: 21px;
}

.cat-print-toolbar strong {
    display: block;
    color: #111827;
    font-family: var(--font-heading);
    font-size: 17px;
    font-weight: 800;
    letter-spacing: -0.1px;
    margin-bottom: 3px;
}

.cat-print-toolbar span {
    color: #64748b;
    font-size: 13px;
    font-weight: 500;
}

.cat-print-actions {
    display: flex;
    gap: 10px;
    align-items: center;
    flex-shrink: 0;
}

.cat-print-action-primary {
    height: 40px !important;
    min-width: 132px !important;
    padding: 0 18px !important;
    border: 0 !important;
    border-radius: 10px !important;
    display: inline-flex !important;
    align-items: center !important;
    justify-content: center !important;
    gap: 8px !important;
    background: #93000b !important;
    color: #ffffff !important;
    font-size: 13px !important;
    font-weight: 800 !important;
    line-height: 1 !important;
    box-shadow: none !important;
    cursor: pointer !important;
    opacity: 1 !important;
    visibility: visible !important;
    text-decoration: none !important;
    appearance: none !important;
    -webkit-appearance: none !important;
}

.cat-print-action-primary:hover,
.cat-print-action-primary:focus {
    background: #760008 !important;
    color: #ffffff !important;
    transform: translateY(-1px);
}

.cat-print-action-primary .material-icons,
.cat-print-action-primary .cat-print-action-text,
.cat-print-action-primary:hover .material-icons,
.cat-print-action-primary:hover .cat-print-action-text,
.cat-print-action-primary:focus .material-icons,
.cat-print-action-primary:focus .cat-print-action-text {
    color: #ffffff !important;
    -webkit-text-fill-color: #ffffff !important;
    opacity: 1 !important;
    visibility: visible !important;
    display: inline-flex !important;
    line-height: 1 !important;
    text-indent: 0 !important;
    font-size: 13px !important;
}

.cat-print-action-primary .material-icons {
    font-size: 18px !important;
}

.cat-print-close {
    width: 40px;
    height: 40px;
    border: 0;
    border-radius: 10px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    background: #f1f5f9;
    color: #334155;
    transition: all 0.2s ease;
}

.cat-print-close:hover,
.cat-print-close:focus {
    background: #e2e8f0;
    color: #111827;
    transform: translateY(-1px);
}

.cat-print-close .material-icons {
    font-size: 21px;
}

body.cat-print-mode .cat-table-wrapper {
    box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
}

body.cat-print-mode .cat-name-icon,
body.cat-print-mode .cat-name-link {
    pointer-events: none !important;
    cursor: default !important;
}

body.cat-print-mode .cat-name-icon {
    text-decoration: none !important;
}

body.cat-print-mode .cat-name-link {
    color: var(--text-primary) !important;
    text-decoration: none !important;
}

body.cat-print-mode .cat-row:hover {
    background-color: transparent !important;
}

body.cat-print-mode .cat-row:hover .cat-name-icon {
    transform: none !important;
    box-shadow: none !important;
}

/* ===== Print: only category list, all pages via printMode ===== */
@media print {
    body * {
        visibility: hidden !important;
    }

    .cat-table-wrapper,
    .cat-table-wrapper * {
        visibility: visible !important;
    }

    .cat-table-wrapper {
        position: absolute !important;
        left: 0 !important;
        top: 0 !important;
        width: 100% !important;
        margin: 0 !important;
        border: none !important;
        box-shadow: none !important;
        border-radius: 0 !important;
        overflow: visible !important;
    }

    .cat-table-header {
        padding: 0 0 12px 0 !important;
        border-bottom: 2px solid #111 !important;
    }

    .cat-table-count,
    .cat-action-btn,
    .cat-table th:last-child,
    .cat-table td:last-child {
        display: none !important;
    }

    .cat-table {
        width: 100% !important;
        border-collapse: collapse !important;
    }

    .cat-table th,
    .cat-table td {
        border: 1px solid #ddd !important;
        padding: 8px !important;
        color: #111 !important;
        background: #fff !important;
    }

    .cat-name-icon,
    .cat-badge,
    .cat-status,
    .cat-product-pill,
    .cat-id {
        print-color-adjust: exact;
        -webkit-print-color-adjust: exact;
    }
}
@media (max-width: 992px) {
    .cat-stats-grid {
        grid-template-columns: repeat(3, 1fr);
        gap: 12px;
    }
    .cat-filter-group {
        flex-direction: column;
    }
}

@media (max-width: 768px) {
    .cat-hero-content {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
    }
    .cat-hero { padding: 24px; }
    .cat-stats-grid {
        grid-template-columns: 1fr;
    }
    .cat-stat-card { padding: 16px 20px; }
    .cat-stat-number { font-size: 24px; }
    .cat-pagination {
        flex-direction: column;
        gap: 12px;
    }
    .cat-desc { max-width: 140px; }
    .cat-filter-form { flex-direction: column; }
    .cat-filter-actions { width: 100%; justify-content: flex-end; }
}
</style>

<jsp:include page="../common/footer.jsp"/>
