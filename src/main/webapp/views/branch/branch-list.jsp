<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<!DOCTYPE html>
<html>
    <head>

        <meta charset="UTF-8">

        <title>Danh sách chi nhánh</title>

        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/branch.css">

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/fontawesome/css/all.min.css">

    </head>

    <body>

        <jsp:include page="/views/common/sidebar.jsp"/>

        <div class="main-content">

            <jsp:include page="header.jsp"/>

            <div class="page-header">

                <div class="page-title">

                    <h1>Quản lý cửa hàng</h1>

                    <p>
                        Quản lý toàn bộ chi nhánh trong hệ thống
                    </p>

                </div>

                <a href="${pageContext.request.contextPath}/branch?action=add"
                   class="btn-add">

                    + Thêm cửa hàng

                </a>

            </div>

            <div class="stats">

                <div class="stat-card">
                    <h3 class="stat-label">TỔNG CỬA HÀNG</h3>
                    <p class="stat-value">${totalBranch}</p>
                </div>

                <div class="stat-card">
                    <h3 class="stat-label">DOANH THU HÔM NAY</h3>
                    <p class="stat-value">${todayRevenue}</p>
                </div>

                <div class="stat-card">
                    <h3 class="stat-label">TỔNG NHÂN VIÊN</h3>
                    <p class="stat-value">${totalEmployee}</p>
                </div>

                <div class="stat-card">
                    <h3 class="stat-label">CỬA HÀNG TỐT NHẤT</h3>
                    <p class="stat-value stat-value--name">${bestBranch}</p>
                </div>

            </div>

            <div class="table-card">

                <div class="table-header">

                    <h3>Danh sách chi nhánh</h3>
                    <div>
                        <!-- ---------------------- Cấu hình Filter ---------------------- -->
                        <form method="GET" action="branch" class="filter-branch">
                            <input type="hidden" name="action" value="list">
                            <select class="filter-branch-select" name="status">
                                <option value="">Tất cả trạng thái</option>
                                <option value="ACTIVE"
                                        ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>
                                    Hoạt động
                                </option>

                                <option value="locked"
                                        ${selectedStatus == 'locked' ? 'selected' : ''}>
                                    Ngừng hoạt động
                                </option>
                            </select>

                            <select class="filter-branch-select" name="city">

                                <option value="">
                                    Tất cả thành phố
                                </option>

                                <c:forEach items="${cityList}" var="city">

                                    <option value="${city}" ${city == selectedCity ? 'selected' : ''}>
                                        ${city}
                                    </option>

                                </c:forEach>

                            </select>
                            <button type="submit" class="btn-filter">
                                <p>Lọc</p>
                            </button>
                        </form>
                    </div>

                </div>
                <table class="table branch-table">
                    <thead>
                        <tr> 
                            <th>Mã</th>
                            <th>Tên chi nhánh</th>
                            <th>Địa chỉ</th>
                            <th>Điện thoại</th>
                            <th>Quản lý</th>
                                <c:if test="${showEmployeeColumn}">
                                <th>Nhân viên</th>
                                </c:if>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>

                    <tbody>
                        <c:forEach items="${branchList}" var="b">
                            <tr>
                                <td>
                                    <div class="branch-code-cell">

                                        <c:choose>
                                            <c:when test="${not empty b.imageUrl}">
                                                <img
                                                    src="${pageContext.request.contextPath}/assets/images/images_branch/${b.imageUrl}"
                                                    alt="${b.branchName}"
                                                    class="branch-avatar">
                                            </c:when>

                                            <c:otherwise>
                                                <div class="branch-avatar-placeholder">
                                                    <i class="fa-solid fa-store"></i>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <span>${b.branchCode}</span>

                                    </div>
                                </td>
                                <td>${b.branchName}</td>
                                <td>${b.fullAddress}</td>
                                <td>${b.phone}</td>
                                <td>${empty b.managerName ? 'Chưa phân công' : b.managerName}</td>
                                <c:if test="${showEmployeeColumn}">
                                    <td>${branch.employeeCount}</td>
                                </c:if>
                                <td>
                                    <c:set var="branchStatus" value="${fn:toLowerCase(b.status)}"/>
                                    <span class="${branchStatus == 'active'
                                                   ? 'status-active'
                                                   : 'status-inactive'}">
                                              ${branchStatus == 'active' ? 'Active' : 'Inactive'}
                                          </span>
                                    </td>

                                    <td>
                                        <div class="action-group">
                                            <a class="btn-view"
                                               href="branch?action=detail&id=${b.branchId}">
                                                <i class="fa-regular fa-eye" style="color: rgb(24, 49, 83);"></i>
                                            </a>

                                            <a class="btn-edit"
                                               href="branch?action=edit&id=${b.branchId}">
                                                <i class="fa-solid fa-pen" style="color: rgb(24, 49, 83);"></i>
                                            </a>

                                            <button type="button"
                                                    class="btn-delete"
                                                    data-id="${b.branchId}"
                                                    data-name="${fn:escapeXml(b.branchName)}"
                                                    onclick="openDeleteModal(this)">
                                                <i class="fa-solid fa-trash-can" style="color: rgb(24, 49, 83);"></i>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>

                    </table>

                    <div class="pagination-info">

                        <form method="get" action="branch">

                            <input type="hidden" name="action" value="list"> 
                            <input type="hidden" name="status" value="${selectedStatus}">
                            <input type="hidden" name="city" value="${selectedCity}">

                            <select name="sizeValue" onchange="this.form.submit()"> 
                                <option value="30"
                                        ${sizeValue == 30 ? "selected" : ""}>
                                    ${option30}
                                </option>

                                <option value="50"
                                        ${sizeValue == 50 ? "selected" : ""}>
                                    ${option50}
                                </option>

                                <option value="70"
                                        ${sizeValue == 70 ? "selected" : ""}>
                                    ${option70}
                                </option>

                                <option value="100"
                                        ${sizeValue == 100 ? "selected" : ""}>
                                    Tất cả
                                </option>
                            </select>

                            <span class="pagination-summary">
                                ${startRecord}
                                -
                                ${endRecord}

                                trong số

                                ${totalRecords}
                            </span>        
                        </form>
                    </div>

                    <div class="pagination">

                        <c:if test="${currentPage > 1}">
                            <a href="branch?action=list&page=${currentPage - 1}&mode=${mode}&pageSize=${pageSize}&status=${selectedStatus}&city=${selectedCity}">
                                <<
                            </a>
                        </c:if>

                        <c:choose>

                            <%-- Nếu <= 5 trang thì hiện tất cả --%>
                            <c:when test="${totalPages <= 5}">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="branch?action=list&page=${i}&mode=${mode}&pageSize=${pageSize}&status=${selectedStatus}&city=${selectedCity}"
                                       class="${i == currentPage ? 'active-page' : ''}">
                                        ${i}
                                    </a>
                                </c:forEach>
                            </c:when>

                            <%-- Nếu > 5 trang --%>
                            <c:otherwise>

                                <%-- Trang đầu --%>
                                <a href="branch?action=list&page=1&mode=${mode}&pageSize=${pageSize}&status=${selectedStatus}&city=${selectedCity}"
                                   class="${currentPage == 1 ? 'active-page' : ''}">
                                    1
                                </a>

                                <%-- ... bên trái --%>
                                <c:if test="${currentPage > 3}">
                                    <span class="dots">...</span>
                                </c:if>

                                <%-- Trang trước, hiện tại, sau --%>
                                <c:forEach
                                    begin="${currentPage - 1 < 2 ? 2 : currentPage - 1}"
                                    end="${currentPage + 1 > totalPages - 1 ? totalPages - 1 : currentPage + 1}"
                                    var="i">

                                    <a href="branch?action=list&page=${i}&mode=${mode}&pageSize=${pageSize}&status=${selectedStatus}&city=${selectedCity}"
                                       class="${i == currentPage ? 'active-page' : ''}">
                                        ${i}
                                    </a>

                                </c:forEach>

                                <%-- ... bên phải --%>
                                <c:if test="${currentPage < totalPages - 2}">
                                    <span class="dots">...</span>
                                </c:if>

                                <%-- Trang cuối --%>
                                <a href="branch?action=list&page=${totalPages}&mode=${mode}&pageSize=${pageSize}&status=${selectedStatus}&city=${selectedCity}"
                                   class="${currentPage == totalPages ? 'active-page' : ''}">
                                    ${totalPages}
                                </a>

                            </c:otherwise>

                        </c:choose>

                        <c:if test="${currentPage < totalPages}">
                            <a href="branch?action=list&page=${currentPage + 1}&mode=${mode}&pageSize=${pageSize}&status=${selectedStatus}&city=${selectedCity}">
                                >>
                            </a>
                        </c:if>
                    </div>
                </div>

            </div>

            <jsp:include page="delete-modal.jsp"/>


            <script>
                (function () {
                    const CONFIRM_PHRASE = 'XAC NHAN';
                    const contextPath = '${pageContext.request.contextPath}';

                    const modal = document.getElementById('deleteModal');
                    const nameEl = document.getElementById('deleteBranchName');
                    const inputEl = document.getElementById('deleteConfirmInput');
                    const confirmBtn = document.getElementById('deleteConfirmBtn');
                    const errorEl = document.getElementById('deleteConfirmError');

                    let pendingDeleteId = null;

                    function isPhraseMatched() {
                        return inputEl.value.trim().toUpperCase() === CONFIRM_PHRASE;
                    }

                    function updateConfirmButton() {
                        const matched = isPhraseMatched();
                        confirmBtn.disabled = !matched;
                        errorEl.hidden = matched || inputEl.value.trim() === '';
                    }

                    window.openDeleteModal = function (trigger) {
                        pendingDeleteId = trigger.getAttribute('data-id');
                        nameEl.textContent = trigger.getAttribute('data-name') || '';
                        inputEl.value = '';
                        errorEl.hidden = true;
                        confirmBtn.disabled = true;
                        modal.classList.add('is-open');
                        inputEl.focus();
                    };

                    window.closeDeleteModal = function () {
                        modal.classList.remove('is-open');
                        pendingDeleteId = null;
                        inputEl.value = '';
                        confirmBtn.disabled = true;
                        errorEl.hidden = true;
                    };

                    inputEl.addEventListener('input', updateConfirmButton);

                    inputEl.addEventListener('keydown', function (e) {
                        if (e.key === 'Enter' && isPhraseMatched() && pendingDeleteId) {
                            confirmBtn.click();
                        }
                        if (e.key === 'Escape') {
                            closeDeleteModal();
                        }
                    });

                    confirmBtn.addEventListener('click', function () {
                        if (!pendingDeleteId || !isPhraseMatched()) {
                            errorEl.hidden = false;
                            return;
                        }
                        window.location.href = contextPath + '/branch?action=delete&id=' + pendingDeleteId;
                    });

                    modal.addEventListener('click', function (e) {
                        if (e.target === modal) {
                            closeDeleteModal();
                        }
                    });
                })();
            </script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        </body>
    </html>