<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : 'Nhân viên'}" />
<c:set var="fullName" value="${sessionScope.currentUser.fullName != null ? sessionScope.currentUser.fullName : 'Lê Minh Quân'}" />
<c:set var="originalUri" value="${requestScope['jakarta.servlet.forward.request_uri'] != null ? requestScope['jakarta.servlet.forward.request_uri'] : pageContext.request.requestURI}" />


<div id="comingSoonToast">
    <span class="material-icons">construction</span>
    <span>Chức năng đang hoàn thiện</span>
</div>

<aside class="sidebar" id="posSidebar">
    <div class="sidebar-brand">
        <div class="sidebar-brand-icon"><span class="material-icons">store</span></div>
        <div class="sidebar-brand-text"><h4>FINORA</h4><small>Máy bán hàng POS</small></div>
    </div>
    <nav class="sidebar-menu">
        <div class="sidebar-menu-title">CHỨC NĂNG CHÍNH</div>

        <!-- Bán hàng (POS) -->
        <a href="${pageContext.request.contextPath}/sales" class="sidebar-menu-item ${originalUri.contains('/sales') ? 'active' : ''}">
            <span class="material-icons">storefront</span><span>Bán hàng (POS)</span>
        </a>

        <!-- Lịch sử đơn hàng -->
        <a href="${pageContext.request.contextPath}/orders" class="sidebar-menu-item ${originalUri.contains('/orders') ? 'active' : ''}">
            <span class="material-icons">receipt_long</span><span>Lịch sử đơn hàng</span>
        </a>

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

        <!-- Danh sách sản phẩm -->
        <a href="${pageContext.request.contextPath}/products" class="sidebar-menu-item ${originalUri.contains('/products') ? 'active' : ''}">
            <span class="material-icons">shopping_bag</span><span>Danh sách sản phẩm</span>
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
            <c:if test="${roleName != 'Admin'}">
            <a href="${pageContext.request.contextPath}/customers" class="sidebar-submenu-item ${originalUri.contains('/customers') ? 'active' : ''}">
                <span>Khách hàng</span>
            </a>
            </c:if>
            <a href="${pageContext.request.contextPath}/suppliers" class="sidebar-submenu-item ${originalUri.contains('/suppliers') ? 'active' : ''}">
                <span>Nhà cung cấp</span>
            </a>
        </div>

        <!-- Chi nhánh (Admin, Owner) -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner'}">
            <a href="${pageContext.request.contextPath}/branch" class="sidebar-menu-item ${originalUri.contains('/branch') ? 'active' : ''}">
                <span class="material-icons">store</span><span>Chi nhánh</span>
            </a>
        </c:if>

        <!-- Nhân viên (Admin, Owner, StoreManager) -->
        <c:if test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager'}">
            <c:choose>
                <c:when test="${roleName == 'Admin'}">
                    <a href="${pageContext.request.contextPath}/admin/user" class="sidebar-menu-item ${originalUri.contains('/admin/user') ? 'active' : ''}">
                        <span class="material-icons">badge</span><span>Nhân viên</span>
                    </a>
                </c:when>
                <c:when test="${roleName == 'Owner'}">
                    <a href="${pageContext.request.contextPath}/owner/emp" class="sidebar-menu-item ${originalUri.contains('/owner/emp') ? 'active' : ''}">
                        <span class="material-icons">badge</span><span>Nhân viên</span>
                    </a>
                </c:when>
                <c:when test="${roleName == 'StoreManager'}">
                    <a href="${pageContext.request.contextPath}/manager/emp" class="sidebar-menu-item ${originalUri.contains('/manager/emp') ? 'active' : ''}">
                        <span class="material-icons">badge</span><span>Nhân viên</span>
                    </a>
                </c:when>
            </c:choose>
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

    function showComingSoon(event) {
        if (event) event.preventDefault();
        const toast = document.getElementById('comingSoonToast');
        toast.classList.add('show');
        setTimeout(() => {
            toast.classList.remove('show');
        }, 2500);
    }
</script>