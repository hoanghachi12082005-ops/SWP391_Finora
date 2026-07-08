<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-3">
        <h5>Lịch Sử Giao Dịch</h5>
        
        <form action="" method="GET" class="d-flex gap-2">
            <input type="hidden" name="tab" value="history">
            <c:if test="${not empty warehouses && sessionScope.currentUser.roleName == 'Admin'}">
                <select name="warehouseId" class="form-select" onchange="this.form.submit()">
                    <option value="">Tất cả kho</option>
                    <c:forEach var="w" items="${warehouses}">
                        <option value="${w.warehouseId}" ${selectedWarehouseId == w.warehouseId ? 'selected' : ''}>${w.warehouseName}</option>
                    </c:forEach>
                </select>
            </c:if>
            <select name="typeFilter" class="form-select" onchange="this.form.submit()">
                <option value="">Tất cả loại</option>
                <option value="IN" ${typeFilter == 'IN' ? 'selected' : ''}>Nhập hàng</option>
                <option value="OUT" ${typeFilter == 'OUT' ? 'selected' : ''}>Chuyển kho</option>
            </select>
            <select name="dateFilter" class="form-select" onchange="this.form.submit()">
                <option value="">Toàn thời gian</option>
                <option value="today" ${dateFilter == 'today' ? 'selected' : ''}>Hôm nay</option>
            </select>
            <button type="submit" class="btn btn-danger">Lọc</button>
        </form>
    </div>

    <div class="activity-feed-container" style="max-height: 600px; overflow-y: auto;">
        <c:choose>
            <c:when test="${empty history}">
                <p class="text-center text-muted p-4">Không có dữ liệu giao dịch.</p>
            </c:when>
            <c:otherwise>
                <div class="premium-table-container">
                    <table class="premium-table">
                        <thead>
                            <tr>
                                <th>Thời Gian</th>
                                <th>Mã Phiếu</th>
                                <th>Loại</th>
                                <th>Nguồn</th>
                                <th>Đích</th>
                                <th>Trạng Thái</th>
                                <th>Người Thực Hiện</th>
                                <th>Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="tx" items="${history}">
                                <tr>
                                    <td>
                                        <fmt:parseDate value="${tx.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDateTime" type="both" />
                                        <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
                                    </td>
                                    <td>
                                        <strong>${tx.ticketCode}</strong>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${tx.ticketType == 'IMPORT'}">
                                                <span class="badge" style="background-color: #059669; color: #fff; font-size: 11px; padding: 4px 10px;">
                                                    <span class="material-icons" style="font-size: 13px; vertical-align: text-bottom;">inventory</span>
                                                    Nhập hàng
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background-color: #2563eb; color: #fff; font-size: 11px; padding: 4px 10px;">
                                                    <span class="material-icons" style="font-size: 13px; vertical-align: text-bottom;">swap_horiz</span>
                                                    Chuyển kho
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span style="font-weight: 500; color: #374151;">
                                            <c:choose>
                                                <c:when test="${tx.ticketType == 'IMPORT'}">
                                                    <span class="material-icons" style="font-size: 16px; color: #059669; vertical-align: bottom;">local_shipping</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="material-icons" style="font-size: 16px; color: #6b7280; vertical-align: bottom;">storefront</span>
                                                </c:otherwise>
                                            </c:choose>
                                            ${tx.fromWarehouseName}
                                        </span>
                                    </td>
                                    <td>
                                        <span style="font-weight: 500; color: #374151;">
                                            <span class="material-icons" style="font-size: 16px; color: #6b7280; vertical-align: bottom;">storefront</span>
                                            ${tx.toWarehouseName}
                                        </span>
                                    </td>
                                    <td>
                                        <c:choose>
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
                                    <td>${tx.createdByName}</td>
                                    <td>
                                        <button type="button" class="btn btn-sm btn-outline-primary" onclick="viewTicketDetails(${tx.ticketId})">Xem</button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
