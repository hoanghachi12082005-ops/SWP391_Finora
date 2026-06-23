<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Danh sách nhà cung cấp"/>
</jsp:include>
<div class="app-container">

    <jsp:include page="../common/sidebar.jsp"/>

    <main class="main-content">

        <div class="container-fluid py-4">

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

                    <a href="suppliers?action=create" class="btn btn-danger">+ Thêm nhà cung cấp</a>
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

                            <div class="col-md-8">

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
                                <th width="150">Thao tác</th>
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
                                        <a href="suppliers?action=edit&id=${s.supplierID}"class="btn btn-sm btn-warning">Sửa</a>

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
                        <ul class="pagination mb-0">
                            <li class="page-item ${page <= 1 ? 'disabled' : ''}">
                                <a class="page-link" href="suppliers?page=${page - 1}&keyword=${keyword}">Trước</a>
                            </li>
                            <c:forEach begin="1" end="${totalPage}" var="i">
                                <li class="page-item ${page == i ? 'active' : ''}">
                                    <a class="page-link" href="suppliers?page=${i}&keyword=${keyword}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${page >= totalPage ? 'disabled' : ''}">
                                <a class="page-link" href="suppliers?page=${page + 1}&keyword=${keyword}">Tiếp</a>
                            </li>
                        </ul>
                    </div>

                </div>

            </div>
    </main>
</div>

<jsp:include page="../common/footer.jsp"/>