<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<section class="table-card">
    <div class="table-scroll">
        <table class="data-table">
            <thead>
            <tr>
                <th>#</th>
                <th>Mã đơn</th>
                <th>Chi nhánh</th>
                <th>Nhân viên</th>
                <th>Khách hàng</th>
                <th>Phương thức</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th class="text-right">Tổng tiền</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty orders}">
                    <tr>
                        <td colspan="10" class="empty-row">
                            <div class="empty-state">
                                <span class="material-symbols-outlined">receipt_long</span>
                                <h4>Không tìm thấy đơn hàng</h4>
                                <p>Hãy điều chỉnh bộ lọc hoặc khoảng thời gian.</p>
                            </div>
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="o" items="${orders}" varStatus="st">
                        <tr>
                            <td>${(currentPage - 1) * pageSize + st.index + 1}</td>
                            <td><strong>${o.orderCode}</strong></td>
                            <td>${o.branchName}</td>
                            <td>${o.employeeName}</td>
                            <td>${empty o.customerName ? 'Khách vãng lai' : o.customerName}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${o.paymentMethod == 'CASH'}">Tiền mặt</c:when>
                                    <c:when test="${o.paymentMethod == 'CARD'}">Thẻ</c:when>
                                    <c:when test="${o.paymentMethod == 'TRANSFER'}">Chuyển khoản</c:when>
                                    <c:otherwise>${o.paymentMethod}</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <span class="status-badge
                                    ${o.status == 'PAID' ? 'paid' : ''}
                                    ${o.status == 'PENDING' ? 'pending' : ''}
                                    ${o.status == 'CANCELLED' ? 'cancelled' : ''}
                                    ${o.status == 'COMPLETED' ? 'completed' : ''}">
                                    ${o.status.displayName}
                                </span>
                            </td>
                            <td>${fn:substring(o.createdAt, 0, 10)}</td>
                            <td class="text-right"><fmt:formatNumber value="${o.totalAmount}" type="number" groupingUsed="true"/> ₫</td>
                            <td>
                                <div class="table-actions">
                                    <button type="button" class="btn-icon" title="Xem chi tiết"
                                            onclick="openOrderDetail(${o.orderId})">
                                        <span class="material-symbols-outlined">visibility</span>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</section>
