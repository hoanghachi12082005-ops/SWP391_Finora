<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-3">
        <h5>Danh sách Phiếu Chuyển Kho</h5>
        <c:if test="${roleName == 'WarehouseStaff' || roleName == 'Admin' || roleName == 'Owner'}">
            <!-- Nút giả lập tạo phiếu để test UI -->
            <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                <input type="hidden" name="action" value="createTransfer">
                <button type="submit" class="btn btn-primary">
                    <span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">add</span>
                    Tạo phiếu chuyển
                </button>
            </form>
        </c:if>
    </div>

    <div class="premium-table-container">
        <table class="premium-table table-hover">
            <thead>
                <tr>
                    <th>Mã Phiếu</th>
                    <th>Kho Nguồn</th>
                    <th>Kho Đích</th>
                    <th>Ngày Tạo</th>
                    <th>Người Tạo</th>
                    <th>Trạng Thái</th>
                    <th>Thao Tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty transfers}">
                        <tr><td colspan="7" class="text-center text-muted">Chưa có phiếu chuyển kho.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="tx" items="${transfers}">
                            <tr>
                                <td><strong>${tx.transferCode}</strong></td>
                                <td>${tx.fromWarehouseName}</td>
                                <td>${tx.toWarehouseName}</td>
                                <td>
                                    <!-- Use custom formatting or simple string -->
                                    ${tx.transferDate}
                                </td>
                                <td>${tx.createdByName}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${tx.status == 'PENDING'}">
                                            <span class="badge bg-warning text-dark">CHỜ XUẤT</span>
                                        </c:when>
                                        <c:when test="${tx.status == 'IN_TRANSIT'}">
                                            <span class="badge bg-info text-dark">ĐANG CHUYỂN</span>
                                        </c:when>
                                        <c:when test="${tx.status == 'COMPLETED'}">
                                            <span class="badge bg-success">ĐÃ NHẬN</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">${tx.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="d-flex gap-2">
                                        <c:if test="${tx.status == 'PENDING' && (roleName == 'WarehouseStaff' || roleName == 'Admin')}">
                                            <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                <input type="hidden" name="action" value="confirmExport">
                                                <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                <button type="submit" class="btn btn-sm btn-outline-warning">Xuất hàng</button>
                                            </form>
                                        </c:if>
                                        <c:if test="${tx.status == 'IN_TRANSIT' && (roleName == 'WarehouseStaff' || roleName == 'Admin')}">
                                            <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                <input type="hidden" name="action" value="confirmReceive">
                                                <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                <button type="submit" class="btn btn-sm btn-outline-info">Nhận hàng</button>
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
