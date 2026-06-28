<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div class="modal-header">
    <h5 class="modal-title fw-bold">Chi tiết phiếu: ${ticket.ticketCode}</h5>
    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
</div>
<div class="modal-body pb-0">
    <div class="row mb-3">
        <div class="col-md-6">
            <p class="mb-1 text-muted small">Người tạo</p>
            <p class="fw-medium">${ticket.createdByName}</p>
        </div>
        <div class="col-md-6 text-md-end">
            <p class="mb-1 text-muted small">Thời gian</p>
            <p class="fw-medium">
                <fmt:parseDate value="${ticket.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDateTime" type="both" />
                <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
            </p>
        </div>
        <div class="col-md-6">
            <p class="mb-1 text-muted small">Kho Đề Xuất</p>
            <p class="fw-medium text-primary">${ticket.fromWarehouseName}</p>
        </div>
        <div class="col-md-6 text-md-end">
            <p class="mb-1 text-muted small">Kho Xử Lý</p>
            <p class="fw-medium text-success">${ticket.toWarehouseName}</p>
        </div>
    </div>
    
    <h6 class="fw-bold mb-3">Danh sách sản phẩm</h6>
    <div class="table-responsive">
        <table class="table table-bordered table-sm text-center align-middle">
            <thead class="table-light">
                <tr>
                    <th class="text-start">Sản phẩm</th>
                    <th width="120px">Loại GD</th>
                    <th width="100px">Số lượng</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="d" items="${ticketDetails}">
                    <tr>
                        <td class="text-start fw-medium">${d.productName}</td>
                        <td>
                            <c:choose>
                                <c:when test="${d.actionType == 'SEND'}">
                                    <span class="badge bg-danger">XUẤT</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-success">NHẬP</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="fw-bold fs-5">${d.quantity}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<div class="modal-footer border-top-0 pt-0">
    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Đóng</button>
    <a href="${pageContext.request.contextPath}/inventory?action=printTicket&ticketId=${ticket.ticketId}" target="_blank" class="btn btn-primary d-flex align-items-center gap-2">
        <i class="ph ph-printer"></i> In Phiếu
    </a>
</div>
