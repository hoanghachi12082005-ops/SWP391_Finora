<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>${pageTitle} - Finora</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/base.css?v=20260601"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/layout.css?v=20260601"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/user-management.css?v=2"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/report-kpi.css?v=1"/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
</head>
<body class="user-page">
<div class="app-layout">
    <jsp:include page="/views/common/sidebar.jsp"/>
    <div class="main-wrapper">
        <main class="page-content">
            <section class="page-header">
                <div>
                    <h2>${pageTitle}</h2>
                    <p>${pageSubtitle}</p>
                </div>
                <div class="filter-actions">
                    <a class="btn-primary" style="font-size:13px;padding:6px 14px;"
                       href="${baseUrl}/export-excel${not empty pageContext.request.queryString ? '?' : ''}${pageContext.request.queryString}">
                         <span class="material-symbols-outlined" style="font-size:16px;">file_download</span> Xuất Excel
                    </a>
                    <a class="btn-primary" style="font-size:13px;padding:6px 14px;background:#d32f2f;"
                       href="${baseUrl}/export-pdf${not empty pageContext.request.queryString ? '?' : ''}${pageContext.request.queryString}">
                        <span class="material-symbols-outlined" style="font-size:16px;">picture_as_pdf</span> Xuất PDF
                    </a>
                </div>
            </section>

            <jsp:include page="/views/reports/_transaction_filter.jsp"/>
            <jsp:include page="/views/reports/_transaction_kpi.jsp"/>
            <jsp:include page="/views/reports/_transaction_table.jsp"/>

            <jsp:include page="/views/common/pagination.jsp">
                <jsp:param name="baseUrl" value="${baseUrl}"/>
                <jsp:param name="queryString" value="&datePreset=${empty datePreset ? '' : datePreset}&dateFrom=${empty filter.dateFrom ? '' : filter.dateFrom}&dateTo=${empty filter.dateTo ? '' : filter.dateTo}&transactionCode=${empty filter.transactionCode ? '' : filter.transactionCode}&transactionType=${empty filter.transactionType ? '' : filter.transactionType}&paymentMethod=${empty filter.paymentMethod ? '' : filter.paymentMethod}&amountFrom=${empty filter.amountFrom ? '' : filter.amountFrom}&amountTo=${empty filter.amountTo ? '' : filter.amountTo}&branchId=${empty filter.branchId ? '' : filter.branchId}&empId=${empty filter.empId ? '' : filter.empId}&keyword=${empty filter.keyword ? '' : filter.keyword}&sortBy=${empty filter.sortBy ? '' : filter.sortBy}&sortDir=${empty filter.sortDir ? '' : filter.sortDir}"/>
            </jsp:include>
        </main>
    </div>
</div>
</body>
</html>
