<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<div class="dashboard-card">
    <div class="card-header border-bottom-0 pb-0 mb-3">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0">Danh Sách Phiếu Chờ Duyệt</h5>
        </div>
    </div>

    <div class="card-body pt-0">
        <div class="premium-table-container">
            <table class="table premium-table mb-0">
                <thead>
                    <tr>
                        <th>Mã Phiếu</th>
                        <th>Loại Phiếu</th>
                        <th>Khách Hàng / Nhà Cung Cấp</th>
                        <th>Người Tạo</th>
                        <th>Tổng Tiền</th>
                        <th>Thời Gian</th>
                        <th>Trạng Thái</th>
                        <th width="180px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty pendingVouchers}">
                            <tr>
                                <td colspan="8" class="text-center py-4 text-muted">Không có phiếu nào đang chờ duyệt.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="vo" items="${pendingVouchers}">
                                <tr>
                                    <td class="fw-semibold text-primary">${vo.code}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${vo.type == 'CHECK'}">
                                                <span class="badge" style="background-color: #6366f1; color: #fff; font-size: 11px; padding: 4px 10px;">${vo.typeLabel}</span>
                                            </c:when>
                                            <c:when test="${vo.type == 'IMPORT'}">
                                                <span class="badge bg-success-subtle text-success border border-success-subtle" style="font-size: 11px; padding: 4px 10px;">Nhập</span>
                                            </c:when>
                                            <c:when test="${vo.type == 'TRANSFER'}">
                                                <span class="badge bg-primary-subtle text-primary border border-primary-subtle" style="font-size: 11px; padding: 4px 10px;">Điều chuyển</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-danger-subtle text-danger border border-danger-subtle" style="font-size: 11px; padding: 4px 10px;">Xuất</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${vo.partner}</td>
                                    <td>${vo.createdBy}</td>
                                    <td class="fw-semibold">
                                        <c:choose>
                                            <c:when test="${not empty vo.amount}">
                                                <fmt:formatNumber value="${vo.amount}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${vo.createdAt}</td>
                                    <td>
                                        <span class="badge bg-warning-subtle text-warning border border-warning-subtle" style="font-size: 11px; padding: 4px 10px;">Chờ duyệt</span>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <button class="btn btn-sm d-inline-flex align-items-center gap-1 px-2.5 py-1.5" style="border: 1px solid #dbeafe; background-color: #eff6ff; color: #2563eb; font-weight: 600; font-size: 12.5px; border-radius: 6px; transition: all 0.2s; height: 32px; cursor: pointer;" onmouseover="this.style.backgroundColor='#dbeafe'; this.style.color='#1d4ed8';" onmouseout="this.style.backgroundColor='#eff6ff'; this.style.color='#2563eb';" onclick="${vo.detailCallback}">
                                                <span class="material-icons" style="font-size: 15px; vertical-align: middle;">visibility</span>
                                                <span>Chi tiết</span>
                                            </button>
                                            <c:if test="${roleName == 'WarehouseStaff' || roleName == 'Owner' || roleName == 'StoreManager'}">
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block; vertical-align: middle;" onsubmit="return confirm('Bạn có chắc chắn muốn hủy phiếu này?')">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="${vo.actionCancel}">
                                                    <input type="hidden" name="${vo.idParamName}" value="${vo.id}">
                                                    <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                                                    <input type="hidden" name="tab" value="pending_vouchers">
                                                    <button type="submit" class="btn btn-sm d-inline-flex align-items-center gap-1 px-2.5 py-1.5" style="border: 1px solid #fee2e2; background-color: #fef2f2; color: #dc2626; font-weight: 600; font-size: 12.5px; border-radius: 6px; transition: all 0.2s; height: 32px; cursor: pointer;" onmouseover="this.style.backgroundColor='#fee2e2'; this.style.color='#b91c1c';" onmouseout="this.style.backgroundColor='#fef2f2'; this.style.color='#dc2626';">
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
