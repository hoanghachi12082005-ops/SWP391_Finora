<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>



<div class="dashboard-card">
    <div class="d-flex justify-content-between align-items-center mb-4 p-3">
        <div>
            <h4 class="mb-1 fw-bold text-dark" style="letter-spacing: -0.5px;">Xử Lý Phiếu</h4>
            <p class="text-muted mb-0 small">Phê duyệt hoặc từ chối các phiếu đang chờ</p>
        </div>
    </div>

    <!-- Filter form -->
    <div class="p-3 bg-white border-bottom rounded-3 mb-3 mx-3">
        <form action="${pageContext.request.contextPath}/inventory" method="GET" id="approvalFilterForm" class="row g-3 align-items-end m-0">
            <input type="hidden" name="tab" value="approval">
            
            <div class="col-md-4 col-sm-6">
                <label class="form-label small text-muted fw-semibold mb-1 ms-1">Mã phiếu</label>
                <div class="position-relative">
                    <span class="material-icons position-absolute text-muted" style="left: 16px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">search</span>
                    <input type="text" name="transferCodeQuery" class="form-control rounded-pill inventory-search-input w-100" 
                           style="padding-left: 48px; padding-right: 20px; padding-top: 10px; padding-bottom: 10px; font-size: 14.5px; box-shadow: none;" 
                           placeholder="Tìm mã phiếu..." value="${transferCodeQuery}">
                </div>
            </div>
            
            <div class="col-md-3 col-sm-6">
                <label class="form-label small text-muted fw-semibold mb-1 ms-1">Kho xuất (Gửi)</label>
                <div class="position-relative">
                    <span class="material-icons position-absolute text-muted" style="left: 14px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">warehouse</span>
                    <select name="fromWarehouseQuery" class="form-select rounded-pill inventory-filter-select" 
                            style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; font-size: 14px; box-shadow: none; appearance: none; cursor: pointer; background-image: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%239CA3AF%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E'); background-repeat: no-repeat; background-position: right 14px top 50%; background-size: 10px auto;">
                        <option value="">Tất cả kho xuất</option>
                        <c:forEach var="w" items="${warehouses}">
                            <option value="${w.warehouseId}" ${fromWarehouseQuery == w.warehouseId ? 'selected' : ''}>${w.warehouseName}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            
            <div class="col-md-3 col-sm-6">
                <label class="form-label small text-muted fw-semibold mb-1 ms-1">Kho nhập (Nhận)</label>
                <div class="position-relative">
                    <span class="material-icons position-absolute text-muted" style="left: 14px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">warehouse</span>
                    <select name="toWarehouseQuery" class="form-select rounded-pill inventory-filter-select" 
                            style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; font-size: 14px; box-shadow: none; appearance: none; cursor: pointer; background-image: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%239CA3AF%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E'); background-repeat: no-repeat; background-position: right 14px top 50%; background-size: 10px auto;">
                        <option value="">Tất cả kho nhập</option>
                        <c:forEach var="w" items="${warehouses}">
                            <option value="${w.warehouseId}" ${toWarehouseQuery == w.warehouseId ? 'selected' : ''}>${w.warehouseName}</option>
                        </c:forEach>
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

    <!-- Danh Sách Phiếu Chờ Duyệt (Gộp làm 1) -->
    <div class="card approval-card mx-3 mb-4">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table mb-0 align-middle">
                    <thead>
                        <tr>
                            <th class="ps-4">Mã Phiếu</th>
                            <th>Loại Phiếu</th>
                            <th>Kho Yêu Cầu</th>
                            <th>Người Yêu Cầu</th>
                            <th>Ngày Tạo</th>
                            <th class="text-end">Tổng Tiền</th>
                            <th class="text-center" width="280px">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty unifiedApprovals}">
                                <tr>
                                    <td colspan="7" class="text-center py-5 text-muted">
                                        <span class="material-icons d-block mb-2 text-muted" style="font-size: 36px;">inbox</span>
                                        Không có phiếu nào đang chờ duyệt.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="item" items="${unifiedApprovals}">
                                    <tr>
                                        <td class="ps-4"><strong style="color: var(--primary-color);">${item.code}</strong></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.type == 'TRANSFER'}">
                                                    <span class="badge" style="background-color: #64748b; color: #fff; font-size: 11px; padding: 4px 10px;">${item.typeLabel}</span>
                                                </c:when>
                                                <c:when test="${item.type == 'PURCHASE'}">
                                                    <span class="badge bg-success-subtle text-success border border-success-subtle" style="font-size: 11px; padding: 4px 10px;">${item.typeLabel}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-danger-subtle text-danger border border-danger-subtle" style="font-size: 11px; padding: 4px 10px;">${item.typeLabel}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${item.warehouseName}</td>
                                        <td>${item.createdBy}</td>
                                        <td>${item.createdAt}</td>
                                        <td class="text-end fw-semibold">
                                            <c:choose>
                                                <c:when test="${not empty item.amount}">
                                                    <fmt:formatNumber value="${item.amount}" type="currency" currencySymbol="đ" maxFractionDigits="0" />
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-center">
                                            <div class="d-flex align-items-center justify-content-center gap-2">
                                                <button class="btn btn-sm d-inline-flex align-items-center gap-1 px-2.5 py-1.5" style="border: 1px solid #dbeafe; background-color: #eff6ff; color: #2563eb; font-weight: 600; font-size: 12.5px; border-radius: 6px; transition: all 0.2s; height: 32px; cursor: pointer;" onmouseover="this.style.backgroundColor='#dbeafe'; this.style.color='#1d4ed8';" onmouseout="this.style.backgroundColor='#eff6ff'; this.style.color='#2563eb';" onclick="${item.detailCallback}">
                                                    <span class="material-icons" style="font-size: 15px; vertical-align: middle;">visibility</span>
                                                    <span>Chi tiết</span>
                                                </button>
                                                <c:if test="${not empty item.actionApprove}">
                                                    <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block;">
                                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                        <input type="hidden" name="action" value="${item.actionApprove}">
                                                        <input type="hidden" name="${item.idParamName}" value="${item.id}">
                                                        <button class="btn btn-sm d-inline-flex align-items-center gap-1 px-2.5 py-1.5" style="border: 1px solid #d1fae5; background-color: #ecfdf5; color: #059669; font-weight: 600; font-size: 12.5px; border-radius: 6px; transition: all 0.2s; height: 32px; cursor: pointer;" onmouseover="this.style.backgroundColor='#d1fae5'; this.style.color='#047857';" onmouseout="this.style.backgroundColor='#ecfdf5'; this.style.color='#059669';" type="submit" onclick="return confirm('Xác nhận duyệt phiếu này?')">
                                                            <span class="material-icons" style="font-size: 15px; vertical-align: middle;">check</span>
                                                            <span>Duyệt</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${not empty item.actionReject}">
                                                    <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block;">
                                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                        <input type="hidden" name="action" value="${item.actionReject}">
                                                        <input type="hidden" name="${item.idParamName}" value="${item.id}">
                                                        <button class="btn btn-sm d-inline-flex align-items-center gap-1 px-2.5 py-1.5" style="border: 1px solid #fee2e2; background-color: #fef2f2; color: #dc2626; font-weight: 600; font-size: 12.5px; border-radius: 6px; transition: all 0.2s; height: 32px; cursor: pointer;" onmouseover="this.style.backgroundColor='#fee2e2'; this.style.color='#b91c1c';" onmouseout="this.style.backgroundColor='#fef2f2'; this.style.color='#dc2626';" type="submit" onclick="return confirm('Từ chối phiếu này?')">
                                                            <span class="material-icons" style="font-size: 15px; vertical-align: middle;">close</span>
                                                            <span>Từ chối</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${not empty item.actionCancel}">
                                                    <form action="${pageContext.request.contextPath}/inventory" method="POST" style="margin:0; display: inline-block;">
                                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                        <input type="hidden" name="action" value="${item.actionCancel}">
                                                        <input type="hidden" name="${item.idParamName}" value="${item.id}">
                                                        <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                        <button class="btn btn-sm d-inline-flex align-items-center gap-1 px-2.5 py-1.5" style="border: 1px solid #fee2e2; background-color: #fef2f2; color: #dc2626; font-weight: 600; font-size: 12.5px; border-radius: 6px; transition: all 0.2s; height: 32px; cursor: pointer;" onmouseover="this.style.backgroundColor='#fee2e2'; this.style.color='#b91c1c';" onmouseout="this.style.backgroundColor='#fef2f2'; this.style.color='#dc2626';" type="submit" onclick="return confirm('Xác nhận hủy toàn bộ phiếu điều chuyển này?')">
                                                            <span class="material-icons" style="font-size: 15px; vertical-align: middle;">block</span>
                                                            <span>Hủy</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
