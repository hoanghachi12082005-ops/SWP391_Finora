<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<section class="table-card">
    <div class="table-scroll">
        <table class="data-table">
            <thead>
            <tr>
                <th style="width: 40px;">#</th>
                <th style="white-space: nowrap;">Mã giao dịch</th>
                <th style="white-space: nowrap;">Mã đơn hàng</th>
                <th style="white-space: nowrap;">Loại đơn</th>
                <th style="white-space: nowrap;">Loại phiếu</th>
                <th style="white-space: nowrap;">Phương thức</th>
                <th class="text-right" style="white-space: nowrap;">Số tiền</th>
                <th style="min-width: 220px; max-width: 320px;">Mô tả</th>
                <th style="white-space: nowrap;">Chi nhánh</th>
                <th style="white-space: nowrap;">Nhân viên</th>
                <th style="white-space: nowrap;">Thời gian</th>
                <th style="white-space: nowrap;">Trạng thái</th>
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
                            <td style="white-space: nowrap;"><strong>${t.transactionCode}</strong></td>
                            <td style="white-space: nowrap;">${empty t.orderCode ? '—' : t.orderCode}</td>
                            <td style="white-space: nowrap;">
                                <c:choose>
                                    <c:when test="${t.orderType == 'SALE'}"><span class="badge badge-sale">Bán hàng</span></c:when>
                                    <c:when test="${t.orderType == 'PURCHASE'}"><span class="badge badge-purchase">Nhập hàng</span></c:when>
                                    <c:otherwise><span class="badge badge-other">${empty t.orderType ? 'Thu/Chi khác' : t.orderType}</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td style="white-space: nowrap;">
                                <c:choose>
                                    <c:when test="${t.transactionType == 'INCOME'}"><span class="text-success fw-bold">Thu</span></c:when>
                                    <c:when test="${t.transactionType == 'EXPENSE'}"><span class="text-danger fw-bold">Chi</span></c:when>
                                    <c:otherwise>${t.transactionType}</c:otherwise>
                                </c:choose>
                            </td>
                            <td style="white-space: nowrap;">
                                <c:choose>
                                    <c:when test="${t.paymentMethod == 'CASH'}">Tiền mặt</c:when>
                                    <c:when test="${t.paymentMethod == 'BANK_TRANSFER'}">Chuyển khoản</c:when>
                                    <c:otherwise>${t.paymentMethod}</c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-right" style="white-space: nowrap;"><fmt:formatNumber value="${t.amount}" type="number" groupingUsed="true"/> ₫</td>
                            <td style="min-width: 220px; max-width: 320px; white-space: normal; word-break: break-word; line-height: 1.4;">${empty t.description ? '—' : t.description}</td>
                            <td style="white-space: nowrap;">${empty t.branchName ? '—' : t.branchName}</td>
                            <td style="white-space: nowrap;">${empty t.employeeName ? '—' : t.employeeName}</td>
                            <td style="white-space: nowrap;">
                                <c:choose>
                                    <c:when test="${not empty t.paymentDate}"><fmt:formatDate value="${t.paymentDate}" pattern="dd/MM/yyyy HH:mm"/></c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </td>
                            <td style="white-space: nowrap;">
                                <span class="status-badge ${t.status == 'PAID' || t.status == 'COMPLETED' ? 'completed' : ''} ${t.status == 'PENDING' ? 'pending' : ''} ${t.status == 'FAILED' ? 'cancelled' : ''}">
                                    <c:choose>
                                        <c:when test="${t.status == 'PAID'}">Đã thanh toán</c:when>
                                        <c:when test="${t.status == 'COMPLETED'}">Hoàn thành</c:when>
                                        <c:when test="${t.status == 'PENDING'}">Chờ thanh toán</c:when>
                                        <c:when test="${t.status == 'FAILED'}">Thất bại</c:when>
                                        <c:otherwise>${t.status}</c:otherwise>
                                    </c:choose>
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
