<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%-- 
  ==========================================================================
  MODAL XÁC NHẬN NHẬP KHO THỰC TẾ (_modal_receive_order.jsp)
  - Màn hình đối chiếu số lượng thực tế nhận được với số lượng hàng cần nhập.
  - Được mở từ trang Xử Lý Điều Chuyển (subtab=transfer_process).
  ==========================================================================
--%>
<form action="${pageContext.request.contextPath}/inventory" method="POST" id="receiveOrderForm">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="action" value="confirmReceiveOrder">
    <input type="hidden" name="orderId" value="${order.orderId}">
    <input type="hidden" name="targetSupplierId" id="targetSupplierIdInput" value="">
    
    <div class="modal-header bg-light border-bottom-0 pb-0">
        <h5 class="modal-title fw-bold text-dark d-flex align-items-center" style="font-size: 18px;">
            <span class="material-icons text-success me-2" style="font-size: 24px;">move_to_inbox</span>
            Xác Nhận Nhập Kho Thực Tế: <span class="text-primary ms-1">${order.orderCode}</span>
        </h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
    </div>
    
    <div class="modal-body pt-3">

        <!-- Thông tin đơn nhập -->
        <div class="card border-0 bg-light p-3 mb-4" style="border-radius: 12px;">
            <div class="row g-3">
                <div class="col-6 col-md-3">
                    <div class="text-muted small mb-1">Người Tạo Đơn</div>
                    <div class="fw-bold text-dark" style="font-size: 14px;">${order.employeeName}</div>
                </div>
                <div class="col-6 col-md-3 text-md-center">
                    <div class="text-muted small mb-1">Thời Gian Tạo</div>
                    <div class="fw-bold text-dark" style="font-size: 14px;">
                        <fmt:parseDate value="${order.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" var="parsedDateTime" type="both" />
                        <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDateTime}" />
                    </div>
                </div>
                <div class="col-6 col-md-3 text-md-center">
                    <div class="text-muted small mb-1">Trạng Thái</div>
                    <div>
                        <span class="badge bg-info text-dark" style="padding: 5px 10px; border-radius: 6px; font-size: 11px;">ĐANG VẬN CHUYỂN</span>
                    </div>
                </div>
                <div class="col-6 col-md-3 text-end">
                    <div class="text-muted small mb-1">Đối Tác NCC</div>
                    <div class="fw-bold text-dark" style="font-size: 14px;">
                        ${not empty order.customerName ? order.customerName : 'Nhiều Nhà Cung Cấp'}
                    </div>
                </div>
            </div>
        </div>

        <%
            java.util.List<model.OrderDetail> allDetails = (java.util.List<model.OrderDetail>) request.getAttribute("orderDetails");
            java.util.Map<String, java.util.List<model.OrderDetail>> supplierGroups = new java.util.LinkedHashMap<>();
            java.util.Map<String, Integer> supplierIdsMap = new java.util.HashMap<>();
            
            if (allDetails != null) {
                for (model.OrderDetail d : allDetails) {
                    String sName = (d.getSupplierName() != null && !d.getSupplierName().trim().isEmpty()) 
                                   ? d.getSupplierName() : "Nhà Cung Cấp Khác";
                    if (!supplierGroups.containsKey(sName)) {
                        supplierGroups.put(sName, new java.util.ArrayList<model.OrderDetail>());
                    }
                    supplierGroups.get(sName).add(d);
                    if (d.getSupplierId() != null) {
                        supplierIdsMap.put(sName, d.getSupplierId());
                    }
                }
            }
            pageContext.setAttribute("supplierGroups", supplierGroups);
            pageContext.setAttribute("supplierIdsMap", supplierIdsMap);
        %>

        <!-- Danh sách mặt hàng phân nhóm theo Nhà Cung Cấp -->
        <c:forEach var="entry" items="${supplierGroups}">
            <c:set var="suppName" value="${entry.key}" />
            <c:set var="itemList" value="${entry.value}" />
            <c:set var="suppId" value="${supplierIdsMap[suppName]}" />
            
            <%
                java.util.List<model.OrderDetail> curGroup = (java.util.List<model.OrderDetail>) pageContext.getAttribute("itemList");
                boolean isGroupCompleted = true;
                if (curGroup != null) {
                    for (model.OrderDetail item : curGroup) {
                        if (!"COMPLETED".equalsIgnoreCase(item.getSupplierStatus())) {
                            isGroupCompleted = false;
                            break;
                        }
                    }
                }
                pageContext.setAttribute("isGroupCompleted", isGroupCompleted);
            %>

            <div class="card border mb-4 shadow-sm" style="border-radius: 12px; overflow: hidden;">
                <div class="card-header bg-white py-3 d-flex align-items-center justify-content-between border-bottom" style="background-color: #f8fafc !important;">
                    <div class="d-flex align-items-center gap-2">
                        <span class="material-icons text-primary" style="font-size: 20px;">storefront</span>
                        <h6 class="mb-0 fw-bold text-dark" style="font-size: 15px;">
                            Nhà Cung Cấp: <span class="text-primary">${suppName}</span>
                        </h6>
                    </div>
                    <div>
                        <c:choose>
                            <c:when test="${isGroupCompleted}">
                                <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-1" style="font-size: 12px; border-radius: 6px;">
                                    <span class="material-icons align-middle me-1" style="font-size: 14px;">check_circle</span>Đã Nhập Kho Thành Công
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle px-3 py-1" style="font-size: 12px; border-radius: 6px;">
                                    <span class="material-icons align-middle me-1" style="font-size: 14px;">schedule</span>Chờ Giao Hàng & Nhập Kho
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0" style="font-size: 13.5px;">
                        <thead class="table-light text-dark" style="border-bottom: 2px solid #e2e8f0;">
                            <tr>
                                <th class="text-start ps-3 py-2.5" style="font-weight: 600; color: #475569;">Sản Phẩm</th>
                                <th class="text-center py-2.5" style="width: 120px; font-weight: 600; color: #475569;">Đơn Giá</th>
                                <th class="text-center py-2.5" style="width: 100px; font-weight: 600; color: #475569;">SL Đặt</th>
                                <th class="text-center py-2.5" style="width: 130px; font-weight: 600; color: #0284c7;">SL Thực Nhập</th>
                                <th class="text-center py-2.5" style="width: 120px; font-weight: 600; color: #475569;">Chênh Lệch</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="d" items="${itemList}">
                                <tr>
                                    <td class="text-start fw-semibold ps-3 py-3 text-dark">
                                        ${d.productName}<br>
                                        <small class="text-muted">${d.productCode}</small>
                                    </td>
                                    <td class="text-center text-muted">
                                        <fmt:formatNumber value="${d.unitPrice}" type="currency" currencySymbol="₫"/>
                                    </td>
                                    <td class="fw-bold text-center text-secondary" style="font-size: 14.5px;">
                                        ${d.quantity}
                                    </td>
                                    <td class="text-center pe-2">
                                        <c:choose>
                                            <c:when test="${d.supplierStatus == 'COMPLETED'}">
                                                <span class="fw-bold text-success" style="font-size: 14.5px;">${d.actualQuantity != null ? d.actualQuantity : d.quantity}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <input type="number" 
                                                       name="actualQty_${d.orderDetailId}" 
                                                       id="actualInput_${d.orderDetailId}"
                                                       data-target-qty="${d.quantity}"
                                                       oninput="updateDiffBadge(${d.orderDetailId}, ${d.quantity})"
                                                       onchange="updateDiffBadge(${d.orderDetailId}, ${d.quantity})"
                                                       onkeyup="updateDiffBadge(${d.orderDetailId}, ${d.quantity})"
                                                       class="form-control form-control-sm text-center fw-bold border-primary shadow-sm rounded-2 mx-auto" 
                                                       style="width: 90px; font-size: 14px; color: #0369a1; background-color: #f0f9ff;" 
                                                       min="0" 
                                                       value="${d.actualQuantity != null ? d.actualQuantity : d.quantity}" 
                                                       required>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center" id="diffBadgeCol_${d.orderDetailId}">
                                        <c:choose>
                                            <c:when test="${d.supplierStatus == 'COMPLETED'}">
                                                <c:set var="act" value="${d.actualQuantity != null ? d.actualQuantity : d.quantity}" />
                                                <c:set var="diff" value="${act - d.quantity}" />
                                                <c:choose>
                                                    <c:when test="${diff == 0}">
                                                        <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1" style="font-size: 11px;">Khớp (0)</span>
                                                    </c:when>
                                                    <c:when test="${diff < 0}">
                                                        <span class="badge bg-danger-subtle text-danger border border-danger-subtle px-2 py-1" style="font-size: 11px;">Thiếu (${diff})</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-2 py-1" style="font-size: 11px;">Thừa (+${diff})</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1" style="font-size: 11px;">Khớp (0)</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <c:if test="${!isGroupCompleted}">
                    <div class="card-footer bg-light text-end py-2 px-3">
                        <button type="submit" 
                                onclick="document.getElementById('targetSupplierIdInput').value='${not empty suppId ? suppId : ''}'; return confirm('Xác nhận nhập kho hàng từ nhà cung cấp [${suppName}]?');" 
                                class="btn btn-sm btn-success px-3 fw-bold d-inline-flex align-items-center gap-1" 
                                style="border-radius: 6px;">
                            <span class="material-icons" style="font-size: 16px;">check_circle</span>
                            Xác Nhận Nhập Kho (NCC: ${suppName})
                        </button>
                    </div>
                </c:if>
            </div>
        </c:forEach>
    </div>
    
    <div class="modal-footer border-top-0 pt-0">
        <button type="button" class="btn btn-secondary px-4" data-bs-dismiss="modal" style="border-radius: 8px;">Đóng</button>
    </div>
</form>

<script>
function updateDiffBadge(detailId, targetQty) {
    const input = document.getElementById('actualInput_' + detailId);
    const badgeCol = document.getElementById('diffBadgeCol_' + detailId);
    if (!input || !badgeCol) return;
    
    const val = parseInt(input.value) || 0;
    const diff = val - targetQty;
    
    if (diff === 0) {
        badgeCol.innerHTML = '<span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1" style="font-size: 11px;">Khớp (0)</span>';
    } else if (diff < 0) {
        badgeCol.innerHTML = '<span class="badge bg-danger-subtle text-danger border border-danger-subtle px-2 py-1" style="font-size: 11px;">Thiếu (' + diff + ')</span>';
    } else {
        badgeCol.innerHTML = '<span class="badge bg-primary-subtle text-primary border border-primary-subtle px-2 py-1" style="font-size: 11px;">Thừa (+' + diff + ')</span>';
    }
}
</script>
