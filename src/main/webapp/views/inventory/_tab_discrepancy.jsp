<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-3">
        <h5>Danh sách Phiếu Hao Hụt & Lỗi</h5>
    </div>

    <div class="premium-table-container">
        <table class="table premium-table mb-0">
            <thead>
                <tr>
                    <th>Mã Phiếu</th>
                    <th>Nơi Phát Hiện</th>
                    <th>Tham Chiếu</th>
                    <th>Người Tạo</th>
                    <th>Thời Gian</th>
                    <th>Ghi Chú</th>
                    <th width="100px">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty discrepancies}">
                        <tr>
                            <td colspan="7" class="text-center py-4 text-muted">Không có dữ liệu hao hụt</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="tx" items="${discrepancies}">
                            <tr>
                                <td class="fw-medium text-danger">${tx.ticketCode}</td>
                                <td>${tx.fromWarehouseName}</td>
                                <td>
                                    <!-- We stored the original transferId in toWarehouseId -->
                                    <button class="btn btn-sm btn-link p-0 text-decoration-none" onclick="viewTicketDetails(${tx.toWarehouseId})">Xem phiếu gốc</button>
                                </td>
                                <td>${tx.createdByName}</td>
                                <td>
                                    <fmt:parseDate value="${tx.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDateTime" type="both" />
                                    <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
                                </td>
                                <td><span class="text-muted small">${tx.note}</span></td>
                                <td>
                                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="viewTicketDetails(${tx.ticketId})">Chi tiết</button>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>
