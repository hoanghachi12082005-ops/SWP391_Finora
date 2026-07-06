<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : 'Nhân viên'}" />
<c:set var="fullName" value="${sessionScope.currentUser.fullName != null ? sessionScope.currentUser.fullName : 'Lê Minh Quân'}" />
<c:set var="originalUri" value="${requestScope['jakarta.servlet.forward.request_uri'] != null ? requestScope['jakarta.servlet.forward.request_uri'] : pageContext.request.requestURI}" />

<style>
    .sidebar-menu-item-dropdown {
        display: flex;
        align-items: center;
        justify-content: space-between;
        cursor: pointer;
    }
    .sidebar-dropdown-arrow {
        transition: transform 0.2s ease;
        font-size: 20px;
        margin-left: auto;
    }
    .sidebar-menu-item-dropdown.open .sidebar-dropdown-arrow {
        transform: rotate(180deg);
    }
    .sidebar-submenu {
        max-height: 0;
        overflow: hidden;
        transition: max-height 0.3s ease;
        padding-left: 12px;
    }
    .sidebar-submenu.open {
        max-height: 300px;
    }
    .sidebar-submenu-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 8px 12px 8px 24px;
        border-radius: 8px;
        color: #475569;
        font-weight: 500;
        font-size: 13px;
        margin-bottom: 2px;
        transition: all 0.2s ease;
        text-decoration: none;
    }
    .sidebar-submenu-item:hover {
        background-color: #f1f5f9;
        color: #1e293b;
    }
    .sidebar-submenu-item.active {
        background-color: var(--primary-light, rgba(147, 0, 11, 0.06));
        color: var(--primary-color, #93000b);
        font-weight: 600;
    }
</style>

<aside class="sidebar" id="posSidebar">
    <div class="sidebar-brand">
        <div class="sidebar-brand-icon"><span class="material-icons">store</span></div>
        <div class="sidebar-brand-text"><h4>FINORA</h4><small>Máy bán hàng POS</small></div>
    </div>
    <nav class="sidebar-menu">
        <div class="sidebar-menu-title">CHỨC NĂNG CHÍNH</div>

        <!-- Giao dịch Dropdown -->
        <c:set var="isGiaoDichActive" value="${originalUri.contains('/sales') || originalUri.contains('/orders') || originalUri.contains('/import')}" />
        <div class="sidebar-menu-item sidebar-menu-item-dropdown ${isGiaoDichActive ? 'open' : ''}" onclick="toggleDropdown(this)">
            <div style="display: flex; align-items: center; gap: 12px;">
                <span class="material-icons">storefront</span>
                <span>Giao dịch</span>
            </div>
            <span class="material-icons sidebar-dropdown-arrow">expand_more</span>
        </div>
        <div class="sidebar-submenu ${isGiaoDichActive ? 'open' : ''}">
            <a href="${pageContext.request.contextPath}/sales" class="sidebar-submenu-item ${originalUri.contains('/sales') ? 'active' : ''}">
                <span>Bán hàng (POS)</span>
            </a>
            <a href="${pageContext.request.contextPath}/orders" class="sidebar-submenu-item ${originalUri.contains('/orders') ? 'active' : ''}">
                <span>Lịch sử đơn hàng</span>
            </a>
            <a href="${pageContext.request.contextPath}/warehouse/import" class="sidebar-submenu-item ${originalUri.contains('/import') ? 'active' : ''}">
                <span>Nhập hàng</span>
            </a>
        </div>

        <!-- Ca làm việc -->
        <a href="${pageContext.request.contextPath}/shift" class="sidebar-menu-item ${originalUri.contains('/shift') ? 'active' : ''}">
            <span class="material-icons">schedule</span><span>Ca làm việc</span>
        </a>

        <!-- Sổ Quỹ (Admin, Owner, StoreManager) -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
            <a href="${pageContext.request.contextPath}/cashbook" class="sidebar-menu-item ${originalUri.contains('/cashbook') ? 'active' : ''}">
                <span class="material-icons">account_balance_wallet</span><span>Sổ Quỹ</span>
            </a>
        </c:if>

        <!-- Kho hàng -->
        <a href="${pageContext.request.contextPath}/warehouse" class="sidebar-menu-item ${originalUri.contains('/warehouse') ? 'active' : ''}">
            <span class="material-icons">warehouse</span><span>Kho hàng</span>
        </a>

        <!-- Hàng hóa Dropdown -->
        <c:set var="isHangHoaActive" value="${originalUri.contains('/products') || originalUri.contains('/category')}" />
        <div class="sidebar-menu-item sidebar-menu-item-dropdown ${isHangHoaActive ? 'open' : ''}" onclick="toggleDropdown(this)">
            <div style="display: flex; align-items: center; gap: 12px;">
                <span class="material-icons">shopping_bag</span>
                <span>Hàng hóa</span>
            </div>
            <span class="material-icons sidebar-dropdown-arrow">expand_more</span>
        </div>
        <div class="sidebar-submenu ${isHangHoaActive ? 'open' : ''}">
            <a href="${pageContext.request.contextPath}/products" class="sidebar-submenu-item ${originalUri.endsWith('/products') ? 'active' : ''}">
                <span>Danh sách sản phẩm</span>
            </a>
            <a href="${pageContext.request.contextPath}/products/categories" class="sidebar-submenu-item ${originalUri.contains('/products/categories') || originalUri.contains('/category') ? 'active' : ''}">
                <span>Danh mục sản phẩm</span>
            </a>
            <a href="${pageContext.request.contextPath}/products/units" class="sidebar-submenu-item ${originalUri.contains('/products/units') ? 'active' : ''}">
                <span>Đơn vị tính</span>
            </a>
        </div>

        <!-- Đối tác Dropdown -->
        <c:set var="isDoiTacActive" value="${originalUri.contains('/customers') || originalUri.contains('/suppliers')}" />
        <div class="sidebar-menu-item sidebar-menu-item-dropdown ${isDoiTacActive ? 'open' : ''}" onclick="toggleDropdown(this)">
            <div style="display: flex; align-items: center; gap: 12px;">
                <span class="material-icons">handshake</span>
                <span>Đối tác</span>
            </div>
            <span class="material-icons sidebar-dropdown-arrow">expand_more</span>
        </div>
        <div class="sidebar-submenu ${isDoiTacActive ? 'open' : ''}">
            <a href="${pageContext.request.contextPath}/customers" class="sidebar-submenu-item ${originalUri.contains('/customers') ? 'active' : ''}">
                <span>Khách hàng</span>
            </a>
            <a href="${pageContext.request.contextPath}/suppliers" class="sidebar-submenu-item ${originalUri.contains('/suppliers') ? 'active' : ''}">
                <span>Nhà cung cấp</span>
            </a>
        </div>

        <!-- Chi nhánh (Admin, Owner) -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner'}">
            <a href="${pageContext.request.contextPath}/branches" class="sidebar-menu-item ${originalUri.contains('/branches') || originalUri.contains('/branch') ? 'active' : ''}">
                <span class="material-icons">store</span><span>Chi nhánh</span>
            </a>
        </c:if>

        <!-- Nhân viên (Admin, Owner, StoreManager) -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
            <a href="${pageContext.request.contextPath}/employees" class="sidebar-menu-item ${originalUri.contains('/employees') || originalUri.contains('/emp') || originalUri.contains('/user') ? 'active' : ''}">
                <span class="material-icons">badge</span><span>Nhân viên</span>
            </a>
        </c:if>

        <!-- Báo cáo doanh thu (Admin, Owner, StoreManager) -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
            <a href="${pageContext.request.contextPath}/revenue" class="sidebar-menu-item ${originalUri.contains('/revenue') ? 'active' : ''}">
                <span class="material-icons">trending_up</span><span>Báo cáo doanh thu</span>
            </a>
        </c:if>
  

    </nav>
    <div class="sidebar-user">
        <div class="sidebar-user-info">
            <div class="sidebar-user-avatar">${fn:toUpperCase(fn:substring(fullName, 0, 2))}</div>
            <div class="sidebar-user-details"><h6><c:out value="${fullName}" /></h6><small><c:out value="${roleName}" /></small></div>
        </div>
        <a href="${pageContext.request.contextPath}/profile" class="sidebar-user-logout" title="Hồ sơ"><span class="material-icons">person</span></a>
        <a href="${pageContext.request.contextPath}/logout" class="sidebar-user-logout" title="Đăng xuất"><span class="material-icons">logout</span></a>
    </div>
</aside>

<script>
    function toggleDropdown(el) {
        el.classList.toggle('open');
        const submenu = el.nextElementSibling;
        if (submenu && submenu.classList.contains('sidebar-submenu')) {
            submenu.classList.toggle('open');
        }
    }
</script>