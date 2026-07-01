<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Danh sách nhà cung cấp"/>
</jsp:include>
<div class="app-container">

    <jsp:include page="../common/sidebar.jsp"/>

    <main class="main-content">

        <div class="container-fluid py-4">

            <!-- Alert Messages -->
            <c:if test="${not empty sessionScope.message and empty sessionScope.modalAction}">
                <div class="alert alert-${sessionScope.messageType} alert-dismissible fade show" role="alert">
                    ${sessionScope.message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <c:remove var="message" scope="session"/>
                <c:remove var="messageType" scope="session"/>
            </c:if>

            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold">Danh sách Nhà cung cấp</h2>
                    <small class="text-muted">
                        Quản lý toàn bộ nhà cung cấp trong hệ thống
                    </small>
                </div>

                <div>
                    <a href="#" class="btn btn-outline-danger me-2">
                        <i class="fa fa-download"></i> Xuất file
                    </a>

                    <button type="button" class="btn btn-danger" onclick="openSupplierModal('create')">+ Thêm nhà cung cấp</button>
                </div>
            </div>

            <!-- Statistic Cards -->

            <div class="row mb-4">

                <div class="col-md-4">
                    <div class="card shadow-sm border-0">
                        <div class="card-body">
                            <small class="text-muted">
                                TỔNG NHÀ CUNG CẤP
                            </small>

                            <h2 class="fw-bold text-danger">${totalSupplier}</h2>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="card shadow-sm border-0">
                        <div class="card-body">

                            <small class="text-muted">
                                ĐANG HOẠT ĐỘNG
                            </small>

                            <h2 class="fw-bold text-success"> ${activeCount} </h2>

                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="card shadow-sm border-0">
                        <div class="card-body">

                            <small class="text-muted">
                                NGƯNG HOẠT ĐỘNG
                            </small>

                            <h2 class="fw-bold text-secondary">${inactiveCount} </h2>
                        </div>
                    </div>
                </div>

            </div>

            <!-- Search -->

            <div class="card shadow-sm border-0">

                <div class="card-body">

                    <form method="get" action="suppliers">

                        <div class="row">

                            <div class="col-md-6">

                                <input type="text" name="keyword" value="${keyword}" class="form-control" placeholder="Nhập NCC muốn tìm...">
                            </div>

                            <div class="col-md-2">

                                <select name="status" class="form-select" onchange="this.form.submit()">

                                    <option value="" ${empty status ? 'selected' : ''}>
                                        All Status
                                    </option>

                                    <option value="active" ${status eq 'active' ? 'selected' : ''}>
                                        Active
                                    </option>

                                    <option value="inactive" ${status eq 'inactive' ? 'selected' : ''}>
                                        Inactive
                                    </option>

                                </select>

                            </div>

                            <div class="col-md-2">

                                <select name="pageSize" class="form-select" onchange="this.form.submit()">

                                    <option value="5" ${pageSizeOption == '5' ? 'selected' : ''}>
                                        5 bản ghi/trang
                                    </option>

                                    <option value="10" ${empty pageSizeOption || pageSizeOption == '10' ? 'selected' : ''}>
                                        10 bản ghi/trang
                                    </option>

                                    <option value="30p" ${pageSizeOption == '30p' ? 'selected' : ''}>
                                        30% số bản ghi
                                    </option>

                                    <option value="50p" ${pageSizeOption == '50p' ? 'selected' : ''}>
                                        50% số bản ghi
                                    </option>

                                </select>

                            </div>

                            <div class="col-md-2">

                                <button class="btn btn-danger w-100"> Tìm kiếm </button>

                            </div>

                        </div>

                    </form>

                    <hr>

                    <table class="table align-middle">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Tên Nhà cung cấp</th>
                                <th>Điện thoại</th>
                                <th>Địa chỉ</th>
                                <th>Trạng thái</th>
                                <th width="280">Thao tác</th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:forEach items="${list}" var="s">
                                <tr>
                                    <td>
                                        ${s.supplierID}
                                    </td>
                                    <td>
                                        <strong>${s.name}</strong>
                                    </td>
                                    <td>
                                        ${s.phone}
                                    </td>
                                    <td>
                                        ${s.address}
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${s.status eq 'active'}">
                                                <span class="badge bg-success">Hoạt động</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">Ngưng hoạt động</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <button type="button" 
                                                class="btn btn-sm btn-warning btn-edit-supplier" 
                                                data-id="${s.supplierID}"
                                                data-name="<c:out value='${s.name}'/>"
                                                data-phone="<c:out value='${s.phone}'/>"
                                                data-address="<c:out value='${s.address}'/>"
                                                data-status="<c:out value='${s.status}'/>">
                                            Sửa
                                        </button>

                                        <a href="suppliers?action=manage-products&id=${s.supplierID}" class="btn btn-sm btn-outline-primary">
                                            Sản phẩm
                                        </a>

                                        <a href="suppliers?action=delete&id=${s.supplierID}&page=${page}&keyword=${keyword}" class="btn btn-sm btn-danger" onclick="return confirm('Xóa nhà cung cấp này?')"> Xóa</a>
                                    </td>
                                </tr>
                            </c:forEach>

                        </tbody>

                    </table>

                    <!-- Pagination -->

                    <div class="d-flex justify-content-between align-items-center mt-4">
                        <div class="text-muted small">
                            Trang <strong>${page}</strong> / <strong>${totalPage}</strong>
                        </div>
                        <jsp:include page="../common/pagination.jsp">
                            <jsp:param name="currentPage" value="${page}"/>
                            <jsp:param name="totalPages" value="${totalPage}"/>
                            <jsp:param name="url" value="suppliers?keyword=${keyword}&status=${status}&pageSize=${pageSizeOption}&page="/>
                        </jsp:include>
                    </div>

                </div>

            </div>
    </main>
</div>

<!-- Modal Thêm/Sửa nhà cung cấp -->
<div class="modal fade" id="supplierModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header d-flex flex-column align-items-start">
        <div class="d-flex justify-content-between w-100 align-items-center">
            <h5 class="modal-title" id="modal-title">Thêm nhà cung cấp mới</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" onclick="closeSupplierModal()"></button>
        </div>
        <c:if test="${not empty sessionScope.modalAction and sessionScope.messageType eq 'danger'}">
            <div class="alert alert-danger w-100 py-2 px-3 mt-2 mb-0" role="alert" style="font-size: 14px;">
                <i class="fa fa-exclamation-circle me-1"></i> ${sessionScope.message}
            </div>
        </c:if>
      </div>
      <div class="modal-body">
        <form action="suppliers" method="post" id="supplier-form">
            <input type="hidden" name="action" id="modal-action" value="create">
            <input type="hidden" name="id" id="modal-id">

            <div class="mb-3">
                <label class="form-label">Tên nhà cung cấp</label>
                <input type="text" id="modal-name" name="name" class="form-control" required placeholder="VD: Công ty A">
            </div>
            <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input type="text" id="modal-phone" name="phone" class="form-control" required placeholder="VD: 0987654321">
            </div>
            <div class="mb-3">
                <label class="form-label">Địa chỉ</label>
                <textarea id="modal-address" name="address" class="form-control" rows="3" placeholder="Nhập địa chỉ..."></textarea>
            </div>
            <div class="mb-3">
                <label class="form-label">Trạng thái</label>
                <select id="modal-status" name="status" class="form-select" required>
                    <option value="active">Active</option>
                    <option value="inactive">Inactive</option>
                </select>
            </div>
            <div class="d-flex justify-content-end">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal" onclick="closeSupplierModal()">Huỷ</button>
                <button type="submit" class="btn btn-danger" id="modal-submit">Lưu nhà cung cấp</button>
            </div>
        </form>
      </div>
    </div>
  </div>
</div>

<script>
    let bsModal;
    document.addEventListener("DOMContentLoaded", function() {
        if (typeof bootstrap !== 'undefined') {
            bsModal = new bootstrap.Modal(document.getElementById('supplierModal'));

            // Tự động hiển thị lại modal nếu xảy ra lỗi trùng lặp khi thêm mới từ Server
            <c:if test="${not empty sessionScope.modalAction}">
                openSupplierModal(
                    '${sessionScope.modalAction}',
                    '${sessionScope.modalId}',
                    '<c:out value="${sessionScope.modalName}"/>',
                    '<c:out value="${sessionScope.modalPhone}"/>',
                    '<c:out value="${sessionScope.modalAddress}"/>',
                    '${sessionScope.modalStatus}'
                );
                // Xóa dữ liệu tạm trong session ngay sau khi hiển thị
                <c:remove var="modalAction" scope="session"/>
                <c:remove var="modalId" scope="session"/>
                <c:remove var="modalName" scope="session"/>
                <c:remove var="modalPhone" scope="session"/>
                <c:remove var="modalAddress" scope="session"/>
                <c:remove var="modalStatus" scope="session"/>
                <c:remove var="message" scope="session"/>
                <c:remove var="messageType" scope="session"/>
            </c:if>
        } else {
            console.warn("Bootstrap JS is not loaded!");
        }
    });

    function openSupplierModal(action, id, name, phone, address, status) {
        document.getElementById('modal-action').value = action;
        if (action === 'edit') {
            document.getElementById('modal-title').innerText = 'Chỉnh sửa nhà cung cấp';
            document.getElementById('modal-submit').innerText = 'Cập nhật';
            document.getElementById('modal-id').value = id;
            document.getElementById('modal-name').value = name;
            document.getElementById('modal-phone').value = phone;
            document.getElementById('modal-address').value = address;
            
            let normStatus = "active";
            if (status) {
                let s = status.trim().toLowerCase();
                if (s === "inactive") {
                    normStatus = "inactive";
                }
            }
            document.getElementById('modal-status').value = normStatus;
        } else {
            document.getElementById('modal-title').innerText = 'Thêm nhà cung cấp mới';
            document.getElementById('modal-submit').innerText = 'Lưu nhà cung cấp';
            document.getElementById('modal-id').value = '';
            
            if (name || phone || address || status) {
                document.getElementById('modal-name').value = name || '';
                document.getElementById('modal-phone').value = phone || '';
                document.getElementById('modal-address').value = address || '';
                
                let normStatus = "active";
                if (status) {
                    let s = status.trim().toLowerCase();
                    if (s === "inactive") {
                        normStatus = "inactive";
                    }
                }
                document.getElementById('modal-status').value = normStatus;
            } else {
                document.getElementById('supplier-form').reset();
                document.getElementById('modal-status').value = 'active';
            }
        }
        if(bsModal) bsModal.show();
    }

    function closeSupplierModal() { 
        if(bsModal) bsModal.hide(); 
    }

    document.addEventListener("click", function(event) {
        let btn = event.target.closest(".btn-edit-supplier");
        if (btn) {
            const id = btn.getAttribute("data-id");
            const name = btn.getAttribute("data-name");
            const phone = btn.getAttribute("data-phone");
            const address = btn.getAttribute("data-address");
            const status = btn.getAttribute("data-status");
            
            openSupplierModal('edit', id, name, phone, address, status);
        }
    });
</script>

<jsp:include page="../common/footer.jsp"/>