<%-- 
    Document   : user-list
    Created on : 27 May 2026, 21:16:05
    Author     : PCQN
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8"/>
        <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
        <title>${pageTitle} - Finora</title>

        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/base.css?v=20260528"/>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/layout.css?v=20260528"/>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/form-modal.css?v=20260528"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/user-management.css?v=6">

        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
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
                                <p>Total Employees</p>
                                <h3>${employeeOverview.totalEmployees}</h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-users">
                                <span class="material-symbols-outlined">verified_user</span>
                            </div>
                            <div class="overview-info">
                                <p>Active Employees</p>
                                <h3>${employeeOverview.activeEmployees}</h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-orders">
                                <span class="material-symbols-outlined">receipt_long</span>
                            </div>
                            <div class="overview-info">
                                <p>Total Orders</p>
                                <h3>${employeeOverview.totalOrders}</h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-revenue">
                                <span class="material-symbols-outlined">payments</span>
                            </div>
                            <div class="overview-info">
                                <p>Total Revenue</p>
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
                                <p>Top Employee</p>
                                <h3>${empty employeeOverview.topEmployeeName ? '—' : employeeOverview.topEmployeeName}</h3>
                                <small>
                                    Revenue:
                                    <fmt:formatNumber value="${employeeOverview.topEmployeeRevenue}" type="number" groupingUsed="true"/> ₫
                                </small>
                            </div>
                        </div>

                    </section>
                </c:if>

                <form class="filter-card" method="get" action="${baseUrl}">
                    <input type="hidden" name="page" value="1"/>

                    <div class="filter-grid">

                        <div class="form-group filter-search">
                            <label>Search</label>
                            <input name="keyword"
                                   value="${keyword}"
                                   type="text"
                                   placeholder="Name, email or phone..."/>
                        </div>

                        <c:if test="${showBranch && not empty branches}">
                            <div class="form-group">
                                <label>Branch</label>
                                <select name="branchId">
                                    <option value="">All Branches</option>

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
                                <label>Role</label>
                                <select name="roleId">
                                    <option value="">All Roles</option>

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
                            <label>Status</label>
                            <select name="status">
                                <option value="">All Status</option>
                                <option value="active" ${statusFilter == 'ACTIVE' ? 'selected' : ''}>Active</option>
                                <option value="locked" ${statusFilter == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Page Size</label>
                            <select name="pageSize">
                                <option value="5" ${empty pageSizeOption || pageSizeOption == '5' ? 'selected' : ''}>
                                    5 records/page
                                </option>

                                <option value="10" ${pageSizeOption == '10' ? 'selected' : ''}>
                                    10 records/page
                                </option>

                                <option value="30p" ${pageSizeOption == '30p' || pageSizeOption == '30%' || pageSizeOption == '30' ? 'selected' : ''}>
                                    30% records/page
                                </option>

                                <option value="50p" ${pageSizeOption == '50p' || pageSizeOption == '50%' || pageSizeOption == '50' ? 'selected' : ''}>
                                    50% records/page
                                </option>
                            </select>
                        </div>

                        <div class="filter-actions">
                            <button class="btn-primary" type="submit">Apply</button>
                            <a class="btn-secondary" href="${baseUrl}">Reset</a>
                        </div>

                    </div>
                </form>

                <section class="table-card">
                    <div class="table-scroll">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Name</th>
                                <th>Phone</th>
                                <th>Role</th>

                                <c:if test="${showBranch}">
                                    <th>Branch</th>
                                </c:if>

                                <th>Status</th>
                                <th class="text-right">Actions</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:choose>
                                <c:when test="${empty users}">
                                    <tr>
                                        <td colspan="${showBranch ? 6 : 5}" class="empty-row">
                                            <div class="empty-state">
                                                <span class="material-symbols-outlined">person_search</span>
                                                <h4>No accounts found</h4>
                                                <p>No user accounts match your search criteria.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach var="user" items="${users}">
                                        <tr>
                                            <td>
                                                <div class="user-cell">
                                                    <div class="avatar-text">
                                                        <c:choose>
                                                            <c:when test="${not empty user.fullName}">
                                                                ${fn:substring(user.fullName, 0, 1)}
                                                            </c:when>
                                                            <c:otherwise>U</c:otherwise>
                                                        </c:choose>
                                                    </div>

                                                    <div>
                                                        <strong>${user.fullName}</strong>
                                                        <span>${user.email}</span>
                                                    </div>
                                                </div>
                                            </td>

                                            <td>${empty user.phone ? '—' : user.phone}</td>

                                            <td>
                                                <span class="role-badge">
                                                    ${user.roleName}
                                                </span>
                                            </td>

                                            <c:if test="${showBranch}">
                                                <td>${empty user.branchName ? '—' : user.branchName}</td>
                                            </c:if>

                                            <td>
                                                <c:choose>
                                                    <c:when test="${user.status == 'ACTIVE'}">
                                                        <span class="status-badge active">ACTIVE</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge locked">INACTIVE</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <td>
                                                <div class="table-actions">

                                                    <a href="${baseUrl}?action=detail&id=${user.employeeID}" title="View Detail">
                                                        <span class="material-symbols-outlined">visibility</span>
                                                    </a>

                                                    <c:if test="${canEdit}">
                                                        <a href="${baseUrl}?action=edit&id=${user.employeeID}" title="Edit">
                                                            <span class="material-symbols-outlined">edit</span>
                                                        </a>
                                                    </c:if>

                                                    <c:if test="${canResetPassword}">
                                                        <a href="${baseUrl}?action=reset&id=${user.employeeID}" title="Reset Password">
                                                            <span class="material-symbols-outlined">key</span>
                                                        </a>
                                                    </c:if>

                                                    <c:if test="${canLock}">
                                                        <form method="post" action="${baseUrl}">
                                                            <input type="hidden" name="employeeID" value="${user.employeeID}"/>

                                                            <c:choose>
                                                                <c:when test="${user.status == 'ACTIVE'}">
                                                                    <input type="hidden" name="action" value="lock"/>
                                                                    <button type="submit"
                                                                            title="Lock"
                                                                            onclick="return confirm('Lock this account?')">
                                                                        <span class="material-symbols-outlined">lock</span>
                                                                    </button>
                                                                </c:when>

                                                                <c:otherwise>
                                                                    <input type="hidden" name="action" value="unlock"/>
                                                                    <button type="submit"
                                                                            title="Unlock"
                                                                            onclick="return confirm('Unlock this account?')">
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

                    <div class="table-footer">
                        <div>
                            Showing ${empty users ? 0 : fn:length(users)} of ${empty totalUsers ? 0 : totalUsers} entries

                            <c:if test="${not empty pageSize}">
                                <span class="page-size-note">
                                    / ${pageSize} per page
                                </span>
                            </c:if>
                        </div>

                        <c:set var="paginationBaseUrl"
                               value="${fn:replace(baseUrl, pageContext.request.contextPath, '')}"/>

                        <c:if test="${totalPages > 1}">
                            <div class="pagination">

                                <c:url var="prevUrl" value="${paginationBaseUrl}">
                                    <c:param name="page" value="${currentPage - 1}"/>
                                    <c:param name="keyword" value="${keyword}"/>
                                    <c:param name="branchId" value="${branchFilter == -1 ? '' : branchFilter}"/>
                                    <c:param name="roleId" value="${roleFilter == -1 ? '' : roleFilter}"/>
                                    <c:param name="status" value="${statusFilter}"/>
                                    <c:param name="pageSize" value="${pageSizeOption}"/>
                                </c:url>

                                <c:choose>
                                    <c:when test="${currentPage > 1}">
                                        <a class="page-btn" href="${prevUrl}">Previous</a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="page-btn disabled">Previous</span>
                                    </c:otherwise>
                                </c:choose>

                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <c:url var="pageUrl" value="${paginationBaseUrl}">
                                        <c:param name="page" value="${i}"/>
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="branchId" value="${branchFilter == -1 ? '' : branchFilter}"/>
                                        <c:param name="roleId" value="${roleFilter == -1 ? '' : roleFilter}"/>
                                        <c:param name="status" value="${statusFilter}"/>
                                        <c:param name="pageSize" value="${pageSizeOption}"/>
                                    </c:url>

                                    <c:choose>
                                        <c:when test="${i == currentPage}">
                                            <span class="page-btn active">${i}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <a class="page-btn" href="${pageUrl}">${i}</a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>

                                <c:url var="nextUrl" value="${paginationBaseUrl}">
                                    <c:param name="page" value="${currentPage + 1}"/>
                                    <c:param name="keyword" value="${keyword}"/>
                                    <c:param name="branchId" value="${branchFilter == -1 ? '' : branchFilter}"/>
                                    <c:param name="roleId" value="${roleFilter == -1 ? '' : roleFilter}"/>
                                    <c:param name="status" value="${statusFilter}"/>
                                    <c:param name="pageSize" value="${pageSizeOption}"/>
                                </c:url>

                                <c:choose>
                                    <c:when test="${currentPage < totalPages}">
                                        <a class="page-btn" href="${nextUrl}">Next</a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="page-btn disabled">Next</span>
                                    </c:otherwise>
                                </c:choose>

                            </div>
                        </c:if>
                    </div>
                </section>

            </main>
        </div>
    </div>

    <c:if test="${formMode == 'add' || formMode == 'edit'}">

        <c:set var="isEdit" value="${formMode == 'edit'}"/>
        <c:set var="formUser" value="${isEdit ? editingUser : null}"/>

        <div class="modal-overlay">
            <form class="modal-box" method="post" action="${baseUrl}">

                <input type="hidden"
                       name="action"
                       value="${isEdit ? 'update' : 'create'}"/>

                <c:if test="${isEdit}">
                    <input type="hidden"
                           name="DId"
                           value="${formUser.employeeID}"/>
                </c:if>

                <div class="modal-header">
                    <h3>${isEdit ? 'Edit Employee' : 'Add Employee'}</h3>

                    <a href="${baseUrl}" class="modal-close">
                        <span class="material-symbols-outlined">close</span>
                    </a>
                </div>

                <div class="modal-body">

                    <div class="form-row">
                        <div class="form-group">
                            <label>Full Name</label>
                            <input type="text"
                                   name="fullName"
                                   value="${isEdit ? formUser.fullName : ''}"
                                   placeholder="Enter full name"
                                   required/>
                        </div>

                        <div class="form-group">
                            <label>Email</label>
                            <input type="email"
                                   name="email"
                                   value="${isEdit ? formUser.email : ''}"
                                   placeholder="Enter email"
                                   required/>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>Phone</label>
                            <input type="text"
                                   name="phone"
                                   value="${isEdit ? formUser.phone : ''}"
                                   placeholder="Enter phone number"/>
                        </div>

                        <div class="form-group">
                            <label>Status</label>
                            <select name="status">
                                <option value="active"
                                    ${!isEdit || formUser.status == 'ACTIVE' ? 'selected' : ''}>
                                    Active
                                </option>

                                <option value="locked"
                                    ${isEdit && formUser.status == 'INACTIVE' ? 'selected' : ''}>
                                    Locked
                                </option>
                            </select>
                        </div>
                    </div>

                    <c:if test="${showBranch}">
                        <div class="form-group">
                            <label>Branch</label>

                            <select name="branchId" required>
                                <option value="">-- Select Branch --</option>

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
                            <label>Role</label>
                            <select name="roleId" required>
                                <option value="">-- Select Role --</option>
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
                    <a href="${baseUrl}" class="btn-secondary">Cancel</a>

                    <button type="submit" class="btn-primary">
                        ${isEdit ? 'Update Employee' : 'Add Employee'}
                    </button>
                </div>

            </form>
        </div>

    </c:if>

    <c:if test="${formMode == 'reset' && not empty resetUser}">
        <div class="modal-overlay">
            <form class="modal-box small-modal" method="post" action="${baseUrl}">
                <input type="hidden" name="action" value="resetPassword"/>
                <input type="hidden" name="employeeID" value="${resetUser.employeeID}"/>

                <div class="modal-header">
                    <h3>Reset Password</h3>

                    <a href="${baseUrl}" class="modal-close">
                        <span class="material-symbols-outlined">close</span>
                    </a>
                </div>

                <div class="modal-body">
                    <p>
                        Reset password for
                        <strong>${resetUser.fullName}</strong>?
                    </p>

                    <p class="text-muted">
                        A new password will be generated automatically and sent to this user's email.
                    </p>

                    <p>
                        <strong>Email:</strong> ${resetUser.email}
                    </p>
                </div>

                <div class="modal-footer">
                    <a class="btn-secondary" href="${baseUrl}">Cancel</a>

                    <button class="btn-primary"
                            type="submit"
                            onclick="return confirm('Reset password for this account?')">
                        Reset Password
                    </button>
                </div>
            </form>
        </div>
    </c:if>

    <c:if test="${formMode == 'detail' && not empty detailUser}">
        <div class="modal-overlay">
            <div class="modal-box small-modal">

                <div class="modal-header">
                    <h3>Account Detail</h3>

                    <a href="${baseUrl}" class="modal-close">
                        <span class="material-symbols-outlined">close</span>
                    </a>
                </div>

                <div class="modal-body detail-list">
                    <p><strong>Name:</strong> ${detailUser.fullName}</p>
                    <p><strong>Email:</strong> ${detailUser.email}</p>
                    <p><strong>Phone:</strong> ${detailUser.phone}</p>
                    <p><strong>Role:</strong> ${detailUser.roleName}</p>

                    <c:if test="${showBranch}">
                        <p><strong>Branch:</strong> ${empty detailUser.branchName ? '—' : detailUser.branchName}</p>
                    </c:if>

                    <p><strong>Status:</strong> ${detailUser.status}</p>

                    <p>
                        <strong>Created At:</strong>
                        <fmt:formatDate value="${detailUser.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                    </p>
                </div>

                <div class="modal-footer">
                    <a class="btn-primary" href="${baseUrl}">Close</a>
                </div>

            </div>
        </div>
    </c:if>

    </body>
</html>














