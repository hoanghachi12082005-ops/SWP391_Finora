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
                        <div class="card-header bg-white py-3 border-0 d-flex flex-wrap justify-content-between align-items-center gap-2">
                            <h5 class="card-title fw-bold mb-0">Lịch Sử Giao Dịch</h5>
                            
                            <!-- Filter Options Form -->
                            <form method="get" action="${pageContext.request.contextPath}/cashbook" class="d-flex flex-wrap gap-2 align-items-center">
                                <div class="input-group input-group-sm" style="max-width: 200px;">
                                    <input type="text" name="keyword" class="form-control" placeholder="Tìm mã, nội dung..." value="${keyword}">
                                </div>

                                <select name="timeRange" class="form-select form-select-sm" style="width: 120px;" onchange="this.form.submit()">
                                    <option value="all" ${timeRange == 'all' ? 'selected' : ''}>Tất cả thời gian</option>
                                    <option value="today" ${timeRange == 'today' ? 'selected' : ''}>Hôm nay</option>
                                    <option value="yesterday" ${timeRange == 'yesterday' ? 'selected' : ''}>Hôm qua</option>
                                    <option value="this_month" ${timeRange == 'this_month' ? 'selected' : ''}>Tháng này</option>
                                    <option value="last_month" ${timeRange == 'last_month' ? 'selected' : ''}>Tháng trước</option>
                                </select>

                                <select name="paymentMethod" class="form-select form-select-sm" style="width: 120px;" onchange="this.form.submit()">
                                    <option value="" ${empty paymentMethod ? 'selected' : ''}>Mọi quỹ</option>
                                    <option value="CASH" ${paymentMethod == 'CASH' ? 'selected' : ''}>Tiền mặt</option>
                                    <option value="BANK_TRANSFER" ${paymentMethod == 'BANK_TRANSFER' ? 'selected' : ''}>Ngân hàng</option>
                                </select>

                                <select name="type" class="form-select form-select-sm" style="width: 120px;" onchange="this.form.submit()">
                                    <option value="" ${empty type ? 'selected' : ''}>Tất cả loại</option>
                                    <option value="INCOME" ${type == 'INCOME' ? 'selected' : ''}>Phiếu thu (PT)</option>
                                    <option value="EXPENSE" ${type == 'EXPENSE' ? 'selected' : ''}>Phiếu chi (PC)</option>
                                </select>

                                <button type="submit" class="btn btn-sm btn-winered">Lọc</button>
                            </form>
                        </div>

                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0" id="transactionTable">
                                    <thead class="table-light">
                                        <tr>
                                            <th style="padding-left: 20px;">Mã phiếu</th>
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
                                                            <span class="badge badge-${item.paymentType} rounded-pill px-2" style="font-size: 0.75rem;">
                                                                <c:choose>
                                                                    <c:when test="${item.paymentType == 'INCOME'}">Thu</c:when>
                                                                    <c:otherwise>Chi</c:otherwise>
                                                                </c:choose>
                                                            </span>
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
                                                    <td colspan="4" class="text-center py-5 text-muted">
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
                                            <a class="page-link" href="?page=${currentPage - 1}&keyword=${keyword}&type=${type}&paymentMethod=${paymentMethod}&timeRange=${timeRange}">Trước</a>
                                        </li>
                                        <c:forEach var="p" begin="1" end="${totalPage}">
                                            <li class="page-item ${p == currentPage ? 'active' : ''}">
                                                <a class="page-link ${p == currentPage ? 'bg-winered border-winered' : ''}" href="?page=${p}&keyword=${keyword}&type=${type}&paymentMethod=${paymentMethod}&timeRange=${timeRange}">${p}</a>
                                            </li>
                                        </c:forEach>
                                        <li class="page-item ${currentPage == totalPage ? 'disabled' : ''}">
                                            <a class="page-link" href="?page=${currentPage + 1}&keyword=${keyword}&type=${type}&paymentMethod=${paymentMethod}&timeRange=${timeRange}">Sau</a>
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
                        <textarea name="description" class="form-control" rows="3" placeholder="Ví dụ: Thu tiền bán hàng, Thu hồi công nợ..." required></textarea>
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
            <form action="${pageContext.request.contextPath}/cashbook/create-payment" method="post">
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
                        <textarea name="description" class="form-control" rows="3" placeholder="Ví dụ: Thanh toán tiền điện, Nhập hàng, Chi trả lương..." required></textarea>
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
