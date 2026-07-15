<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="selectedWarehouseName" value="Kho hiện tại" />
<c:forEach var="w" items="${warehouses}">
    <c:if test="${w.warehouseId == selectedWarehouseId}">
        <c:set var="selectedWarehouseName" value="${w.warehouseName}" />
    </c:if>
</c:forEach>



<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-4 border-bottom-0 pb-0">
        <div>
            <h5 class="mb-0 fw-bold" style="color: #93000b;">📋 Tạo Phiếu Điều Chuyển Kho</h5>
            <small class="text-muted">Kho hiện tại: <strong>${selectedWarehouseName}</strong></small>
        </div>
        <a href="${pageContext.request.contextPath}/inventory?tab=transfer&warehouseId=${selectedWarehouseId}" class="btn btn-outline-secondary btn-sm" style="border-radius: 8px;">
            <span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">arrow_back</span>
            Quay Lại
        </a>
    </div>

    <div class="card-body">
        <!-- Search Section -->
        <div class="search-box">
            <span class="material-icons search-icon">search</span>
            <input type="text" class="search-input" id="productSearch" placeholder="Tìm kiếm sản phẩm hoặc chi nhánh..." autocomplete="off">
            <div class="search-results" id="searchResults">
                <!-- JS Populated -->
            </div>
        </div>

        <!-- Form Section -->
        <form action="${pageContext.request.contextPath}/inventory" method="POST" id="transferForm">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="saveTransfer">
            <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">

            <div class="smart-table mb-4">
                <table class="table mb-0" style="table-layout: fixed; width: 100%; min-width: 850px;">
                    <thead>
                        <tr>
                            <th width="20%" style="white-space: nowrap;">Sản Phẩm</th>
                            <th width="15%" style="white-space: nowrap;">Tồn Của Bạn</th>
                            <th width="25%" style="white-space: nowrap;">Kho Đối Tác</th>
                            <th width="15%" style="white-space: nowrap;">Loại Giao Dịch</th>
                            <th width="18%" style="white-space: nowrap;">Số Lượng</th>
                            <th width="7%" class="text-center" style="white-space: nowrap;">Xóa</th>
                        </tr>
                    </thead>
                    <tbody id="transferTableBody">
                        <tr id="emptyRow">
                            <td colspan="6">
                                <div class="empty-state text-center py-5">
                                    <span class="material-icons mb-3" style="font-size: 48px; color: #cbd5e1;">inventory</span>
                                    <h6 class="fw-bold mb-2">Chưa có sản phẩm nào</h6>
                                    <p class="text-muted small mb-0">Tìm kiếm và chọn sản phẩm ở trên để thêm vào phiếu</p>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="d-flex justify-content-end">
                <button type="submit" class="page-action-btn px-4 py-2" id="submitBtn" disabled style="background-color: var(--primary-color); border: none; color: white; border-radius: 8px; font-weight: 600; display: flex; align-items: center; gap: 8px;">
                    <span class="material-icons" style="font-size: 18px;">send</span>
                    Khởi Tạo Lệnh Điều Chuyển
                </button>
            </div>
        </form>
    </div>
</div>


