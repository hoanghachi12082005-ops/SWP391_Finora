<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-3">
        <h5>Danh sách Phiếu Kiểm Kho</h5>
        <c:if test="${roleName == 'WarehouseStaff' || roleName == 'Admin' || roleName == 'Owner'}">
            <!-- Nút giả lập tạo phiếu để test UI -->
            <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                <input type="hidden" name="action" value="createCheck">
                <button type="submit" class="btn btn-primary">
                    <span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">add</span>
                    Tạo phiếu kiểm
                </button>
            </form>
        </c:if>
    </div>

    <div class="premium-table-container">
        <table class="premium-table table-hover">
            <thead>
                <tr>
                    <th>Mã Phiếu</th>
                    <th>Kho</th>
                    <th>Ngày Tạo</th>
                    <th>Người Tạo</th>
                    <th>Trạng Thái</th>
                    <th>Tổng Lệch</th>
                    <th>Người Duyệt</th>
                    <th>Thao Tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty checks}">
                        <tr><td colspan="8" class="text-center text-muted">Chưa có phiếu kiểm kho.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="c" items="${checks}">
                            <tr>
                                <td><strong>${c.checkCode}</strong></td>
                                <td>${c.warehouseName}</td>
                                <td>${c.checkDate}</td>
                                <td>${c.createdByName}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${c.status == 'PENDING'}">
                                            <span class="badge bg-warning text-dark">CHỜ DUYỆT</span>
                                        </c:when>
                                        <c:when test="${c.status == 'APPROVED'}">
                                            <span class="badge bg-success">ĐÃ DUYỆT</span>
                                        </c:when>
                                        <c:when test="${c.status == 'REJECTED'}">
                                            <span class="badge bg-danger">TỪ CHỐI</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">${c.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span style="color: ${c.totalDifference < 0 ? 'red' : (c.totalDifference > 0 ? 'green' : 'black')}; font-weight:bold;">
                                        ${c.totalDifference > 0 ? '+' : ''}${c.totalDifference}
                                    </span>
                                </td>
                                <td>${c.approvedByName != null ? c.approvedByName : '-'}</td>
                                <td>
                                    <c:if test="${c.status == 'PENDING' && (roleName == 'StoreManager' || roleName == 'Admin' || roleName == 'Owner')}">
                                        <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                            <input type="hidden" name="action" value="approveCheck">
                                            <input type="hidden" name="checkId" value="${c.stockCheckId}">
                                            <button type="submit" class="btn btn-sm btn-success">Duyệt Phiếu</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>
