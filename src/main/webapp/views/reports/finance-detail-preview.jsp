<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>${pageTitle} - Finora</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/base.css?v=20260528"/>
    <style>
        :root {
            --primary: #1a1a2e;
            --primary-light: #16213e;
            --accent: #0f3460;
            --text: #1a1a2e;
            --text-light: #666;
            --border: #e0e0e0;
            --bg: #fff;
            --header-bg: #1a1a2e;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: 'Inter', -apple-system, sans-serif;
            color: var(--text);
            background: #f5f5f5;
            line-height: 1.5;
        }
        .toolbar {
            position: sticky;
            top: 0;
            z-index: 100;
            background: #fff;
            border-bottom: 1px solid var(--border);
            padding: 12px 32px;
            display: flex;
            align-items: center;
            gap: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,.06);
        }
        .toolbar .title { font-size: 16px; font-weight: 600; flex: 1; }
        .toolbar button, .toolbar a {
            padding: 8px 20px;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            border: none;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .btn-print { background: var(--primary); color: #fff; }
        .btn-print:hover { opacity: .9; }
        .btn-excel { background: #217346; color: #fff; }
        .btn-excel:hover { opacity: .9; }
        .btn-close { background: #f0f0f0; color: var(--text); }
        .btn-close:hover { background: #e0e0e0; }
        .btn-disabled { opacity: .4; pointer-events: none; }

        .report-container {
            max-width: 1000px;
            margin: 32px auto;
            background: #fff;
            padding: 48px 56px;
            box-shadow: 0 1px 4px rgba(0,0,0,.08);
            min-height: 1123px;
        }
        .report-header {
            text-align: center;
            margin-bottom: 24px;
        }
        .report-header h1 {
            font-size: 24px;
            font-weight: 700;
            color: var(--primary);
            margin-bottom: 4px;
        }
        .report-header .company {
            font-size: 14px;
            font-weight: 600;
            color: var(--primary-light);
        }
        .report-header .meta {
            font-size: 12px;
            color: var(--text-light);
            margin-top: 2px;
        }
        .report-divider {
            border: none;
            border-top: 2px solid var(--primary);
            margin: 16px 0;
        }

        .filter-info {
            font-size: 12px;
            color: var(--text-light);
            margin-bottom: 20px;
            line-height: 1.6;
        }
        .filter-info strong { color: var(--text); }

        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13px;
            margin-bottom: 24px;
        }
        thead th {
            background: var(--header-bg);
            color: #fff;
            padding: 10px 8px;
            text-align: center;
            font-weight: 600;
            font-size: 12px;
        }
        thead th:first-child { text-align: left; padding-left: 12px; }
        tbody td {
            padding: 8px;
            border-bottom: 1px solid var(--border);
            text-align: center;
        }
        tbody td:first-child { text-align: left; padding-left: 12px; }
        .text-right { text-align: right !important; padding-right: 12px !important; }

        .summary-section {
            margin-top: 24px;
            border-top: 2px solid var(--primary);
            padding-top: 16px;
        }
        .summary-section h3 {
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 8px;
            color: var(--primary);
        }
        .summary-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 4px 32px;
            font-size: 13px;
        }
        .summary-grid .label { color: var(--text-light); }
        .summary-grid .value { text-align: right; font-weight: 500; }

        .report-footer {
            margin-top: 40px;
            padding-top: 16px;
            border-top: 1px solid var(--border);
            font-size: 11px;
            color: var(--text-light);
            text-align: center;
        }

        .empty-state {
            text-align: center;
            padding: 60px 24px;
            color: var(--text-light);
        }
        .empty-state .material-symbols-outlined {
            font-size: 56px;
            opacity: .4;
            margin-bottom: 8px;
            color: var(--primary);
        }
        .empty-state h4 { margin: 8px 0 4px; font-size: 18px; color: var(--text-dark, #111827); }
        .empty-state p { margin: 0; font-size: 14px; }

        @media print {
            body { background: #fff; }
            .toolbar { display: none !important; }
            .report-container {
                margin: 0;
                padding: 32px 40px;
                box-shadow: none;
                max-width: 100%;
            }
            @page { margin: 20mm 15mm; }
        }
    </style>
</head>
<body>

<div class="toolbar" id="toolbar">
    <span class="title">${pageTitle}</span>
    <c:choose>
        <c:when test="${empty allFinanceReports}">
            <button class="btn-excel btn-disabled" disabled>Xuất Excel</button>
            <button class="btn-print btn-disabled" disabled>In</button>
        </c:when>
        <c:otherwise>
            <a class="btn-excel" href="${pageContext.request.contextPath}/reports/finance-detail-export-excel?keyword=${param.keyword}&branchId=${param.branchId}&typeFilter=${param.typeFilter}&dateFrom=${param.dateFrom}&dateTo=${param.dateTo}">
                <span class="material-symbols-outlined" style="font-size:18px">table_chart</span> Xuất Excel
            </a>
            <button class="btn-print" onclick="window.print()">
                <span class="material-symbols-outlined" style="font-size:18px">print</span> In báo cáo
            </button>
        </c:otherwise>
    </c:choose>
    <a class="btn-close" href="${pageContext.request.contextPath}/reports/finance-detail?keyword=${param.keyword}&branchId=${param.branchId}&typeFilter=${param.typeFilter}&dateFrom=${param.dateFrom}&dateTo=${param.dateTo}">
        <span class="material-symbols-outlined" style="font-size:18px">close</span> Đóng
    </a>
</div>

<div class="report-container">
    <div class="report-header">
        <p class="company">FINORA RETAIL SYSTEM</p>
        <h1>BÁO CÁO DOANH THU CHI TIẾT (TÀI CHÍNH)</h1>
        <p class="meta">Ngày tạo: <span id="generation-time"></span></p>
    </div>

    <hr class="report-divider"/>

    <div class="filter-info">
        <div><strong>Từ khóa:</strong> ${empty param.keyword ? '—' : param.keyword}</div>
        <div><strong>Chi nhánh:</strong> ${empty reportBranchName ? 'Tất cả chi nhánh' : reportBranchName}</div>
        <div><strong>Loại giao dịch:</strong> 
            <c:choose>
                <c:when test="${param.typeFilter == 'INCOME'}">Thu (Inflow)</c:when>
                <c:when test="${param.typeFilter == 'EXPENSE'}">Chi (Outflow)</c:when>
                <c:otherwise>Tất cả</c:otherwise>
            </c:choose>
        </div>
        <div><strong>Thời gian:</strong> 
            ${empty param.dateFrom ? 'Từ trước tới nay' : param.dateFrom} 
            — 
            ${empty param.dateTo ? 'Hiện tại' : param.dateTo}
        </div>
    </div>

    <c:choose>
        <c:when test="${empty allFinanceReports}">
            <div class="empty-state">
                <span class="material-symbols-outlined">account_balance_wallet</span>
                <h4>Không tìm thấy dữ liệu phát sinh tài chính</h4>
                <p>Hãy điều chỉnh bộ lọc hoặc khoảng thời gian.</p>
            </div>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                <tr>
                    <th style="width: 5%">#</th>
                    <th style="width: 12%">Mã GD</th>
                    <th style="width: 8%">Loại</th>
                    <th style="width: 12%" class="text-right">Số tiền</th>
                    <th style="width: 10%">Phương thức</th>
                    <th style="width: 15%">Thời gian</th>
                    <th style="width: 15%">Chi nhánh</th>
                    <th style="width: 13%">Người thực hiện</th>
                    <th style="width: 10%">Mô tả</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="row" items="${allFinanceReports}" varStatus="st">
                    <tr>
                        <td>${st.index + 1}</td>
                        <td><strong>${row.name}</strong></td>
                        <td>${row.paymentType == 'INCOME' ? 'Thu' : 'Chi'}</td>
                        <td class="text-right">
                            <fmt:formatNumber value="${row.amount}" type="number" groupingUsed="true"/> ₫
                        </td>
                        <td>${row.method}</td>
                        <td>
                            <fmt:formatDate value="${row.paymentDate}" pattern="dd/MM/yyyy HH:mm"/>
                        </td>
                        <td>${empty row.branchName ? '—' : row.branchName}</td>
                        <td>${empty row.creatorName ? '—' : row.creatorName}</td>
                        <td style="text-align: left; padding-left: 8px;">${row.description}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <c:if test="${not empty reportOverview}">
                <div class="summary-section">
                    <h3>TỔNG KẾT BÁO CÁO</h3>
                    <div class="summary-grid">
                        <div class="label">Tổng số giao dịch:</div>
                        <div class="value">${reportOverview.totalTransactions}</div>

                        <div class="label">Tổng Thu:</div>
                        <div class="value">
                            <fmt:formatNumber value="${reportOverview.totalIncome}" type="number" groupingUsed="true"/> ₫
                        </div>

                        <div class="label">Tổng Chi:</div>
                        <div class="value">
                            <fmt:formatNumber value="${reportOverview.totalExpense}" type="number" groupingUsed="true"/> ₫
                        </div>

                        <div class="label" style="font-weight: 700; color: var(--primary);">Lợi nhuận ròng:</div>
                        <div class="value" style="font-weight: 700; color: ${reportOverview.netProfit >= 0 ? '#16a34a' : '#dc2626'}">
                            <fmt:formatNumber value="${reportOverview.netProfit}" type="number" groupingUsed="true"/> ₫
                        </div>
                    </div>
                </div>
            </c:if>
        </c:otherwise>
    </c:choose>

    <div class="report-footer">
        <p>Báo cáo này được kết xuất tự động từ hệ thống quản lý Finora Retail.</p>
    </div>
</div>

<script>
    document.getElementById('generation-time').innerText = new Date().toLocaleString('vi-VN');
</script>
</body>
</html>
