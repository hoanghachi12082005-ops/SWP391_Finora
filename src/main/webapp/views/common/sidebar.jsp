<%-- 
    Document   : sidebar
    Created on : 27 May 2026, 21:11:59
    Author     : PCQN
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="sessionRoleName" value="${empty sessionScope.currentUser.roleName ? '' : sessionScope.currentUser.roleName}" />
<c:set var="currentRoleName" value="${empty sessionRoleName ? (empty sessionScope.roleName ? '' : sessionScope.roleName) : sessionRoleName}" />
<c:set var="normalizedRoleName" value="${fn:toLowerCase(currentRoleName)}" />
<c:set var="isAdmin" value="${normalizedRoleName == 'admin'}" />
<c:set var="isOwner" value="${normalizedRoleName == 'owner'}" />
<c:set var="isManager" value="${normalizedRoleName == 'storemanager' || normalizedRoleName == 'store manager'}" />
<c:set var="isSalesStaff" value="${normalizedRoleName == 'salesstaff' || normalizedRoleName == 'sales staff' || normalizedRoleName == 'sales'}" />
<c:set var="canViewOrders" value="${isAdmin || isOwner || isManager || isSalesStaff}" />

<nav class="sidebar">
    <div class="sidebar-brand">
        <div class="brand-logo">F</div>
        <div>
            <h1>Finora</h1>
            <p>
                <c:choose>
                    <c:when test="${isAdmin}">System Admin</c:when>
                    <c:when test="${isOwner}">Shop Owner</c:when>
                    <c:when test="${isManager}">Store Manager</c:when>
                    <c:when test="${isSalesStaff}">Sales Staff</c:when>
                    <c:otherwise>Employee</c:otherwise>
                </c:choose>
            </p>
        </div>
    </div>

    <div class="sidebar-menu">
        <a class="menu-item ${fn:contains(pageContext.request.requestURI, '/dashboard') ? 'active' : ''}" href="${pageContext.request.contextPath}/dashboard">
            <span class="material-symbols-outlined">dashboard</span>
            <span>Dashboard</span>
        </a>

        <c:if test="${isAdmin}">
            <a class="menu-item ${fn:contains(pageContext.request.requestURI, '/admin/user') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/user">
                <span class="material-symbols-outlined">manage_accounts</span>
                <span>Accounts</span>
            </a>
        </c:if>

        <c:if test="${isOwner}">
            <a class="menu-item ${fn:contains(pageContext.request.requestURI, '/owner/emp') ? 'active' : ''}" href="${pageContext.request.contextPath}/owner/emp">
                <span class="material-symbols-outlined">badge</span>
                <span>Employees</span>
            </a>
            <a class="menu-item ${fn:contains(pageContext.request.requestURI, '/owner/branch') ? 'active' : ''}" href="${pageContext.request.contextPath}/owner/branch">
                <span class="material-symbols-outlined">storefront</span>
                <span>Stores</span>
            </a>
            <a class="menu-item ${fn:contains(pageContext.request.requestURI, '/reports/employee-sales') ? 'active' : ''}" href="${pageContext.request.contextPath}/reports/employee-sales">
                <span class="material-symbols-outlined">bar_chart</span>
                <span>Employee Sales</span>
            </a>
        </c:if>

        <c:if test="${isManager}">
            <a class="menu-item ${fn:contains(pageContext.request.requestURI, '/manager/emp') ? 'active' : ''}" href="${pageContext.request.contextPath}/manager/emp">
                <span class="material-symbols-outlined">badge</span>
                <span>Branch Employees</span>
            </a>
            <a class="menu-item ${fn:contains(pageContext.request.requestURI, '/reports/employee-sales') ? 'active' : ''}" href="${pageContext.request.contextPath}/reports/employee-sales">
                <span class="material-symbols-outlined">bar_chart</span>
                <span>Employee Sales</span>
            </a>
        </c:if>

        <c:if test="${isOwner || isManager}">
            <a class="menu-item ${fn:contains(pageContext.request.requestURI, '/customers') ? 'active' : ''}" href="${pageContext.request.contextPath}/customers">
                <span class="material-symbols-outlined">group</span>
                <span>Customers</span>
            </a>
        </c:if>

        <c:if test="${canViewOrders}">
            <a class="menu-item ${fn:contains(pageContext.request.requestURI, '/orders/') ? 'active' : ''}" href="${pageContext.request.contextPath}/orders/list">
                <span class="material-symbols-outlined">receipt_long</span>
                <span>Order List</span>
            </a>
        </c:if>
    </div>

    <div class="sidebar-footer">
        <a class="menu-item" href="${pageContext.request.contextPath}/profile">
            <span class="material-symbols-outlined">person</span>
            <span>Profile</span>
        </a>
        <a class="menu-item" href="${pageContext.request.contextPath}/logout">
            <span class="material-symbols-outlined">logout</span>
            <span>Logout</span>
        </a>
    </div>
</nav>

