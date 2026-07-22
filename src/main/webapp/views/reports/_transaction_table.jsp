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
                <th>Mã giao dịch</th>
                <th>Mã đơn hàng</th>
                <th>Loại đơn</th>
                <th>Loại phiếu</th>
                <th>Phương thức</th>
                <th class="text-right">Số tiền</th>
                <th>Mô tả</th>
                <th>Chi nhánh</th>
                <th>Nhân viên</th>
                <th>Thời gian</th>
                <th>Trạng thái</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty transactions}">
                    <tr>
                        <td colspan="12" class="empty-row">
                            <div class="empty-state">
                                <span class="material-symbols-outlined">payments</span>
                                <h4>Không tìm thấy dữ liệu giao dịch.</h4>
                                <p>Hãy điều chỉnh bộ lọc hoặc khoảng thời gian.</p>
                            </div>
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="t" items="${transactions}" varStatus="st">
                        <tr>
                            <td>${(currentPage - 1) * pageSize + st.index + 1}</td>
                            <td><strong>${t.transactionCode}</strong></td>
                            <td>${empty t.orderCode ? '—' : t.orderCode}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${t.orderType == 'SALE'}"><span class="badge badge-sale">SALE</span></c:when>
                                    <c:when test="${t.orderType == 'PURCHASE'}"><span class="badge badge-purchase">PURCHASE</span></c:when>
                                    <c:otherwise><span class="badge badge-other">${empty t.orderType ? 'OTHER' : t.orderType}</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${t.transactionType == 'INCOME'}"><span class="text-success fw-bold">Thu</span></c:when>
                                    <c:when test="${t.transactionType == 'EXPENSE'}"><span class="text-danger fw-bold">Chi</span></c:when>
                                    <c:otherwise>${t.transactionType}</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${t.paymentMethod == 'CASH'}">Tiền mặt</c:when>
                                    <c:when test="${t.paymentMethod == 'BANK_TRANSFER'}">Chuyển khoản</c:when>
                                    <c:otherwise>${t.paymentMethod}</c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-right"><fmt:formatNumber value="${t.amount}" type="number" groupingUsed="true"/> ₫</td>
                            <td>${empty t.description ? '—' : t.description}</td>
                            <td>${empty t.branchName ? '—' : t.branchName}</td>
                            <td>${empty t.employeeName ? '—' : t.employeeName}</td>
                            <td>${fn:substring(t.paymentDate, 0, 19)}</td>
                            <td>
                                <span class="status-badge ${t.status == 'PAID' || t.status == 'COMPLETED' ? 'completed' : ''} ${t.status == 'PENDING' ? 'pending' : ''} ${t.status == 'FAILED' ? 'cancelled' : ''}">
                                    ${t.status}
                                </span>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</section>
