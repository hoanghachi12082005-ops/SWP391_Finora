<%-- 
    Document   : user-list
    Created on : 27 May 2026, 21:16:05
    Author     : PCQN
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8"/>
        <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
        <title>${pageTitle} - Finora</title>

        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/base.css?v=20260601"/>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/layout.css?v=20260601"/>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/form-modal.css?v=20260601"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/user-management.css?v=2">

        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    </head>

    <body class="user-page">
    <div class="app-layout">
        <jsp:include page="/views/common/sidebar.jsp"/>

        <div class="main-wrapper">
            <main class="page-content">

                <c:if test="${not empty sessionScope.successMessage}">
                    <div class="alert alert-success">${sessionScope.successMessage}</div>
                    <c:remove var="successMessage" scope="session"/>
                </c:if>

                <c:if test="${not empty sessionScope.errorMessage}">
                    <div class="alert alert-error">${sessionScope.errorMessage}</div>
                    <c:remove var="errorMessage" scope="session"/>
                </c:if>

                <section class="page-header">
                    <div>
                        <h2>${pageTitle}</h2>
                        <p>${pageSubtitle}</p>
                    </div>

                    <c:if test="${canCreate}">
                        <a class="btn-primary" href="${baseUrl}?action=add">
                            <span class="material-symbols-outlined">add</span>
                            ${addButtonText}
                        </a>
                    </c:if>
                </section>

                <c:if test="${not empty employeeOverview}">
                    <section class="overview-grid">

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-users">
                                <span class="material-symbols-outlined">groups</span>
                            </div>
                            <div class="overview-info">
                                <p>Tổng nhân viên</p>
                                <h3>${employeeOverview.totalEmployees}</h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-users">
                                <span class="material-symbols-outlined">verified_user</span>
                            </div>
                            <div class="overview-info">
                                <p>Nhân viên hoạt động</p>
                                <h3>${employeeOverview.activeEmployees}</h3>
                            </div>
                        </div>  

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-revenue">
                                <span class="material-symbols-outlined">payments</span>
                            </div>
                            <div class="overview-info">
                                <p>Tổng doanh thu</p>
                                <h3>
                                    <fmt:formatNumber value="${employeeOverview.totalRevenue}" type="number" groupingUsed="true"/> ₫
                                </h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-warning">
                                <span class="material-symbols-outlined">emoji_events</span>
                            </div>
                            <div class="overview-info">
                                <p>Nhân viên xuất sắc</p>
                                <c:choose>
                                    <c:when test="${empty employeeOverview.topEmployeeName}">
                                        <h3 class="text-muted">Chưa có dữ liệu</h3>
                                    </c:when>
                                    <c:otherwise>
                                        <h3>${employeeOverview.topEmployeeName}</h3>
                                        <small>
                                            Doanh thu:
                                            <fmt:formatNumber value="${employeeOverview.topEmployeeRevenue}" type="number" groupingUsed="true"/> ₫
                                        </small>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                    </section>
                </c:if>

                <form class="filter-card" method="get" action="${baseUrl}">
                    <input type="hidden" name="page" value="1"/>

                    <div class="filter-grid">

                        <div class="form-group filter-search">
                            <label>Tìm kiếm</label>
                            <input name="keyword"
                                   value="${keyword}"
                                   type="text"
                                   placeholder="Tên, email hoặc số điện thoại..."/>
                        </div>

                        <c:if test="${showBranch && not empty branches}">
                            <div class="form-group">
                                <label>Chi nhánh</label>
                                <select name="branchId">
                                    <option value="">Tất cả chi nhánh</option>

                                    <c:forEach var="branch" items="${branches}">
                                        <option value="${branch.branchID}"
                                            ${branchFilter == branch.branchID ? 'selected' : ''}>
                                            ${branch.name}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </c:if>

                        <c:if test="${not empty roles}">
                            <div class="form-group">
                                <label>Vai trò</label>
                                <select name="roleId">
                                    <option value="">Tất cả vai trò</option>

                                    <c:forEach var="role" items="${roles}">
                                        <option value="${role.roleID}"
                                            ${roleFilter == role.roleID ? 'selected' : ''}>
                                            ${role.name}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </c:if>

                        <div class="form-group">
                            <label>Trạng thái</label>
                            <select name="status">
                                <option value="">Tất cả trạng thái</option>
                                <option value="active" ${fn:toUpperCase(statusFilter) == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                                <option value="inactive" ${fn:toUpperCase(statusFilter) == 'INACTIVE' ? 'selected' : ''}>Không hoạt động</option>
                                <option value="locked" ${fn:toUpperCase(statusFilter) == 'LOCKED' ? 'selected' : ''}>Khóa</option>
                            </select>
                        </div>

                        <input type="hidden" name="sizeValue" value="${sizeValue}"/>

                        <div class="filter-actions">
                            <button class="btn-primary" type="submit">Áp dụng</button>
                            <a class="btn-secondary" href="${baseUrl}">Đặt lại</a>
                        </div>

                    </div>
                </form>

                <section class="table-card">
                    <div class="table-scroll">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Tên</th>
                                <th>Số điện thoại</th>
                                <th>Vai trò</th>

                                <c:if test="${showBranch}">
                                    <th>Chi nhánh</th>
                                </c:if>

                                <th>Trạng thái</th>
                                <th class="text-right">Thao tác</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:choose>
                                <c:when test="${empty users}">
                                    <tr>
                                        <td colspan="${showBranch ? 6 : 5}" class="empty-row">
                                            <div class="empty-state">
                                                <span class="material-symbols-outlined">person_search</span>
                                                <h4>Không tìm thấy tài khoản</h4>
                                                <p>Không có tài khoản nào phù hợp với tiêu chí tìm kiếm.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach var="user" items="${users}">
                                        <tr>
                                            <td>
                                                <div class="user-cell">
                                                    <c:choose>
                                                        <c:when test="${not empty user.avatarUrl}">
                                                            <div class="avatar-text">
                                                                <img src="${user.avatarUrl}" alt="${not empty user.fullName ? fn:substring(user.fullName, 0, 1) : 'U'}" data-name="${user.fullName}" loading="lazy"/>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="avatar-text">
                                                                ${not empty user.fullName ? fn:substring(user.fullName, 0, 1) : 'U'}
                                                            </div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <div>
                                                        <strong title="${user.fullName}">${user.fullName}</strong>
                                                        <span title="${user.email}">${user.email}</span>
                                                    </div>
                                                </div>
                                            </td>

                                            <td class="text-nowrap">${empty user.phone ? '—' : user.phone}</td>

                                            <td>
                                                <c:set var="roleClass" value="role-default"/>
                                                <c:set var="roleLabel" value="${user.roleName}"/>
                                                <c:if test="${fn:containsIgnoreCase(user.roleName, 'Admin')}"><c:set var="roleClass" value="role-admin"/><c:set var="roleLabel" value="Quản trị viên"/></c:if>
                                                <c:if test="${fn:containsIgnoreCase(user.roleName, 'Owner')}"><c:set var="roleClass" value="role-owner"/><c:set var="roleLabel" value="Chủ sở hữu"/></c:if>
                                                <c:if test="${fn:containsIgnoreCase(user.roleName, 'StoreManager') || fn:containsIgnoreCase(user.roleName, 'Store Manager')}"><c:set var="roleClass" value="role-manager"/><c:set var="roleLabel" value="Quản lý cửa hàng"/></c:if>
                                                <c:if test="${fn:containsIgnoreCase(user.roleName, 'SalesStaff') || fn:containsIgnoreCase(user.roleName, 'Sales Staff') || fn:containsIgnoreCase(user.roleName, 'Sales')}"><c:set var="roleClass" value="role-sales"/><c:set var="roleLabel" value="Nhân viên bán hàng"/></c:if>
                                                <c:if test="${fn:containsIgnoreCase(user.roleName, 'WarehouseStaff') || fn:containsIgnoreCase(user.roleName, 'Warehouse Staff') || fn:containsIgnoreCase(user.roleName, 'Warehouse')}"><c:set var="roleClass" value="role-warehouse"/><c:set var="roleLabel" value="Nhân viên kho"/></c:if>
                                                <span class="role-badge ${roleClass}">${roleLabel}</span>
                                            </td>

                                            <c:if test="${showBranch}">
                                                <td>${empty user.branchName ? '—' : user.branchName}</td>
                                            </c:if>

                                            <td>
                                                <c:choose>
                                                    <c:when test="${fn:toUpperCase(user.status) == 'ACTIVE'}">
                                                        <span class="status-badge active">Hoạt động</span>
                                                    </c:when>
                                                    <c:when test="${fn:toUpperCase(user.status) == 'LOCKED' || fn:toUpperCase(user.status) == 'INACTIVE'}">
                                                        <span class="status-badge inactive">Không hoạt động</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge locked">Khóa</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <td>
                                                <div class="table-actions">
                                                    <a href="${baseUrl}?action=detail&id=${user.employeeID}" title="Xem chi tiết" aria-label="Xem chi tiết ${user.fullName}">
                                                        <span class="material-symbols-outlined">visibility</span>
                                                    </a>
                                                    <c:if test="${canEdit}">
                                                        <a href="${baseUrl}?action=edit&id=${user.employeeID}" title="Chỉnh sửa" aria-label="Chỉnh sửa ${user.fullName}">
                                                            <span class="material-symbols-outlined">edit</span>
                                                        </a>
                                                    </c:if>
                                                    <c:if test="${canResetPassword}">
                                                        <a href="${baseUrl}?action=reset&id=${user.employeeID}" title="Đặt lại mật khẩu" aria-label="Đặt lại mật khẩu ${user.fullName}">
                                                            <span class="material-symbols-outlined">key</span>
                                                        </a>
                                                    </c:if>
                                                    <c:if test="${canLock}">
                                                        <form method="post" action="${baseUrl}" style="display:inline">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>
                                                            <input type="hidden" name="employeeId" value="${user.employeeID}"/>
                                                            <c:choose>
                                                                <c:when test="${fn:toUpperCase(user.status) == 'ACTIVE'}">
                                                                    <input type="hidden" name="action" value="lock"/>
                                                                    <button type="submit" title="Khóa tài khoản" aria-label="Khóa tài khoản ${user.fullName}" onclick="return confirm('Khóa tài khoản này?')">
                                                                        <span class="material-symbols-outlined">lock</span>
                                                                    </button>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <input type="hidden" name="action" value="unlock"/>
                                                                    <button type="submit" title="Mở khóa tài khoản" aria-label="Mở khóa tài khoản ${user.fullName}" onclick="return confirm('Mở khóa tài khoản này?')">
                                                                        <span class="material-symbols-outlined">lock_open</span>
                                                                    </button>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </form>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <jsp:include page="/views/common/pagination.jsp">
                        <jsp:param name="baseUrl" value="${baseUrl}"/>
                        <jsp:param name="queryString" value="&keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&roleId=${roleFilter == -1 ? '' : roleFilter}&status=${empty statusFilter ? '' : statusFilter}"/>
                    </jsp:include>
                </section>

            </main>
        </div>
    </div>

    <c:if test="${formMode == 'add' || formMode == 'edit'}">

        <c:set var="isEdit" value="${formMode == 'edit'}"/>
        <c:set var="formUser" value="${isEdit ? editingUser : null}"/>

        <div class="modal-overlay">
            <form class="modal-box" method="post" action="${baseUrl}">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>

                <input type="hidden"
                       name="action"
                       value="${isEdit ? 'update' : 'create'}"/>

                <c:if test="${isEdit}">
                    <input type="hidden"
                           name="employeeId"
                           value="${formUser.employeeID}"/>
                </c:if>

                <div class="modal-header">
                    <h3>${isEdit ? 'Chỉnh sửa nhân viên' : 'Thêm nhân viên'}</h3>

                    <a href="${baseUrl}" class="modal-close">
                        <span class="material-symbols-outlined">close</span>
                    </a>
                </div>

                <div class="modal-body">

                    <div class="form-row">
                        <div class="form-group">
                            <label>Họ tên</label>
                            <input type="text"
                                   name="fullName"
                                   value="${isEdit ? formUser.fullName : ''}"
                                   placeholder="Nhập họ tên"
                                   required/>
                        </div>

                        <div class="form-group">
                            <label>Email</label>
                            <input type="email"
                                   name="email"
                                   value="${isEdit ? formUser.email : ''}"
                                   placeholder="Nhập email"
                                   required/>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>Số điện thoại</label>
                            <input type="text"
                                   name="phone"
                                   value="${isEdit ? formUser.phone : ''}"
                                   placeholder="Nhập số điện thoại"/>
                        </div>

                        <div class="form-group">
                            <label>Trạng thái</label>
                            <select name="status">
                                <option value="active"
                                    ${!isEdit || fn:toUpperCase(formUser.status) == 'ACTIVE' ? 'selected' : ''}>
                                    Hoạt động
                                </option>

                                <option value="locked"
                                    ${isEdit && fn:toUpperCase(formUser.status) == 'INACTIVE' ? 'selected' : ''}>
                                    Ngừng hoạt động
                                </option>
                            </select>
                        </div>
                    </div>

                    <c:if test="${showBranch}">
                        <div class="form-group">
                            <label>Chi nhánh</label>

                            <select name="branchId" required>
                                <option value="">-- Chọn chi nhánh --</option>

                                <c:forEach var="branch" items="${branches}">
                                    <option value="${branch.branchID}"
                                        ${isEdit && formUser.branchId == branch.branchID ? 'selected' : ''}>
                                        ${branch.name}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:if>

                    <c:if test="${not empty roles}">
                        <div class="form-group">
                            <label>Vai trò</label>
                            <select name="roleId" required>
                                <option value="">-- Chọn vai trò --</option>
                                <c:forEach var="role" items="${roles}">
                                    <option value="${role.roleID}"
                                        ${isEdit && formUser.roleID == role.roleID ? 'selected' : ''}>
                                        ${role.name}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:if>

                </div>

                <div class="modal-footer">
                    <a href="${baseUrl}" class="btn-secondary">Hủy</a>

                    <button type="submit" class="btn-primary">
                        ${isEdit ? 'Cập nhật nhân viên' : 'Thêm nhân viên'}
                    </button>
                </div>

            </form>
        </div>

    </c:if>

    <c:if test="${formMode == 'reset' && not empty resetUser}">
        <div class="modal-overlay">
            <form class="modal-box small-modal" method="post" action="${baseUrl}">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>
                <input type="hidden" name="action" value="resetPassword"/>
                <input type="hidden" name="employeeId" value="${resetUser.employeeID}"/>

                <div class="modal-header">
                    <h3>Đặt lại mật khẩu</h3>

                    <a href="${baseUrl}" class="modal-close">
                        <span class="material-symbols-outlined">close</span>
                    </a>
                </div>

                <div class="modal-body">
                    <p>
                        Đặt lại mật khẩu cho
                        <strong>${resetUser.fullName}</strong>?
                    </p>

                    <p class="text-muted">
                        Mật khẩu mới sẽ được tạo tự động và gửi đến email của người dùng này.
                    </p>

                    <p>
                        <strong>Email:</strong> ${resetUser.email}
                    </p>
                </div>

                <div class="modal-footer">
                    <a class="btn-secondary" href="${baseUrl}">Hủy</a>

                    <button class="btn-primary"
                            type="submit"
                            onclick="return confirm('Đặt lại mật khẩu cho tài khoản này?')">
                        Đặt lại mật khẩu
                    </button>
                </div>
            </form>
        </div>
    </c:if>

    <c:if test="${formMode == 'detail' && not empty detailUser}">
        <div class="modal-overlay">
            <div class="modal-box small-modal">

                <div class="modal-header">
                    <h3>Chi tiết tài khoản</h3>

                    <a href="${baseUrl}" class="modal-close">
                        <span class="material-symbols-outlined">close</span>
                    </a>
                </div>

                <div class="modal-body detail-list">
                    <p><strong>Tên:</strong> ${detailUser.fullName}</p>
                    <p><strong>Email:</strong> ${detailUser.email}</p>
                    <p><strong>Số điện thoại:</strong> ${detailUser.phone}</p>
                    <p><strong>Vai trò:</strong>
                        <c:choose>
                            <c:when test="${fn:containsIgnoreCase(detailUser.roleName, 'Admin')}">Quản trị viên</c:when>
                            <c:when test="${fn:containsIgnoreCase(detailUser.roleName, 'Owner')}">Chủ sở hữu</c:when>
                            <c:when test="${fn:containsIgnoreCase(detailUser.roleName, 'StoreManager') || fn:containsIgnoreCase(detailUser.roleName, 'Store Manager')}">Quản lý cửa hàng</c:when>
                            <c:when test="${fn:containsIgnoreCase(detailUser.roleName, 'SalesStaff') || fn:containsIgnoreCase(detailUser.roleName, 'Sales Staff') || fn:containsIgnoreCase(detailUser.roleName, 'Sales')}">Nhân viên bán hàng</c:when>
                            <c:when test="${fn:containsIgnoreCase(detailUser.roleName, 'WarehouseStaff') || fn:containsIgnoreCase(detailUser.roleName, 'Warehouse Staff') || fn:containsIgnoreCase(detailUser.roleName, 'Warehouse')}">Nhân viên kho</c:when>
                            <c:otherwise>${detailUser.roleName}</c:otherwise>
                        </c:choose>
                    </p>

                    <c:if test="${showBranch}">
                        <p><strong>Chi nhánh:</strong> ${empty detailUser.branchName ? '—' : detailUser.branchName}</p>
                    </c:if>

                    <p><strong>Trạng thái:</strong>
                        <c:choose>
                            <c:when test="${fn:toUpperCase(detailUser.status) == 'ACTIVE'}">Hoạt động</c:when>
                            <c:when test="${fn:toUpperCase(detailUser.status) == 'INACTIVE'}">Không hoạt động</c:when>
                            <c:when test="${fn:toUpperCase(detailUser.status) == 'LOCKED'}">Khóa</c:when>
                            <c:otherwise>${detailUser.status}</c:otherwise>
                        </c:choose>
                    </p>

                    <p>
                        <strong>Ngày tạo:</strong>
                        <c:if test="${not empty detailUser.createdAt}">
                            <fmt:formatDate value="${detailUser.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                        </c:if>
                        <c:if test="${empty detailUser.createdAt}">—</c:if>
                    </p>
                </div>

                <div class="modal-footer">
                    <a class="btn-primary" href="${baseUrl}">Đóng</a>
                </div>

            </div>
        </div>
    </c:if>

<script>
var avatarColors = ['#f3e8ff','#dbeafe','#dcfce7','#fef3c7','#ffedd5','#fce7f3','#e0e7ff','#d1fae5','#ffe4e6','#e0f2fe'];
function applyAvatarColor(el) {
    var idx = (el.textContent.trim().charCodeAt(0) || 65) % avatarColors.length;
    el.style.background = avatarColors[idx];
    el.style.color = '#4a1d96';
}
document.querySelectorAll('.avatar-text:not(:has(img))').forEach(applyAvatarColor);
document.querySelectorAll('.avatar-text img').forEach(function(img) {
    img.addEventListener('error', function() {
        var parent = this.parentElement;
        var initial = this.getAttribute('alt') || 'U';
        var name = this.getAttribute('data-name') || '';
        parent.textContent = initial;
        parent.className = 'avatar-text';
        parent.setAttribute('title', name);
        applyAvatarColor(parent);
    });
});
</script>
    </body>
</html>
