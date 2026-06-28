<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Quản Lý Kho Hàng"/>
    <jsp:param name="additionalCSS" value="inventory.css"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />
    
    <div class="main-content">
        <jsp:include page="/views/common/topbar.jsp" />
        
        <div class="page-container">
            <!-- Breadcrumbs -->
            <div class="page-breadcrumb">
                <a href="${pageContext.request.contextPath}/dashboard/owner">Dashboard</a>
                <span class="material-icons">chevron_right</span>
                <span>Kho hàng</span>
            </div>

            <!-- Page Header -->
            <div class="page-header d-flex justify-content-between align-items-center">
                <div class="page-title">
                    <h2>Quản lý kho hàng</h2>
                    <p>Kiểm tra tồn kho thời gian thực, nhập kho, xuất kho và kiểm kê</p>
                </div>
            </div>

            <c:if test="${not empty sessionScope.message}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    ${sessionScope.message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <c:remove var="message" scope="session" />
            </c:if>
            <c:if test="${not empty sessionScope.error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    ${sessionScope.error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <c:remove var="error" scope="session" />
            </c:if>

            <!-- KPI Cards Grid (Render only on stock tab for simplicity, or keep global) -->
            <c:if test="${activeTab == 'stock'}">
                <div class="kpi-grid mb-4">
                    <div class="kpi-card">
                        <div class="kpi-card-info">
                            <p>Tổng mặt hàng</p>
                            <h3>${totalItems != null ? totalItems : 0}</h3>
                            <span class="kpi-subtext">Danh mục sản phẩm</span>
                        </div>
                        <div class="kpi-card-icon blue">
                            <span class="material-icons">category</span>
                        </div>
                    </div>

                    <div class="kpi-card">
                        <div class="kpi-card-info">
                            <p>Sắp hết hàng</p>
                            <h3 style="color: var(--danger-color);">${lowStockCount != null ? lowStockCount : 0}</h3>
                            <span class="kpi-trend down">
                                <span class="material-icons" style="font-size: 14px;">warning</span>
                                <span>Cần nhập gấp</span>
                            </span>
                        </div>
                        <div class="kpi-card-icon red">
                            <span class="material-icons">notification_important</span>
                        </div>
                    </div>
                </div>
            </c:if>

            <!-- Tab Navigation -->
            <div class="tab-nav mb-4">
                <a href="?tab=stock${selectedWarehouseId != null ? '&warehouseId='.concat(selectedWarehouseId) : ''}" class="tab-btn ${activeTab == 'stock' ? 'active' : ''}">Tồn kho</a>
                <a href="?tab=transfer${selectedWarehouseId != null ? '&warehouseId='.concat(selectedWarehouseId) : ''}" class="tab-btn ${activeTab == 'transfer' ? 'active' : ''}">Chuyển kho</a>
                <a href="?tab=check${selectedWarehouseId != null ? '&warehouseId='.concat(selectedWarehouseId) : ''}" class="tab-btn ${activeTab == 'check' ? 'active' : ''}">Kiểm kho</a>
                <a href="?tab=history${selectedWarehouseId != null ? '&warehouseId='.concat(selectedWarehouseId) : ''}" class="tab-btn ${activeTab == 'history' ? 'active' : ''}">Lịch sử XNK</a>
            </div>

            <!-- Tab Content -->
            <div class="tab-content">
                <c:choose>
                    <c:when test="${activeTab == 'stock'}">
                        <jsp:include page="_tab_stock.jsp" />
                    </c:when>
                    <c:when test="${activeTab == 'transfer'}">
                        <jsp:include page="_tab_transfer.jsp" />
                    </c:when>
                    <c:when test="${activeTab == 'check'}">
                        <jsp:include page="_tab_check.jsp" />
                    </c:when>
                    <c:when test="${activeTab == 'history'}">
                        <jsp:include page="_tab_history.jsp" />
                    </c:when>
                </c:choose>
            </div>
            
        </div>
    </div>
</div>

<jsp:include page="/views/common/footer.jsp" />
