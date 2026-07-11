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
            <h5 class="mb-0">Điều chuyển kho</h5>
            <c:if test="${roleName == 'WarehouseStaff' || roleName == 'StoreManager' || roleName == 'Admin' || roleName == 'Owner'}">
                <div class="d-flex gap-2">
                    <a href="${pageContext.request.contextPath}/inventory?tab=createTransfer&type=RECEIVE&warehouseId=${selectedWarehouseId}" class="btn btn-success btn-sm d-flex align-items-center gap-1" style="border-radius: 8px; font-weight: 500; padding: 6px 12px;">
                        <span class="material-icons" style="font-size:18px;">call_received</span>
                        Tạo Phiếu Nhập Chuyển Kho
                    </a>
                    <a href="${pageContext.request.contextPath}/inventory?tab=createTransfer&type=SEND&warehouseId=${selectedWarehouseId}" class="btn btn-danger btn-sm d-flex align-items-center gap-1" style="border-radius: 8px; font-weight: 500; padding: 6px 12px;">
                        <span class="material-icons" style="font-size:18px;">call_made</span>
                        Tạo Phiếu Xuất Chuyển Kho
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
            <%-- ============ SUB-TAB 1: ĐƠN ĐIỀU CHUYỂN (Xem trạng thái) ============ --%>
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
                                        style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; font-size: 14px; box-shadow: none; appearance: none; cursor: pointer; background-image: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%239CA3AF%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E'); background-repeat: no-repeat; background-position: right 14px top 50%; background-size: 10px auto;">
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
                                        style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; font-size: 14px; box-shadow: none; appearance: none; cursor: pointer; background-image: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%239CA3AF%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E'); background-repeat: no-repeat; background-position: right 14px top 50%; background-size: 10px auto;">
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="PENDING_DISPATCH" ${statusQuery == 'PENDING_DISPATCH' ? 'selected' : ''}>Chờ duyệt</option>
                                    <option value="APPROVED_DISPATCH" ${statusQuery == 'APPROVED_DISPATCH' ? 'selected' : ''}>Chờ xuất kho</option>
                                    <option value="IN_TRANSIT" ${statusQuery == 'IN_TRANSIT' ? 'selected' : ''}>Đang chuyển</option>
                                    <option value="COMPLETED" ${statusQuery == 'COMPLETED' ? 'selected' : ''}>Hoàn thành</option>
                                    <option value="CANCELLED" ${statusQuery == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                                </select>
                            </div>
                        </div>
                        
                        <div class="col-md-2 col-sm-6">
                            <button type="submit" class="btn inventory-btn-filter w-100" style="height: 43px;">
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
                                <th>Từ Kho</th>
                                <th>Đến Kho</th>
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
                                        <td colspan="8" class="text-center py-4 text-muted">Không có phiếu điều chuyển nào</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="tx" items="${transfers}">
                                        <tr>
                                            <td class="fw-medium" style="color: #4f46e5;">${tx.transferCode}</td>
                                            <td>${tx.fromWarehouseName}</td>
                                            <td>${tx.toWarehouseName}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${tx.status == 'PENDING_DISPATCH'}">
                                                        <span class="badge bg-warning text-dark">CHỜ DUYỆT</span>
                                                    </c:when>
                                                    <c:when test="${tx.status == 'APPROVED_DISPATCH'}">
                                                        <span class="badge bg-info text-dark">CHỜ XUẤT KHO</span>
                                                    </c:when>
                                                    <c:when test="${tx.status == 'IN_TRANSIT'}">
                                                        <span class="badge bg-primary">ĐANG CHUYỂN</span>
                                                    </c:when>
                                                    <c:when test="${tx.status == 'COMPLETED'}">
                                                        <span class="badge bg-success">HOÀN THÀNH</span>
                                                    </c:when>
                                                    <c:when test="${tx.status == 'CANCELLED'}">
                                                        <span class="badge bg-danger">ĐÃ HỦY</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary">${tx.status}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <%-- Visual timeline --%>
                                                <div class="status-timeline">
                                                    <c:choose>
                                                        <c:when test="${tx.status == 'PENDING_DISPATCH'}">
                                                            <span class="status-step current">Chờ duyệt</span>
                                                            <span class="material-icons" style="font-size:12px;color:#cbd5e1;">arrow_forward</span>
                                                            <span class="status-step pending">Xuất kho</span>
                                                            <span class="material-icons" style="font-size:12px;color:#cbd5e1;">arrow_forward</span>
                                                            <span class="status-step pending">Nhập kho</span>
                                                        </c:when>
                                                        <c:when test="${tx.status == 'APPROVED_DISPATCH'}">
                                                            <span class="status-step done">Đã duyệt</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step current">Chờ xuất</span>
                                                            <span class="material-icons" style="font-size:12px;color:#cbd5e1;">arrow_forward</span>
                                                            <span class="status-step pending">Nhập kho</span>
                                                        </c:when>
                                                        <c:when test="${tx.status == 'IN_TRANSIT'}">
                                                            <span class="status-step done">Đã duyệt</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step done">Đã xuất</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step current">Chờ nhập</span>
                                                        </c:when>
                                                        <c:when test="${tx.status == 'COMPLETED'}">
                                                            <span class="status-step done">Đã duyệt</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step done">Đã xuất</span>
                                                            <span class="material-icons" style="font-size:12px;color:#16a34a;">arrow_forward</span>
                                                            <span class="status-step done">Đã nhập</span>
                                                        </c:when>
                                                        <c:when test="${tx.status == 'CANCELLED'}">
                                                            <span class="status-step" style="background:#fef2f2;color:#e11d48;">Đã hủy</span>
                                                        </c:when>
                                                    </c:choose>
                                                </div>
                                            </td>
                                            <td>${tx.createdByName}</td>
                                            <td>
                                                <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${tx.transferDate}" />
                                            </td>
                                            <td class="text-center">
                                                <button class="btn btn-sm btn-outline-primary" style="padding: 4px 8px; font-size: 12px; border-radius: 6px;" onclick="viewTicketDetails(${tx.stockTransferId})">
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

            <%-- ============ SUB-TAB 2: XỬ LÝ ĐIỀU CHUYỂN (Xác nhận Xuất/Nhập) ============ --%>
            <c:when test="${currentSubtab == 'transfer_process'}">
                <div class="mb-3">
                    <p class="text-muted small mb-0">
                        <span class="material-icons" style="font-size:16px; vertical-align:text-bottom; margin-right:4px;">info</span>
                        Nhân viên kho thực hiện xác nhận Xuất kho (tại kho gửi) và Nhập kho (tại kho nhận) tại đây.
                    </p>
                </div>

                <%-- Phiếu chờ Xuất kho --%>
                <h6 class="fw-bold text-dark mb-3" style="font-size: 14px;">
                    <span class="material-icons" style="font-size:18px; vertical-align:text-bottom; margin-right:4px; color:#0ea5e9;">outbound</span>
                    Chờ Xác Nhận Xuất Kho
                </h6>
                <div class="premium-table-container mb-4">
                    <table class="table premium-table mb-0">
                        <thead>
                            <tr>
                                <th>Mã Phiếu</th>
                                <th>Từ Kho</th>
                                <th>Đến Kho</th>
                                <th>Người Tạo</th>
                                <th>Ngày Tạo</th>
                                <th class="text-center" width="160px">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="hasDispatch" value="false" />
                            <c:forEach var="tx" items="${transfers}">
                                <c:if test="${tx.status == 'APPROVED_DISPATCH' && (empty selectedWarehouseId || selectedWarehouseId == tx.fromWarehouseId)}">
                                    <c:set var="hasDispatch" value="true" />
                                    <tr>
                                        <td class="fw-medium" style="color: #4f46e5;">${tx.transferCode}</td>
                                        <td>${tx.fromWarehouseName}</td>
                                        <td>${tx.toWarehouseName}</td>
                                        <td>${tx.createdByName}</td>
                                        <td><fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${tx.transferDate}" /></td>
                                        <td class="text-center">
                                            <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                <input type="hidden" name="action" value="confirmDispatch">
                                                <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                <button type="submit" class="btn btn-sm btn-info text-white fw-medium" style="border-radius: 8px; padding: 6px 16px;" 
                                                        onclick="return confirm('Xác nhận xuất kho trung chuyển? Tồn kho sẽ được trừ tương ứng.')">
                                                    <span class="material-icons" style="font-size:14px; vertical-align:text-bottom;">outbound</span> Xác Nhận Xuất
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:if test="${!hasDispatch}">
                                <tr><td colspan="6" class="text-center py-3 text-muted">Không có phiếu nào chờ xuất kho.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <%-- Phiếu chờ Nhập kho --%>
                <h6 class="fw-bold text-dark mb-3" style="font-size: 14px;">
                    <span class="material-icons" style="font-size:18px; vertical-align:text-bottom; margin-right:4px; color:#22c55e;">move_to_inbox</span>
                    Chờ Xác Nhận Nhập Kho
                </h6>
                <div class="premium-table-container">
                    <table class="table premium-table mb-0">
                        <thead>
                            <tr>
                                <th>Mã Phiếu</th>
                                <th>Từ Kho</th>
                                <th>Đến Kho</th>
                                <th>Người Tạo</th>
                                <th>Ngày Tạo</th>
                                <th class="text-center" width="160px">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="hasReceive" value="false" />
                            <c:forEach var="tx" items="${transfers}">
                                <c:if test="${tx.status == 'IN_TRANSIT' && (empty selectedWarehouseId || selectedWarehouseId == tx.toWarehouseId)}">
                                    <c:set var="hasReceive" value="true" />
                                    <tr>
                                        <td class="fw-medium" style="color: #4f46e5;">${tx.transferCode}</td>
                                        <td>${tx.fromWarehouseName}</td>
                                        <td>${tx.toWarehouseName}</td>
                                        <td>${tx.createdByName}</td>
                                        <td><fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${tx.transferDate}" /></td>
                                        <td class="text-center">
                                            <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0;">
                                                <input type="hidden" name="action" value="confirmReceive">
                                                <input type="hidden" name="transferId" value="${tx.stockTransferId}">
                                                <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                <button type="submit" class="btn btn-sm btn-success fw-medium" style="border-radius: 8px; padding: 6px 16px;" 
                                                        onclick="return confirm('Xác nhận nhập kho trung chuyển? Tồn kho sẽ được cộng tương ứng.')">
                                                    <span class="material-icons" style="font-size:14px; vertical-align:text-bottom;">move_to_inbox</span> Xác Nhận Nhập
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:if test="${!hasReceive}">
                                <tr><td colspan="6" class="text-center py-3 text-muted">Không có phiếu nào chờ nhập kho.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </c:when>
        </c:choose>
    </div>
</div>
