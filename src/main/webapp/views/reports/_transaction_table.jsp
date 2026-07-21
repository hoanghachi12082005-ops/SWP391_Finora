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
                <th>Mã giao dịch / Hóa đơn</th>
                <th>Thời gian</th>
                <th>Loại giao dịch</th>
                <th>Phương thức</th>
                <th class="text-right">Số tiền</th>
                <th>Mô tả</th>
                <th>Chi nhánh</th>
                <th>Nhân viên</th>
                <th>Trạng thái</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty transactions}">
                    <tr>
                        <td colspan="10" class="empty-row">
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
                            <td>${fn:substring(t.paymentDate, 0, 19)}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${t.transactionType == 'INCOME'}">Thu</c:when>
                                    <c:when test="${t.transactionType == 'EXPENSE'}">Chi</c:when>
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
                            <td>
                                <span class="status-badge ${t.status == 'PAID' ? 'completed' : ''} ${t.status == 'PENDING' ? 'pending' : ''} ${t.status == 'FAILED' ? 'cancelled' : ''}">
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
