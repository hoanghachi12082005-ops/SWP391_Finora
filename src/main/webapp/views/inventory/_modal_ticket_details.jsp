<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div class="modal-header">
    <h5 class="modal-title fw-bold">Chi Tiết Chứng Từ: ${ticket.ticketCode}</h5>
    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
</div>
<div class="modal-body">
    <div class="row mb-4">
        <div class="col-6 mb-3">
            <div class="text-muted small mb-1">Người Lập Phiếu</div>
            <div class="fw-bold">${ticket.createdByName}</div>
        </div>
        <div class="col-6 mb-3 text-end">
            <div class="text-muted small mb-1">Thời Gian</div>
            <div class="fw-bold">
                <fmt:parseDate value="${ticket.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDateTime" type="both" />
                <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
            </div>
        </div>
        <div class="col-6">
            <div class="text-muted small mb-1">Kho Đề Xuất</div>
            <div class="fw-bold">${ticket.fromWarehouseName}</div>
        </div>
        <div class="col-6 text-end">
            <div class="text-muted small mb-1">Kho Xử Lý</div>
            <div class="fw-bold">${ticket.toWarehouseName}</div>
        </div>
    </div>
    
    <h6 class="fw-bold mb-3">Danh Sách Sản Phẩm</h6>
    <div class="table-responsive">
        <table class="table table-bordered table-sm text-center align-middle mb-0">
            <thead class="table-light">
                <tr>
                    <th class="text-start">Sản Phẩm</th>
                    <th style="width: 100px;">Loại GD</th>
                    <c:if test="${not empty transactions}">
                        <th style="width: 80px;">Trước</th>
                    </c:if>
                    <th style="width: 90px;">Số Lượng</th>
                    <c:if test="${not empty transactions}">
                        <th style="width: 80px;">Sau</th>
                    </c:if>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="d" items="${ticketDetails}">
                    <c:set var="myTx" value="${null}"/>
                    <c:if test="${not empty transactions}">
                        <c:forEach var="tx" items="${transactions}">
                            <c:if test="${tx.productId == d.productId && (empty selectedWarehouseId || tx.warehouseId == selectedWarehouseId)}">
                                <c:set var="myTx" value="${tx}"/>
                            </c:if>
                        </c:forEach>
                        <c:if test="${empty myTx}">
                            <c:forEach var="tx" items="${transactions}">
                                <c:if test="${tx.productId == d.productId}">
                                    <c:set var="myTx" value="${tx}"/>
                                </c:if>
                            </c:forEach>
                        </c:if>
                    </c:if>
                    <tr>
                        <td class="text-start fw-medium">${d.productName}</td>
                        <td>
                            <c:set var="trueSourceId" value="${ticket.fromWarehouseId}" />
                            <c:if test="${ticket.ticketType == 'TRANSFER_REQUEST' && d.actionType == 'RECEIVE'}">
                                <c:set var="trueSourceId" value="${ticket.toWarehouseId}" />
                            </c:if>
                            
                            <c:choose>
                                <c:when test="${not empty selectedWarehouseId and selectedWarehouseId == trueSourceId}">
                                    <span class="badge bg-danger">XUẤT</span>
                                </c:when>
                                <c:when test="${not empty selectedWarehouseId and selectedWarehouseId != trueSourceId}">
                                    <span class="badge bg-success">NHẬP</span>
                                </c:when>
                                <c:otherwise>
                                    <c:choose>
                                        <c:when test="${trueSourceId == ticket.fromWarehouseId}">
                                            <span class="badge bg-danger">XUẤT</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-success">NHẬP</span>
                                        </c:otherwise>
                                    </c:choose>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <c:if test="${not empty transactions}">
                            <td class="text-muted">${myTx != null ? myTx.beforeQuantity : '-'}</td>
                        </c:if>
                        <td class="fw-bold fs-6">
                            ${d.quantity}
                        </td>
                        <c:if test="${not empty transactions}">
                            <td class="fw-bold">${myTx != null ? myTx.afterQuantity : '-'}</td>
                        </c:if>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<div class="modal-footer">
    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
    <a href="${pageContext.request.contextPath}/inventory?action=printTicket&ticketId=${ticket.ticketId}" target="_blank" class="btn btn-primary">
        In Phiếu
    </a>
</div>
