<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Quản lý nhóm hàng"/>
    <jsp:param name="additionalCSS" value="category.css"/>
</jsp:include>

<div class="d-flex">
    <jsp:include page="/views/common/sidebar.jsp">
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
                    
                    <c:if test="${sessionScope.canManageCategory}">
                        <button class="btn cat-btn-primary" data-bs-toggle="modal" data-bs-target="#categoryModal" onclick="prepareAddCategory()">
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

        

        <!-- ===== Filter Bar ===== -->
        <div class="cat-filter-bar">
            <div class="cat-filter-title">
                <span class="material-icons">filter_list</span>
                <span>Bộ lọc</span>
            </div>
            <form method="get" action="${pageContext.request.contextPath}/category" class="cat-filter-form">
                <div class="cat-filter-group">
                    <div class="cat-filter-item cat-filter-search">
                        <span class="material-icons cat-filter-icon">search</span>
                        <input type="text" class="cat-filter-input" name="keyword" placeholder="Tìm kiếm nhóm hàng, nhóm cha..." value="${keyword}">
                    </div>
                    <div class="cat-filter-item cat-filter-status-item">
                        <span class="material-icons cat-filter-icon">toggle_on</span>
                        <select class="cat-filter-select" name="status">
                            <option value="" ${empty selectedStatus ? 'selected' : ''}>Tất cả trạng thái</option>
                            <option value="active" ${selectedStatus == 'active' ? 'selected' : ''}>Đang sử dụng</option>
                            <option value="inactive" ${selectedStatus == 'inactive' ? 'selected' : ''}>Ngừng sử dụng</option>
                        </select>
                    </div>
                </div>
                <div class="cat-filter-actions">
                    <button type="submit" class="btn cat-btn-filter">
                        <span class="material-icons">filter_alt</span>
                        <span class="cat-btn-text">Lọc</span>
                    </button>
                    <a href="${pageContext.request.contextPath}/category" class="btn cat-btn-reset" title="Xóa bộ lọc">
                        <span class="material-icons">refresh</span>
                    </a>
                </div>
            </form>
        </div>

        <!-- =====  ===== -->
        <div id="teacherCheckBlock">
            
            <form method="get" action="${pageContext.request.contextPath}/category" >
                <label style="font-weight: 600; font-size: 14px;">Nhập số lượng row cần show :</label>
                <input type="text" name="limit" value="${currentLimit}"  />
                <button type="submit" >Áp dụng số lượng</button>
            </form>

            <form method="get" action="${pageContext.request.contextPath}/category" >
                <button type="submit" name="percentAction" value="first" >30% Đầu</button>
                <button type="submit" name="percentAction" value="middle">30% Giữa</button>
                <button type="submit" name="percentAction" value="last">30% Cuối</button>
            </form>
        </div>
        <!-- =====  ===== -->

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
                                                <button class="btn cat-btn-primary btn-sm" data-bs-toggle="modal" data-bs-target="#categoryModal" onclick="prepareAddCategory()">
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
                                            <span class="cat-id">${category.id}</span>
                                        </td>
                                        <td>
                                            <div class="cat-name-cell">
                                                <a class="cat-name-icon ${empty category.parentName ? 'is-root' : 'is-child'}"
                                                   href="${pageContext.request.contextPath}/products?categoryId=${category.id}"
                                                   title="Xem hàng hóa trong danh mục này">
                                                    <span class="material-icons">
                                                        ${empty category.parentName ? 'widgets' : 'segment'}
                                                    </span>
                                                </a>
                                                <a class="cat-name-text cat-name-link"
                                                   href="${pageContext.request.contextPath}/products?categoryId=${category.id}"
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
                                                        data-bs-target="#categoryModal"
                                                        data-category-id="${category.id}"
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
                            <a class="page-link" href="${pageContext.request.contextPath}/category?page=${currentPage - 1}&keyword=${keyword}&status=${selectedStatus}&parentName=${parentNameFilter}">
                                <span class="material-icons" style="font-size:18px;">chevron_left</span>
                            </a>
                        </li>
                        <c:forEach var="pageIndex" begin="1" end="${totalPages}">
                            <li class="page-item ${currentPage == pageIndex ? 'active' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/category?page=${pageIndex}&keyword=${keyword}&status=${selectedStatus}&parentName=${parentNameFilter}">
                                    ${pageIndex}
                                </a>
                            </li>
                        </c:forEach>
                        <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/category?page=${currentPage + 1}&keyword=${keyword}&status=${selectedStatus}&parentName=${parentNameFilter}">
                                <span class="material-icons" style="font-size:18px;">chevron_right</span>
                            </a>
                        </li>
                    </ul>
                </nav>
            </div>
        </c:if>

    </div>
</div>

<!-- ===== Category Modal (Add/Edit) ===== -->
<div class="modal fade" id="categoryModal" tabindex="-1">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content cat-modal">
            <form method="post" action="${pageContext.request.contextPath}/category">
                <input type="hidden" name="action" id="categoryFormAction" value="add">
                <input type="hidden" name="categoryId" id="categoryId">
                <div class="modal-header cat-modal-header">
                    <div class="d-flex align-items-center gap-3">
                        <div class="cat-modal-icon add" id="categoryModalIconWrap">
                            <span class="material-icons" id="categoryModalIcon">create_new_folder</span>
                        </div>
                        <div>
                            <h5 class="modal-title fw-bold mb-0" id="categoryModalTitle">Thêm nhóm hàng mới</h5>
                            <small class="cat-modal-subtitle" id="categoryModalSubtitle">Tạo danh mục phân loại sản phẩm</small>
                        </div>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body cat-modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label cat-label">Tên nhóm hàng <span class="text-danger">*</span></label>
                            <input type="text" class="form-control cat-input" name="name" id="categoryName" maxlength="255" required placeholder="Nhập tên nhóm hàng">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label cat-label">Nhóm cha</label>
                            <select class="form-select cat-input" name="parentName" id="categoryParentName">
                                <option value="">Không có (nhóm gốc)</option>
                                <c:forEach var="parent" items="${parentOptions}">
                                    <option value="${fn:escapeXml(parent.name)}"><c:out value="${parent.name}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label cat-label">Trạng thái</label>
                            <select class="form-select cat-input" name="status" id="categoryStatus">
                                <option value="active" selected>Đang sử dụng</option>
                                <option value="inactive">Ngừng sử dụng</option>
                            </select>
                        </div>
                        <div class="col-12">
                            <label class="form-label cat-label">Mô tả</label>
                            <textarea class="form-control cat-input" name="description" id="categoryDescription" rows="3" maxlength="1000" placeholder="Mô tả ngắn về nhóm hàng..."></textarea>
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
                        <span id="categorySubmitText">Lưu nhóm hàng</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function prepareAddCategory() {
    document.getElementById('categoryFormAction').value = 'add';
    document.getElementById('categoryId').value = '';
    document.getElementById('categoryName').value = '';
    document.getElementById('categoryParentName').selectedIndex = 0;
    document.getElementById('categoryStatus').value = 'active';
    document.getElementById('categoryDescription').value = '';
    
    // Update UI for Add
    document.getElementById('categoryModalTitle').innerText = 'Thêm nhóm hàng mới';
    document.getElementById('categoryModalSubtitle').innerText = 'Tạo danh mục phân loại sản phẩm';
    document.getElementById('categoryModalIcon').innerText = 'create_new_folder';
    document.getElementById('categoryModalIconWrap').className = 'cat-modal-icon add';
    document.getElementById('categorySubmitText').innerText = 'Lưu nhóm hàng';
}

function prepareEditCategory(button) {
    document.getElementById('categoryFormAction').value = 'update';
    document.getElementById('categoryId').value = button.dataset.categoryId || '';
    document.getElementById('categoryName').value = button.dataset.categoryName || '';
    document.getElementById('categoryDescription').value = button.dataset.categoryDescription || '';
    document.getElementById('categoryStatus').value = button.dataset.categoryStatus || 'active';

    // Set parent name select
    var parentSelect = document.getElementById('categoryParentName');
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
    
    // Update UI for Edit
    document.getElementById('categoryModalTitle').innerText = 'Cập nhật nhóm hàng';
    document.getElementById('categoryModalSubtitle').innerText = 'Chỉnh sửa thông tin danh mục';
    document.getElementById('categoryModalIcon').innerText = 'edit_note';
    document.getElementById('categoryModalIconWrap').className = 'cat-modal-icon edit';
    document.getElementById('categorySubmitText').innerText = 'Cập nhật';
}
</script>



<jsp:include page="/views/common/footer.jsp"/>
