<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="modal-header border-bottom-0 pb-0">
    <h5 class="modal-title fw-bold text-dark" id="modalTitle">Chi Tiết Phiếu Kiểm Kê</h5>
    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
</div>

<div class="modal-body pt-3">
    <!-- Metadata Info -->
    <div class="row g-3 mb-4 p-3 bg-light rounded-3 border">
        <div class="col-md-6 col-lg-3">
            <span class="text-muted small d-block">Mã Phiếu:</span>
            <strong class="text-primary fs-6" id="printCheckCode">${check.checkCode}</strong>
        </div>
        <div class="col-md-6 col-lg-3">
            <span class="text-muted small d-block">Trạng Thái:</span>
            <div id="printCheckStatus">
                <c:choose>
                    <c:when test="${check.status == 'PENDING'}">
                        <span class="badge bg-warning text-dark" style="border-radius: 6px; padding: 4px 8px; font-size:12px;">CHỜ DUYỆT</span>
                    </c:when>
                    <c:when test="${check.status == 'APPROVED'}">
                        <span class="badge bg-success text-white" style="border-radius: 6px; padding: 4px 8px; font-size:12px;">ĐÃ DUYỆT</span>
                    </c:when>
                    <c:when test="${check.status == 'CANCELLED'}">
                        <span class="badge bg-danger text-white" style="border-radius: 6px; padding: 4px 8px; font-size:12px;">ĐÃ HỦY</span>
                    </c:when>
                </c:choose>
            </div>
        </div>
        <div class="col-md-6 col-lg-3">
            <span class="text-muted small d-block">Thời Gian Lập:</span>
            <strong class="text-dark" id="printCheckCreatedAt">${check.formattedCreatedAt}</strong>
        </div>
        <div class="col-md-6 col-lg-3">
            <span class="text-muted small d-block">Kho Hàng:</span>
            <strong class="text-dark" id="printCheckWarehouseName">${check.warehouseName}</strong>
        </div>
        <div class="col-md-6 col-lg-4 mt-2">
            <span class="text-muted small d-block">Người Lập Phiếu:</span>
            <strong class="text-dark" id="printCheckCreatedByName">${check.createdByName}</strong>
        </div>
        <div class="col-md-6 col-lg-4 mt-2">
            <span class="text-muted small d-block">Người Phê Duyệt:</span>
            <strong class="text-dark" id="printCheckApprovedByName">${not empty check.approvedByName ? check.approvedByName : 'Chưa có'}</strong>
        </div>
        <div class="col-md-12 col-lg-4 mt-2">
            <span class="text-muted small d-block">Tổng Sai Lệch:</span>
            <strong class="text-danger" id="printCheckTotalDiscrepancy">${check.totalDiscrepancy} SP</strong>
        </div>
    </div>

    <!-- Product list -->
    <h6 class="fw-bold text-dark mb-3">Danh Sách Sản Phẩm Kiểm Kê</h6>
    <div class="table-responsive border rounded-3 overflow-hidden">
        <table class="table table-hover align-middle mb-0" style="font-size:14px;">
            <thead class="table-light">
                <tr>
                    <th width="35%">Sản Phẩm</th>
                    <th width="15%">Danh Mục</th>
                    <th width="15%" class="text-center">Tồn Hệ Thống</th>
                    <th width="15%" class="text-center">Tồn Thực Tế</th>
                    <th width="15%" class="text-center">Chênh Lệch</th>
                    <th width="20%">Ghi Chú</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="item" items="${checkDetails}">
                    <tr>
                        <td><strong class="text-dark">${item.productName}</strong></td>
                        <td><span class="text-muted small">${item.categoryName}</span></td>
                        <td class="text-center fw-medium">${item.systemQty}</td>
                        <td class="text-center fw-medium text-primary">${item.actualQty}</td>
                        <td class="text-center fw-bold ${item.discrepancy == 0 ? 'text-success' : 'text-danger'}">
                            <c:choose>
                                <c:when test="${item.discrepancy > 0}">+${item.discrepancy}</c:when>
                                <c:otherwise>${item.discrepancy}</c:otherwise>
                            </c:choose>
                        </td>
                        <td><span class="text-muted small">${not empty item.note ? item.note : '-'}</span></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<c:set var="role" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />
<div class="modal-footer border-top-0 pt-0">
    <button type="button" class="btn btn-outline-danger d-flex align-items-center gap-1" onclick="printCheckVoucher()" style="border-radius: 8px;">
        <span class="material-icons" style="font-size: 18px;">print</span>
        In Phiếu
    </button>
    <c:if test="${check.status == 'PENDING' && (role == 'Owner' || role == 'StoreManager')}">
        <form action="${pageContext.request.contextPath}/inventory" method="POST" class="d-inline">
            <input type="hidden" name="action" value="approveCheck">
            <input type="hidden" name="checkId" value="${check.checkId}">
            <input type="hidden" name="currentWarehouseId" value="${check.warehouseId}">
            <button type="submit" class="btn btn-success d-flex align-items-center gap-1" onclick="return confirm('Phê duyệt phiếu kiểm kho này và thực hiện cân bằng tồn kho?')" style="border-radius: 8px;">
                <span class="material-icons" style="font-size: 18px;">check</span> Duyệt Phiếu
            </button>
        </form>
        <form action="${pageContext.request.contextPath}/inventory" method="POST" class="d-inline ms-1">
            <input type="hidden" name="action" value="cancelCheck">
            <input type="hidden" name="checkId" value="${check.checkId}">
            <input type="hidden" name="currentWarehouseId" value="${check.warehouseId}">
            <button type="submit" class="btn btn-danger d-flex align-items-center gap-1" onclick="return confirm('Từ chối và hủy bỏ phiếu kiểm kho này?')" style="border-radius: 8px;">
                <span class="material-icons" style="font-size: 18px;">close</span> Hủy Phiếu
            </button>
        </form>
    </c:if>
    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" style="border-radius: 8px;">Đóng</button>

