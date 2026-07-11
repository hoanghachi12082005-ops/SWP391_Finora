<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<div class="dashboard-card">
    <div class="card-header border-bottom-0 pb-0 mb-3">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0">Danh Sách Phiếu Xuất Hàng</h5>
            <c:if test="${roleName == 'WarehouseStaff' || roleName == 'StoreManager' || roleName == 'Owner'}">
                <button type="button" class="page-action-btn border-0" data-bs-toggle="modal" data-bs-target="#exportStockModal">
                    <span class="material-icons" style="font-size: 20px;">remove_circle_outline</span>
                    <span>Tạo Phiếu Xuất</span>
                </button>
            </c:if>
        </div>
    </div>

    <div class="card-body pt-0">
        <div class="premium-table-container">
            <table class="table premium-table mb-0">
                <thead>
                    <tr>
                        <th>Mã Phiếu</th>
                        <th>Khách Hàng/Đích</th>
                        <th>Người Tạo</th>
                        <th>Tổng Tiền</th>
                        <th>Thời Gian</th>
                        <th>Trạng Thái</th>
                        <th width="120px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty exports}">
                            <tr>
                                <td colspan="7" class="text-center py-4 text-muted">Không có dữ liệu phiếu xuất hàng.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="po" items="${exports}">
                                <tr>
                                    <td class="fw-medium">${po.orderCode}</td>
                                    <td>${po.supplierName != null ? po.supplierName : 'Khác'}</td>
                                    <td>${po.empName}</td>
                                    <td><fmt:formatNumber value="${po.totalAmount}" type="currency" currencySymbol="₫"/></td>
                                    <td>
                                        ${po.createdAtFormatted}
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${po.status == 'PENDING'}">
                                                <span class="badge bg-warning text-dark">Chờ duyệt</span>
                                            </c:when>
                                            <c:when test="${po.status == 'COMPLETED'}">
                                                <span class="badge bg-success">Đã hoàn thành</span>
                                            </c:when>
                                            <c:when test="${po.status == 'CANCELLED'}">
                                                <span class="badge bg-danger">Đã hủy</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">${po.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <button class="btn btn-sm btn-outline-primary" onclick="viewOrderDetails(${po.orderId})">Chi tiết</button>
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
