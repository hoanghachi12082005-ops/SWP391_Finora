<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<div class="dashboard-card">
    <div class="card-header border-bottom-0 pb-0">
        <h5 class="mb-3">Kiểm kho</h5>
    </div>

    <div class="card-body pt-0">
        <div class="d-flex justify-content-end mb-3 mt-2">
            <c:if test="${roleName == 'WarehouseStaff' || roleName == 'Admin' || roleName == 'Owner'}">
                <!-- Nút giả lập tạo phiếu để test UI -->
                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="createCheck">
                    <button type="submit" class="page-action-btn">
                        <span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">add</span>
                        Tạo Lệnh Kiểm Kê
                    </button>
                </form>
            </c:if>
        </div>
        <div class="premium-table-container">
            <table class="premium-table table-hover">
                <thead>
                    <tr>
                        <th>Mã Phiếu</th>
                        <th>Chi Nhánh</th>
                        <th>Ngày Lập</th>
                        <th>Người Thực Hiện</th>
                        <th>Trạng Thái</th>
                        <th>Tổng Sai Lệch</th>
                        <th>Người Phê Duyệt</th>
                        <th>Thao Tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty checks}">
                            <tr><td colspan="8" class="text-center text-muted">Chưa có dữ liệu kiểm kê.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="c" items="${checks}">
                                <tr>
                                    <td><strong>${c.orderCode}</strong></td>
                                    <td>${c.customerName}</td>
                                    <td>${c.createdAt}</td>
                                    <td>${c.employeeName}</td>
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
                                    <td><span>-</span></td>
                                    <td>-</td>
                                    <td>
                                        <c:if test="${c.status == 'PENDING' && (roleName == 'StoreManager' || roleName == 'Admin' || roleName == 'Owner')}">
                                            <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                <input type="hidden" name="action" value="approveCheck">
                                                <input type="hidden" name="checkId" value="${c.ticketId}">
                                                <button type="submit" class="btn btn-sm inventory-btn-success">Duyệt Phiếu</button>
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
</div>
