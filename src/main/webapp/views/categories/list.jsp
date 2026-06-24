<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Quản lý danh mục"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <main class="main-content">
        <div class="container-fluid py-4">

            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold">Quản lý Danh mục</h2>
                    <small class="text-muted">Tổ chức và quản lý danh mục sản phẩm trong hệ thống</small>
                </div>
                <div>
                    <c:if test="${sessionScope.canManageCategory}">
                        <button class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#categoryModal" onclick="prepareAddCategory()">
                            + Thêm danh mục
                        </button>
                    </c:if>
                </div>
            </div>

            <!-- ===== Alert Messages ===== -->
            <c:if test="${not empty sessionScope.message}">
                <div class="alert alert-${sessionScope.messageType} alert-dismissible fade show" role="alert">
                    ${sessionScope.message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <c:remove var="message" scope="session"/>
                <c:remove var="messageType" scope="session"/>
            </c:if>


            <!-- Search & Filter -->
            <div class="card shadow-sm border-0 mb-4">
                <div class="card-body">
                    <form method="get" action="${pageContext.request.contextPath}/category">
                        <div class="d-flex gap-2">
                            <div class="flex-grow-1">
                                <input type="text" class="form-control" name="keyword" placeholder="Tìm kiếm tên danh mục..." value="${keyword}">
                            </div>
                            <div style="width: 200px;">
                                <select class="form-select" name="status">
                                    <option value="" ${empty selectedStatus ? 'selected' : ''}>Tất cả trạng thái</option>
                                    <option value="active" ${selectedStatus == 'active' ? 'selected' : ''}>Đang sử dụng</option>
                                    <option value="inactive" ${selectedStatus == 'inactive' ? 'selected' : ''}>Ngừng sử dụng</option>
                                </select>
                            </div>
                            <div style="width: 120px;">
                                <button type="submit" class="btn btn-danger w-100">Tìm kiếm</button>
                            </div>
                            <div style="width: 60px;">
                                <a href="${pageContext.request.contextPath}/category" class="btn btn-outline-secondary w-100" title="Xóa bộ lọc">
                                    <span class="material-icons" style="font-size: 20px; line-height: 1.2;">refresh</span>
                                </a>
                            </div>
                        </div>
                        
<!--                         Extra teacher checks - hidden usually but ported for functionality 
                        <div class="row mt-3 border-top pt-3">
                            <div class="col-md-4 d-flex align-items-center gap-2">
                                <label class="fw-bold mb-0 text-nowrap">Hiển thị:</label>
                                <input type="number" class="form-control form-control-sm" name="limit" value="${currentLimit}" style="width: 80px;" />
                                <button type="submit" class="btn btn-sm btn-outline-primary text-nowrap">Áp dụng</button>
                            </div>
                            <div class="col-md-8 d-flex justify-content-end gap-2">
                                <button type="submit" class="btn btn-sm btn-outline-info" name="percentAction" value="first">30% Đầu</button>
                                <button type="submit" class="btn btn-sm btn-outline-info" name="percentAction" value="middle">30% Giữa</button>
                                <button type="submit" class="btn btn-sm btn-outline-info" name="percentAction" value="last">30% Cuối</button>
                            </div>
                        </div>-->
                    </form>
                </div>
            </div>

            <!-- Table -->
            <div class="card shadow-sm border-0">
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table align-middle table-hover mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th class="ps-4">Mã</th>
                                    <th>Tên danh mục</th>
                                    <th>Mô tả</th>
                                    <th class="text-center">Sản phẩm</th>
                                    <th>Trạng thái</th>
                                    <c:if test="${sessionScope.canManageCategory}">
                                        <th class="text-center pe-4">Thao tác</th>
                                    </c:if>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty categories}">
                                        <tr>
                                            <td colspan="7" class="text-center text-muted py-5">
                                                <span class="material-icons d-block mb-2" style="font-size: 48px; opacity: 0.5;">folder_off</span>
                                                Không tìm thấy danh mục nào.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="category" items="${categories}">
                                            <tr style="cursor: pointer;" onclick="window.location.href='${pageContext.request.contextPath}/products?categoryID=${category.id}'">
                                                <td class="ps-4 text-muted">#${category.id}</td>
                                                <td>
                                                    <span class="fw-bold text-dark">
                                                        <c:out value="${category.name}"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${empty category.description}">
                                                            <span class="text-muted fst-italic">— Chưa có mô tả</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-truncate d-inline-block" style="max-width: 200px;"><c:out value="${category.description}"/></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-center">
                                                    <span class="badge ${category.productCount > 0 ? 'bg-warning text-dark' : 'bg-secondary'} rounded-pill px-3">
                                                        ${category.productCount}
                                                    </span>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${category.status == 'active'}">
                                                            <span class="badge bg-success">Đang sử dụng</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-secondary">Ngừng sử dụng</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <c:if test="${sessionScope.canManageCategory}">
                                                    <td class="text-center pe-4 text-nowrap" onclick="event.stopPropagation()">
                                                        <button type="button" class="btn btn-sm btn-warning"
                                                                data-bs-toggle="modal"
                                                                data-bs-target="#categoryModal"
                                                                data-category-id="${category.id}"
                                                                data-category-name="${fn:escapeXml(category.name)}"
                                                                data-category-description="${fn:escapeXml(category.description)}"
                                                                data-category-status="${category.status}"
                                                                onclick="prepareEditCategory(this)">
                                                            Sửa
                                                        </button>
                                                        <a href="${pageContext.request.contextPath}/category?action=delete&id=${category.id}&page=${currentPage}" class="btn btn-sm btn-danger ms-1" onclick="return confirm('Bạn có chắc chắn muốn xóa danh mục này?')">Xóa</a>
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
            </div>

            <!-- ===== Pagination ===== -->
            <c:if test="${totalPages > 1}">
                <div class="d-flex justify-content-between align-items-center mt-4">
                    <div class="text-muted small">
                        Hiển thị <strong>${fn:length(categories)}</strong> / <strong>${totalItems}</strong> danh mục
                    </div>
                    <ul class="pagination mb-0">
                        <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/category?page=${currentPage - 1}&keyword=${keyword}&status=${selectedStatus}&parentName=${parentNameFilter}">
                                Trước
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
                                Tiếp
                            </a>
                        </li>
                    </ul>
                </div>
            </c:if>

        </div>
    </main>
</div>

<!-- ===== Category Modal (Add/Edit) ===== -->
<div class="modal fade" id="categoryModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form method="post" action="${pageContext.request.contextPath}/category">
                <input type="hidden" name="action" id="categoryFormAction" value="add">
                <input type="hidden" name="categoryId" id="categoryId">
                
                <div class="modal-header">
                    <h5 class="modal-title fw-bold" id="categoryModalTitle">Thêm danh mục mới</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Tên danh mục <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" name="name" id="categoryName" maxlength="255" required placeholder="VD: Đồ điện tử">
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Trạng thái</label>
                        <select class="form-select" name="status" id="categoryStatus">
                            <option value="active" selected>Đang sử dụng</option>
                            <option value="inactive">Ngừng sử dụng</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Mô tả</label>
                        <textarea class="form-control" name="description" id="categoryDescription" rows="3" maxlength="1000" placeholder="Mô tả ngắn về danh mục..."></textarea>
                    </div>
                </div>
                
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger" id="categorySubmitText">Lưu danh mục</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
function prepareAddCategory() {
    document.getElementById('categoryFormAction').value = 'add';
    document.getElementById('categoryId').value = '';
    document.getElementById('categoryName').value = '';
    document.getElementById('categoryStatus').value = 'active';
    document.getElementById('categoryDescription').value = '';
    
    document.getElementById('categoryModalTitle').innerText = 'Thêm danh mục mới';
    document.getElementById('categorySubmitText').innerText = 'Lưu danh mục';
}

function prepareEditCategory(button) {
    document.getElementById('categoryFormAction').value = 'update';
    document.getElementById('categoryId').value = button.dataset.categoryId || '';
    document.getElementById('categoryName').value = button.dataset.categoryName || '';
    document.getElementById('categoryDescription').value = button.dataset.categoryDescription || '';
    document.getElementById('categoryStatus').value = button.dataset.categoryStatus || 'active';
    
    document.getElementById('categoryModalTitle').innerText = 'Cập nhật danh mục';
    document.getElementById('categorySubmitText').innerText = 'Cập nhật';
}
</script>

<jsp:include page="/views/common/footer.jsp"/>
