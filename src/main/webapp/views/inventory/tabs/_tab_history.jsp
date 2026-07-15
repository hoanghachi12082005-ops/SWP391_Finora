<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- 
  ==========================================================================
  TAB NHẬT KÝ BIẾN ĐỘNG KHO TỔNG HỢP (_tab_history.jsp)
  - Hiển thị lịch sử chi tiết mọi giao dịch thay đổi hàng tồn kho (thẻ kho).
  - Cho phép người dùng tìm kiếm theo tên sản phẩm, lọc theo loại giao dịch (Nhập, Xuất, Trả hàng, Chuyển đi, Chuyển đến, Kiểm kho) và lọc theo khoảng thời gian.
  ==========================================================================
--%>

<div class="dashboard-card mb-4">
    <div class="card-header mb-3">
        <h5 class="mb-0" style="font-weight: 700; color: #111827;">Lịch sử</h5>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show mx-3 my-2" role="alert" style="border-radius: 8px; font-size: 13.5px; background-color: #fdf2f2; border-color: #fde8e8; color: #9b1c1c;">
            <span class="material-icons" style="font-size:16px; vertical-align:text-bottom; margin-right:4px;">warning</span>
            ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <!-- Filter form -->
    <div class="p-3 bg-white border-bottom rounded-3 mb-3">
        <form action="${pageContext.request.contextPath}/inventory" method="GET" id="historyFilterForm" class="row g-3 align-items-end m-0">
            <input type="hidden" name="tab" value="history">
            <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
            
            <div class="col-md-3 col-sm-6">
                <label class="form-label small text-muted fw-semibold mb-1 ms-1">Tìm tên sản phẩm</label>
                <div class="position-relative">
                    <span class="material-icons position-absolute text-muted" style="left: 16px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">search</span>
                    <input type="text" name="productNameQuery" id="productNameQuery" class="form-control rounded-pill inventory-search-input w-100" 
                           style="padding-left: 48px; padding-right: 20px; padding-top: 10px; padding-bottom: 10px; font-size: 14.5px; box-shadow: none;" 
                           placeholder="Tìm tên sản phẩm..." value="${productNameQuery}">
                </div>
            </div>
            
            <div class="col-md-3 col-sm-6">
                <label class="form-label small text-muted fw-semibold mb-1 ms-1">Loại giao dịch</label>
                <div class="position-relative">
                    <span class="material-icons position-absolute text-muted" style="left: 14px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">category</span>
                    <select name="typeFilter" class="form-select rounded-pill inventory-filter-select" 
                            style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; font-size: 14px; box-shadow: none; appearance: none; cursor: pointer; background-image: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%239CA3AF%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E'); background-repeat: no-repeat; background-position: right 14px top 50%; background-size: 10px auto;">
                        <option value="">Tất cả loại</option>
                        <option value="IMPORT" ${typeFilter == 'IMPORT' ? 'selected' : ''}>Nhập hàng (Import)</option>
                        <option value="EXPORT" ${typeFilter == 'EXPORT' ? 'selected' : ''}>Xuất hàng (Export)</option>
                        <option value="RETURN" ${typeFilter == 'RETURN' ? 'selected' : ''}>Trả hàng (Return)</option>
                        <option value="TRANSFER_IN" ${typeFilter == 'TRANSFER_IN' ? 'selected' : ''}>Nhận điều chuyển</option>
                        <option value="TRANSFER_OUT" ${typeFilter == 'TRANSFER_OUT' ? 'selected' : ''}>Xuất điều chuyển</option>
                        <option value="CHECK" ${typeFilter == 'CHECK' ? 'selected' : ''}>Kiểm kho (Check)</option>
                    </select>
                </div>
            </div>
            
            <div class="col-md-2 col-sm-6">
                <label class="form-label small text-muted fw-semibold mb-1 ms-1">Từ ngày</label>
                <input type="date" name="fromDate" id="fromDate" class="form-control rounded-pill inventory-search-input w-100" 
                       min="1000-01-01" max="9999-12-31"
                       style="padding-top: 10px; padding-bottom: 10px; padding-left: 20px; padding-right: 20px; font-size: 14px; box-shadow: none;" value="${fromDate}">
            </div>
            
            <div class="col-md-2 col-sm-6">
                <label class="form-label small text-muted fw-semibold mb-1 ms-1">Đến ngày</label>
                <input type="date" name="toDate" id="toDate" class="form-control rounded-pill inventory-search-input w-100" 
                       min="1000-01-01" max="9999-12-31"
                       style="padding-top: 10px; padding-bottom: 10px; padding-left: 20px; padding-right: 20px; font-size: 14px; box-shadow: none;" value="${toDate}">
            </div>
            
            <div class="col-md-2 col-sm-12">
                <button type="submit" class="btn inventory-btn-filter w-100" style="height: 43px;">
                    <span class="material-icons" style="font-size: 18px; margin-right: 6px;">filter_alt</span>
                    <span>Lọc</span>
                </button>
            </div>
        </form>
    </div>




        <div class="premium-table-container">
            <table class="table premium-table mb-0 align-middle">
                <thead>
                    <tr>
                        <th class="ps-3">Mã Phiếu</th>
                        <th>Loại Phiếu</th>
                        <th>Người Tạo</th>
                        <th>Người Duyệt</th>
                        <th class="text-center">Thời Gian</th>
                        <th class="text-center">Trạng Thái</th>
                        <th width="320px">Ghi Chú</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty voucherHistory}">
                            <tr>
                                <td colspan="7" class="text-center py-4 text-muted">Không có dữ liệu lịch sử phiếu.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="vo" items="${voucherHistory}">
                                <tr>
                                    <td class="ps-3 fw-semibold" style="color: var(--primary-color);">${vo.code}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${vo.type == 'TRANSFER'}">
                                                <span class="badge" style="background-color: #64748b; color: #fff; font-size: 11px; padding: 4px 10px;">${vo.typeLabel}</span>
                                            </c:when>
                                            <c:when test="${vo.type == 'CHECK'}">
                                                <span class="badge" style="background-color: #6366f1; color: #fff; font-size: 11px; padding: 4px 10px;">${vo.typeLabel}</span>
                                            </c:when>
                                            <c:when test="${vo.type == 'IMPORT'}">
                                                <span class="badge bg-success-subtle text-success border border-success-subtle" style="font-size: 11px; padding: 4px 10px;">${vo.typeLabel}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-danger-subtle text-danger border border-danger-subtle" style="font-size: 11px; padding: 4px 10px;">${vo.typeLabel}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${vo.createdBy}</td>
                                    <td class="fw-medium text-dark">${vo.approvedBy}</td>
                                    <td class="text-center">${vo.createdAt}</td>
                                    <td class="text-center">
                                        <span class="badge ${vo.statusColor}" style="font-size: 11px; padding: 4px 10px;">${vo.statusLabel}</span>
                                    </td>
                                    <td>
                                        <div style="font-size: 13px; color: #4b5563; margin-bottom: 2px;">
                                            <c:choose>
                                                <c:when test="${vo.type == 'IMPORT'}">
                                                    Nhập hàng từ phiếu ${vo.code}
                                                </c:when>
                                                <c:when test="${vo.type == 'EXPORT'}">
                                                    Xuất hàng từ phiếu ${vo.code}
                                                </c:when>
                                                <c:when test="${vo.type == 'TRANSFER'}">
                                                    Điều chuyển hàng tới ${vo.partner}
                                                </c:when>
                                                <c:when test="${vo.type == 'CHECK'}">
                                                    Kiểm kê kho ${vo.partner}
                                                </c:when>
                                                <c:otherwise>
                                                    Xem chi tiết phiếu
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <a href="javascript:void(0)" class="text-primary small fw-semibold d-inline-flex align-items-center gap-1" style="font-size: 11.5px; text-decoration: none;" onclick="${vo.detailCallback}">
                                            <span class="material-icons" style="font-size: 13px; vertical-align: middle;">open_in_new</span>
                                            <span>Xem chi tiết</span>
                                        </a>
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
