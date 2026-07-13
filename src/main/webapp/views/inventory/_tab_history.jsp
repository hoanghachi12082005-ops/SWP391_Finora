<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="dashboard-card mb-4">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Lịch sử giao dịch</h5>
    </div>

    <!-- Filter form can go here -->

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
                                <th>Loại</th>
                                <th>Sản Phẩm</th>
                                <th>Người Thực Hiện</th>
                                <th>Ghi Chú</th>
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
                                        <c:choose>
                                            <c:when test="${tx.transactionType == 'IMPORT' || tx.transactionType == 'TRANSFER_IN' || tx.transactionType == 'RETURN'}">
                                                <span class="badge" style="background-color: #059669; color: #fff; font-size: 11px; padding: 4px 10px;">
                                                    <span class="material-icons" style="font-size: 13px; vertical-align: text-bottom;">call_received</span>
                                                    Nhập Kho
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background-color: #e11d48; color: #fff; font-size: 11px; padding: 4px 10px;">
                                                    <span class="material-icons" style="font-size: 13px; vertical-align: text-bottom;">call_made</span>
                                                    Xuất Kho
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                     <td>
                                         <strong>${tx.productName}</strong>
                                     </td>
                                     <td>${tx.createdByName}</td>
                                    <td>
                                        ${tx.note}
                                        <c:if test="${not empty tx.referenceType and not empty tx.referenceId}">
                                            <br>
                                            <c:choose>
                                                <c:when test="${tx.referenceType == 'STOCK_TRANSFER'}">
                                                    <a href="javascript:void(0)" class="text-primary small fw-semibold" style="font-size: 11.5px; text-decoration: none;" onclick="viewTicketDetails(${tx.referenceId})">
                                                        <span class="material-icons" style="font-size: 13px; vertical-align: text-bottom;">open_in_new</span> Xem phiếu gốc
                                                    </a>
                                                </c:when>
                                                <c:when test="${tx.referenceType == 'PURCHASE_ORDER' || tx.referenceType == 'EXPORT_ORDER'}">
                                                    <a href="javascript:void(0)" class="text-primary small fw-semibold" style="font-size: 11.5px; text-decoration: none;" onclick="viewOrderDetails(${tx.referenceId})">
                                                        <span class="material-icons" style="font-size: 13px; vertical-align: text-bottom;">open_in_new</span> Xem phiếu gốc
                                                    </a>
                                                </c:when>
                                            </c:choose>
                                        </c:if>
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
