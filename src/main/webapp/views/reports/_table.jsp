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
                <th>Ma don</th>
                <th>Chi nhanh</th>
                <th>Nhan vien</th>
                <th>Khach hang</th>
                <th class="text-right">Tong tien</th>
                <th>Phuong thuc</th>
                <th>Trang thai</th>
                <th>Ngay tao</th>
                <th>Thao tac</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty orders}">
                    <tr>
                        <td colspan="10" class="empty-row">
                            <div class="empty-state">
                                <span class="material-symbols-outlined">receipt_long</span>
                                <h4>Khong tim thay don hang</h4>
                                <p>Hay dieu chinh bo loc hoac khoang thoi gian.</p>
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
                            <td>${empty o.customerName ? 'Khach vang lai' : o.customerName}</td>
                            <td class="text-right"><fmt:formatNumber value="${o.totalAmount}" type="number" groupingUsed="true"/> ₫</td>
                            <td>
                                <c:choose>
                                    <c:when test="${o.paymentMethod == 'CASH'}">Tien mat</c:when>
                                    <c:when test="${o.paymentMethod == 'CARD'}">The</c:when>
                                    <c:when test="${o.paymentMethod == 'TRANSFER'}">Chuyen khoan</c:when>
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
                            <td>
                                <div class="table-actions">
                                    <button type="button" class="btn-icon" title="Xem chi tiet"
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
