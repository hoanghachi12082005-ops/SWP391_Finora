<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div class="modal-header bg-light border-bottom-0 pb-0">
    <h5 class="modal-title fw-bold text-dark d-flex align-items-center" style="font-size: 18px;">
        <span class="material-icons text-primary me-2" style="font-size: 24px;">swap_horiz</span>
        Chi Tiết Phiếu Điều Chuyển Gộp: <span class="text-primary ms-1">${ticket.transferCode}</span>
    </h5>
    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
</div>
<div class="modal-body pt-3">
    <!-- Thông tin chung -->
    <div class="card border-0 bg-light p-3 mb-4" style="border-radius: 12px;">
        <div class="row g-3">
            <div class="col-6 col-md-3">
                <div class="text-muted small mb-1">Kho Đề Xuất</div>
                <div class="fw-bold text-primary" style="font-size: 14.5px;">${ticket.creatorBranchId == ticket.fromBranchId ? ticket.fromWarehouseName : ticket.toWarehouseName}</div>
            </div>
            <div class="col-6 col-md-3 text-md-center">
                <div class="text-muted small mb-1">Người Lập Phiếu</div>
                <div class="fw-bold text-dark" style="font-size: 14.5px;">${ticket.createdByName}</div>
            </div>
            <div class="col-6 col-md-3 text-md-center">
                <div class="text-muted small mb-1">Thời Gian Tạo</div>
                <div class="fw-bold text-dark" style="font-size: 14.5px;">
                    <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${ticket.transferDate}" />
                </div>
            </div>
            <div class="col-6 col-md-3 text-md-end">
                <div class="text-muted small mb-1">Trạng Thái Tổng Hợp</div>
                <div>
                    <c:choose>
                        <c:when test="${ticket.displayStatus == 'PENDING_OWNER'}">
                            <span class="badge bg-warning text-dark" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">CHỜ DUYỆT</span>
                        </c:when>
                        <c:when test="${ticket.displayStatus == 'PENDING_PARTNER'}">
                            <span class="badge bg-info text-dark" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">CHỜ ĐỐI TÁC DUYỆT</span>
                        </c:when>
                        <c:when test="${ticket.displayStatus == 'APPROVED_DISPATCH' || ticket.displayStatus == 'IN_PROGRESS'}">
                            <span class="badge bg-primary" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">ĐANG XỬ LÝ</span>
                        </c:when>
                        <c:when test="${ticket.displayStatus == 'IN_TRANSIT'}">
                            <span class="badge bg-primary" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">ĐANG TRUNG CHUYỂN</span>
                        </c:when>
                        <c:when test="${ticket.displayStatus == 'COMPLETED'}">
                            <span class="badge bg-success" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">HOÀN THÀNH</span>
                        </c:when>
                        <c:when test="${ticket.displayStatus == 'PARTIAL_COMPLETE'}">
                            <span class="badge bg-warning text-dark" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">⚠️ HOÀN THÀNH 1 PHẦN (CÓ LỖI)</span>
                        </c:when>
                        <c:when test="${ticket.displayStatus == 'CANCELLED'}">
                            <span class="badge bg-danger" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">ĐÃ HỦY / BỊ TỪ CHỐI</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-secondary" style="padding: 6px 12px; border-radius: 6px; font-size: 11px;">${ticket.displayStatus}</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
        <c:if test="${not empty ticket.note}">
            <hr class="my-3 text-muted" style="opacity: 0.15;">
            <div class="row">
                <div class="col-12">
                    <div class="text-muted small mb-1">Ghi Chú</div>
                    <div class="text-dark" style="font-size: 13.5px;">${ticket.note}</div>
                </div>
            </div>
        </c:if>
    </div>
    
    <!-- Danh sách phiếu con chi tiết theo từng kho đối tác -->
    <h6 class="fw-bold mb-3 text-dark d-flex align-items-center" style="font-size: 15px;">
        <span class="material-icons text-secondary me-1" style="font-size: 20px;">account_tree</span>
        Chi Tiết Từng Kho Đối Tác
    </h6>

    <%
        model.StockTransfer ticket = (model.StockTransfer) request.getAttribute("ticket");
        java.util.List<model.StockTransfer> subTransfers = (java.util.List<model.StockTransfer>) request.getAttribute("subTransfers");
        Integer selectedWarehouseId = (Integer) request.getSession().getAttribute("selectedWarehouseId");
        if (selectedWarehouseId == null) {
            selectedWarehouseId = 0;
        }
        
        model.Employee currentUser = (model.Employee) session.getAttribute("currentUser");
        boolean isSystemOwner = currentUser != null && 
            ("Owner".equalsIgnoreCase(currentUser.getRoleName()) || "Admin".equalsIgnoreCase(currentUser.getRoleName()));
            
        // Determine the clicked partner warehouse
        int clickedPartnerId = 0;
        if (ticket != null) {
            boolean fromIsCreator = (ticket.getCreatorBranchId() == ticket.getFromBranchId());
            clickedPartnerId = fromIsCreator ? ticket.getToWarehouseId() : ticket.getFromWarehouseId();
        }

        boolean showAll = Boolean.TRUE.equals(request.getAttribute("showAll"));

        // Filter subTransfers to only show those involving the selectedWarehouseId for non-system Owner/Admin
        // For Owner/Admin, filter by the clickedPartnerId if it is a partner-pending approval ticket, unless showAll is true (clicked from approval)
        java.util.List<model.StockTransfer> filteredSubTransfers = new java.util.ArrayList<>();
        if (subTransfers != null) {
            for (model.StockTransfer sub : subTransfers) {
                if (selectedWarehouseId > 0) {
                    if (sub.getFromWarehouseId() == selectedWarehouseId || 
                        sub.getToWarehouseId() == selectedWarehouseId) {
                        filteredSubTransfers.add(sub);
                    }
                } else {
                    if (isSystemOwner) {
                        if (!showAll && ticket != null && "PENDING_PARTNER".equals(ticket.getStatus())) {
                            boolean subFromIsCreator = (sub.getFromBranchId() == ticket.getCreatorBranchId());
                            int subPartnerId = subFromIsCreator ? sub.getToWarehouseId() : sub.getFromWarehouseId();
                            if (subPartnerId == clickedPartnerId) {
                                filteredSubTransfers.add(sub);
                            }
                        } else {
                            filteredSubTransfers.add(sub);
                        }
                    } else {
                        filteredSubTransfers.add(sub);
                    }
                }
            }
        }
        
        // Group filteredSubTransfers by partner warehouse ID using standard Maps to avoid EL access restrictions
        java.util.Map<Integer, java.util.Map<String, Object>> groups = new java.util.LinkedHashMap<>();
        int ticketCreatorBranchId = (ticket != null) ? ticket.getCreatorBranchId() : 0;
        
        for (model.StockTransfer sub : filteredSubTransfers) {
            boolean isExp;
            int partnerId;
            String partnerName;
            
            if (selectedWarehouseId > 0) {
                // Perspective of the selected warehouse
                isExp = (sub.getFromWarehouseId() == selectedWarehouseId);
                partnerId = isExp ? sub.getToWarehouseId() : sub.getFromWarehouseId();
                partnerName = isExp ? sub.getToWarehouseName() : sub.getFromWarehouseName();
            } else {
                // Perspective of the creator (global Owner/Admin view)
                isExp = (sub.getFromBranchId() == ticketCreatorBranchId);
                partnerId = isExp ? sub.getToWarehouseId() : sub.getFromWarehouseId();
                partnerName = isExp ? sub.getToWarehouseName() : sub.getFromWarehouseName();
            }
            
            java.util.Map<String, Object> group = groups.get(partnerId);
            if (group == null) {
                group = new java.util.HashMap<>();
                group.put("partnerId", partnerId);
                group.put("partnerName", partnerName);
                group.put("items", new java.util.ArrayList<java.util.Map<String, Object>>());
                group.put("statuses", new java.util.ArrayList<String>());
                group.put("repSub", sub);
                groups.put(partnerId, group);
            }
            
            java.util.List<String> statuses = (java.util.List<String>) group.get("statuses");
            if (!statuses.contains(sub.getStatus())) {
                statuses.add(sub.getStatus());
            }
            
            java.util.List<java.util.Map<String, Object>> items = (java.util.List<java.util.Map<String, Object>>) group.get("items");
            if (sub.getDetails() != null) {
                for (model.StockTransferDetail d : sub.getDetails()) {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("productName", d.getProductName());
                    item.put("productCodebar", d.getProductCodebar());
                    item.put("quantity", d.getQuantity());
                    item.put("unitName", d.getUnitName());
                    item.put("direction", isExp ? "SEND" : "RECEIVE");
                    item.put("status", sub.getStatus());
                    items.add(item);
                }
            }
        }
        pageContext.setAttribute("partnerGroups", groups.values());
    %>

    <c:forEach var="group" items="${partnerGroups}">
        <div class="border rounded-3 mb-4 p-3 shadow-sm bg-white">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <span class="fw-bold text-dark fs-6">🏢 Kho đối tác: ${group.partnerName}</span>
                </div>
                <div class="d-flex gap-2">
                    <c:forEach var="status" items="${group.statuses}">
                        <c:choose>
                            <c:when test="${status == 'PENDING_OWNER'}">
                                <span class="badge bg-warning text-dark">Chờ duyệt</span>
                            </c:when>
                            <c:when test="${status == 'PENDING_PARTNER'}">
                                <span class="badge bg-info text-dark">Chờ đối tác duyệt</span>
                            </c:when>
                            <c:when test="${status == 'APPROVED_DISPATCH'}">
                                <span class="badge bg-primary">Chờ xuất kho</span>
                            </c:when>
                            <c:when test="${status == 'DISPATCH_REJECTED'}">
                                <span class="badge bg-danger">Từ chối xuất hàng</span>
                            </c:when>
                            <c:when test="${status == 'IN_TRANSIT'}">
                                <span class="badge bg-primary">Đang trung chuyển</span>
                            </c:when>
                            <c:when test="${status == 'RECEIVE_REJECTED'}">
                                <span class="badge bg-danger">Từ chối nhận hàng</span>
                            </c:when>
                            <c:when test="${status == 'COMPLETED'}">
                                <span class="badge bg-success">Hoàn thành</span>
                            </c:when>
                            <c:when test="${status == 'PARTNER_REJECTED'}">
                                <span class="badge bg-danger">Đối tác từ chối</span>
                            </c:when>
                            <c:when test="${status == 'CANCELLED'}">
                                <span class="badge bg-danger">Đã hủy</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary">${status}</span>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>
            </div>

            <!-- Bảng sản phẩm của kho này -->
            <div class="table-responsive border rounded-3 mb-0" style="overflow: hidden;">
                <table class="table table-hover align-middle mb-0" style="font-size: 13.5px;">
                    <thead class="table-light text-dark" style="border-bottom: 2px solid #e2e8f0;">
                        <tr>
                            <th class="text-start ps-3 py-2" style="font-weight: 600; color: #475569;">Sản Phẩm</th>
                            <th class="text-center py-2" style="width: 150px; font-weight: 600; color: #475569;">Mã Vạch</th>
                            <th class="text-center py-2" style="width: 180px; font-weight: 600; color: #475569;">Loại Giao Dịch</th>
                            <th class="text-center py-2" style="width: 100px; font-weight: 600; color: #475569;">Số Lượng</th>
                            <th class="text-center py-2" style="width: 100px; font-weight: 600; color: #475569;">Đơn Vị</th>
                            <th class="text-center py-2" style="width: 150px; font-weight: 600; color: #475569;">Trạng Thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${group.items}">
                            <tr>
                                <td class="text-start fw-semibold ps-3 py-2 text-dark">${item.productName}</td>
                                <td class="text-center text-muted">${item.productCodebar}</td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${item.direction == 'SEND'}">
                                            <span class="badge bg-danger-subtle text-danger border border-danger-subtle" style="font-size: 11px; padding: 4px 8px; font-weight: 600;">
                                                XUẤT (Đi kho đối tác)
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-success-subtle text-success border border-success-subtle" style="font-size: 11px; padding: 4px 8px; font-weight: 600;">
                                                NHẬP (Từ kho đối tác)
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="fw-bold text-center text-primary" style="font-size: 14px;">${item.quantity}</td>
                                <td class="text-center text-muted">${not empty item.unitName ? item.unitName : 'Cái'}</td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${item.status == 'PENDING_OWNER'}">
                                            <span class="badge bg-warning text-dark" style="font-size: 11px; padding: 4px 8px;">Chờ duyệt (Quản lý)</span>
                                        </c:when>
                                        <c:when test="${item.status == 'PENDING_PARTNER'}">
                                            <span class="badge bg-info text-dark" style="font-size: 11px; padding: 4px 8px;">Chờ duyệt (Đối tác)</span>
                                        </c:when>
                                        <c:when test="${item.status == 'APPROVED_DISPATCH'}">
                                            <span class="badge bg-primary-subtle text-primary border border-primary-subtle" style="font-size: 11px; padding: 4px 8px;">Chờ xuất kho</span>
                                        </c:when>
                                        <c:when test="${item.status == 'DISPATCH_REJECTED'}">
                                            <span class="badge bg-danger-subtle text-danger border border-danger-subtle" style="font-size: 11px; padding: 4px 8px;">Từ chối xuất</span>
                                        </c:when>
                                        <c:when test="${item.status == 'IN_TRANSIT'}">
                                            <span class="badge bg-info-subtle text-info border border-info-subtle" style="font-size: 11px; padding: 4px 8px;">Đang chuyển</span>
                                        </c:when>
                                        <c:when test="${item.status == 'RECEIVE_REJECTED'}">
                                            <span class="badge bg-danger-subtle text-danger border border-danger-subtle" style="font-size: 11px; padding: 4px 8px;">Từ chối nhận</span>
                                        </c:when>
                                        <c:when test="${item.status == 'COMPLETED'}">
                                            <span class="badge bg-success-subtle text-success border border-success-subtle" style="font-size: 11px; padding: 4px 8px;">Thành công</span>
                                        </c:when>
                                        <c:when test="${item.status == 'PARTNER_REJECTED'}">
                                            <span class="badge bg-danger-subtle text-danger border border-danger-subtle" style="font-size: 11px; padding: 4px 8px;">Đối tác từ chối</span>
                                        </c:when>
                                        <c:when test="${item.status == 'CANCELLED'}">
                                            <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle" style="font-size: 11px; padding: 4px 8px;">Đã hủy</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary" style="font-size: 11px; padding: 4px 8px;">${item.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            
            <!-- Card-specific approval actions -->
            <c:set var="subRep" value="${group.repSub}" />
            <c:set var="showCardApproval" value="false" />
            <c:set var="cardActionApprove" value="" />
            <c:set var="cardActionReject" value="" />
            <c:set var="cardApprovalTypeLabel" value="" />

            <c:if test="${not empty sessionScope.currentUser && not empty subRep}">
                <c:set var="userRole" value="${sessionScope.currentUser.roleName}" />
                <c:if test="${userRole == 'Owner' || userRole == 'Admin' || userRole == 'StoreManager'}">
                    
                    <c:set var="currentBranchId" value="0" />
                    <c:if test="${not empty sessionScope.selectedWarehouseId}">
                        <c:choose>
                            <c:when test="${sessionScope.selectedWarehouseId == subRep.fromWarehouseId}">
                                <c:set var="currentBranchId" value="${subRep.fromBranchId}" />
                            </c:when>
                            <c:when test="${sessionScope.selectedWarehouseId == subRep.toWarehouseId}">
                                <c:set var="currentBranchId" value="${subRep.toBranchId}" />
                            </c:when>
                        </c:choose>
                    </c:if>

                    <c:set var="fromIsCreator" value="${subRep.creatorBranchId == subRep.fromBranchId}" />
                    <c:choose>
                        <c:when test="${fromIsCreator}">
                            <c:set var="partnerBranchId" value="${subRep.toBranchId}" />
                            <c:set var="creatorBranchId" value="${subRep.fromBranchId}" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="partnerBranchId" value="${subRep.fromBranchId}" />
                            <c:set var="creatorBranchId" value="${subRep.toBranchId}" />
                        </c:otherwise>
                    </c:choose>

                    <c:set var="isSystemOwner" value="${userRole == 'Owner' || userRole == 'Admin'}" />

                    <c:if test="${subRep.status == 'PENDING_PARTNER'}">
                        <c:if test="${isSystemOwner || (not empty sessionScope.currentUser && sessionScope.currentUser.branchId == partnerBranchId)}">
                            <c:set var="showCardApproval" value="true" />
                            <c:set var="cardActionApprove" value="partnerApproveTransfer" />
                            <c:set var="cardActionReject" value="partnerRejectTransfer" />
                            <c:set var="cardApprovalTypeLabel" value="Duyệt" />
                        </c:if>
                    </c:if>
                </c:if>
            </c:if>

            <c:if test="${showCardApproval}">
                <div class="d-flex justify-content-end gap-2 mt-3 pt-3 border-top border-light-subtle">
                    <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="${cardActionApprove}">
                        <input type="hidden" name="transferId" value="${subRep.stockTransferId}">
                        <input type="hidden" name="currentWarehouseId" value="${sessionScope.selectedWarehouseId}">
                        <button class="btn btn-sm btn-success px-3" style="border-radius: 6px;" type="submit" onclick="return confirm('Xác nhận duyệt phần chuyển kho này?')">
                            <span class="material-icons" style="font-size: 14px; vertical-align: middle; margin-right: 4px;">check</span>
                            ${cardApprovalTypeLabel}
                        </button>
                    </form>
                    <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="${cardActionReject}">
                        <input type="hidden" name="transferId" value="${subRep.stockTransferId}">
                        <input type="hidden" name="currentWarehouseId" value="${sessionScope.selectedWarehouseId}">
                        <button class="btn btn-sm btn-danger px-3" style="border-radius: 6px;" type="submit" onclick="return confirm('Xác nhận từ chối phần chuyển kho này?')">
                            <span class="material-icons" style="font-size: 14px; vertical-align: middle; margin-right: 4px;">close</span>
                            Từ Chối
                        </button>
                    </form>
                </div>
            </c:if>
        </div>
    </c:forEach>
</div>

<!-- General proposal approval (PENDING_OWNER) for Owner/Admin or Proposing Store Manager -->
<c:set var="isSystemOwner" value="${sessionScope.currentUser.roleName == 'Owner' || sessionScope.currentUser.roleName == 'Admin'}" />
<c:set var="isCreatorManager" value="${sessionScope.currentUser.roleName == 'StoreManager' && sessionScope.currentUser.branchId == ticket.creatorBranchId}" />
<c:if test="${ticket.displayStatus == 'PENDING_OWNER' && (isSystemOwner || isCreatorManager)}">
    <div class="d-flex justify-content-end gap-2 px-4 pb-3 mb-2 border-bottom border-light-subtle">
        <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="approveTransfer">
            <input type="hidden" name="transferId" value="${ticket.stockTransferId}">
            <input type="hidden" name="currentWarehouseId" value="${sessionScope.selectedWarehouseId}">
            <button class="btn btn-success px-4" style="border-radius: 8px; font-weight: 500;" type="submit" onclick="return confirm('Xác nhận duyệt đề xuất chuyển kho này?')">
                <span class="material-icons" style="font-size: 18px; vertical-align: middle; margin-right: 4px;">check</span>
                Duyệt Đề Xuất
            </button>
        </form>
        <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="rejectTransfer">
            <input type="hidden" name="transferId" value="${ticket.stockTransferId}">
            <input type="hidden" name="currentWarehouseId" value="${sessionScope.selectedWarehouseId}">
            <button class="btn btn-danger px-4" style="border-radius: 8px; font-weight: 500;" type="submit" onclick="return confirm('Xác nhận từ chối đề xuất chuyển kho này?')">
                <span class="material-icons" style="font-size: 18px; vertical-align: middle; margin-right: 4px;">close</span>
                Từ Chối
            </button>
        </form>
    </div>
</c:if>

<div class="modal-footer border-top-0 pt-0 d-flex justify-content-end align-items-center">
    <div class="d-flex gap-2">
        <a href="${pageContext.request.contextPath}/inventory?action=printTicket&ticketId=${ticket.stockTransferId}" target="_blank" 
           class="btn btn-outline-primary px-4 d-inline-flex align-items-center justify-content-center" style="border-radius: 8px; text-decoration: none;">
            <span class="material-icons me-1" style="font-size: 16px;">print</span>
            In Phiếu
        </a>
        <button type="button" class="btn btn-secondary px-4" data-bs-dismiss="modal" style="border-radius: 8px;">Đóng</button>
    </div>
</div>
