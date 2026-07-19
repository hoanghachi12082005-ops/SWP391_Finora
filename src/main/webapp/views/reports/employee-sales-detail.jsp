<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>${pageTitle} - Finora</title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/base.css?v=20260601"/>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/layout.css?v=20260601"/>
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
                    <p>Chi tiết doanh số và đơn hàng của nhân viên</p>
                </div>
                <div class="filter-actions">
                    <a class="btn-secondary" href="${pageContext.request.contextPath}/reports/employee-sales${empty param.dateFrom ? '' : '?dateFrom='.concat(param.dateFrom).concat('&dateTo=').concat(param.dateTo)}">
                        <span class="material-symbols-outlined">arrow_back</span> Quay lại
                    </a>
                    <a class="btn-primary" href="${pageContext.request.contextPath}/reports/employee-sales-detail-export?empId=${employeeInfo.employeeId}&dateFrom=${empty param.dateFrom ? '' : param.dateFrom}&dateTo=${empty param.dateTo ? '' : param.dateTo}">
                        <span class="material-symbols-outlined">file_download</span> Xuất Excel
                    </a>
                </div>
            </section>

            <c:if test="${not empty employeeInfo}">
                <section class="overview-grid">
                    <div class="overview-card">
                        <div class="overview-icon overview-icon-users">
                            <span class="material-symbols-outlined">person</span>
                        </div>
                        <div class="overview-info">
                            <p>Nhân viên</p>
                            <h3>${employeeInfo.fullName}</h3>
                            <small>ID: ${employeeInfo.employeeId} | ${empty employeeInfo.branchName ? '—' : employeeInfo.branchName} | ${empty employeeInfo.roleName ? '—' : employeeInfo.roleName}</small>
                        </div>
                    </div>
                    <div class="overview-card">
                        <div class="overview-icon overview-icon-revenue">
                            <span class="material-symbols-outlined">payments</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng doanh thu</p>
                            <h3><fmt:formatNumber value="${employeeInfo.totalRevenue}" type="number" groupingUsed="true"/> ₫</h3>
                        </div>
                    </div>
                    <div class="overview-card">
                        <div class="overview-icon overview-icon-orders">
                            <span class="material-symbols-outlined">receipt_long</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng đơn hàng</p>
                            <h3>${employeeInfo.totalOrders}</h3>
                        </div>
                    </div>
                    <div class="overview-card">
                        <div class="overview-icon overview-icon-warning">
                            <span class="material-symbols-outlined">trending_up</span>
                        </div>
                        <div class="overview-info">
                            <p>Giá trị TB đơn</p>
                            <h3><fmt:formatNumber value="${employeeInfo.averageOrderValue}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</h3>
                        </div>
                    </div>
                </section>
            </c:if>

            <div class="card" style="padding: 20px; margin-bottom: 20px;">
                <h3 style="margin-bottom: 12px; font-size: 16px;">Doanh thu theo ngày</h3>
                <div class="table-scroll">
                    <table class="data-table" style="font-size: 13px;">
                        <thead>
                            <tr>
                                <th>Ngày</th>
                                <th class="text-right">Số đơn</th>
                                <th class="text-right">Doanh thu</th>
                                <th class="text-right">TB đơn</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty dailyRevenue}">
                                    <tr><td colspan="4" class="empty-row"><div class="empty-state"><span class="material-symbols-outlined">bar_chart</span><h4>Không có dữ liệu</h4></div></td></tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="dr" items="${dailyRevenue}">
                                        <tr>
                                            <td>${dr.date}</td>
                                            <td class="text-right">${dr.totalOrders}</td>
                                            <td class="text-right"><fmt:formatNumber value="${dr.totalRevenue}" type="number" groupingUsed="true"/> ₫</td>
                                            <td class="text-right"><fmt:formatNumber value="${dr.avgOrderValue}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <section class="table-card">
                <div class="table-header">
                    <h3>Danh sách đơn hàng</h3>
                </div>
                <form class="filter-card" method="get" action="${baseUrl}">
                    <input type="hidden" name="empId" value="${employeeInfo.employeeId}"/>
                    <input type="hidden" name="page" value="1"/>
                    <input type="hidden" name="dateFrom" value="${dateFrom}"/>
                    <input type="hidden" name="dateTo" value="${dateTo}"/>
                    <div class="filter-grid">
                        <div class="form-group filter-search">
                            <label>Tìm kiếm đơn hàng</label>
                            <input name="orderKeyword" value="${orderKeyword}" type="text" placeholder="Mã đơn hoặc tên khách hàng..."/>
                        </div>
                        <div class="filter-actions" style="align-self: flex-end;">
                            <button class="btn-primary" type="submit">Tìm</button>
                            <a class="btn-secondary" href="${baseUrl}?empId=${employeeInfo.employeeId}">Đặt lại</a>
                        </div>
                    </div>
                </form>
                <div class="table-scroll">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Mã đơn</th>
                                <th>Ngày</th>
                                <th>Khách hàng</th>
                                <th>Phương thức</th>
                                <th class="text-right">Tạm tính</th>
                                <th class="text-right">Giảm giá</th>
                                <th class="text-right">Tổng</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty orders}">
                                    <tr><td colspan="7" class="empty-row"><div class="empty-state"><span class="material-symbols-outlined">receipt_long</span><h4>Không tìm thấy đơn hàng</h4></div></td></tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="o" items="${orders}">
                                        <tr>
                                            <td>${empty o.orderCode ? '—' : o.orderCode}</td>
                                            <td>${empty o.createdAt ? '—' : o.createdAt}</td>
                                            <td>${empty o.customerName ? '—' : o.customerName}</td>
                                            <td>${empty o.paymentMethod ? '—' : o.paymentMethod}</td>
                                            <td class="text-right"><fmt:formatNumber value="${o.subtotal}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</td>
                                            <td class="text-right"><fmt:formatNumber value="${o.discountAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</td>
                                            <td class="text-right"><fmt:formatNumber value="${o.totalAmount}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫</td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
                <jsp:include page="/views/common/pagination.jsp">
                    <jsp:param name="baseUrl" value="${baseUrl}"/>
                    <jsp:param name="queryString" value="&empId=${employeeInfo.employeeId}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}&orderKeyword=${empty orderKeyword ? '' : orderKeyword}"/>
                </jsp:include>
            </section>
        </main>
    </div>
</div>
</body>
</html>