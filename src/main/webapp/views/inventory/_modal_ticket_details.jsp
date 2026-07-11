<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div class="modal-header bg-light border-bottom-0 pb-0">
    <h5 class="modal-title fw-bold text-dark d-flex align-items-center" style="font-size: 18px;">
        <span class="material-icons text-primary me-2" style="font-size: 24px;">description</span>
        Chi Tiết Chứng Từ: <span class="text-primary ms-1">${ticket.ticketCode}</span>
    </h5>
    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
</div>
<div class="modal-body pt-3">
    <!-- Thông tin chung -->
    <div class="card border-0 bg-light p-3 mb-4" style="border-radius: 12px;">
        <div class="row g-3">
            <div class="col-6 col-md-3">
                <div class="text-muted small mb-1">Người Lập Phiếu</div>
                <div class="fw-bold text-dark" style="font-size: 14.5px;">${ticket.createdByName}</div>
            </div>
            <div class="col-6 col-md-3 text-md-center">
                <div class="text-muted small mb-1">Thời Gian Hoàn Tất</div>
                <div class="fw-bold text-dark" style="font-size: 14.5px;">
                    <fmt:parseDate value="${ticket.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDateTime" type="both" />
                    <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
                </div>
            </div>
            <div class="col-6 col-md-3 text-md-center">
                <div class="text-muted small mb-1">Trạng Thái</div>
                <div>
                    <c:choose>
                        <c:when test="${ticket.status == 'COMPLETED'}">
                            <span class="badge bg-success" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">HOÀN TẤT</span>
                        </c:when>
                        <c:when test="${ticket.status == 'COMPLETED_WITH_ERROR'}">
                            <span class="badge bg-warning text-dark" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">HOÀN TẤT (LỆCH)</span>
                        </c:when>
                        <c:when test="${ticket.status == 'REJECTED' || ticket.status == 'CANCELLED'}">
                            <span class="badge bg-danger" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">ĐÃ HỦY / TỪ CHỐI</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-secondary" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">${ticket.status}</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="col-6 col-md-3 text-end">
                <div class="text-muted small mb-1">Mã Phiếu</div>
                <div class="fw-bold text-dark" style="font-size: 14.5px;">#${ticket.ticketId}</div>
            </div>
        </div>
        <hr class="my-3 text-muted" style="opacity: 0.15;">
        <div class="row g-3">
            <div class="col-6">
                <div class="text-muted small mb-1">
                    <c:choose>
                        <c:when test="${ticket.ticketType == 'IMPORT'}">Nguồn / Nhà Cung Cấp</c:when>
                        <c:otherwise>Kho Chuyển (Nguồn)</c:otherwise>
                    </c:choose>
                </div>
                <div class="fw-bold text-primary" style="font-size: 14.5px;">
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
                <div class="fw-bold text-success" style="font-size: 14.5px;">
                    <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">storefront</span>
                    ${ticket.toWarehouseName}
                </div>
            </div>
        </div>
    </div>
    
    <!-- Danh sách sản phẩm -->
    <h6 class="fw-bold mb-3 text-dark d-flex align-items-center" style="font-size: 14.5px;">
        <span class="material-icons text-secondary me-1" style="font-size: 18px;">list</span>
        Chi Tiết Hàng Hóa Giao Dịch
    </h6>
    <div class="table-responsive border rounded-3 mb-4 shadow-sm" style="overflow: hidden;">
        <table class="table table-hover align-middle mb-0" style="font-size: 13.5px;">
            <thead class="table-light text-dark" style="border-bottom: 2px solid #e2e8f0;">
                <tr>
                    <th class="text-start ps-3 py-3" style="font-weight: 600; color: #475569;">Sản Phẩm</th>
                    <th class="text-center py-3" style="width: 130px; font-weight: 600; color: #475569;">Loại GD</th>
                    <th class="text-end py-3" style="width: 110px; font-weight: 600; color: #475569;">Tồn Trước</th>
                    <th class="text-center py-3" style="width: 100px; font-weight: 600; color: #475569;">Số Lượng</th>
                    <th class="text-end pe-3 py-3" style="width: 110px; font-weight: 600; color: #475569;">Tồn Sau</th>
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
                        <td class="text-start fw-semibold ps-3 py-3 text-dark">${d.productName}</td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${ticket.ticketType == 'IMPORT'}">
                                    <span class="badge bg-success-subtle text-success border border-success-subtle" style="padding: 4px 8px; font-size: 11px;">NHẬP HÀNG</span>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="trueSourceId" value="${ticket.fromWarehouseId}" />
                                    <c:if test="${ticket.ticketType == 'TRANSFER_REQUEST' && d.actionType == 'RECEIVE'}">
                                        <c:set var="trueSourceId" value="${ticket.toWarehouseId}" />
                                    </c:if>
                                    
                                    <c:choose>
                                        <c:when test="${not empty selectedWarehouseId and selectedWarehouseId == trueSourceId}">
                                            <span class="badge bg-danger-subtle text-danger border border-danger-subtle" style="padding: 4px 8px; font-size: 11px;">XUẤT KHO</span>
                                        </c:when>
                                        <c:when test="${not empty selectedWarehouseId and selectedWarehouseId != trueSourceId}">
                                            <span class="badge bg-success-subtle text-success border border-success-subtle" style="padding: 4px 8px; font-size: 11px;">NHẬP KHO</span>
                                        </c:when>
                                        <c:otherwise>
                                            <c:choose>
                                                <c:when test="${trueSourceId == ticket.fromWarehouseId}">
                                                    <span class="badge bg-danger-subtle text-danger border border-danger-subtle" style="padding: 4px 8px; font-size: 11px;">XUẤT KHO</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-success-subtle text-success border border-success-subtle" style="padding: 4px 8px; font-size: 11px;">NHẬP KHO</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:otherwise>
                                    </c:choose>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end fw-semibold text-muted">
                            ${myTx != null ? myTx.beforeQuantity : '-'}
                        </td>
                        <td class="fw-bold text-center text-primary" style="font-size: 14.5px;">
                            ${d.quantity}
                        </td>
                        <td class="fw-bold text-end text-success pe-3">
                            ${myTx != null ? myTx.afterQuantity : '-'}
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    
    <!-- Thông tin điều chuyển chi tiết (Xuất / Nhập thực tế) -->
    <c:if test="${not empty txTicket}">
        <div class="card mt-4 border shadow-sm" style="border-radius: 8px; overflow: hidden; border-color: #dee2e6;">
            <div class="card-header bg-light-subtle py-3 d-flex justify-content-between align-items-center" style="border-bottom: 1px solid #e2e8f0; background-color: #f8fafc;">
                <div class="fw-bold text-primary d-flex align-items-center" style="font-size: 13.5px; text-transform: uppercase; letter-spacing: 0.5px;">
                    <span class="material-icons me-1" style="font-size: 18px; vertical-align: text-bottom;">call_made</span>
                    Bước 1: Chi Tiết Thực Tế Xuất Kho
                </div>
                <div style="font-size: 13px; font-weight: 500;" class="text-muted">
                    Từ: <strong class="text-dark">${txTicket.fromWarehouseName}</strong> ➜ Đến: <strong class="text-dark">${txTicket.toWarehouseName}</strong>
                </div>
            </div>
            <div class="card-body p-0">
                <table class="table table-hover text-center align-middle mb-0" style="font-size: 13px; border-style: hidden;">
                    <thead class="table-light text-dark" style="border-bottom: 2px solid #e2e8f0;">
                        <tr>
                            <th class="text-start ps-3 py-2" style="font-weight: 600; color: #475569;">Sản Phẩm</th>
                            <th style="width: 100px;" class="fw-bold py-2 text-end pe-3">Tồn Trước</th>
                            <th style="width: 100px;" class="fw-bold py-2 text-center">Yêu Cầu</th>
                            <th style="width: 110px;" class="fw-bold py-2 text-center text-danger">Thực Tế Xuất</th>
                            <th style="width: 100px;" class="fw-bold py-2 text-end pe-3">Tồn Sau</th>
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
                                        <td class="text-end text-muted pe-3">${myTx.beforeQuantity}</td>
                                        <td class="text-center">${td.quantity}</td>
                                        <td class="fw-bold text-center text-danger">${td.actualQuantity != null ? td.actualQuantity : (txTicket.status == 'COMPLETED' ? td.quantity : '-')}</td>
                                        <td class="fw-bold text-end text-success pe-3">${myTx.afterQuantity}</td>
                                    </c:when>
                                    <c:otherwise>
                                        <td class="text-end text-muted pe-3">${txCurrentStock[td.productId] != null ? txCurrentStock[td.productId] : '-'}</td>
                                        <td class="text-center">${td.quantity}</td>
                                        <td class="fw-bold text-center text-danger">${td.actualQuantity != null ? td.actualQuantity : (txTicket.status == 'COMPLETED' ? td.quantity : '-')}</td>
                                        <td class="fw-bold text-end text-success pe-3">${txCurrentStock[td.productId] != null ? txCurrentStock[td.productId] - td.quantity : '-'}</td>
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
            
            <div class="card-footer bg-light-subtle py-2 px-3 d-flex justify-content-between align-items-center" style="font-size: 12.5px; border-top: 1px dashed #e2e8f0; background-color: #f8fafc;">
                <span class="text-muted">Nhân sự xuất: <strong class="text-dark">${txTicket.createdByName}</strong></span>
                <c:if test="${not empty exportTime}">
                    <span class="text-muted">Thời gian xuất: <strong class="text-dark">
                        <fmt:parseDate value="${exportTime}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedTxTime" type="both" />
                        <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedTxTime}" />
                    </strong></span>
                </c:if>
                <span class="text-muted">Trạng thái xuất: 
                    <strong class="${txTicket.status == 'COMPLETED' ? 'text-success' : 'text-warning'}">
                        ${txTicket.status == 'COMPLETED' ? 'Đã xuất kho' : (txTicket.status == 'COMPLETED_WITH_ERROR' ? 'Xuất lệch' : txTicket.status)}
                    </strong>
                </span>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty tiTicket}">
        <div class="card mt-4 border shadow-sm" style="border-radius: 8px; overflow: hidden; border-color: #dee2e6;">
            <div class="card-header bg-light-subtle py-3 d-flex justify-content-between align-items-center" style="border-bottom: 1px solid #e2e8f0; background-color: #f8fafc;">
                <div class="fw-bold text-success d-flex align-items-center" style="font-size: 13.5px; text-transform: uppercase; letter-spacing: 0.5px;">
                    <span class="material-icons me-1" style="font-size: 18px; vertical-align: text-bottom;">call_received</span>
                    Bước 2: Chi Tiết Thực Tế Nhập Kho
                </div>
                <div style="font-size: 13px; font-weight: 500;" class="text-muted">
                    Nhập tại kho: <strong class="text-dark">${tiTicket.toWarehouseName}</strong>
                </div>
            </div>
            <div class="card-body p-0">
                <table class="table table-hover text-center align-middle mb-0" style="font-size: 13px; border-style: hidden;">
                    <thead class="table-light text-dark" style="border-bottom: 2px solid #e2e8f0;">
                        <tr>
                            <th class="text-start ps-3 py-2" style="font-weight: 600; color: #475569;">Sản Phẩm</th>
                            <th style="width: 100px;" class="fw-bold py-2 text-end pe-3">Tồn Trước</th>
                            <th style="width: 100px;" class="fw-bold py-2 text-center">Yêu Cầu</th>
                            <th style="width: 110px;" class="fw-bold py-2 text-center text-success">Thực Tế Nhập</th>
                            <th style="width: 100px;" class="fw-bold py-2 text-end pe-3">Tồn Sau</th>
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
                                        <td class="text-end text-muted pe-3">${myTi.beforeQuantity}</td>
                                        <td class="text-center">${tid.quantity}</td>
                                        <td class="fw-bold text-center text-success">${tid.actualQuantity != null ? tid.actualQuantity : (tiTicket.status == 'COMPLETED' ? tid.quantity : '-')}</td>
                                        <td class="fw-bold text-end text-success pe-3">${myTi.afterQuantity}</td>
                                    </c:when>
                                    <c:otherwise>
                                        <td class="text-end text-muted pe-3">${tiCurrentStock[tid.productId] != null ? tiCurrentStock[tid.productId] : '-'}</td>
                                        <td class="text-center">${tid.quantity}</td>
                                        <td class="fw-bold text-center text-success">${tid.actualQuantity != null ? tid.actualQuantity : (tiTicket.status == 'COMPLETED' ? tid.quantity : '-')}</td>
                                        <td class="fw-bold text-end text-success pe-3">${tiCurrentStock[tid.productId] != null ? tiCurrentStock[tid.productId] + tid.quantity : '-'}</td>
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
            
            <div class="card-footer bg-light-subtle py-2 px-3 d-flex justify-content-between align-items-center" style="font-size: 12.5px; border-top: 1px dashed #e2e8f0; background-color: #f8fafc;">
                <span class="text-muted">Nhân sự nhập: <strong class="text-dark">${tiTicket.createdByName}</strong></span>
                <c:if test="${not empty importTime}">
                    <span class="text-muted">Thời gian nhập: <strong class="text-dark">
                        <fmt:parseDate value="${importTime}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedTiTime" type="both" />
                        <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedTiTime}" />
                    </strong></span>
                </c:if>
                <span class="text-muted">Trạng thái nhập: 
                    <strong class="${tiTicket.status == 'COMPLETED' ? 'text-success' : 'text-warning'}">
                        ${tiTicket.status == 'COMPLETED' ? 'Đã nhập kho' : (tiTicket.status == 'COMPLETED_WITH_ERROR' ? 'Nhập lệch' : tiTicket.status)}
                    </strong>
                </span>
            </div>
        </div>
    </c:if>

</div>
<div class="modal-footer border-top-0 pt-0">
    <button type="button" class="btn btn-secondary px-4" data-bs-dismiss="modal" style="border-radius: 8px;">Đóng</button>
    <a href="${pageContext.request.contextPath}/inventory?action=printTicket&ticketId=${ticket.ticketId}" target="_blank" class="btn btn-primary px-4 d-inline-flex align-items-center" style="border-radius: 8px;">
        <span class="material-icons me-1" style="font-size: 18px;">print</span>
        In Phiếu
    </a>
</div>
