<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : 'Nhân viên'}" />

<header class="top-header">
    <!-- Search Bar -->
    <div class="header-search">
        <span class="material-icons">search</span>
        <input type="text" placeholder="Tìm kiếm cửa hàng, mã số, hàng hóa...">
    </div>

    <!-- Actions -->
    <div class="header-actions">
        <!-- Notification Button -->
        <button class="header-icon-btn" title="Thông báo">
            <span class="material-icons">notifications</span>
            <span class="badge-dot"></span>
        </button>

        <!-- Help Button -->
        <button class="header-icon-btn" title="Trợ giúp">
            <span class="material-icons">help_outline</span>
        </button>

        <!-- Settings Button -->
        <button class="header-icon-btn" title="Cài đặt">
            <span class="material-icons">settings</span>
        </button>

        <!-- Action Button based on Role -->
        <c:choose>
            <c:when test="${roleName == 'Admin' || roleName == 'Owner' || roleName == 'StoreManager' || roleName == 'SalesStaff'}">
                <a href="${pageContext.request.contextPath}/sales" class="btn-sales-primary">
                    <span class="material-icons">shopping_cart</span>
                    <span>Bán hàng</span>
                </a>
            </c:when>
            <c:when test="${roleName == 'WarehouseStaff'}">
                <a href="${pageContext.request.contextPath}/inventory/import" class="btn-sales-primary">
                    <span class="material-icons">add</span>
                    <span>Nhập kho</span>
                </a>
            </c:when>
        </c:choose>
    </div>
</header>
