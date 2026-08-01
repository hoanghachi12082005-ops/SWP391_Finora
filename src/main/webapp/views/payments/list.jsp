<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Sổ Quỹ - Quản lý thu chi"/>
</jsp:include>

<style>
    .card-stat {
        border-radius: 12px;
        transition: all 0.3s ease;
    }
    .card-stat:hover {
        transform: translateY(-4px);
    }
    .badge-PT {
        background-color: rgba(25, 135, 84, 0.1) !important;
        color: #198754 !important;
        font-weight: 600;
        border: 1px solid rgba(25, 135, 84, 0.2);
    }
    .badge-PC {
        background-color: rgba(220, 53, 69, 0.1) !important;
        color: #dc3545 !important;
        font-weight: 600;
        border: 1px solid rgba(220, 53, 69, 0.2);
    }
    .bg-winered {
        background-color: #8b0000 !important;
        color: #ffffff;
    }
    .btn-winered {
        background-color: #8b0000;
        color: white;
        border: none;
    }
    .btn-winered:hover {
        background-color: #a00000;
        color: white;
    }
    .text-winered {
        color: #8b0000 !important;
    }
    .trend-up {
        color: #198754;
        font-size: 0.85rem;
    }
    .date-filter-control {
        background-color: #f8f9fa;
        border: 1px solid #dee2e6;
        border-radius: 50rem;
        padding: 6px 16px;
        font-size: 0.875rem;
        color: #495057;
        box-shadow: none;
        transition: all 0.2s ease-in-out;
    }
    .date-filter-control:focus {
        background-color: #ffffff;
        border-color: #8b0000;
        box-shadow: 0 0 0 0.2rem rgba(139, 0, 0, 0.15);
    }
    input[type="date"].date-filter-control::-webkit-calendar-picker-indicator {
        cursor: pointer;
        opacity: 0.7;
    }
    input[type="date"].date-filter-control::-webkit-calendar-picker-indicator:hover {
        opacity: 1;
    }
</style>

<div class="app-container">

    <jsp:include page="../common/sidebar.jsp"/>

    <main class="main-content">

        <div class="container-fluid py-4">

            <!-- Alert Messages -->
            <c:if test="${not empty sessionScope.message}">
                <div class="alert alert-${sessionScope.messageType} alert-dismissible fade show" role="alert">
                    ${sessionScope.message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <c:remove var="message" scope="session"/>
                <c:remove var="messageType" scope="session"/>
            </c:if>

            <!-- Header Section -->
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="fw-bold">Sổ Quỹ</h2>
                    <small class="text-muted">
                        Quản lý dòng tiền và các giao dịch thu chi trong hệ thống.
                    </small>
                </div>

                <div class="d-flex gap-2">
                    <button class="btn btn-outline-secondary" onclick="exportToCSV()">
                        <span class="material-icons align-middle" style="font-size: 1.1rem; margin-right: 4px;">file_download</span> Xuất file
                    </button>
                    <button class="btn btn-success" data-bs-toggle="modal" data-bs-target="#receiptModal">
                        <span class="material-icons align-middle" style="font-size: 1.1rem; margin-right: 4px;">add</span> Lập phiếu thu
                    </button>
                    <button class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#paymentModal">
                        <span class="material-icons align-middle" style="font-size: 1.1rem; margin-right: 4px;">remove</span> Lập phiếu chi
                    </button>
                </div>
            </div>

            <!-- Stats Summary Cards -->
            <div class="row g-3 mb-4">
                <!-- TỔNG QUỸ -->
                <div class="col-md-4">
                    <div class="card card-stat shadow-sm border-0 h-100">
                        <div class="card-body p-4 d-flex flex-column justify-content-between">
                            <div>
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <small class="text-uppercase fw-bold text-muted" style="letter-spacing: 0.5px;">TỔNG QUỸ</small>
                                    <span class="material-icons text-winered">account_balance_wallet</span>
                                </div>
                                <h3 class="fw-extrabold my-2">
                                    <fmt:formatNumber value="${totalFund}" type="number" maxFractionDigits="0"/> đ
                                </h3>
                            </div>
                            <div class="trend-up mt-2">
                                <span class="material-icons align-middle" style="font-size: 1rem;">trending_up</span>
                                <span>+5.2% so với tháng trước</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- TIỀN MẶT -->
                <div class="col-md-4">
                    <div class="card card-stat shadow-sm border-0 h-100">
                        <div class="card-body p-4 d-flex flex-column justify-content-between">
                            <div>
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <small class="text-uppercase fw-bold text-muted" style="letter-spacing: 0.5px;">TIỀN MẶT</small>
                                    <span class="material-icons text-primary">payments</span>
                                </div>
                                <h3 class="fw-extrabold my-2">
                                    <fmt:formatNumber value="${totalCash}" type="number" maxFractionDigits="0"/> đ
                                </h3>
                            </div>
                            <div class="d-flex justify-content-between mt-2 pt-2 border-top text-muted" style="font-size: 0.85rem;">
                                <span>Thu: <strong class="text-success"><fmt:formatNumber value="${cashIncome}" type="number" maxFractionDigits="0"/></strong></span>
                                <span>Chi: <strong class="text-danger"><fmt:formatNumber value="${cashExpense}" type="number" maxFractionDigits="0"/></strong></span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- NGÂN HÀNG -->
                <div class="col-md-4">
                    <div class="card card-stat shadow-sm border-0 h-100">
                        <div class="card-body p-4 d-flex flex-column justify-content-between">
                            <div>
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <small class="text-uppercase fw-bold text-muted" style="letter-spacing: 0.5px;">NGÂN HÀNG</small>
                                    <span class="material-icons text-info">account_balance</span>
                                </div>
                                <h3 class="fw-extrabold my-2">
                                    <fmt:formatNumber value="${totalBank}" type="number" maxFractionDigits="0"/> đ
                                </h3>
                            </div>
                            <div class="d-flex justify-content-between mt-2 pt-2 border-top text-muted" style="font-size: 0.85rem;">
                                <span>Thu: <strong class="text-success"><fmt:formatNumber value="${bankIncome}" type="number" maxFractionDigits="0"/></strong></span>
                                <span>Chi: <strong class="text-danger"><fmt:formatNumber value="${bankExpense}" type="number" maxFractionDigits="0"/></strong></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <!-- Left: Overview Chart -->
                <div class="col-lg-4">
                    <div class="card shadow-sm border-0 h-100">
                        <div class="card-header bg-white py-3 border-0">
                            <h5 class="card-title fw-bold mb-0">Tổng Quan Thu Chi</h5>
                        </div>
                        <div class="card-body d-flex align-items-center justify-content-center">
                            <div style="width: 100%; height: 300px; position: relative;">
                                <canvas id="overviewChart"></canvas>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Right: Transaction History Table -->
                <div class="col-lg-8">
                    <div class="card shadow-sm border-0 h-100">
                        <div class="card-header bg-white py-3 border-0">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <h5 class="card-title fw-bold mb-0">Lịch Sử Giao Dịch</h5>
                            </div>
                            
                            <!-- Filter Options Form -->
                            <form method="get" action="${pageContext.request.contextPath}/cashbook" class="d-flex flex-column gap-3">
                                <!-- Row 1: Keyword, OrderType, PaymentType, Method -->
                                <div class="d-flex flex-wrap gap-3 align-items-end">
                                    <div class="flex-grow-1" style="min-width: 180px;">
                                        <label class="form-label mb-1 text-muted fw-semibold" style="font-size: 0.85rem;">Từ khóa</label>
                                        <input type="text" name="keyword" class="form-control form-control-sm rounded-pill px-3" placeholder="Tìm mã phiếu, mã đơn, nội dung..." value="${keyword}">
                                    </div>

                                    <div>
                                        <label class="form-label mb-1 text-muted fw-semibold" style="font-size: 0.85rem;">Loại đơn hàng</label>
                                        <select name="orderType" class="form-select form-select-sm rounded-pill px-3" style="width: 150px;">
                                            <option value="" ${empty orderType ? 'selected' : ''}>Tất cả loại đơn</option>
                                            <option value="SALE" ${orderType == 'SALE' ? 'selected' : ''}>Bán hàng (SALE)</option>
                                            <option value="PURCHASE" ${orderType == 'PURCHASE' ? 'selected' : ''}>Nhập hàng (PURCHASE)</option>
                                            <option value="OTHER" ${orderType == 'OTHER' ? 'selected' : ''}>Thu/Chi khác (OTHER)</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label class="form-label mb-1 text-muted fw-semibold" style="font-size: 0.85rem;">Loại phiếu</label>
                                        <select name="type" class="form-select form-select-sm rounded-pill px-3" style="width: 130px;">
                                            <option value="" ${empty type ? 'selected' : ''}>Tất cả loại</option>
                                            <option value="INCOME" ${type == 'INCOME' ? 'selected' : ''}>Phiếu thu (PT)</option>
                                            <option value="EXPENSE" ${type == 'EXPENSE' ? 'selected' : ''}>Phiếu chi (PC)</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label class="form-label mb-1 text-muted fw-semibold" style="font-size: 0.85rem;">Phương thức</label>
                                        <select name="paymentMethod" class="form-select form-select-sm rounded-pill px-3" style="width: 130px;">
                                            <option value="" ${empty paymentMethod ? 'selected' : ''}>Mọi quỹ</option>
                                            <option value="CASH" ${paymentMethod == 'CASH' ? 'selected' : ''}>Tiền mặt</option>
                                            <option value="BANK_TRANSFER" ${paymentMethod == 'BANK_TRANSFER' ? 'selected' : ''}>Ngân hàng</option>
                                        </select>
                                    </div>
                                </div>

                                <!-- Row 2: Date pickers & Action Buttons -->
                                <div class="d-flex flex-wrap gap-3 align-items-end">
                                    <div>
                                        <label class="form-label mb-1 text-muted fw-semibold" style="font-size: 0.85rem;">Từ ngày</label>
                                        <input type="date" name="fromDate" value="${fromDate}" class="form-control form-control-sm date-filter-control" style="width: 150px;">
                                    </div>

                                    <div>
                                        <label class="form-label mb-1 text-muted fw-semibold" style="font-size: 0.85rem;">Đến ngày</label>
                                        <input type="date" name="toDate" value="${toDate}" class="form-control form-control-sm date-filter-control" style="width: 150px;">
                                    </div>

                                    <div class="d-flex gap-2">
                                        <button type="submit" class="btn btn-sm btn-winered rounded-pill px-4">Lọc</button>
                                        <c:if test="${not empty fromDate or not empty toDate or not empty keyword or not empty paymentMethod or not empty type or not empty orderType}">
                                            <a href="${pageContext.request.contextPath}/cashbook" class="btn btn-sm btn-outline-secondary rounded-circle d-inline-flex align-items-center justify-content-center" style="width: 31px; height: 31px;" title="Đặt lại bộ lọc">
                                                <span class="material-icons" style="font-size: 1rem;">refresh</span>
                                            </a>
                                        </c:if>
                                    </div>
                                </div>
                            </form>
                        </div>

                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0" id="transactionTable">
                                    <thead class="table-light">
                                        <tr>
                                            <th style="padding-left: 20px;">Mã phiếu / Đơn hàng</th>
                                            <th>Loại đơn</th>
                                            <th>Thời gian / Người tạo</th>
                                            <th>Nội dung</th>
                                            <th class="text-end" style="padding-right: 20px;">Giá trị (VNĐ)</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${not empty transactions}">
                                                <c:forEach var="item" items="${transactions}">
                                                    <tr>
                                                        <td style="padding-left: 20px;">
                                                            <div class="fw-bold">${item.name}</div>
                                                            <c:if test="${not empty item.orderCode}">
                                                                <small class="text-muted"><span class="material-icons align-middle" style="font-size: 0.8rem;">receipt</span> ${item.orderCode}</small><br/>
                                                            </c:if>
                                                            <span class="badge badge-${item.paymentType} rounded-pill px-2 mt-1" style="font-size: 0.75rem;">
                                                                <c:choose>
                                                                    <c:when test="${item.paymentType == 'INCOME'}">Thu</c:when>
                                                                    <c:otherwise>Chi</c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${item.orderType == 'SALE'}">
                                                                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle rounded-pill px-2">SALE</span>
                                                                </c:when>
                                                                <c:when test="${item.orderType == 'PURCHASE'}">
                                                                    <span class="badge bg-warning-subtle text-warning border border-warning-subtle rounded-pill px-2">PURCHASE</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle rounded-pill px-2">OTHER</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <div><fmt:formatDate value="${item.paymentDate}" pattern="dd/MM/yyyy HH:mm"/></div>
                                                            <small class="text-muted">${not empty item.creatorName ? item.creatorName : 'Hệ thống'}</small>
                                                        </td>
                                                        <td>
                                                            <div class="text-truncate" style="max-width: 250px;">
                                                                ${item.description}
                                                            </div>
                                                            <c:if test="${not empty item.branchName}">
                                                                <small class="text-muted"><span class="material-icons align-middle" style="font-size: 0.85rem;">location_on</span> ${item.branchName}</small>
                                                            </c:if>
                                                        </td>
                                                        <td class="text-end fw-bold" style="padding-right: 20px;">
                                                            <c:choose>
                                                                <c:when test="${item.paymentType == 'INCOME'}">
                                                                    <span class="text-success">+ <fmt:formatNumber value="${item.amount}" type="number" maxFractionDigits="0"/> đ</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="text-danger">- <fmt:formatNumber value="${item.amount}" type="number" maxFractionDigits="0"/> đ</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <tr>
                                                    <td colspan="5" class="text-center py-5 text-muted">
                                                        <span class="material-icons d-block mb-2" style="font-size: 2.5rem;">history</span>
                                                        Không tìm thấy giao dịch nào.
                                                    </td>
                                                </tr>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- Pagination Footer -->
                        <c:if test="${totalPage > 1}">
                            <div class="card-footer bg-white border-0 py-3 d-flex justify-content-between align-items-center">
                                <small class="text-muted">Hiển thị trang ${currentPage} trên tổng số ${totalPage}</small>
                                <nav aria-label="Page navigation">
                                    <ul class="pagination pagination-sm mb-0">
                                        <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                            <a class="page-link" href="?page=${currentPage - 1}&keyword=${keyword}&type=${type}&orderType=${orderType}&paymentMethod=${paymentMethod}&fromDate=${fromDate}&toDate=${toDate}&timeRange=${timeRange}">Trước</a>
                                        </li>
                                        <c:choose>
                                            <c:when test="${totalPage <= 5}">
                                                <c:forEach var="p" begin="1" end="${totalPage}">
                                                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                                                        <a class="page-link ${p == currentPage ? 'bg-winered border-winered' : ''}" href="?page=${p}&keyword=${keyword}&type=${type}&orderType=${orderType}&paymentMethod=${paymentMethod}&fromDate=${fromDate}&toDate=${toDate}&timeRange=${timeRange}">${p}</a>
                                                    </li>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <li class="page-item ${currentPage == 1 ? 'active' : ''}">
                                                    <a class="page-link ${currentPage == 1 ? 'bg-winered border-winered' : ''}" href="?page=1&keyword=${keyword}&type=${type}&orderType=${orderType}&paymentMethod=${paymentMethod}&fromDate=${fromDate}&toDate=${toDate}&timeRange=${timeRange}">1</a>
                                                </li>
                                                <c:if test="${currentPage > 3}">
                                                    <li class="page-item disabled"><span class="page-link">...</span></li>
                                                </c:if>
                                                <c:forEach var="p" begin="${currentPage - 1 < 2 ? 2 : currentPage - 1}"
                                                           end="${currentPage + 1 > totalPage - 1 ? totalPage - 1 : currentPage + 1}">
                                                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                                                        <a class="page-link ${p == currentPage ? 'bg-winered border-winered' : ''}" href="?page=${p}&keyword=${keyword}&type=${type}&orderType=${orderType}&paymentMethod=${paymentMethod}&fromDate=${fromDate}&toDate=${toDate}&timeRange=${timeRange}">${p}</a>
                                                    </li>
                                                </c:forEach>
                                                <c:if test="${currentPage < totalPage - 2}">
                                                    <li class="page-item disabled"><span class="page-link">...</span></li>
                                                </c:if>
                                                <li class="page-item ${currentPage == totalPage ? 'active' : ''}">
                                                    <a class="page-link ${currentPage == totalPage ? 'bg-winered border-winered' : ''}" href="?page=${totalPage}&keyword=${keyword}&type=${type}&orderType=${orderType}&paymentMethod=${paymentMethod}&fromDate=${fromDate}&toDate=${toDate}&timeRange=${timeRange}">${totalPage}</a>
                                                </li>
                                            </c:otherwise>
                                        </c:choose>
                                        <li class="page-item ${currentPage == totalPage ? 'disabled' : ''}">
                                            <a class="page-link" href="?page=${currentPage + 1}&keyword=${keyword}&type=${type}&orderType=${orderType}&paymentMethod=${paymentMethod}&fromDate=${fromDate}&toDate=${toDate}&timeRange=${timeRange}">Sau</a>
                                        </li>
                                    </ul>
                                </nav>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>

        </div>

    </main>
</div>

<!-- ============================================================
     MODALS FOR TRANSACTIONS
     ============================================================ -->

<!-- Modal: Lập Phiếu Thu -->
<div class="modal fade" id="receiptModal" tabindex="-1" aria-labelledby="receiptModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header bg-success text-white">
                <h5 class="modal-title fw-bold" id="receiptModalLabel">Lập Phiếu Thu</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="${pageContext.request.contextPath}/cashbook/create-receipt" method="post">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <div class="modal-body p-4">
                    <div class="mb-3">
                        <label class="form-label fw-bold">Số tiền (VNĐ) <span class="text-danger">*</span></label>
                        <input type="number" name="amount" class="form-control" placeholder="Nhập số tiền thu" required min="1000">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Hình thức thu <span class="text-danger">*</span></label>
                        <select name="method" class="form-select" required>
                            <option value="CASH">Tiền mặt</option>
                            <option value="BANK_TRANSFER">Chuyển khoản / Ngân hàng</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Nội dung thu <span class="text-danger">*</span></label>
                        <textarea name="description" class="form-control" rows="3" placeholder="Ví dụ: Thu tiền bán hàng, Thu hồi công nợ, Thu nhập khác..." required></textarea>
                    </div>
                </div>
                <div class="modal-footer border-top-0 p-3">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-success px-4">Lưu phiếu thu</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal: Lập Phiếu Chi -->
<div class="modal fade" id="paymentModal" tabindex="-1" aria-labelledby="paymentModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header bg-danger text-white">
                <h5 class="modal-title fw-bold" id="paymentModalLabel">Lập Phiếu Chi</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="${pageContext.request.contextPath}/cashbook/create-payment" method="post" id="createPaymentForm">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <div class="modal-body p-4">
                    <div class="mb-3">
                        <label class="form-label fw-bold">Số tiền (VNĐ) <span class="text-danger">*</span></label>
                        <input type="number" name="amount" class="form-control" placeholder="Nhập số tiền chi" required min="1000">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Hình thức chi <span class="text-danger">*</span></label>
                        <select name="method" class="form-select" required>
                            <option value="CASH">Tiền mặt</option>
                            <option value="BANK_TRANSFER">Chuyển khoản / Ngân hàng</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Nội dung chi <span class="text-danger">*</span></label>
                        <textarea name="description" class="form-control" rows="3" placeholder="Ví dụ: Thanh toán tiền điện, Nhập hàng, Chi trả lương, Chi phí vận hành..." required></textarea>
                    </div>
                </div>
                <div class="modal-footer border-top-0 p-3">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger px-4">Lưu phiếu chi</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Chart.js and Dashboard Logic -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
    // 1. Initializing Chart.js
    document.addEventListener("DOMContentLoaded", function() {
        const ctx = document.getElementById('overviewChart').getContext('2d');
        
        // Dữ liệu từ servlet
        const chartIncomeData = [${chartIncome}];
        const chartExpenseData = [${chartExpense}];
        
        // Nhãn mặc định
        const labels = ['Tuần 1', 'Tuần 2', 'Tuần 3', 'Tuần 4'];
        
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Tổng Thu',
                        data: chartIncomeData,
                        backgroundColor: '#198754', // màu xanh lá
                        borderRadius: 6,
                        borderSkipped: false
                    },
                    {
                        label: 'Tổng Chi',
                        data: chartExpenseData,
                        backgroundColor: '#dc3545', // màu đỏ
                        borderRadius: 6,
                        borderSkipped: false
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            font: {
                                family: 'Inter',
                                size: 12
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            borderDash: [5, 5]
                        },
                        ticks: {
                            callback: function(value) {
                                if (value >= 1000000) {
                                    return (value / 1000000) + 'M';
                                }
                                return value;
                            }
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });

        // Validation for payment voucher form balance
        const paymentForm = document.getElementById("createPaymentForm");
        if (paymentForm) {
            paymentForm.addEventListener("submit", function(event) {
                const amountInput = paymentForm.querySelector('input[name="amount"]');
                const methodSelect = paymentForm.querySelector('select[name="method"]');
                const amount = parseFloat(amountInput.value);
                const method = methodSelect.value;
                
                let balance = 0;
                if (method === "CASH") {
                    balance = parseFloat("${totalCash}");
                } else if (method === "BANK_TRANSFER") {
                    balance = parseFloat("${totalBank}");
                }
                
                if (amount > balance) {
                    event.preventDefault();
                    let errorDiv = document.getElementById("paymentErrorMsg");
                    if (!errorDiv) {
                        errorDiv = document.createElement("div");
                        errorDiv.id = "paymentErrorMsg";
                        errorDiv.className = "alert alert-danger mt-3";
                        errorDiv.style.color = "#842029";
                        errorDiv.style.backgroundColor = "#f8d7da";
                        errorDiv.style.borderColor = "#f5c2c7";
                        errorDiv.role = "alert";
                        
                        const modalBody = paymentForm.querySelector(".modal-body");
                        modalBody.appendChild(errorDiv);
                    }
                    errorDiv.innerText = "Số dư quỹ tiền mặt không đủ để thực hiện chi khoản này.";
                }
            });
        }
    });

    // 2. Export Client-side CSV
    function exportToCSV() {
        const table = document.getElementById("transactionTable");
        let csvContent = "\uFEFF"; // BOM for UTF-8 compatibility in Excel
        csvContent += "Mã phiếu,Thời gian / Người tạo,Nội dung,Giá trị (VNĐ)\n";
        
        const rows = table.querySelectorAll("tbody tr");
        if (rows.length === 1 && rows[0].innerText.includes("Không tìm thấy")) {
            alert("Không có dữ liệu để xuất.");
            return;
        }

        rows.forEach(row => {
            const cols = row.querySelectorAll("td");
            if (cols.length >= 4) {
                // Mã phiếu
                const code = cols[0].querySelector("div").innerText.trim();
                const type = cols[0].querySelector("span").innerText.trim();
                
                // Thời gian / Người tạo
                const time = cols[1].querySelector("div").innerText.trim();
                const creator = cols[1].querySelector("small").innerText.trim();
                
                // Nội dung
                const content = cols[2].querySelector("div").innerText.trim().replace(/"/g, '""');
                
                // Giá trị
                const val = cols[3].innerText.trim().replace(/[^\d.+-]/g, '');
                
                csvContent += "\"" + code + " (" + type + ")\",\"" + time + " - " + creator + "\",\"" + content + "\",\"" + val + "\"\n";
            }
        });
        
        const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
        const link = document.createElement("a");
        const url = URL.createObjectURL(blob);
        link.setAttribute("href", url);
        link.setAttribute("download", "SoQuy_" + new Date().toISOString().slice(0, 10) + ".csv");
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
</script>

<jsp:include page="../common/footer.jsp" />
