<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- 
  ==========================================================================
  TAB DANH SÁCH PHIẾU NHẬP KHO (_tab_import.jsp)
  - Hiển thị danh sách lịch sử phiếu nhập hàng của kho hàng được chọn.
  - Cung cấp thanh menu phụ để chuyển đổi qua lại giữa Phiếu nhập và Phiếu xuất (tab=export).
  ==========================================================================
--%>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<div class="dashboard-card">
    <div class="card-header border-bottom-0 pb-0 mb-3">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0">
                <c:choose>
                    <c:when test="${roleName == 'WarehouseStaff'}">Danh Sách Phiếu Chờ Duyệt</c:when>
                    <c:otherwise>Danh Sách Phiếu</c:otherwise>
                </c:choose>
            </h5>
        </div>
        
        <c:if test="${roleName == 'WarehouseStaff' || roleName == 'StoreManager'}">

            <div class="import-subtab-nav">
                <a href="?tab=import&warehouseId=${selectedWarehouseId}" 
                   class="import-subtab-link active">
                    <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">download</span>
                    <c:choose>
                        <c:when test="${roleName == 'WarehouseStaff'}">Phiếu Nhập Chờ Duyệt</c:when>
                        <c:otherwise>Phiếu Nhập</c:otherwise>
                    </c:choose>
                </a>
                <a href="?tab=export&warehouseId=${selectedWarehouseId}" 
                   class="import-subtab-link">
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
                        <th>Nhà Cung Cấp</th>
                        <th>Người Tạo</th>
                        <th>Tổng Tiền</th>
                        <th>Thời Gian</th>
                        <th>Trạng Thái</th>
                        <th width="120px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty imports}">
                            <tr>
                                <td colspan="7" class="text-center py-4 text-muted">Không có dữ liệu phiếu nhập hàng.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="po" items="${imports}">
                                <tr>
                                    <td class="fw-medium">${po.orderCode}</td>
                                    <td>${po.supplierName}</td>
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
                                                    <input type="hidden" name="tab" value="import">
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
