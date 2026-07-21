<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>${pageTitle} - Finora</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/base.css?v=20260601"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/layout.css?v=20260601"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/user-management.css?v=2"/>
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
                    <a class="btn-primary" style="font-size:13px;padding:6px 14px;" href="${baseUrl}/export-excel${not empty pageContext.request.queryString ? '?' : ''}${pageContext.request.queryString}">
                        <span class="material-symbols-outlined" style="font-size:16px;">file_download</span> Xuat Excel
                    </a>
                </div>
            </section>

            <jsp:include page="/views/reports/_filter.jsp"/>

            <jsp:include page="/views/reports/_table.jsp"/>

            <jsp:include page="/views/common/pagination.jsp">
                <jsp:param name="baseUrl" value="${baseUrl}"/>
                <jsp:param name="queryString" value="&datePreset=${empty datePreset ? '' : datePreset}&dateFrom=${empty filter.dateFrom ? '' : filter.dateFrom}&dateTo=${empty filter.dateTo ? '' : filter.dateTo}&empId=${empty filter.empId ? '' : filter.empId}&branchId=${empty filter.branchId ? '' : filter.branchId}&orderId=${empty filter.orderId ? '' : filter.orderId}&customerId=${empty filter.customerId ? '' : filter.customerId}&orderStatus=${empty filter.orderStatus ? '' : filter.orderStatus}&paymentMethod=${empty filter.paymentMethod ? '' : filter.paymentMethod}&keyword=${empty filter.keyword ? '' : filter.keyword}&sortBy=${empty filter.sortBy ? '' : filter.sortBy}&sortDir=${empty filter.sortDir ? '' : filter.sortDir}"/>
            </jsp:include>

            <jsp:include page="/views/reports/_order_detail_modal.jsp"/>
        </main>
    </div>
</div>
</body>
</html>
