<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%-- 
  ==========================================================================
  MODAL HIỂN THỊ CHI TIẾT ĐƠN HÀNG NHẬP/XUẤT (_modal_order_details.jsp)
  - Được load qua AJAX từ `OrderVoucherController` (action=viewOrderDetails).
  - Hiển thị danh sách sản phẩm, nhà cung cấp, đơn giá, tổng tiền của đơn nhập/xuất kho.
  ==========================================================================
--%>
<div class="modal-header bg-light border-bottom-0 pb-0">
    <h5 class="modal-title fw-bold text-dark d-flex align-items-center" style="font-size: 18px;">
        <span class="material-icons text-primary me-2" style="font-size: 24px;">receipt_long</span>
        Chi Tiết Phiếu ${order.orderType == 'PURCHASE' ? 'Nhập Hàng' : 'Xuất Hàng'}: <span class="text-primary ms-1">${order.orderCode}</span>
    </h5>
    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
</div>
<div class="modal-body pt-3">
    <!-- Thông tin chung -->
    <div class="card border-0 bg-light p-3 mb-4" style="border-radius: 12px;">
        <div class="row g-3">
            <div class="col-6 col-md-3">
                <div class="text-muted small mb-1">Người Tạo Phiếu</div>
                <div class="fw-bold text-dark" style="font-size: 14.5px;">${order.employeeName}</div>
            </div>
            <div class="col-6 col-md-3 text-md-center">
                <div class="text-muted small mb-1">Thời Gian Tạo</div>
                <div class="fw-bold text-dark" style="font-size: 14.5px;">
                    <fmt:parseDate value="${order.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" var="parsedDateTime" type="both" />
                    <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
                </div>
            </div>
            <div class="col-6 col-md-3 text-md-center">
                <div class="text-muted small mb-1">Trạng Thái</div>
                <div>
                    <c:choose>
                        <c:when test="${order.status == 'PENDING'}">
                            <span class="badge bg-warning text-dark" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">CHỜ DUYỆT</span>
                        </c:when>
                        <c:when test="${order.status == 'COMPLETED'}">
                            <span class="badge bg-success" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">HOÀN THÀNH</span>
                        </c:when>
                        <c:when test="${order.status == 'CANCELLED'}">
                            <span class="badge bg-danger" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">ĐÃ HỦY</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-secondary" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">${order.status}</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="col-6 col-md-3 text-end">
                <div class="text-muted small mb-1">ID Hệ Thống</div>
                <div class="fw-bold text-dark" style="font-size: 14.5px;">#${order.orderId}</div>
            </div>
        </div>
        <hr class="my-3 text-muted" style="opacity: 0.15;">
        <div class="row g-3">
            <div class="col-6">
                <div class="text-muted small mb-1">Chi Nhánh</div>
                <div class="fw-bold text-dark" style="font-size: 14.5px;">
                    <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px; color:#4f46e5;">storefront</span>
                    ${order.branchName}
                </div>
            </div>
            <div class="col-6 text-end">
                <div class="text-muted small mb-1">Đối Tác</div>
                <div class="fw-bold text-dark" style="font-size: 14.5px;">
                    <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px; color:#10b981;">business</span>
                    ${not empty order.customerName ? order.customerName : 'Nhiều Nhà Cung Cấp / Vãng Lai'}
                </div>
            </div>
        </div>
    </div>
    
    <!-- Danh sách sản phẩm -->
    <h6 class="fw-bold mb-3 text-dark d-flex align-items-center" style="font-size: 14.5px;">
        <span class="material-icons text-secondary me-1" style="font-size: 18px;">list</span>
        Chi Tiết Hàng Hóa
    </h6>
    <div class="table-responsive border rounded-3 mb-4 shadow-sm" style="overflow: hidden;">
        <table class="table table-hover align-middle mb-0" style="font-size: 13.5px;">
            <thead class="table-light text-dark" style="border-bottom: 2px solid #e2e8f0;">
                <tr>
                    <th class="text-start ps-3 py-3" style="font-weight: 600; color: #475569;">Sản Phẩm</th>
                    <th class="text-start py-3" style="font-weight: 600; color: #475569;">Nhà Cung Cấp</th>
                    <th class="text-center py-3" style="width: 120px; font-weight: 600; color: #475569;">Đơn Giá</th>
                    <th class="text-center py-3" style="width: 100px; font-weight: 600; color: #475569;">Số Lượng</th>
                    <th class="text-end pe-3 py-3" style="width: 140px; font-weight: 600; color: #475569;">Thành Tiền</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="d" items="${orderDetails}">
                    <tr>
                        <td class="text-start fw-semibold ps-3 py-3 text-dark">
                            ${d.productName}<br>
                            <small class="text-muted">${d.productCode}</small>
                        </td>
                        <td class="text-start text-muted py-3">
                            ${not empty d.supplierName ? d.supplierName : '-'}
                        </td>
                        <td class="text-center text-muted">
                            <fmt:formatNumber value="${d.unitPrice}" type="currency" currencySymbol="₫"/>
                        </td>
                        <td class="fw-bold text-center text-primary" style="font-size: 14.5px;">${d.quantity}</td>
                        <td class="text-end fw-bold text-dark pe-3">
                            <fmt:formatNumber value="${d.totalPrice}" type="currency" currencySymbol="₫"/>
                        </td>
                    </tr>
                </c:forEach>
                <!-- Dòng tổng cộng -->
                <tr class="table-light fw-bold" style="border-top: 2px solid #e2e8f0;">
                    <td colspan="4" class="text-end py-3">Tổng cộng:</td>
                    <td class="text-end text-danger pe-3 py-3" style="font-size: 16px;">
                        <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <!-- Lịch sử giao dịch kho phát sinh -->
    <c:if test="${not empty transactions}">
        <h6 class="fw-bold mb-3 text-dark d-flex align-items-center" style="font-size: 14.5px;">
            <span class="material-icons text-success me-1" style="font-size: 18px;">history</span>
            Biến Động Kho Ghi Nhận
        </h6>
        <div class="table-responsive border rounded-3 mb-4 shadow-sm" style="overflow: hidden;">
            <table class="table table-hover align-middle mb-0" style="font-size: 13px;">
                <thead class="table-light text-dark" style="border-bottom: 2px solid #e2e8f0;">
                    <tr>
                        <th class="ps-3 py-2">Kho</th>
                        <th>Sản Phẩm</th>
                        <th class="text-center">Loại GD</th>
                        <th class="text-center">Số Lượng</th>
                        <th class="text-end">Tồn Trước</th>
                        <th class="text-end pe-3">Tồn Sau</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="tx" items="${transactions}">
                        <tr>
                            <td class="ps-3 text-dark fw-medium">${tx.warehouseName}</td>
                            <td>${tx.productName}</td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${tx.transactionType == 'IMPORT'}">
                                        <span class="badge bg-success-subtle text-success border border-success-subtle" style="padding: 3px 6px; font-size: 10.5px;">NHẬP HÀNG</span>
                                    </c:when>
                                    <c:when test="${tx.transactionType == 'EXPORT'}">
                                        <span class="badge bg-danger-subtle text-danger border border-danger-subtle" style="padding: 3px 6px; font-size: 10.5px;">XUẤT HÀNG</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary-subtle text-secondary" style="padding: 3px 6px; font-size: 10.5px;">${tx.transactionType}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-center fw-bold ${tx.transactionType == 'IMPORT' ? 'text-success' : 'text-danger'}">
                                ${tx.transactionType == 'IMPORT' ? '+' : '-'}${tx.quantity}
                            </td>
                            <td class="text-end text-muted">${tx.beforeQuantity}</td>
                            <td class="text-end fw-semibold text-dark pe-3">${tx.afterQuantity}</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>
</div>
<div class="modal-footer border-top-0 pt-0">
    <button type="button" class="btn btn-secondary px-4" data-bs-dismiss="modal" style="border-radius: 8px;">Đóng</button>
    <a href="${pageContext.request.contextPath}/inventory?action=printOrder&orderId=${order.orderId}" target="_blank" class="btn btn-primary px-4 d-inline-flex align-items-center" style="border-radius: 8px;">
        <span class="material-icons me-1" style="font-size: 18px;">print</span>
        In Phiếu / Hóa Đơn
    </a>
</div>
