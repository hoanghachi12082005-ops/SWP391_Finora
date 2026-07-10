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
            <div class="text-muted small mb-1">
                <c:choose>
                    <c:when test="${ticket.ticketType == 'IMPORT'}">Nguồn / Nhà Cung Cấp</c:when>
                    <c:otherwise>Kho Chuyển (Nguồn)</c:otherwise>
                </c:choose>
            </div>
            <div class="fw-bold text-primary">
                <c:choose>
                    <c:when test="${ticket.ticketType == 'IMPORT'}">
                        <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">local_shipping</span>
                    </c:when>
                    <c:otherwise>
                        <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">storefront</span>
                    </c:otherwise>
                </c:choose>
                ${ticket.fromWarehouseName}
            </div>
        </div>
        <div class="col-6 text-end">
            <div class="text-muted small mb-1">
                <c:choose>
                    <c:when test="${ticket.ticketType == 'IMPORT'}">Kho Nhập (Đích)</c:when>
                    <c:otherwise>Kho Nhận (Đích)</c:otherwise>
                </c:choose>
            </div>
            <div class="fw-bold text-success">
                <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">storefront</span>
                ${ticket.toWarehouseName}
            </div>
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
                            <c:choose>
                                <c:when test="${ticket.ticketType == 'IMPORT'}">
                                    <span class="badge bg-success">NHẬP HÀNG</span>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="trueSourceId" value="${ticket.fromWarehouseId}" />
                                    <c:if test="${ticket.ticketType == 'TRANSFER_REQUEST' && d.actionType == 'RECEIVE'}">
                                        <c:set var="trueSourceId" value="${ticket.toWarehouseId}" />
                                    </c:if>
                                    
                                    <c:choose>
                                        <c:when test="${not empty selectedWarehouseId and selectedWarehouseId == trueSourceId}">
                                            <span class="badge bg-danger">XUẤT KHO</span>
                                        </c:when>
                                        <c:when test="${not empty selectedWarehouseId and selectedWarehouseId != trueSourceId}">
                                            <span class="badge bg-success">NHẬP KHO</span>
                                        </c:when>
                                        <c:otherwise>
                                            <c:choose>
                                                <c:when test="${trueSourceId == ticket.fromWarehouseId}">
                                                    <span class="badge bg-danger">XUẤT KHO</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-success">NHẬP KHO</span>
                                                </c:otherwise>
                                            </c:choose>
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
    
    <c:if test="${not empty txTicket}">
        <div class="card mt-4 border shadow-sm" style="border-radius: 4px; overflow: hidden; border-color: #dee2e6;">
            <div class="card-header bg-light py-2 d-flex justify-content-between align-items-center" style="border-bottom: 1px solid #dee2e6;">
                <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px; text-transform: uppercase;">
                    Phiếu Xuất Kho Tương Ứng
                </div>
                <div style="font-size: 13px; font-weight: 500;" class="text-dark">
                    Từ: <strong>${txTicket.fromWarehouseName}</strong>
                </div>
            </div>
            <div class="card-body p-0">
                <table class="table table-bordered text-center align-middle mb-0" style="font-size: 14px; border-style: hidden;">
                    <thead class="table-light text-dark" style="border-bottom: 2px solid #dee2e6;">
                        <tr>
                            <th class="text-start ps-3 fw-bold py-2">Sản Phẩm</th>
                            <th style="width: 100px;" class="fw-bold py-2">Tồn Trước</th>
                            <th style="width: 100px;" class="fw-bold py-2">Yêu Cầu</th>
                            <th style="width: 110px;" class="fw-bold py-2">Thực Tế Xuất</th>
                            <th style="width: 100px;" class="fw-bold py-2">Tồn Sau</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="td" items="${txDetails}">
                            <c:set var="myTx" value="${null}"/>
                            <c:if test="${not empty txTransactions}">
                                <c:forEach var="trx" items="${txTransactions}">
                                    <c:if test="${trx.productId == td.productId}">
                                        <c:set var="myTx" value="${trx}"/>
                                    </c:if>
                                </c:forEach>
                            </c:if>
                            <tr>
                                <td class="text-start fw-medium ps-3 text-dark">${td.productName}</td>
                                <c:choose>
                                    <c:when test="${not empty myTx}">
                                        <td class="text-dark">${myTx.beforeQuantity}</td>
                                        <td class="text-dark">${td.quantity}</td>
                                        <td class="fw-bold text-dark">${td.actualQuantity != null ? td.actualQuantity : ''}</td>
                                        <td class="fw-bold text-dark">${myTx.afterQuantity}</td>
                                    </c:when>
                                    <c:otherwise>
                                        <td class="text-dark">${txCurrentStock[td.productId] != null ? txCurrentStock[td.productId] : '-'}</td>
                                        <td class="text-dark">${td.quantity}</td>
                                        <td class="fw-bold text-dark">${td.actualQuantity != null ? td.actualQuantity : ''}</td>
                                        <td class="fw-bold text-dark">${txCurrentStock[td.productId] != null ? txCurrentStock[td.productId] - td.quantity : '-'}</td>
                                    </c:otherwise>
                                </c:choose>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            
            <c:set var="exportTime" value="${null}"/>
            <c:if test="${not empty txTransactions}">
                <c:forEach var="trx" items="${txTransactions}" end="0">
                    <c:set var="exportTime" value="${trx.createdAt}"/>
                </c:forEach>
            </c:if>
            
            <div class="card-footer bg-white py-2 d-flex justify-content-between align-items-center" style="font-size: 13px; border-top: 1px dashed #dee2e6;">
                <span class="text-dark">Nhân sự xuất: <strong>${txTicket.createdByName}</strong></span>
                <c:if test="${not empty exportTime}">
                    <span class="text-dark">Thời gian xuất: <strong>
                        <fmt:parseDate value="${exportTime}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedTxTime" type="both" />
                        <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedTxTime}" />
                    </strong></span>
                </c:if>
                <span class="text-dark">Trạng thái: <strong>${txTicket.status == 'COMPLETED' ? 'Hoàn Tất' : (txTicket.status == 'COMPLETED_WITH_ERROR' ? 'Hoàn Tất (Lệch)' : txTicket.status)}</strong></span>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty tiTicket}">
        <div class="card mt-4 border shadow-sm" style="border-radius: 4px; overflow: hidden; border-color: #dee2e6;">
            <div class="card-header bg-light py-2 d-flex justify-content-between align-items-center" style="border-bottom: 1px solid #dee2e6;">
                <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px; text-transform: uppercase;">
                    Phiếu Nhập Kho Tương Ứng
                </div>
                <div style="font-size: 13px; font-weight: 500;" class="text-dark">
                    Đến: <strong>${tiTicket.toWarehouseName}</strong>
                </div>
            </div>
            <div class="card-body p-0">
                <table class="table table-bordered text-center align-middle mb-0" style="font-size: 14px; border-style: hidden;">
                    <thead class="table-light text-dark" style="border-bottom: 2px solid #dee2e6;">
                        <tr>
                            <th class="text-start ps-3 fw-bold py-2">Sản Phẩm</th>
                            <th style="width: 100px;" class="fw-bold py-2">Tồn Trước</th>
                            <th style="width: 100px;" class="fw-bold py-2">Yêu Cầu</th>
                            <th style="width: 110px;" class="fw-bold py-2">Thực Tế Nhập</th>
                            <th style="width: 100px;" class="fw-bold py-2">Tồn Sau</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="tid" items="${tiDetails}">
                            <c:set var="myTi" value="${null}"/>
                            <c:if test="${not empty tiTransactions}">
                                <c:forEach var="trx" items="${tiTransactions}">
                                    <c:if test="${trx.productId == tid.productId}">
                                        <c:set var="myTi" value="${trx}"/>
                                    </c:if>
                                </c:forEach>
                            </c:if>
                            <tr>
                                <td class="text-start fw-medium ps-3 text-dark">${tid.productName}</td>
                                <c:choose>
                                    <c:when test="${not empty myTi}">
                                        <td class="text-dark">${myTi.beforeQuantity}</td>
                                        <td class="text-dark">${tid.quantity}</td>
                                        <td class="fw-bold text-dark">${tid.actualQuantity != null ? tid.actualQuantity : ''}</td>
                                        <td class="fw-bold text-dark">${myTi.afterQuantity}</td>
                                    </c:when>
                                    <c:otherwise>
                                        <td class="text-dark">${tiCurrentStock[tid.productId] != null ? tiCurrentStock[tid.productId] : '-'}</td>
                                        <td class="text-dark">${tid.quantity}</td>
                                        <td class="fw-bold text-dark">${tid.actualQuantity != null ? tid.actualQuantity : ''}</td>
                                        <td class="fw-bold text-dark">${tiCurrentStock[tid.productId] != null ? tiCurrentStock[tid.productId] + tid.quantity : '-'}</td>
                                    </c:otherwise>
                                </c:choose>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            
            <c:set var="importTime" value="${null}"/>
            <c:if test="${not empty tiTransactions}">
                <c:forEach var="trx" items="${tiTransactions}" end="0">
                    <c:set var="importTime" value="${trx.createdAt}"/>
                </c:forEach>
            </c:if>
            
            <div class="card-footer bg-white py-2 d-flex justify-content-between align-items-center" style="font-size: 13px; border-top: 1px dashed #dee2e6;">
                <span class="text-dark">Nhân sự nhập: <strong>${tiTicket.createdByName}</strong></span>
                <c:if test="${not empty importTime}">
                    <span class="text-dark">Thời gian nhập: <strong>
                        <fmt:parseDate value="${importTime}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedTiTime" type="both" />
                        <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedTiTime}" />
                    </strong></span>
                </c:if>
                <span class="text-dark">Trạng thái: <strong>${tiTicket.status == 'COMPLETED' ? 'Hoàn Tất' : (tiTicket.status == 'COMPLETED_WITH_ERROR' ? 'Hoàn Tất (Lệch)' : tiTicket.status)}</strong></span>
            </div>
        </div>
    </c:if>

</div>
<div class="modal-footer">
    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
    <a href="${pageContext.request.contextPath}/inventory?action=printTicket&ticketId=${ticket.ticketId}" target="_blank" class="btn btn-primary">
        In Phiếu
    </a>
</div>
