<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<style>
    .subtab-nav { border-bottom: 1px solid #e2e8f0; margin-bottom: 20px; display: flex; gap: 20px; }
    .subtab-link { padding: 10px 16px; color: #64748b; text-decoration: none; font-weight: 500; border-bottom: 2px solid transparent; transition: all 0.2s; }
    .subtab-link:hover { color: var(--primary-color); }
    .subtab-link.active { color: var(--primary-color); border-bottom-color: var(--primary-color); }
</style>

<div class="dashboard-card">
    <div class="card-header border-bottom-0 pb-0 mb-3">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0">Điều chuyển</h5>
            <c:if test="${empty activeSubtab || activeSubtab == 'transfer_list'}">
                <c:if test="${roleName == 'WarehouseStaff' || roleName == 'StoreManager' || roleName == 'Admin' || roleName == 'Owner'}">
                    <a href="${pageContext.request.contextPath}/inventory?action=createTransfer&warehouseId=${selectedWarehouseId}" class="page-action-btn text-decoration-none">
                        <span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">add</span>
                        Tạo Phiếu Điều Chuyển
                    </a>
                </c:if>
            </c:if>
        </div>
        <div class="subtab-nav">
            <a href="?tab=transfer&subtab=transfer_list&warehouseId=${selectedWarehouseId}" class="subtab-link ${empty activeSubtab || activeSubtab == 'transfer_list' ? 'active' : ''}">Danh Sách Điều Chuyển</a>
            <a href="?tab=transfer&subtab=transfer_check&warehouseId=${selectedWarehouseId}" class="subtab-link ${activeSubtab == 'transfer_check' ? 'active' : ''}">Kiểm Tra Điều Chuyển</a>
            <a href="?tab=transfer&subtab=discrepancy&warehouseId=${selectedWarehouseId}" class="subtab-link ${activeSubtab == 'discrepancy' ? 'active' : ''}">Sai Lệch Điều Chuyển</a>
        </div>
    </div>

    <div class="card-body pt-0">
        <c:choose>
            <c:when test="${activeSubtab == 'transfer_check'}">
                <div class="premium-table-container">
                    <table class="table premium-table mb-0">
                        <thead>
                            <tr>
                                <th>Mã Phiếu</th>
                                <th>Loại Phiếu</th>
                                <th>Kho Đề Xuất</th>
                                <th>Kho Xử Lý</th>
                                <th>Người Tạo</th>
                                <th>Thời Gian</th>
                                <th>Trạng Thái</th>
                                <th width="200px">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty transferChecks}">
                                    <tr>
                                        <td colspan="8" class="text-center py-4 text-muted">Không có phiếu điều chuyển nào cần kiểm tra</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="tx" items="${transferChecks}">
                                        <tr>
                                            <td class="fw-medium">${tx.ticketCode}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty selectedWarehouseId and selectedWarehouseId == tx.fromWarehouseId}">
                                                        <span class="badge bg-warning text-dark">Phiếu Xuất</span>
                                                    </c:when>
                                                    <c:when test="${not empty selectedWarehouseId and selectedWarehouseId == tx.toWarehouseId}">
                                                        <span class="badge bg-success">Phiếu Nhập</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary">Điều Chuyển</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${tx.status == 'PENDING' && not empty selectedWarehouseId && selectedWarehouseId == tx.fromWarehouseId}">
                                                        Kho hiện tại
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${tx.fromWarehouseName}
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${tx.status == 'PENDING' && not empty selectedWarehouseId && selectedWarehouseId == tx.toWarehouseId}">
                                                        Kho hiện tại
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${tx.toWarehouseName}
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${tx.createdByName}</td>
                                            <td>
                                                <fmt:parseDate value="${tx.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDateTime" type="both" />
                                                <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${!tx.exportedBySender}">
                                                        <span class="badge bg-secondary">CHỜ XUẤT HÀNG</span>
                                                    </c:when>
                                                    <c:when test="${tx.exportedBySender}">
                                                        <span class="badge bg-primary">ĐANG CHUYỂN</span>
                                                    </c:when>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div class="d-flex gap-2">
                                                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="viewTicketDetails(${tx.ticketId})">Xem</button>
                                                    
                                                    <!-- IN_TRANSIT: Xác nhận Xuất (Kho Gửi) -->
                                                    <c:if test="${!tx.exportedBySender && (empty selectedWarehouseId || selectedWarehouseId == tx.fromWarehouseId)}">
                                                        <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                            <input type="hidden" name="action" value="confirmDispatch">
                                                            <input type="hidden" name="transferId" value="${tx.ticketId}">
                                                            <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                                                            <button type="submit" class="btn btn-sm btn-outline-info">Xác Nhận Xuất</button>
                                                        </form>
                                                        <button type="button" class="btn btn-sm btn-outline-danger" onclick="openRejectModal(${tx.ticketId})">Hủy Lệnh</button>
                                                    </c:if>
 
                                                    <!-- IN_TRANSIT: Xác nhận Nhập (Kho Nhận) -->
                                                    <c:if test="${tx.exportedBySender && !tx.importedByReceiver && (empty selectedWarehouseId || selectedWarehouseId == tx.toWarehouseId)}">
                                                        <button type="button" class="btn btn-sm btn-outline-success" onclick="openReceiptModal(${tx.ticketId})">Kiểm Tra Nhập</button>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </c:when>

            <c:when test="${activeSubtab == 'discrepancy'}">
                <div class="premium-table-container">
                    <table class="table premium-table mb-0">
                        <thead>
                            <tr>
                                <th>Mã Phiếu</th>
                                <th>Kho Ghi Nhận</th>
                                <th>Phiếu Gốc</th>
                                <th>Người Tạo</th>
                                <th>Thời Gian</th>
                                <th>Lý Do Sai Lệch</th>
                                <th width="100px">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty discrepancies}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">Không có dữ liệu sai lệch</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="tx" items="${discrepancies}">
                                        <tr>
                                            <td class="fw-medium text-danger">${tx.ticketCode}</td>
                                            <td>${tx.fromWarehouseName}</td>
                                            <td>
                                                <button class="btn btn-sm btn-link p-0 text-decoration-none" onclick="viewTicketDetails(${tx.toWarehouseId})">Xem Lệnh Điều Chuyển</button>
                                            </td>
                                            <td>${tx.createdByName}</td>
                                            <td>
                                                <fmt:parseDate value="${tx.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDateTime" type="both" />
                                                <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
                                            </td>
                                            <td><span class="text-muted small">${tx.note}</span></td>
                                            <td>
                                                <button type="button" class="btn btn-sm btn-outline-primary" onclick="viewTicketDetails(${tx.ticketId})">Xem</button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </c:when>

            <c:otherwise>
                <div class="premium-table-container">
                    <table class="table premium-table mb-0">
                        <thead>
                            <tr>
                                <th>Mã Phiếu</th>
                                <th>Kho Xuất</th>
                                <th>Kho Nhận</th>
                                <th>Người Tạo</th>
                                <th>Thời Gian</th>
                                <th>Trạng Thái</th>
                                <th width="120px">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty transfers}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">Không có phiếu điều chuyển nào</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="tx" items="${transfers}">
                                        <tr>
                                            <td class="fw-medium">${tx.ticketCode}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${tx.status == 'PENDING' && not empty selectedWarehouseId && selectedWarehouseId == tx.fromWarehouseId}">
                                                        Kho hiện tại
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${tx.fromWarehouseName}
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${tx.status == 'PENDING' && not empty selectedWarehouseId && selectedWarehouseId == tx.toWarehouseId}">
                                                        Kho hiện tại
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${tx.toWarehouseName}
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${tx.createdByName}</td>
                                            <td>
                                                ${tx.formattedCreatedAt}
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${tx.status == 'PENDING'}">
                                                        <span class="badge bg-warning text-dark">CHỜ DUYỆT</span>
                                                    </c:when>
                                                    <c:when test="${tx.status == 'IN_TRANSIT'}">
                                                        <span class="badge bg-info text-dark">${tx.transferProgress}</span>
                                                    </c:when>
                                                    <c:when test="${tx.status == 'COMPLETED'}">
                                                        <span class="badge bg-success">HOÀN TẤT</span>
                                                    </c:when>
                                                    <c:when test="${tx.status == 'COMPLETED_WITH_ERROR'}">
                                                        <span class="badge bg-warning text-dark">HOÀN TẤT (CÓ LỖI)</span>
                                                    </c:when>
                                                    <c:when test="${tx.status == 'REJECTED' || tx.status == 'CANCELLED'}">
                                                        <span class="badge bg-danger">ĐÃ HỦY</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary">${tx.status}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div class="d-flex gap-2">
                                                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="viewTicketDetails(${tx.ticketId})">Xem</button>
                                                    
                                                    <!-- PENDING: Duyệt phiếu -->
                                                    <c:if test="${tx.status == 'PENDING' && (roleName == 'WarehouseStaff' || roleName == 'Admin' || roleName == 'Owner')}">
                                                        <c:choose>
                                                            <c:when test="${empty selectedWarehouseId || selectedWarehouseId == tx.toWarehouseId}">
                                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                                    <input type="hidden" name="action" value="confirmExport">
                                                                    <input type="hidden" name="transferId" value="${tx.ticketId}">
                                                                    <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                                                                    <button type="submit" class="btn btn-sm btn-outline-success">Phê Duyệt</button>
                                                                </form>
                                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;" onsubmit="return confirm('Bạn có chắc muốn từ chối phiếu này không?');">
                                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                                    <input type="hidden" name="action" value="rejectTransfer">
                                                                    <input type="hidden" name="transferId" value="${tx.ticketId}">
                                                                    <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                                                                    <button type="submit" class="btn btn-sm btn-outline-danger">Từ Chối</button>
                                                                </form>
                                                            </c:when>
                                                            <c:when test="${selectedWarehouseId == tx.fromWarehouseId}">
                                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;" onsubmit="return confirm('Bạn có chắc muốn hủy lệnh này không?');">
                                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                                    <input type="hidden" name="action" value="cancelTransfer">
                                                                    <input type="hidden" name="transferId" value="${tx.ticketId}">
                                                                    <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                                                                    <button type="submit" class="btn btn-sm btn-outline-danger">Hủy Lệnh</button>
                                                                </form>
                                                            </c:when>
                                                        </c:choose>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
