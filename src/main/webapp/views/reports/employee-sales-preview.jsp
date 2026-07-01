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
        .btn-pdf { background: #c0392b; color: #fff; }
        .btn-pdf:hover { opacity: .9; }
        .btn-close { background: #f0f0f0; color: var(--text); }
        .btn-close:hover { background: #e0e0e0; }
        .btn-disabled { opacity: .4; pointer-events: none; }

        .report-container {
            max-width: 900px;
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
            color: var(--primary, #95002a);
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
        <c:when test="${empty allSalesReports}">
            <button class="btn-pdf btn-disabled" disabled>Export PDF</button>
            <button class="btn-print btn-disabled" disabled>Print</button>
        </c:when>
        <c:otherwise>
            <a class="btn-pdf" href="${pageContext.request.contextPath}/reports/employee-sales-export?keyword=${param.keyword}&branchId=${param.branchId}&dateFrom=${param.dateFrom}&dateTo=${param.dateTo}" target="_blank">
                <span class="material-symbols-outlined" style="font-size:18px">picture_as_pdf</span> Export PDF
            </a>
            <button class="btn-print" onclick="window.print()">
                <span class="material-symbols-outlined" style="font-size:18px">print</span> Print
            </button>
        </c:otherwise>
    </c:choose>
    <a class="btn-close" href="${pageContext.request.contextPath}/reports/employee-sales?keyword=${param.keyword}&branchId=${param.branchId}&dateFrom=${param.dateFrom}&dateTo=${param.dateTo}">
        Close Preview
    </a>
</div>

<div class="report-container">

    <c:if test="${empty allSalesReports}">
        <div class="empty-state">
            <span class="material-symbols-outlined">description</span>
            <h4>No report data available</h4>
            <p>Try adjusting your filters or date range.</p>
        </div>
    </c:if>

    <c:if test="${not empty allSalesReports}">
        <div class="report-header">
            <h1>Employee Sales Report</h1>
            <div class="company">Finora Retail</div>
            <div class="meta">
                Generated: <fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy HH:mm"/>
                | By: ${sessionScope.currentUser.fullName}
            </div>
        </div>
        <hr class="report-divider"/>

        <div class="filter-info">
            <strong>Date Range:</strong>
            ${empty param.dateFrom ? 'Earliest' : param.dateFrom} ΓÇö ${empty param.dateTo ? 'Latest' : param.dateTo}
            &nbsp;|&nbsp; <strong>Branch:</strong> ${empty reportBranchName ? 'All Branches' : reportBranchName}
            &nbsp;|&nbsp; <strong>Period:</strong> ${empty param.dateFrom ? 'ΓÇö' : param.dateFrom} to ${empty param.dateTo ? 'ΓÇö' : param.dateTo}
        </div>

        <table>
            <thead>
                <tr>
                    <th>Employee</th>
                    <th>Branch</th>
                    <th>Role</th>
                    <th>Orders</th>
                    <th class="text-right">Revenue</th>
                    <th class="text-right">Avg. Order</th>
                    <th>Completed</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="row" items="${allSalesReports}">
                    <tr>
                        <td>${row.fullName}</td>
                        <td>${empty row.branchName ? 'ΓÇö' : row.branchName}</td>
                        <td>${empty row.roleName ? 'ΓÇö' : row.roleName}</td>
                        <td>${row.totalOrders}</td>
                        <td class="text-right"><fmt:formatNumber value="${row.totalRevenue}" type="number" groupingUsed="true"/> Γé½</td>
                        <td class="text-right"><fmt:formatNumber value="${row.averageOrderValue}" type="number" groupingUsed="true"/> Γé½</td>
                        <td>${row.completedOrders}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

        <div class="summary-section">
            <h3>Summary</h3>
            <div class="summary-grid">
                <span class="label">Total Employees</span>
                <span class="value">${reportOverview.totalEmployees}</span>
                <span class="label">Total Orders</span>
                <span class="value">${reportOverview.totalOrders}</span>
                <span class="label">Total Revenue</span>
                <span class="value"><fmt:formatNumber value="${reportOverview.totalRevenue}" type="number" groupingUsed="true"/> Γé½</span>
                <span class="label">Avg Revenue / Employee</span>
                <span class="value"><fmt:formatNumber value="${reportOverview.avgRevenuePerEmployee}" type="number" groupingUsed="true"/> Γé½</span>
                <c:if test="${not empty reportOverview.topEmployeeName}">
                    <span class="label">Top Employee</span>
                    <span class="value">${reportOverview.topEmployeeName}</span>
                    <span class="label">Highest Revenue</span>
                    <span class="value"><fmt:formatNumber value="${reportOverview.topEmployeeRevenue}" type="number" groupingUsed="true"/> Γé½</span>
                </c:if>
                <c:if test="${not empty reportOverview.lowestEmployeeName}">
                    <span class="label">Lowest Employee</span>
                    <span class="value">${reportOverview.lowestEmployeeName}</span>
                    <span class="label">Lowest Revenue</span>
                    <span class="value"><fmt:formatNumber value="${reportOverview.lowestEmployeeRevenue}" type="number" groupingUsed="true"/> Γé½</span>
                </c:if>
            </div>
        </div>
    </c:if>

    <div class="report-footer">
        Generated by Finora Retail Management System
    </div>
</div>

</body>
</html>
