<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-3">
        <h5>Danh sách Phiếu Chuyển Kho</h5>
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
                        <th>Kho Đề Xuất</th>
                        <th>Kho Xử Lý</th>
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
                                        <fmt:parseDate value="${tx.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDateTime" type="both" />
                                        <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${tx.status == 'PENDING'}">
                                                <span class="badge bg-warning text-dark">CHỜ DUYỆT</span>
                                            </c:when>
                                            <c:when test="${tx.status == 'IN_TRANSIT'}">
                                                <span class="badge bg-info text-dark">ĐANG CHUYỂN</span>
                                            </c:when>
                                            <c:when test="${tx.status == 'COMPLETED'}">
                                                <span class="badge bg-success">ĐÃ DUYỆT</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">${tx.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="d-flex gap-2">
                                            <button type="button" class="btn btn-sm btn-outline-primary" onclick="viewTicketDetails(${tx.ticketId})">Xem</button>
                                            <c:if test="${tx.status == 'PENDING' && (roleName == 'WarehouseStaff' || roleName == 'Admin' || roleName == 'Owner') && (empty selectedWarehouseId || selectedWarehouseId == tx.toWarehouseId)}">
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                    <input type="hidden" name="action" value="confirmExport">
                                                    <input type="hidden" name="transferId" value="${tx.ticketId}">
                                                    <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm btn-outline-warning">Duyệt Phiếu</button>
                                                </form>
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

<!-- Modal for Ticket Details -->
<div class="modal fade" id="ticketDetailsModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content" id="ticketDetailsModalContent">
            <!-- Content loaded via AJAX -->
            <div class="modal-body text-center py-5">
                <div class="spinner-border text-primary" role="status"></div>
                <p class="mt-2 text-muted">Đang tải dữ liệu...</p>
            </div>
        </div>
    </div>
</div>

<script>
    function viewTicketDetails(ticketId) {
        // Show loading state
        const modalContent = document.getElementById('ticketDetailsModalContent');
        modalContent.innerHTML = '<div class="modal-body text-center py-5"><div class="spinner-border text-primary" role="status"></div><p class="mt-2 text-muted">Đang tải dữ liệu...</p></div>';
        
        // Show modal
        const myModal = new bootstrap.Modal(document.getElementById('ticketDetailsModal'));
        myModal.show();
        
        // Fetch data
        fetch('${pageContext.request.contextPath}/inventory?action=viewTicket&ticketId=' + ticketId)
            .then(response => response.text())
            .then(html => {
                modalContent.innerHTML = html;
            })
            .catch(err => {
                modalContent.innerHTML = '<div class="modal-body py-5 text-center text-danger"><i class="ph ph-warning-circle fs-1 mb-2"></i><p>Lỗi khi tải dữ liệu phiếu.</p></div>';
            });
    }
</script>
