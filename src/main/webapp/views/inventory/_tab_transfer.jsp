<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />
<c:set var="currentSubtab" value="${not empty param.subtab ? param.subtab : 'transfer_list'}" />

<style>
    .transfer-subtab-nav { border-bottom: 2px solid #e2e8f0; margin-bottom: 20px; display: flex; gap: 0; }
    .transfer-subtab-link { padding: 12px 20px; color: #64748b; text-decoration: none; font-weight: 500; font-size: 14px; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.2s; }
    .transfer-subtab-link:hover { color: var(--primary-color); background-color: rgba(79,70,229,0.04); }
    .transfer-subtab-link.active { color: var(--primary-color); border-bottom-color: var(--primary-color); font-weight: 600; }
    .status-timeline { display: flex; align-items: center; gap: 4px; font-size: 12px; }
    .status-step { padding: 3px 8px; border-radius: 12px; font-weight: 500; }
    .status-step.done { background: #dcfce7; color: #16a34a; }
    .status-step.current { background: #dbeafe; color: #2563eb; animation: pulse-status 2s infinite; }
    .status-step.pending { background: #f1f5f9; color: #94a3b8; }
    @keyframes pulse-status { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }
</style>

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
                                    <option value="PENDING_OWNER" ${statusQuery == 'PENDING_OWNER' ? 'selected' : ''}>Chờ Owner duyệt</option>
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
                                                        <span class="badge bg-warning text-dark">CHỜ OWNER DUYỆT</span>
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
                                                        <span class="badge bg-warning text-dark">⚠️ HOÀN THÀNH 1 PHẦN (CÓ LỖI)</span>
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
                                                            <span class="status-step current">Chờ Owner</span>
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
                                                <button class="btn btn-sm btn-outline-danger" style="padding: 4px 10px; font-size: 12px; border-radius: 6px; border-color: var(--primary-color); color: var(--primary-color);" onclick="viewTicketDetails(${tx.stockTransferId})">
                                                    Chi tiết
                                                </button>
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
                                            <div class="d-flex gap-2 justify-content-center">
                                                <button type="button" class="btn btn-sm btn-outline-primary fw-medium d-flex align-items-center gap-1" style="border-radius: 8px; padding: 6px 12px;" onclick="viewTicketDetails(${tx.stockTransferId})">
                                                    <span class="material-icons" style="font-size:14px;">visibility</span> Chi Tiết
                                                </button>
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="confirmDispatch">
                                                    <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                    <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm btn-success fw-medium d-flex align-items-center gap-1" style="border-radius: 8px; padding: 6px 12px;" 
                                                            onclick="return confirm('Xác nhận xuất kho cho phiếu này? Tồn kho kho hiện tại sẽ bị trừ.')">
                                                        <span class="material-icons" style="font-size:14px;">check</span> Xác Nhận Xuất
                                                    </button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="rejectDispatch">
                                                    <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                    <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger fw-medium d-flex align-items-center gap-1" style="border-radius: 8px; padding: 6px 12px;" 
                                                            onclick="return confirm('Từ chối xuất hàng cho phiếu này?')">
                                                        <span class="material-icons" style="font-size:14px;">close</span> Từ Chối Xuất
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
                                            <div class="d-flex gap-2 justify-content-center">
                                                <button type="button" class="btn btn-sm btn-outline-primary fw-medium d-flex align-items-center gap-1" style="border-radius: 8px; padding: 6px 12px;" onclick="viewTicketDetails(${tx.stockTransferId})">
                                                    <span class="material-icons" style="font-size:14px;">visibility</span> Chi Tiết
                                                </button>
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="confirmReceive">
                                                    <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                    <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm btn-success fw-medium d-flex align-items-center gap-1" style="border-radius: 8px; padding: 6px 12px;" 
                                                            onclick="return confirm('Xác nhận nhập kho cho phiếu này? Tồn kho kho hiện tại sẽ được cộng.')">
                                                        <span class="material-icons" style="font-size:14px;">check</span> Xác Nhận Nhập
                                                    </button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                    <input type="hidden" name="action" value="rejectReceive">
                                                    <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                    <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger fw-medium d-flex align-items-center gap-1" style="border-radius: 8px; padding: 6px 12px;" 
                                                            onclick="return confirm('Từ chối nhận hàng cho phiếu này?')">
                                                        <span class="material-icons" style="font-size:14px;">close</span> Từ Chối Nhận
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
