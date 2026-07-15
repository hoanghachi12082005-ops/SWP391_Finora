<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- 
  ==========================================================================
  TAB ĐIỀU CHUYỂN KHO HÀNG (_tab_transfer.jsp)
  - Quản lý các phiếu điều chuyển nội bộ giữa các kho (Stock Transfer).
  - Có các subtab: Danh sách phiếu, phiếu chuyển đi, phiếu nhận hàng.
  - Cho phép tạo mới phiếu chuyển kho, hiển thị chi tiết và thay đổi trạng thái phiếu.
  ==========================================================================
--%>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />
<c:set var="currentSubtab" value="${not empty param.subtab ? param.subtab : 'transfer_list'}" />



<div class="dashboard-card">
    <div class="card-header border-bottom-0 pb-0 mb-3">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0 fw-bold" style="color: #93000b;">🚚 Điều chuyển kho</h5>
            <c:if test="${roleName == 'WarehouseStaff' || roleName == 'StoreManager' || roleName == 'Admin' || roleName == 'Owner'}">
                <div class="d-flex gap-2">
                    <a href="${pageContext.request.contextPath}/inventory?tab=createTransfer&warehouseId=${selectedWarehouseId}" class="btn btn-danger btn-sm d-flex align-items-center gap-1" style="background-color: var(--primary-color); border-color: var(--primary-color); border-radius: 8px; font-weight: 500; padding: 6px 14px; box-shadow: none;">
                        <span class="material-icons" style="font-size:18px;">add_circle_outline</span>
                        Tạo Phiếu Điều Chuyển Kho
                    </a>
                </div>
            </c:if>
        </div>
        <div class="transfer-subtab-nav">
            <a href="?tab=transfer&subtab=transfer_list&warehouseId=${selectedWarehouseId}" 
               class="transfer-subtab-link ${currentSubtab == 'transfer_list' ? 'active' : ''}">
               <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">list_alt</span>
               Đơn Điều Chuyển
            </a>
            <a href="?tab=transfer&subtab=transfer_process&warehouseId=${selectedWarehouseId}" 
               class="transfer-subtab-link ${currentSubtab == 'transfer_process' ? 'active' : ''}">
               <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">local_shipping</span>
               Xử Lý Điều Chuyển
            </a>
        </div>
    </div>

    <div class="card-body pt-0">
        <c:choose>
            <%-- ============ SUB-TAB 1: ĐƠN ĐIỀU CHUYỂN ============ --%>
            <c:when test="${currentSubtab == 'transfer_list'}">
                <!-- Filter form -->
                <div class="p-3 bg-white border-bottom rounded-3 mb-3">
                    <form action="${pageContext.request.contextPath}/inventory" method="GET" id="transferFilterForm" class="row g-3 align-items-end m-0">
                        <input type="hidden" name="tab" value="transfer">
                        <input type="hidden" name="subtab" value="transfer_list">
                        <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                        
                        <div class="col-md-4 col-sm-6">
                            <label class="form-label small text-muted fw-semibold mb-1 ms-1">Mã phiếu điều chuyển</label>
                            <div class="position-relative">
                                <span class="material-icons position-absolute text-muted" style="left: 16px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">search</span>
                                <input type="text" name="transferCodeQuery" class="form-control rounded-pill inventory-search-input w-100" 
                                       style="padding-left: 48px; padding-right: 20px; padding-top: 10px; padding-bottom: 10px; font-size: 14.5px; box-shadow: none;" 
                                       placeholder="Tìm mã phiếu điều chuyển..." value="${transferCodeQuery}">
                            </div>
                        </div>
                        
                        <div class="col-md-3 col-sm-6">
                            <label class="form-label small text-muted fw-semibold mb-1 ms-1">Kho đối tác</label>
                            <div class="position-relative">
                                <span class="material-icons position-absolute text-muted" style="left: 14px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">warehouse</span>
                                <select name="partnerWarehouseQuery" class="form-select rounded-pill inventory-filter-select" 
                                        style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; font-size: 14px; box-shadow: none; appearance: none; cursor: pointer;">
                                    <option value="">Tất cả kho đối tác</option>
                                    <c:forEach var="w" items="${warehouses}">
                                        <c:if test="${w.warehouseId != selectedWarehouseId}">
                                            <option value="${w.warehouseId}" ${partnerWarehouseQuery == w.warehouseId ? 'selected' : ''}>${w.warehouseName}</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        
                        <div class="col-md-3 col-sm-6">
                            <label class="form-label small text-muted fw-semibold mb-1 ms-1">Trạng thái</label>
                            <div class="position-relative">
                                <span class="material-icons position-absolute text-muted" style="left: 14px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">inventory_2</span>
                                <select name="statusQuery" class="form-select rounded-pill inventory-filter-select" 
                                        style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; font-size: 14px; box-shadow: none; appearance: none; cursor: pointer;">
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="PENDING_OWNER" ${statusQuery == 'PENDING_OWNER' ? 'selected' : ''}>Chờ duyệt</option>
                                    <option value="PENDING_PARTNER" ${statusQuery == 'PENDING_PARTNER' ? 'selected' : ''}>Chờ đối tác duyệt</option>
                                    <option value="APPROVED_DISPATCH" ${statusQuery == 'APPROVED_DISPATCH' ? 'selected' : ''}>Đang xử lý</option>
                                    <option value="COMPLETED" ${statusQuery == 'COMPLETED' ? 'selected' : ''}>Hoàn thành</option>
                                    <option value="CANCELLED" ${statusQuery == 'CANCELLED' ? 'selected' : ''}>Đã hủy / Bị từ chối</option>
                                </select>
                            </div>
                        </div>
                        
                        <div class="col-md-2 col-sm-6">
                            <button type="submit" class="btn inventory-btn-filter w-100" style="height: 43px; background-color: var(--primary-color); border: none; color: white;">
                                <span class="material-icons" style="font-size: 18px; margin-right: 6px;">filter_alt</span>
                                <span>Lọc</span>
                            </button>
                        </div>
                    </form>
                </div>

                <div class="premium-table-container">
                    <table class="table premium-table mb-0">
                        <thead>
                            <tr>
                                <th>Mã Phiếu</th>
                                <th>Kho Đối Tác</th>
                                <th>Trạng Thái</th>
                                <th>Tiến Trình</th>
                                <th>Người Tạo</th>
                                <th>Thời Gian</th>
                                <th width="100px" class="text-center">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty transfers}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">Không có phiếu điều chuyển nào</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="tx" items="${transfers}">
                                        <tr>
                                            <td class="fw-bold" style="color: var(--primary-color);">${tx.transferCode}</td>
                                            <td>
                                                <%
                                                     model.StockTransfer tx = (model.StockTransfer) pageContext.getAttribute("tx");
                                                     Integer selWId = (Integer) session.getAttribute("selectedWarehouseId");
                                                     int currentWId = selWId != null ? selWId : 0;
                                                     java.util.Set<String> uniquePartners = new java.util.LinkedHashSet<>();
                                                     
                                                     if (tx != null && tx.getSubTransfers() != null) {
                                                         int ticketCreatorBranchId = tx.getCreatorBranchId();
                                                         for (model.StockTransfer sub : tx.getSubTransfers()) {
                                                             if (currentWId > 0) {
                                                                 // Filter: only include sub-transfers involving the active warehouse
                                                                 if (sub.getFromWarehouseId() == currentWId || sub.getToWarehouseId() == currentWId) {
                                                                     boolean isExp = (sub.getFromWarehouseId() == currentWId);
                                                                     String name = isExp ? sub.getToWarehouseName() : sub.getFromWarehouseName();
                                                                     if (name != null) {
                                                                         uniquePartners.add(name);
                                                                     }
                                                                 }
                                                             } else {
                                                                 // Owner view: show all partner warehouses relative to creator branch
                                                                 boolean isExp = (sub.getFromBranchId() == ticketCreatorBranchId);
                                                                 String name = isExp ? sub.getToWarehouseName() : sub.getFromWarehouseName();
                                                                 if (name != null) {
                                                                     uniquePartners.add(name);
                                                                 }
                                                             }
                                                         }
                                                     }
                                                     for (String partnerName : uniquePartners) {
                                                 %>
                                                     <span class="badge bg-light text-dark border"><%= partnerName %></span>
                                                 <%
                                                     }
                                                 %>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${tx.displayStatus == 'PENDING_OWNER'}">
                                                        <span class="badge bg-warning text-dark">CHỜ DUYỆT</span>
                                                    </c:when>
                                                    <c:when test="${tx.displayStatus == 'PENDING_PARTNER'}">
                                                        <span class="badge bg-info text-dark">CHỜ ĐỐI TÁC DUYỆT</span>
                                                    </c:when>
                                                    <c:when test="${tx.displayStatus == 'APPROVED_DISPATCH' || tx.displayStatus == 'IN_PROGRESS'}">
                                                        <span class="badge bg-primary">ĐANG XỬ LÝ</span>
                                                    </c:when>
                                                    <c:when test="${tx.displayStatus == 'IN_TRANSIT'}">
                                                        <span class="badge bg-primary">ĐANG TRUNG CHUYỂN</span>
                                                    </c:when>
                                                    <c:when test="${tx.displayStatus == 'COMPLETED'}">
                                                        <span class="badge bg-success">HOÀN THÀNH</span>
                                                    </c:when>
                                                    <c:when test="${tx.displayStatus == 'PARTIAL_COMPLETE'}">
                                                        <span class="badge bg-warning text-dark">⚠️ HOÀN THÀNH CÓ LỖI)</span>
                                                    </c:when>
                                                    <c:when test="${tx.displayStatus == 'CANCELLED'}">
                                                        <span class="badge bg-danger">ĐÃ HỦY / BỊ TỪ CHỐI</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary">${tx.displayStatus}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div class="status-timeline">
                                                    <c:choose>
                                                        <c:when test="${tx.displayStatus == 'PENDING_OWNER'}">
                                                            <span class="status-step current">Chờ Duyệt</span>
                                                            <span class="material-icons" style="font-size:12px;color:#cbd5e1;">arrow_forward</span>
                                                            <span class="status-step pending">Chờ đối tác</span>
                                                            <span class="material-icons" style="font-size:12px;color:#cbd5e1;">arrow_forward</span>
                                                            <span class="status-step pending">Trung chuyển</span>
                                                            <span class="material-icons" style="font-size:12px;color:#cbd5e1;">arrow_forward</span>
                                                            <span class="status-step pending">Hoàn thành</span>
                                                        </c:when>
                                                        <c:when test="${tx.displayStatus == 'PENDING_PARTNER'}">
                                                            <span class="status-step done">Đã duyệt</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step current">Chờ đối tác</span>
                                                            <span class="material-icons" style="font-size:12px;color:#cbd5e1;">arrow_forward</span>
                                                            <span class="status-step pending">Trung chuyển</span>
                                                            <span class="material-icons" style="font-size:12px;color:#cbd5e1;">arrow_forward</span>
                                                            <span class="status-step pending">Hoàn thành</span>
                                                        </c:when>
                                                        <c:when test="${tx.displayStatus == 'APPROVED_DISPATCH' || tx.displayStatus == 'IN_PROGRESS' || tx.displayStatus == 'IN_TRANSIT'}">
                                                            <span class="status-step done">Đã duyệt</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step done">Đối tác duyệt</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step current">Trung chuyển</span>
                                                            <span class="material-icons" style="font-size:12px;color:#cbd5e1;">arrow_forward</span>
                                                            <span class="status-step pending">Hoàn thành</span>
                                                        </c:when>
                                                        <c:when test="${tx.displayStatus == 'COMPLETED' || tx.displayStatus == 'PARTIAL_COMPLETE'}">
                                                            <span class="status-step done">Đã duyệt</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step done">Đối tác duyệt</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step done">Trung chuyển</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step done">Hoàn thành</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="status-step" style="background:#fef2f2;color:#e11d48;">Đã kết thúc</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </td>
                                            <td>${tx.createdByName}</td>
                                            <td>
                                                <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${tx.transferDate}" />
                                            </td>
                                            <td class="text-center">
                                                <div class="d-flex align-items-center justify-content-center gap-2">
                                                    <button class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                            style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #dbeafe; background-color: #eff6ff; color: #2563eb; cursor: pointer; transition: all 0.2s;" 
                                                            title="Xem chi tiết"
                                                            onmouseover="this.style.backgroundColor='#dbeafe';"
                                                            onmouseout="this.style.backgroundColor='#eff6ff';"
                                                            onclick="viewTicketDetails(${tx.stockTransferId}, true)">
                                                        <span class="material-icons" style="font-size: 16px;">visibility</span>
                                                    </button>
                                                    <%
                                                        model.Employee curUser = (model.Employee) session.getAttribute("currentUser");
                                                        int curBranchId = (curUser != null && curUser.getBranchId() != null) ? curUser.getBranchId() : 0;
                                                        model.StockTransfer currentTicket = (model.StockTransfer) pageContext.getAttribute("tx");
                                                        Integer selectedWarehouseId = (Integer) session.getAttribute("selectedWarehouseId");
                                                        if (currentTicket != null && selectedWarehouseId != null) {
                                                            int selectedWarehouseBranchId = 0;
                                                            if (currentTicket.getSubTransfers() != null) {
                                                                for (model.StockTransfer sub : currentTicket.getSubTransfers()) {
                                                                    if (sub.getFromWarehouseId() == selectedWarehouseId) {
                                                                        selectedWarehouseBranchId = sub.getFromBranchId();
                                                                        break;
                                                                    } else if (sub.getToWarehouseId() == selectedWarehouseId) {
                                                                        selectedWarehouseBranchId = sub.getToBranchId();
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            boolean isActiveViewCreator = (selectedWarehouseBranchId == currentTicket.getCreatorBranchId());
                                                            
                                                            if (isActiveViewCreator) {
                                                                boolean canCancel = true;
                                                                if (currentTicket.getSubTransfers() != null) {
                                                                    for (model.StockTransfer sub : currentTicket.getSubTransfers()) {
                                                                        String s = sub.getStatus();
                                                                        if (!"PENDING_OWNER".equals(s) && !"PENDING_PARTNER".equals(s) && !"CANCELLED".equals(s) && !"PARTNER_REJECTED".equals(s)) {
                                                                            canCancel = false;
                                                                            break;
                                                                        }
                                                                    }
                                                                } else {
                                                                    canCancel = false;
                                                                }
                                                                if (canCancel) {
                                                    %>
                                                        <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block;">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                            <input type="hidden" name="action" value="cancelTransfer">
                                                            <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                            <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                            <button type="submit" class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                                    style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #fee2e2; background-color: #fef2f2; color: #dc2626; cursor: pointer; transition: all 0.2s;" 
                                                                    title="Hủy toàn bộ phiếu"
                                                                    onmouseover="this.style.backgroundColor='#fee2e2';"
                                                                    onmouseout="this.style.backgroundColor='#fef2f2';"
                                                                    onclick="return confirm('Xác nhận hủy toàn bộ phiếu điều chuyển này?')">
                                                                <span class="material-icons" style="font-size: 16px;">block</span>
                                                            </button>
                                                        </form>
                                                    <%
                                                                }
                                                            } else {
                                                                if (currentTicket.getSubTransfers() != null) {
                                                                    for (model.StockTransfer sub : currentTicket.getSubTransfers()) {
                                                                        if ((sub.getFromWarehouseId() == selectedWarehouseId || sub.getToWarehouseId() == selectedWarehouseId) &&
                                                                            "PENDING_PARTNER".equals(sub.getStatus())) {
                                                    %>
                                                        <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block;">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                            <input type="hidden" name="action" value="partnerApproveTransfer">
                                                            <input type="hidden" name="transferId" value="<%= sub.getStockTransferId() %>">
                                                            <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                            <button type="submit" class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                                    style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #d1fae5; background-color: #ecfdf5; color: #059669; cursor: pointer; transition: all 0.2s;" 
                                                                    title="Duyệt chặng"
                                                                    onmouseover="this.style.backgroundColor='#d1fae5';"
                                                                    onmouseout="this.style.backgroundColor='#ecfdf5';"
                                                                    onclick="return confirm('Xác nhận duyệt yêu cầu chuyển kho này?')">
                                                                <span class="material-icons" style="font-size: 16px;">check</span>
                                                            </button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block;">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                            <input type="hidden" name="action" value="partnerRejectTransfer">
                                                            <input type="hidden" name="transferId" value="<%= sub.getStockTransferId() %>">
                                                            <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                            <button type="submit" class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                                    style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #fee2e2; background-color: #fef2f2; color: #dc2626; cursor: pointer; transition: all 0.2s;" 
                                                                    title="Từ chối chặng"
                                                                    onmouseover="this.style.backgroundColor='#fee2e2';"
                                                                    onmouseout="this.style.backgroundColor='#fef2f2';"
                                                                    onclick="return confirm('Xác nhận từ chối yêu cầu chuyển kho này?')">
                                                                <span class="material-icons" style="font-size: 16px;">close</span>
                                                            </button>
                                                        </form>
                                                    <%
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    %>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </c:when>

            <%-- ============ SUB-TAB 2: XỬ LÝ ĐIỀU CHUYỂN ============ --%>
            <c:when test="${currentSubtab == 'transfer_process'}">
                <div class="mb-3 bg-light p-3 rounded-3">
                    <p class="text-muted small mb-0 d-flex align-items-center gap-1">
                        <span class="material-icons" style="font-size:18px;">info</span>
                        Thực hiện xác nhận Xuất kho (tại kho gửi) và Nhập kho (tại kho nhận) cho các phiếu đã được phê duyệt.
                    </p>
                </div>

                <!-- ================= BẢNG 1: ĐƠN HÀNG ĐANG VẬN CHUYỂN ================= -->
                <div class="card-header border-0 bg-transparent ps-0 mt-3 mb-2">
                    <h6 class="mb-0 fw-bold text-dark d-flex align-items-center gap-1" style="font-size: 15px;">
                        <span class="material-icons text-info">local_shipping</span> Đơn Hàng Đang Vận Chuyển
                    </h6>
                </div>
                
                <div class="table-responsive border rounded-3 mb-4" style="overflow: hidden;">
                    <table class="table table-hover align-middle mb-0" style="font-size: 13.5px;">
                        <thead class="table-light text-dark" style="border-bottom: 2px solid #e2e8f0;">
                            <tr>
                                <th class="text-start ps-3 py-2" style="font-weight: 600; color: #475569;">Mã Phiếu</th>
                                <th class="text-start py-2" style="font-weight: 600; color: #475569;">Kho Gửi</th>
                                <th class="text-start py-2" style="font-weight: 600; color: #475569;">Kho Nhận</th>
                                <th class="text-start py-2" style="font-weight: 600; color: #475569;">Người Tạo</th>
                                <th class="text-center py-2" style="width: 150px; font-weight: 600; color: #475569;">Thời Gian</th>
                                <th class="text-center py-2" style="width: 120px; font-weight: 600; color: #475569;">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="hasInTransit" value="false" />
                            <c:forEach var="tx" items="${transfers}">
                                <c:if test="${tx.status == 'IN_TRANSIT' && (empty selectedWarehouseId || selectedWarehouseId == tx.fromWarehouseId || selectedWarehouseId == tx.toWarehouseId)}">
                                    <c:set var="hasInTransit" value="true" />
                                    <tr>
                                        <td class="fw-bold ps-3 text-start" style="color: var(--primary-color);">${tx.transferCode}</td>
                                        <td class="text-start">${tx.fromWarehouseName}</td>
                                        <td class="text-start">${tx.toWarehouseName}</td>
                                        <td class="text-start">${tx.createdByName}</td>
                                        <td class="text-center"><fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${tx.transferDate}" /></td>
                                        <td class="text-center">
                                            <button type="button" class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                    style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #dbeafe; background-color: #eff6ff; color: #2563eb; cursor: pointer; transition: all 0.2s; margin: 0 auto;" 
                                                    title="Xem chi tiết"
                                                    onmouseover="this.style.backgroundColor='#dbeafe';"
                                                    onmouseout="this.style.backgroundColor='#eff6ff';"
                                                    onclick="viewTicketDetails(${tx.stockTransferId})">
                                                <span class="material-icons" style="font-size: 16px;">visibility</span>
                                            </button>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:if test="${!hasInTransit}">
                                <tr><td colspan="6" class="text-center py-3 text-muted">Không có đơn hàng nào đang vận chuyển.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <%-- Phiếu chờ Xuất kho --%>
                <h6 class="fw-bold text-dark mb-3 mt-4" style="font-size: 15px; display: flex; align-items: center; gap: 6px;">
                    <span class="material-icons" style="color:#0ea5e9;">outbound</span>
                    Chờ Xác Nhận Xuất Kho
                </h6>
                <div class="premium-table-container mb-4">
                    <table class="table premium-table mb-0">
                        <thead>
                            <tr>
                                <th>Mã Phiếu</th>
                                <th>Đến Kho</th>
                                <th>Người Tạo</th>
                                <th>Ngày Tạo</th>
                                <th class="text-center" width="280px">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="hasDispatch" value="false" />
                            <c:forEach var="tx" items="${transfers}">
                                <c:if test="${tx.status == 'APPROVED_DISPATCH' && (empty selectedWarehouseId || selectedWarehouseId == tx.fromWarehouseId)}">
                                    <c:set var="hasDispatch" value="true" />
                                    <tr>
                                        <td class="fw-bold" style="color: var(--primary-color);">${tx.transferCode}</td>
                                        <td>${tx.toWarehouseName}</td>
                                        <td>${tx.createdByName}</td>
                                        <td><fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${tx.transferDate}" /></td>
                                        <td class="text-center">
                                            <div class="d-flex align-items-center justify-content-center gap-2">
                                                <button type="button" class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                        style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #dbeafe; background-color: #eff6ff; color: #2563eb; cursor: pointer; transition: all 0.2s;" 
                                                        title="Xem chi tiết"
                                                        onmouseover="this.style.backgroundColor='#dbeafe';"
                                                        onmouseout="this.style.backgroundColor='#eff6ff';"
                                                        onclick="viewTicketDetails(${tx.stockTransferId})">
                                                    <span class="material-icons" style="font-size: 16px;">visibility</span>
                                                </button>
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="confirmDispatch">
                                                    <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                    <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                            style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #d1fae5; background-color: #ecfdf5; color: #059669; cursor: pointer; transition: all 0.2s;" 
                                                            title="Xác nhận xuất kho"
                                                            onmouseover="this.style.backgroundColor='#d1fae5';"
                                                            onmouseout="this.style.backgroundColor='#ecfdf5';"
                                                            onclick="return confirm('Xác nhận xuất kho cho phiếu này? Tồn kho kho hiện tại sẽ bị trừ.')">
                                                        <span class="material-icons" style="font-size: 16px;">check</span>
                                                    </button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="rejectDispatch">
                                                    <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                    <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                            style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #fee2e2; background-color: #fef2f2; color: #dc2626; cursor: pointer; transition: all 0.2s;" 
                                                            title="Từ chối xuất kho"
                                                            onmouseover="this.style.backgroundColor='#fee2e2';"
                                                            onmouseout="this.style.backgroundColor='#fef2f2';"
                                                            onclick="return confirm('Từ chối xuất hàng cho phiếu này?')">
                                                        <span class="material-icons" style="font-size: 16px;">close</span>
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:if test="${!hasDispatch}">
                                <tr><td colspan="5" class="text-center py-3 text-muted">Không có phiếu nào chờ xuất kho.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <%-- Phiếu chờ Nhập kho --%>
                <h6 class="fw-bold text-dark mb-3 mt-4" style="font-size: 15px; display: flex; align-items: center; gap: 6px;">
                    <span class="material-icons" style="color:#22c55e;">move_to_inbox</span>
                    Chờ Xác Nhận Nhập Kho
                </h6>
                <div class="premium-table-container">
                    <table class="table premium-table mb-0">
                        <thead>
                            <tr>
                                <th>Mã Phiếu</th>
                                <th>Từ Kho</th>
                                <th>Người Tạo</th>
                                <th>Ngày Tạo</th>
                                <th class="text-center" width="280px">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="hasReceive" value="false" />
                            <c:forEach var="tx" items="${transfers}">
                                <c:if test="${tx.status == 'IN_TRANSIT' && (empty selectedWarehouseId || selectedWarehouseId == tx.toWarehouseId)}">
                                    <c:set var="hasReceive" value="true" />
                                    <tr>
                                        <td class="fw-bold" style="color: var(--primary-color);">${tx.transferCode}</td>
                                        <td>${tx.fromWarehouseName}</td>
                                        <td>${tx.createdByName}</td>
                                        <td><fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${tx.transferDate}" /></td>
                                        <td class="text-center">
                                            <div class="d-flex align-items-center justify-content-center gap-2">
                                                <button type="button" class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                        style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #dbeafe; background-color: #eff6ff; color: #2563eb; cursor: pointer; transition: all 0.2s;" 
                                                        title="Xem chi tiết"
                                                        onmouseover="this.style.backgroundColor='#dbeafe';"
                                                        onmouseout="this.style.backgroundColor='#eff6ff';"
                                                        onclick="viewTicketDetails(${tx.stockTransferId})">
                                                    <span class="material-icons" style="font-size: 16px;">visibility</span>
                                                </button>
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="confirmReceive">
                                                    <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                    <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                            style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #d1fae5; background-color: #ecfdf5; color: #059669; cursor: pointer; transition: all 0.2s;" 
                                                            title="Xác nhận nhập kho"
                                                            onmouseover="this.style.backgroundColor='#d1fae5';"
                                                            onmouseout="this.style.backgroundColor='#ecfdf5';"
                                                            onclick="return confirm('Xác nhận nhập kho cho phiếu này? Tồn kho kho hiện tại sẽ được cộng.')">
                                                        <span class="material-icons" style="font-size: 16px;">check</span>
                                                    </button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="rejectReceive">
                                                    <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                    <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm d-inline-flex align-items-center justify-content-center" 
                                                            style="width: 32px; height: 32px; border-radius: 6px; border: 1px solid #fee2e2; background-color: #fef2f2; color: #dc2626; cursor: pointer; transition: all 0.2s;" 
                                                            title="Từ chối nhập kho"
                                                            onmouseover="this.style.backgroundColor='#fee2e2';"
                                                            onmouseout="this.style.backgroundColor='#fef2f2';"
                                                            onclick="return confirm('Từ chối nhận hàng cho phiếu này?')">
                                                        <span class="material-icons" style="font-size: 16px;">close</span>
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:if test="${!hasReceive}">
                                <tr><td colspan="5" class="text-center py-3 text-muted">Không có phiếu nào chờ nhập kho.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </c:when>
        </c:choose>
    </div>
</div>
