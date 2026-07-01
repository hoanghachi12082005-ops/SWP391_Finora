<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-3">
        <h5>Danh sách Phiếu Điều Chuyển</h5>
        <c:if test="${roleName == 'WarehouseStaff' || roleName == 'StoreManager' || roleName == 'Admin' || roleName == 'Owner'}">
            <a href="${pageContext.request.contextPath}/inventory?action=createTransfer&warehouseId=${selectedWarehouseId}" class="btn btn-danger text-decoration-none">
                <span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">add</span>
                Tạo Phiếu Điều Chuyển
            </a>
        </c:if>
    </div>

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
                                    <td>${tx.fromWarehouseName}</td>
                                    <td>${tx.toWarehouseName}</td>
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
                                                            <input type="hidden" name="action" value="confirmExport">
                                                            <input type="hidden" name="transferId" value="${tx.ticketId}">
                                                            <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                                                            <button type="submit" class="btn btn-sm btn-outline-success">Phê Duyệt</button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;" onsubmit="return confirm('Bạn có chắc muốn từ chối phiếu này không?');">
                                                            <input type="hidden" name="action" value="rejectTransfer">
                                                            <input type="hidden" name="transferId" value="${tx.ticketId}">
                                                            <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                                                            <button type="submit" class="btn btn-sm btn-outline-danger">Từ Chối</button>
                                                        </form>
                                                    </c:when>
                                                    <c:when test="${selectedWarehouseId == tx.fromWarehouseId}">
                                                        <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;" onsubmit="return confirm('Bạn có chắc muốn hủy lệnh này không?');">
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
</div>

