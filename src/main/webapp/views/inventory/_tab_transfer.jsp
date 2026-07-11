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
                <a href="${pageContext.request.contextPath}/inventory?tab=createTransfer&warehouseId=${selectedWarehouseId}" class="page-action-btn text-decoration-none">
                    <span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">add</span>
                    Tạo Phiếu Điều Chuyển
                </a>
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
