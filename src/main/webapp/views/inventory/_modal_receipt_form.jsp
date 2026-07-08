<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<form action="${pageContext.request.contextPath}/inventory" method="POST">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="action" value="confirmReceiveWithDiscrepancy">
    <input type="hidden" name="transferId" value="${ticket.ticketId}">
    <input type="hidden" name="warehouseId" value="${not empty param.warehouseId ? param.warehouseId : ticket.toWarehouseId}">
    
    <div class="modal-header border-bottom-0 pb-0">
        <h5 class="modal-title fw-bold" style="color: #111827;">Kiểm Tra Nhập Hàng - ${ticket.ticketCode}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
    </div>
    <div class="modal-body pt-4">
        <div class="alert alert-info d-flex align-items-center gap-2" role="alert">
            <i class="ph ph-info fs-5"></i>
            <div>Vui lòng nhập số lượng <b>thực tế đếm được</b>. Nếu có chênh lệch, hệ thống sẽ tự động tạo phiếu Sai lệch.</div>
        </div>
        
        <div class="table-responsive">
            <table class="table table-bordered table-sm text-center align-middle">
                <thead class="table-light">
                    <tr>
                        <th class="text-start">Sản Phẩm</th>
                        <th width="120px">SL Chứng Từ</th>
                        <th width="150px">SL Thực Tế</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="d" items="${ticketDetails}" varStatus="loop">
                            <tr>
                                <td class="text-start fw-medium">
                                    ${d.productName}
                                    <input type="hidden" name="productId[]" value="${d.productId}">
                                </td>
                                <td>
                                    <span class="badge bg-secondary fs-6">${d.quantity}</span>
                                    <input type="hidden" name="expectedQty[]" value="${d.quantity}">
                                </td>
                                <td>
                                    <input type="number" name="actualQty[]" class="form-control form-control-sm text-center fw-bold" 
                                           value="${d.quantity}" min="0" required>
                                </td>
                            </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        
        <div class="mb-3 mt-4">
            <label class="form-label fw-semibold text-muted small">Ghi chú (Tùy chọn)</label>
            <textarea name="note" class="form-control" rows="2" placeholder="Ghi chú về tình trạng hàng hóa..." style="border-radius: 8px;"></textarea>
        </div>
    </div>
    <div class="modal-footer border-top-0 pt-0">
        <button type="button" class="btn btn-light" data-bs-dismiss="modal" style="border-radius: 8px; font-weight: 500;">Hủy</button>
        <button type="submit" class="btn btn-import-submit" style="border-radius: 8px; font-weight: 500;">Xác Nhận Nhập Kho</button>
    </div>
</form>
