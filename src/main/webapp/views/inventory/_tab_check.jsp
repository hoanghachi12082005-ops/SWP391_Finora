<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />

<style>
    .discrepant-row {
        background-color: #fff5f5 !important;
        border-left: 4px solid #ef4444 !important;
    }
    .discrepant-row td {
        color: #b91c1c !important;
    }
    .discrepant-row td strong.text-primary {
        color: #b91c1c !important;
    }
    .discrepant-row:hover {
        background-color: #fee2e2 !important;
    }
</style>

<div class="dashboard-card">
    <div class="card-header border-bottom-0 pb-0 d-flex justify-content-between align-items-center mb-3">
        <h5 class="mb-0 fw-bold text-dark">Lịch Sử Kiểm Kho</h5>
        <c:if test="${roleName == 'WarehouseStaff' || roleName == 'Admin' || roleName == 'Owner'}">
            <a href="${pageContext.request.contextPath}/inventory?tab=createCheck&warehouseId=${selectedWarehouseId}" class="page-action-btn text-decoration-none d-flex align-items-center gap-1">
                <span class="material-icons" style="font-size:18px;">add</span>
                <span>Nhập Phiếu Kiểm Kho</span>
            </a>
        </c:if>
    </div>

    <div class="card-body pt-0">
        <!-- Filter Section -->
        <div class="filter-section mb-4 p-3 bg-light rounded" style="border: 1px solid #e2e8f0; border-radius: 8px;">
            <form action="${pageContext.request.contextPath}/inventory" method="GET" class="row g-2 align-items-center">
                <input type="hidden" name="tab" value="check">
                <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                
                <div class="col-md-4">
                    <div class="input-group">
                        <span class="input-group-text bg-white border-end-0" style="border-radius: 8px 0 0 8px; border: 1px solid #ced4da;"><span class="material-icons text-muted" style="font-size: 18px;">search</span></span>
                        <input type="text" name="checkCodeQuery" class="form-control border-start-0" placeholder="Tìm theo mã phiếu..." value="<c:out value="${param.checkCodeQuery}"/>" style="border-radius: 0 8px 8px 0; font-size: 14px; border: 1px solid #ced4da;">
                    </div>
                </div>
                
                <div class="col-md-3">
                    <select name="statusQuery" class="form-select" style="border-radius: 8px; font-size: 14px; border: 1px solid #ced4da;">
                        <option value="">Tất cả trạng thái</option>
                        <option value="PENDING" ${param.statusQuery == 'PENDING' ? 'selected' : ''}>Chờ duyệt</option>
                        <option value="APPROVED" ${param.statusQuery == 'APPROVED' ? 'selected' : ''}>Đã duyệt</option>
                        <option value="CANCELLED" ${param.statusQuery == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                    </select>
                </div>
                
                <div class="col-md-3">
                    <select name="discrepancyQuery" class="form-select" style="border-radius: 8px; font-size: 14px; border: 1px solid #ced4da;">
                        <option value="">Tất cả phiếu</option>
                        <option value="has_disc" ${param.discrepancyQuery == 'has_disc' ? 'selected' : ''}>Có chênh lệch</option>
                        <option value="no_disc" ${param.discrepancyQuery == 'no_disc' ? 'selected' : ''}>Không chênh lệch</option>
                    </select>
                </div>
                
                <div class="col-md-2">
                    <button type="submit" class="btn btn-primary w-100" style="border-radius: 8px; font-size: 14px; font-weight: 500; height: 38px; background-color: var(--primary-color, #800000); border-color: var(--primary-color, #800000);">
                        Lọc
                    </button>
                </div>
            </form>
        </div>

        <div class="premium-table-container">
            <table class="table premium-table table-hover mb-0">
                <thead>
                    <tr>
                        <th>Mã Phiếu</th>
                        <th>Kho Hàng</th>
                        <th>Ngày Lập</th>
                        <th>Người Thực Hiện</th>
                        <th>Trạng Thái</th>
                        <th class="text-end">Tổng Lệch</th>
                        <th width="200px" class="text-center">Thao Tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty checks}">
                            <tr>
                                <td colspan="7" class="text-center py-4 text-muted">Chưa có dữ liệu kiểm kê.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="c" items="${checks}">
                                <tr class="${c.totalDiscrepancy > 0 ? 'discrepant-row' : ''}">
                                    <td><strong class="text-primary">${c.checkCode}</strong></td>
                                    <td>${c.warehouseName}</td>
                                    <td>${c.formattedCreatedAt}</td>
                                    <td>${c.createdByName}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.status == 'PENDING'}">
                                                <span class="badge bg-warning text-dark" style="border-radius: 6px; font-weight: 600; padding: 6px 12px;">CHỜ DUYỆT</span>
                                            </c:when>
                                            <c:when test="${c.status == 'APPROVED'}">
                                                <span class="badge bg-success" style="border-radius: 6px; font-weight: 600; padding: 6px 12px;">ĐÃ DUYỆT</span>
                                            </c:when>
                                            <c:when test="${c.status == 'CANCELLED'}">
                                                <span class="badge bg-danger" style="border-radius: 6px; font-weight: 600; padding: 6px 12px;">ĐÃ HỦY</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">${c.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end fw-semibold text-danger">
                                        ${c.totalDiscrepancy} SP
                                    </td>
                                    <td class="text-center">
                                        <div class="d-flex align-items-center justify-content-center gap-1">
                                            <button class="btn btn-sm btn-outline-primary" style="border-radius: 6px; padding: 4px 10px; font-size: 13px;" onclick="viewCheckDetails(${c.checkId})">
                                                Chi tiết
                                            </button>
                                            <c:if test="${c.status == 'PENDING' && (roleName == 'Owner' || roleName == 'StoreManager')}">
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" class="d-inline">
                                                    <input type="hidden" name="action" value="approveCheck">
                                                    <input type="hidden" name="checkId" value="${c.checkId}">
                                                    <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm btn-success" style="border-radius: 6px; padding: 4px 10px; font-size: 13px;" onclick="return confirm('Xác nhận duyệt phiếu kiểm kho ${c.checkCode}?')">
                                                        Duyệt
                                                    </button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/inventory" method="POST" class="d-inline">
                                                    <input type="hidden" name="action" value="cancelCheck">
                                                    <input type="hidden" name="checkId" value="${c.checkId}">
                                                    <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                                                    <button type="submit" class="btn btn-sm btn-danger" style="border-radius: 6px; padding: 4px 10px; font-size: 13px;" onclick="return confirm('Xác nhận hủy phiếu kiểm kho ${c.checkCode}?')">
                                                        Hủy
                                                    </button>
                                                </form>
                                            </c:if>
                                            <c:if test="${c.status != 'CANCELLED' && (roleName == 'Owner' || roleName == 'Admin' || roleName == 'StoreManager')}">
                                                <a href="${pageContext.request.contextPath}/inventory?tab=editCheck&action=editCheck&checkId=${c.checkId}&warehouseId=${selectedWarehouseId}" class="btn btn-sm btn-outline-warning" style="border-radius: 6px; padding: 4px 10px; font-size: 13px; font-weight: 500; text-decoration: none;">
                                                    Sửa
                                                </a>
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
