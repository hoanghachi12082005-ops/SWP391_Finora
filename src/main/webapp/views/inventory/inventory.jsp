<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Quản Lý Kho Hàng"/>
    <jsp:param name="additionalCSS" value="inventory.css?v=5"/>
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
                    
                    <div class="d-flex align-items-center justify-content-between mb-4">
                        <div class="d-flex align-items-center">
                            <c:if test="${fn:length(warehouses) > 1}">
                                <button type="button" class="btn btn-light rounded-circle me-3" onclick="window.location.href='?tab=stock&clearSelected=true'" style="width: 42px; height: 42px; display: flex; align-items: center; justify-content: center; border: 1px solid #e5e7eb; background: #ffffff; transition: all 0.2s; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
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
                        <c:if test="${activeTab == 'stock'}">
                            <button type="button" class="page-action-btn" data-bs-toggle="modal" data-bs-target="#importStockModal">
                                <span class="material-icons" style="font-size: 20px;">add_circle_outline</span>
                                <span>Nhập Hàng</span>
                            </button>
                        </c:if>
                    </div>

                    <style>
                        .subtab-nav { border-bottom: 1px solid #e2e8f0; margin-bottom: 20px; display: flex; gap: 20px; }
                        .subtab-link { padding: 10px 16px; color: #64748b; text-decoration: none; font-weight: 500; border-bottom: 2px solid transparent; transition: all 0.2s; }
                        .subtab-link:hover { color: var(--primary-color); }
                        .subtab-link.active { color: var(--primary-color); border-bottom-color: var(--primary-color); }
                    </style>

                    <c:if test="${roleName == 'Owner' || roleName == 'StoreManager' || roleName == 'WarehouseStaff'}">
                        <div class="subtab-nav mb-4">
                            <a href="${pageContext.request.contextPath}/inventory?tab=stock&warehouseId=${selectedWarehouseId}"
                                class="subtab-link ${empty activeTab || activeTab == 'stock' ? 'active' : ''}">
                                Tồn Kho
                            </a>

                            <a href="${pageContext.request.contextPath}/inventory?tab=transfer&warehouseId=${selectedWarehouseId}"
                                class="subtab-link ${activeTab == 'transfer' || activeTab == 'createTransfer' ? 'active' : ''}">
                                Điều Chuyển
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=check&warehouseId=${selectedWarehouseId}"
                                class="subtab-link ${activeTab == 'check' ? 'active' : ''}">
                                Kiểm kho
                            </a>
                            <a href="${pageContext.request.contextPath}/inventory?tab=history&warehouseId=${selectedWarehouseId}"
                                class="subtab-link ${activeTab == 'history' ? 'active' : ''}">
                                Lịch sử xuất nhập
                            </a>
                        </div>
                    </c:if>

                    <!-- KPI Cards for specific warehouse -->
                    <c:if test="${activeTab == 'stock'}">
                        <div class="kpi-grid mb-4">
                            <div class="kpi-card">
                                <div class="kpi-card-info">
                                    <p>Tổng Sản Phẩm</p>
                                    <h3>${totalProducts != null ? totalProducts : 0}</h3>
                                    <span class="kpi-subtext">Thuộc ${totalCategories != null ? totalCategories : 0} danh mục</span>
                                </div>
                                <div class="kpi-card-icon green">
                                    <span class="material-icons">category</span>
                                </div>
                            </div>

                            <div class="kpi-card">
                                <div class="kpi-card-info">
                                    <p>Sắp Hết Hàng</p>
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
                                    <p>Phiếu Chờ Xử Lý</p>
                                    <h3 style="color: #f59e0b;">${pendingTransferCount != null ? pendingTransferCount : 0}</h3>
                                    <span class="kpi-subtext" style="color: #d97706;">Điều chuyển cần duyệt</span>
                                </div>
                                <div class="kpi-card-icon" style="background: rgba(245,158,11,0.1); color: #f59e0b;">
                                    <span class="material-icons">pending_actions</span>
                                </div>
                            </div>
                        </div>
                    </c:if>



                    <!-- Tab Content -->
                    <div class="tab-content">
                        <c:choose>
                            <c:when test="${activeTab == 'stock'}">
                                <jsp:include page="_tab_stock.jsp" />
                            </c:when>
                            <c:when test="${activeTab == 'import'}">
                                <jsp:include page="_tab_import.jsp" />
                            </c:when>
                            <c:when test="${activeTab == 'export'}">
                                <jsp:include page="_tab_export.jsp" />
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
                            <c:when test="${activeTab == 'approval'}">
                                <jsp:include page="_tab_approval.jsp" />
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

<jsp:include page="_modal_create_import.jsp" />
<jsp:include page="_modal_create_export.jsp" />

<!-- Edit Warehouse Modal -->
<div class="modal fade" id="editWarehouseModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0" style="border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
            <form action="${pageContext.request.contextPath}/inventory" method="POST">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
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
                    <button type="submit" class="btn inventory-btn-primary" style="border-radius: 8px; font-weight: 500;">Lưu thay đổi</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal for Ticket Details -->
<div class="modal fade" id="ticketDetailsModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content" id="ticketDetailsModalContent">
            <div class="modal-body text-center py-5">
                <div class="spinner-border text-primary" role="status"></div>
                <p class="mt-2 text-muted">Đang tải dữ liệu...</p>
            </div>
        </div>
    </div>
</div>

<!-- Modal Reject Dispatch -->
<div class="modal fade" id="rejectModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0" style="border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
            <form action="${pageContext.request.contextPath}/inventory" method="POST">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="action" value="rejectDispatch">
                <input type="hidden" name="transferId" id="rejectTransferId">
                <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                
                <div class="modal-header border-bottom-0 pb-0">
                    <h5 class="modal-title fw-bold text-danger">Từ chối Xuất Kho</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body pt-3">
                    <div class="mb-3">
                        <label class="form-label fw-semibold text-muted small">Lý do từ chối <span class="text-danger">*</span></label>
                        <textarea name="note" class="form-control" rows="3" required placeholder="Nhập lý do không xuất hàng (VD: hàng hỏng, đếm thiếu...)" style="border-radius: 8px;"></textarea>
                    </div>
                </div>
                <div class="modal-footer border-top-0 pt-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal" style="border-radius: 8px; font-weight: 500;">Hủy</button>
                    <button type="submit" class="btn btn-danger" style="border-radius: 8px; font-weight: 500;">Xác nhận Từ chối</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Receipt with Discrepancy -->
<div class="modal fade" id="receiptModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content" id="receiptModalContent">
            <div class="modal-body text-center py-5">
                <div class="spinner-border text-primary" role="status"></div>
                <p class="mt-2 text-muted">Đang tải dữ liệu phiếu...</p>
            </div>
        </div>
    </div>
</div>

<script>
    function viewTicketDetails(ticketId) {
        const modalContent = document.getElementById('ticketDetailsModalContent');
        modalContent.innerHTML = '<div class="modal-body text-center py-5"><div class="spinner-border text-primary" role="status"></div><p class="mt-2 text-muted">Đang tải dữ liệu...</p></div>';
        
        const myModal = new bootstrap.Modal(document.getElementById('ticketDetailsModal'));
        myModal.show();
        
        const currentWarehouseId = '${selectedWarehouseId}';
        let url = '${pageContext.request.contextPath}/inventory?action=viewTicket&ticketId=' + ticketId;
        if (currentWarehouseId) {
            url += '&warehouseId=' + currentWarehouseId;
        }
        
        fetch(url)
            .then(response => response.text())
            .then(html => {
                modalContent.innerHTML = html;
            })
            .catch(err => {
                modalContent.innerHTML = '<div class="modal-body py-5 text-center text-danger"><i class="ph ph-warning-circle fs-1 mb-2"></i><p>Lỗi khi tải dữ liệu phiếu.</p></div>';
            });
    }

    function viewOrderDetails(orderId) {
        const modalContent = document.getElementById('ticketDetailsModalContent');
        modalContent.innerHTML = '<div class="modal-body text-center py-5"><div class="spinner-border text-primary" role="status"></div><p class="mt-2 text-muted">Đang tải dữ liệu...</p></div>';
        
        const myModal = new bootstrap.Modal(document.getElementById('ticketDetailsModal'));
        myModal.show();
        
        let url = '${pageContext.request.contextPath}/inventory?action=viewOrderDetails&orderId=' + orderId;
        
        fetch(url)
            .then(response => response.text())
            .then(html => {
                modalContent.innerHTML = html;
            })
            .catch(err => {
                modalContent.innerHTML = '<div class="modal-body py-5 text-center text-danger"><i class="ph ph-warning-circle fs-1 mb-2"></i><p>Lỗi khi tải dữ liệu phiếu.</p></div>';
            });
    }

    function openRejectModal(ticketId) {
        document.getElementById('rejectTransferId').value = ticketId;
        const myModal = new bootstrap.Modal(document.getElementById('rejectModal'));
        myModal.show();
    }

    function openReceiptModal(ticketId) {
        const modalContent = document.getElementById('receiptModalContent');
        modalContent.innerHTML = '<div class="modal-body text-center py-5"><div class="spinner-border text-primary" role="status"></div><p class="mt-2 text-muted">Đang tải dữ liệu...</p></div>';
        
        const myModal = new bootstrap.Modal(document.getElementById('receiptModal'));
        myModal.show();
        
        // Fetch the receipt form view
        fetch('${pageContext.request.contextPath}/inventory?action=viewReceiptForm&ticketId=' + ticketId + '&warehouseId=${selectedWarehouseId}')
            .then(response => response.text())
            .then(html => {
                modalContent.innerHTML = html;
            })
            .catch(err => {
                modalContent.innerHTML = '<div class="modal-body py-5 text-center text-danger"><i class="ph ph-warning-circle fs-1 mb-2"></i><p>Lỗi khi tải dữ liệu phiếu.</p></div>';
            });
    }
</script>

<jsp:include page="/views/common/footer.jsp" />
