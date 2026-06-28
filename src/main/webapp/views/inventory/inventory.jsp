<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Quản Lý Kho Hàng"/>
    <jsp:param name="additionalCSS" value="inventory.css?v=3"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />
    
    <div class="main-content">
        <jsp:include page="/views/common/topbar.jsp" />
        
        <div class="page-container">

            <!-- Global alerts are now handled beautifully via SweetAlert in footer.jsp -->

            <c:choose>
                <c:when test="${empty selectedWarehouseId}">
                    <!-- DASHBOARD CHUNG -->
                    <div class="kpi-grid mb-4">
                        <div class="kpi-card">
                            <div class="kpi-card-info">
                                <p>Số lượng kho</p>
                                <h3>${fn:length(warehouses)}</h3>
                                <span class="kpi-subtext">Kho đang quản lý</span>
                            </div>
                            <div class="kpi-card-icon blue">
                                <span class="material-icons">storefront</span>
                            </div>
                        </div>

                        <div class="kpi-card">
                            <div class="kpi-card-info">
                                <p>Tổng mặt hàng</p>
                                <h3>${totalProducts != null ? totalProducts : 0}</h3>
                                <span class="kpi-subtext">Thuộc ${totalCategories != null ? totalCategories : 0} danh mục</span>
                            </div>
                            <div class="kpi-card-icon green">
                                <span class="material-icons">category</span>
                            </div>
                        </div>
                    </div>

                    <!-- Tab Navigation for Dashboard -->
                    <div class="tab-nav mb-4">
                        <a href="?tab=stock" class="tab-btn ${activeTab == 'stock' ? 'active' : ''}">Danh sách Kho</a>
                        <a href="?tab=history" class="tab-btn ${activeTab == 'history' ? 'active' : ''}">Lịch sử xuất nhập kho</a>
                    </div>

                    <div class="tab-content">
                        <c:choose>
                            <c:when test="${activeTab == 'stock'}">
                                <!-- Include _tab_stock to render the Warehouse Cards -->
                                <jsp:include page="_tab_stock.jsp" />
                            </c:when>
                            <c:when test="${activeTab == 'history'}">
                                <jsp:include page="_tab_history.jsp" />
                            </c:when>
                        </c:choose>
                    </div>
                </c:when>

                <c:otherwise>
                    <!-- CHI TIẾT KHO -->
                    
                    <div class="d-flex align-items-center mb-4">
                        <c:if test="${fn:length(warehouses) > 1}">
                            <button type="button" class="btn btn-light rounded-circle me-3" onclick="window.location.href='?tab=stock'" style="width: 42px; height: 42px; display: flex; align-items: center; justify-content: center; border: 1px solid #e5e7eb; background: #ffffff; transition: all 0.2s; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
                                <span class="material-icons">arrow_back</span>
                            </button>
                        </c:if>
                        <div class="flex-grow-1 d-flex align-items-center gap-2">
                            <c:set var="selectedWarehouseName" value="Chi tiết kho" />
                            <c:set var="selectedWarehouseAddress" value="" />
                            <c:forEach var="w" items="${warehouses}">
                                <c:if test="${w.warehouseId == selectedWarehouseId}">
                                    <c:set var="selectedWarehouseName" value="${w.warehouseName}" />
                                    <c:set var="selectedWarehouseAddress" value="${w.address}" />
                                </c:if>
                            </c:forEach>
                            <div>
                                <div class="d-flex align-items-center gap-2">
                                    <h4 class="mb-0" style="font-weight: 700; color: #111827;">${selectedWarehouseName}</h4>
                                    <button class="btn btn-sm btn-light rounded-circle" data-bs-toggle="modal" data-bs-target="#editWarehouseModal" title="Sửa thông tin kho" style="width: 32px; height: 32px; padding: 0; display: flex; align-items: center; justify-content: center;">
                                        <span class="material-icons" style="font-size: 16px; color: #6b7280;">edit</span>
                                    </button>
                                </div>
                                <small class="text-muted">Quản lý toàn bộ thông tin và giao dịch của kho này</small>
                            </div>
                        </div>
                    </div>

                    <!-- KPI Cards for specific warehouse -->
                    <c:if test="${activeTab == 'stock'}">
                        <div class="kpi-grid mb-4">
                            <div class="kpi-card">
                                <div class="kpi-card-info">
                                    <p>Tổng mặt hàng</p>
                                    <h3>${totalProducts != null ? totalProducts : 0}</h3>
                                    <span class="kpi-subtext">Thuộc ${totalCategories != null ? totalCategories : 0} danh mục</span>
                                </div>
                                <div class="kpi-card-icon green">
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

                            <div class="kpi-card" style="cursor: pointer;" onclick="window.location.href='?tab=transfer&warehouseId=${selectedWarehouseId}'">
                                <div class="kpi-card-info">
                                    <p>Đơn cần duyệt</p>
                                    <h3 style="color: #f59e0b;">${pendingTransferCount != null ? pendingTransferCount : 0}</h3>
                                    <span class="kpi-subtext" style="color: #d97706;">Đang chờ xử lý</span>
                                </div>
                                <div class="kpi-card-icon" style="background: rgba(245,158,11,0.1); color: #f59e0b;">
                                    <span class="material-icons">pending_actions</span>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <!-- Tab Navigation -->
                    <div class="tab-nav mb-4">
                        <a href="?tab=stock&warehouseId=${selectedWarehouseId}" class="tab-btn ${activeTab == 'stock' ? 'active' : ''}">Tồn kho</a>
                        <a href="?tab=transfer&warehouseId=${selectedWarehouseId}" class="tab-btn ${activeTab == 'transfer' || activeTab == 'createTransfer' ? 'active' : ''}">Chuyển kho</a>
                        <a href="?tab=check&warehouseId=${selectedWarehouseId}" class="tab-btn ${activeTab == 'check' ? 'active' : ''}">Kiểm kho</a>
                        <a href="?tab=history&warehouseId=${selectedWarehouseId}" class="tab-btn ${activeTab == 'history' ? 'active' : ''}">Lịch sử xuất nhập kho</a>
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
                            <c:when test="${activeTab == 'createTransfer'}">
                                <jsp:include page="_tab_transfer_create.jsp" />
                            </c:when>
                            <c:when test="${activeTab == 'check'}">
                                <jsp:include page="_tab_check.jsp" />
                            </c:when>
                            <c:when test="${activeTab == 'history'}">
                                <jsp:include page="_tab_history.jsp" />
                            </c:when>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>
            
        </div>
    </div>
</div>

<!-- Edit Warehouse Modal -->
<div class="modal fade" id="editWarehouseModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0" style="border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
            <form action="${pageContext.request.contextPath}/inventory" method="POST">
                <input type="hidden" name="action" value="updateWarehouse">
                <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                
                <div class="modal-header border-bottom-0 pb-0">
                    <h5 class="modal-title fw-bold" style="color: #111827;">Sửa thông tin Kho</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body pt-3">
                    <div class="mb-3">
                        <label class="form-label fw-semibold text-muted small">Tên Kho Hàng <span class="text-danger">*</span></label>
                        <input type="text" name="warehouseName" class="form-control" required 
                               value="${selectedWarehouseName}" style="border-radius: 8px;">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold text-muted small">Địa Chỉ Kho</label>
                        <input type="text" name="address" class="form-control" 
                               value="${selectedWarehouseAddress}" style="border-radius: 8px;">
                    </div>
                </div>
                <div class="modal-footer border-top-0 pt-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal" style="border-radius: 8px; font-weight: 500;">Hủy</button>
                    <button type="submit" class="btn btn-danger" style="border-radius: 8px; font-weight: 500;">Lưu thay đổi</button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/views/common/footer.jsp" />
