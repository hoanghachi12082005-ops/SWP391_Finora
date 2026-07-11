<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<div class="dashboard-card">
    <div class="card-header border-bottom-0 pb-0 mb-3">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0">
                <c:choose>
                    <c:when test="${roleName == 'WarehouseStaff'}">Danh Sách Phiếu Xuất Chờ Duyệt</c:when>
                    <c:otherwise>Danh Sách Phiếu Xuất Hàng</c:otherwise>
                </c:choose>
            </h5>
            <c:if test="${roleName == 'WarehouseStaff' || roleName == 'StoreManager' || roleName == 'Owner'}">
                <button type="button" class="page-action-btn border-0" data-bs-toggle="modal" data-bs-target="#exportStockModal">
                    <span class="material-icons" style="font-size: 20px;">remove_circle_outline</span>
                    <span>Tạo Phiếu Xuất</span>
                </button>
            </c:if>
        </div>
        
        <c:if test="${roleName == 'WarehouseStaff' || roleName == 'StoreManager'}">
            <style>
                .import-subtab-nav { border-bottom: 2px solid #e2e8f0; margin-bottom: 20px; display: flex; gap: 0; }
                .import-subtab-link { padding: 12px 20px; color: #64748b; text-decoration: none; font-weight: 500; font-size: 14px; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.2s; }
                .import-subtab-link:hover { color: var(--primary-color); background-color: rgba(79,70,229,0.04); }
                .import-subtab-link.active { color: var(--primary-color); border-bottom-color: var(--primary-color); font-weight: 600; }
            </style>
            <div class="import-subtab-nav">
                <a href="?tab=import&warehouseId=${selectedWarehouseId}" 
                   class="import-subtab-link">
                    <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">download</span>
                    <c:choose>
                        <c:when test="${roleName == 'WarehouseStaff'}">Phiếu Nhập Chờ Duyệt</c:when>
                        <c:otherwise>Phiếu Nhập</c:otherwise>
                    </c:choose>
                </a>
                <a href="?tab=export&warehouseId=${selectedWarehouseId}" 
                   class="import-subtab-link active">
                    <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">upload</span>
                    <c:choose>
                        <c:when test="${roleName == 'WarehouseStaff'}">Phiếu Xuất Chờ Duyệt</c:when>
                        <c:otherwise>Phiếu Xuất</c:otherwise>
                    </c:choose>
                </a>
            </div>
        </c:if>
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
                                        <div class="d-flex align-items-center gap-2">
                                            <button class="btn btn-sm d-inline-flex align-items-center gap-1 px-2.5 py-1.5" style="border: 1px solid #dbeafe; background-color: #eff6ff; color: #2563eb; font-weight: 600; font-size: 13px; border-radius: 6px; transition: all 0.2s; height: 32px;" onmouseover="this.style.backgroundColor='#dbeafe'; this.style.color='#1d4ed8';" onmouseout="this.style.backgroundColor='#eff6ff'; this.style.color='#2563eb';" onclick="viewOrderDetails(${po.orderId})">
                                                <span class="material-icons" style="font-size: 15px; vertical-align: middle;">visibility</span>
                                                <span>Chi tiết</span>
                                            </button>
                                            <c:if test="${po.status == 'PENDING' && (roleName == 'WarehouseStaff' || roleName == 'Owner' || roleName == 'StoreManager')}">
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block; vertical-align: middle;" onsubmit="return confirm('Bạn có chắc chắn muốn hủy phiếu này?')">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="cancelOrder">
                                                    <input type="hidden" name="orderId" value="${po.orderId}">
                                                    <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                                                    <input type="hidden" name="tab" value="export">
                                                    <button type="submit" class="btn btn-sm d-inline-flex align-items-center gap-1 px-2.5 py-1.5" style="border: 1px solid #fee2e2; background-color: #fef2f2; color: #dc2626; font-weight: 600; font-size: 13px; border-radius: 6px; transition: all 0.2s; height: 32px;" onmouseover="this.style.backgroundColor='#fee2e2'; this.style.color='#b91c1c';" onmouseout="this.style.backgroundColor='#fef2f2'; this.style.color='#dc2626';">
                                                        <span class="material-icons" style="font-size: 15px; vertical-align: middle;">cancel</span>
                                                        <span>Hủy</span>
                                                    </button>
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
</div>
