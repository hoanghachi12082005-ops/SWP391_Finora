<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : 'Nhân viên'}" />
<c:set var="fullName" value="${sessionScope.currentUser.fullName != null ? sessionScope.currentUser.fullName : 'Lê Minh Quân'}" />

<aside class="sidebar">
    <!-- Brand Logo -->
    <div class="sidebar-brand">
        <div class="sidebar-brand-icon">
            <span class="material-icons">store</span>
        </div>
        <div class="sidebar-brand-text">
            <h4>FINORA</h4>
            <small>Hệ thống Quản trị Bán hàng</small>
        </div>
    </div>

    <!-- Navigation Menu -->
    <nav class="sidebar-menu">
        <div class="sidebar-menu-title">Chức năng chính</div>
        
        <c:set var="originalUri" value="${requestScope['jakarta.servlet.forward.request_uri'] != null ? requestScope['jakarta.servlet.forward.request_uri'] : pageContext.request.requestURI}" />

        <!-- Dashboard Owner Overview (Admin, Owner, StoreManager) -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
            <a href="${pageContext.request.contextPath}/dashboard/owner" 
               class="sidebar-menu-item ${originalUri.contains('/dashboard/owner') ? 'active' : ''}">
                <span class="material-icons">dashboard</span>
                <span>Tổng quan</span>
            </a>
        </c:if>

        <!-- Inventory Dashboard (WarehouseStaff) -->
        <c:if test="${roleName == 'WarehouseStaff'}">
            <a href="${pageContext.request.contextPath}/inventory/dashboard" 
               class="sidebar-menu-item ${originalUri.contains('/inventory/') ? 'active' : ''}">
                <span class="material-icons">inventory_2</span>
                <span>Kho hàng</span>
            </a>
        </c:if>

        <!-- Product Management -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager' || roleName == 'WarehouseStaff'}">
            <c:set var="isProductActive" value="${originalUri.contains('/products') || originalUri.contains('/category')}" />
            <a href="#productsCollapse" data-bs-toggle="collapse" role="button" aria-expanded="${isProductActive ? 'true' : 'false'}" aria-controls="productsCollapse"
               class="sidebar-menu-item ${isProductActive ? 'active' : ''} d-flex align-items-center">
                <span class="material-icons">shopping_bag</span>
                <span>Hàng hóa</span>
                <span class="material-icons ms-auto transition-icon" style="font-size: 1.2rem;">expand_more</span>
            </a>
            <div class="collapse ${isProductActive ? 'show' : ''}" id="productsCollapse">
                <div class="sidebar-submenu">
                    <a href="${pageContext.request.contextPath}/products" class="sidebar-submenu-item ${originalUri.contains('/products') ? 'active' : ''}">
                        Sản phẩm
                    </a>
                    <a href="${pageContext.request.contextPath}/category" class="sidebar-submenu-item ${originalUri.contains('/category') ? 'active' : ''}">
                        Danh mục
                    </a>
                </div>
            </div>
        </c:if>

        <!-- Create Order (SalesStaff, StoreManager, Admin, Owner) -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager' || roleName == 'SalesStaff'}">
            <a href="${pageContext.request.contextPath}/orders/create" 
               class="sidebar-menu-item ${pageContext.request.requestURI.contains('/orders/') ? 'active' : ''}">
                <span class="material-icons">point_of_sale</span>
                <span>Bán hàng (POS)</span>
            </a>
        </c:if>

        <!-- Cashbook (Sổ Quỹ) (Admin, Owner, StoreManager) -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
            <a href="${pageContext.request.contextPath}/cashbook" 
               class="sidebar-menu-item ${originalUri.contains('/cashbook') ? 'active' : ''}">
                <span class="material-icons">account_balance_wallet</span>
                <span>Sổ Quỹ</span>
            </a>
        </c:if>

        <!-- Suppliers / Partners -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
            <a href="${pageContext.request.contextPath}/suppliers" 
               class="sidebar-menu-item ${pageContext.request.requestURI.contains('/suppliers') ? 'active' : ''}">
                <span class="material-icons">handshake</span>
                <span>Đối tác</span>
            </a>
        </c:if>

        <!-- User / Role Manager (Dũng's User Management) -->
        <c:if test="${roleName == 'Admin'}">
            <a href="${pageContext.request.contextPath}/admin/user" 
               class="sidebar-menu-item ${originalUri.contains('/admin/user') ? 'active' : ''}">
                <span class="material-icons">manage_accounts</span>
                <span>Quản lý Owner</span>
            </a>
        </c:if>
        <c:if test="${roleName == 'Owner'}">
            <a href="${pageContext.request.contextPath}/owner/emp" 
               class="sidebar-menu-item ${originalUri.contains('/owner/emp') ? 'active' : ''}">
                <span class="material-icons">badge</span>
                <span>Nhân viên</span>
            </a>
        </c:if>
        <c:if test="${roleName == 'StoreManager'}">
            <a href="${pageContext.request.contextPath}/manager/emp" 
               class="sidebar-menu-item ${originalUri.contains('/manager/emp') ? 'active' : ''}">
                <span class="material-icons">badge</span>
                <span>Nhân viên chi nhánh</span>
            </a>
        </c:if>

        <!-- Reports -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
            <a href="${pageContext.request.contextPath}/reports/export" 
               class="sidebar-menu-item ${pageContext.request.requestURI.contains('/reports/') ? 'active' : ''}">
                <span class="material-icons">bar_chart</span>
                <span>Báo cáo</span>
            </a>
        </c:if>

        <!-- Configuration -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner'}">
            <div class="sidebar-menu-title" style="margin-top: 16px;">Hệ thống</div>
            <c:if test="${roleName == 'Owner'}">
                <a href="${pageContext.request.contextPath}/activity-log"
                   class="sidebar-menu-item ${originalUri.contains('/activity-log') ? 'active' : ''}">
                    <span class="material-icons">history</span>
                    <span>Activity Center</span>
                </a>
            </c:if>
            <a href="${pageContext.request.contextPath}/configuration/business" 
               class="sidebar-menu-item ${pageContext.request.requestURI.contains('/configuration/') ? 'active' : ''}">
                <span class="material-icons">settings</span>
                <span>Cấu hình</span>
            </a>
        </c:if>
    </nav>

    <!-- User Profile block at bottom -->
    <div class="sidebar-user">
        <div class="sidebar-user-info">
            <div class="sidebar-user-avatar">
                ${fn:toUpperCase(fn:substring(fullName, 0, 2))}
            </div>
            <div class="sidebar-user-details">
                <h6><c:out value="${fullName}" /></h6>
                <small><c:out value="${roleName}" /></small>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/profile" class="sidebar-user-logout" title="Hồ sơ">
            <span class="material-icons">person</span>
        </a>
        <a href="${pageContext.request.contextPath}/logout" class="sidebar-user-logout" title="Đăng xuất">
            <span class="material-icons">logout</span>
        </a>
    </div>
</aside>
