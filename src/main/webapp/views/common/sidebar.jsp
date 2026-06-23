<%-- 
    Document   : sidebar
    Created on : 27 May 2026, 21:11:59
    Author     : PCQN
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<nav class="sidebar">
    <div class="sidebar-brand">
        <div class="brand-logo">F</div>
        <div>
            <h1>Finora</h1>
            <p>
                <c:choose>
                    <c:when test="${sessionScope.roleName == 'Admin'}">System Admin</c:when>
                    <c:when test="${sessionScope.roleName == 'Owner'}">Shop Owner</c:when>
                    <c:when test="${sessionScope.roleName == 'StoreManager'}">Store Manager</c:when>
                    <c:otherwise>Employee</c:otherwise>
                </c:choose>
            </p>
        </div>
    </div>

    <div class="sidebar-menu">
        <a class="menu-item" href="${pageContext.request.contextPath}/dashboard">
            <span class="material-symbols-outlined">dashboard</span>
            <span>Dashboard</span>
        </a>

        <c:if test="${sessionScope.roleName == 'Admin'}">
            <a class="menu-item active" href="${pageContext.request.contextPath}/admin/user">
                <span class="material-symbols-outlined">manage_accounts</span>
                <span>Owner Accounts</span>
            </a>
        </c:if>

        <c:if test="${sessionScope.roleName == 'Owner'}">
            <a class="menu-item active" href="${pageContext.request.contextPath}/owner/emp">
                <span class="material-symbols-outlined">badge</span>
                <span>Employees</span>
            </a>
            <a class="menu-item" href="${pageContext.request.contextPath}/owner/branch">
                <span class="material-symbols-outlined">storefront</span>
                <span>Stores</span>
            </a>
        </c:if>

        <c:if test="${sessionScope.roleName == 'StoreManager'}">
            <a class="menu-item active" href="${pageContext.request.contextPath}/manager/emp">
                <span class="material-symbols-outlined">badge</span>
                <span>Branch Employees</span>
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

