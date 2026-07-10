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
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/components.css?v=20260601"/>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/employee-sales-report.css?v=20260601"/>

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
</head>

<body class="report-page">
<div class="app-layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-wrapper">
        <main class="page-content">

            <section class="page-header">
                <div>
                    <h2>${pageTitle}</h2>
                    <p>${pageSubtitle}</p>
                </div>
            </section>

            <c:if test="${not empty reportOverview}">
                <section class="overview-grid">
                    <div class="overview-card">
                        <div class="overview-icon overview-icon-users">
                            <span class="material-symbols-outlined">groups</span>
                        </div>
                        <div class="overview-info">
                            <p>Total Employees</p>
                            <h3>${reportOverview.totalEmployees}</h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-users">
                            <span class="material-symbols-outlined">verified_user</span>
                        </div>
                        <div class="overview-info">
                            <p>Active Employees</p>
                            <h3>${reportOverview.activeEmployees}</h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-orders">
                            <span class="material-symbols-outlined">receipt_long</span>
                        </div>
                        <div class="overview-info">
                            <p>Total Orders</p>
                            <h3>${reportOverview.totalOrders}</h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-revenue">
                            <span class="material-symbols-outlined">payments</span>
                        </div>
                        <div class="overview-info">
                            <p>Total Revenue</p>
                            <h3>
                                <fmt:formatNumber value="${reportOverview.totalRevenue}" type="number" groupingUsed="true"/> ₫
                            </h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-warning">
                            <span class="material-symbols-outlined">emoji_events</span>
                        </div>
                        <div class="overview-info">
                            <p>Top Employee</p>
                            <h3>${empty reportOverview.topEmployeeName ? '—' : reportOverview.topEmployeeName}</h3>
                            <small>
                                Revenue:
                                <fmt:formatNumber value="${reportOverview.topEmployeeRevenue}" type="number" groupingUsed="true"/> ₫
                            </small>
                        </div>
                    </div>
                </section>
            </c:if>

            <form class="filter-card" method="get" action="${baseUrl}">
                <input type="hidden" name="page" value="1"/>

                <div class="filter-grid">
                    <div class="form-group filter-search">
                        <label>Search</label>
                        <input name="keyword"
                               value="${keyword}"
                               type="text"
                               placeholder="Name, email or phone..."/>
                    </div>

                    <c:if test="${not empty branches}">
                        <div class="form-group">
                            <label>Branch</label>
                            <select name="branchId">
                                <option value="">All Branches</option>
                                <c:forEach var="branch" items="${branches}">
                                    <option value="${branch.branchID}"
                                        ${branchFilter == branch.branchID ? 'selected' : ''}>
                                        ${branch.name}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:if>

                    <div class="form-group">
                        <label>From Date</label>
                        <input type="date" name="dateFrom" value="${dateFrom}"/>
                    </div>

                    <div class="form-group">
                        <label>To Date</label>
                        <input type="date" name="dateTo" value="${dateTo}"/>
                    </div>

                    <input type="hidden" name="sizeValue" value="${sizeValue}"/>

                    <div class="filter-actions">
                        <button class="btn-primary" type="submit">Apply</button>
                        <a class="btn-secondary" href="${pageContext.request.contextPath}/reports/employee-sales-preview?keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}">
                            <span class="material-symbols-outlined">visibility</span> Preview
                        </a>
                        <a class="btn-secondary" href="${baseUrl}">Reset</a>
                    </div>
                </div>
            </form>

            <section class="table-card">
                <div class="table-scroll">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>#</th>
                            <th>Employee</th>
                            <th>Branch</th>
                            <th>Role</th>
                            <th class="text-right">Orders</th>
                            <th class="text-right">Revenue</th>
                            <th class="text-right">Avg. Order</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty salesReports}">
                                <tr>
                                        <td colspan="7" class="empty-row">
                                            <div class="empty-state">
                                                <span class="material-symbols-outlined">bar_chart</span>
                                                <h4>No sales data found</h4>
                                                <p>Try adjusting your filters or date range.</p>
                                            </div>
                                        </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="row" items="${salesReports}" varStatus="st">
                                    <tr>
                                        <td>${(currentPage - 1) * pageSize + st.index + 1}</td>
                                        <td>
                                            <div class="user-cell">
                                                <div class="avatar-text">
                                                    <c:choose>
                                                        <c:when test="${not empty row.fullName}">
                                                            ${fn:substring(row.fullName, 0, 1)}
                                                        </c:when>
                                                        <c:otherwise>E</c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div>
                                                    <strong>${row.fullName}</strong>
                                                </div>
                                            </div>
                                        </td>
                                        <td>${empty row.branchName ? '—' : row.branchName}</td>
                                        <td>
                                            <span class="role-badge">${empty row.roleName ? '—' : row.roleName}</span>
                                        </td>
                                        <td class="text-right">${row.totalOrders}</td>
                                        <td class="text-right">
                                            <fmt:formatNumber value="${row.totalRevenue}" type="number" groupingUsed="true"/> ₫
                                        </td>
                                        <td class="text-right">
                                            <fmt:formatNumber value="${row.averageOrderValue}" type="number" groupingUsed="true" maxFractionDigits="0"/> ₫
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>

                <div class="table-footer">

                    <form method="get" action="${baseUrl}" class="pagination-info">
                        <input type="hidden" name="keyword" value="${keyword}">
                        <input type="hidden" name="branchId" value="${branchFilter == -1 ? '' : branchFilter}">
                        <input type="hidden" name="dateFrom" value="${dateFrom}">
                        <input type="hidden" name="dateTo" value="${dateTo}">
                        <select name="sizeValue" onchange="this.form.submit()">
                            <option value="30" ${sizeValue == 30 or (sizeValue == 50 and option50 == option30) or (sizeValue == 70 and option70 == option30) ? "selected" : ""}>
                                ${option30}
                            </option>
                            <c:if test="${option50 != option30}">
                                <option value="50" ${sizeValue == 50 or (sizeValue == 70 and option70 == option50) ? "selected" : ""}>
                                    ${option50}
                                </option>
                            </c:if>
                            <c:if test="${option70 != option50 and option70 != option30 and option70 != option100}">
                                <option value="70" ${sizeValue == 70 ? "selected" : ""}>
                                    ${option70}
                                </option>
                            </c:if>
                            <option value="100" ${sizeValue == 100 or (sizeValue == 70 and option70 == option100) or (sizeValue == 50 and option50 == option100) ? "selected" : ""}>
                                Tất cả
                            </option>
                        </select>
                        <span class="pagination-summary">
                            ${startRecord} - ${endRecord} trong số ${totalRecords}
                        </span>
                    </form>

                    <c:if test="${totalPages > 1}">
                        <div class="pagination">

                            <c:if test="${currentPage > 1}">
                                <a href="${baseUrl}?page=1&sizeValue=${sizeValue}&keyword=${keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}">
                                    <<
                                </a>
                            </c:if>

                            <c:choose>
                                <c:when test="${totalPages <= 5}">
                                    <c:forEach begin="1" end="${totalPages}" var="i">
                                        <a href="${baseUrl}?page=${i}&sizeValue=${sizeValue}&keyword=${keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}"
                                           class="${i == currentPage ? 'active-page' : ''}">
                                            ${i}
                                        </a>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <a href="${baseUrl}?page=1&sizeValue=${sizeValue}&keyword=${keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}"
                                       class="${currentPage == 1 ? 'active-page' : ''}">1</a>
                                    <c:if test="${currentPage > 3}">
                                        <span class="dots">...</span>
                                    </c:if>
                                    <c:forEach begin="${currentPage - 1 < 2 ? 2 : currentPage - 1}"
                                               end="${currentPage + 1 > totalPages - 1 ? totalPages - 1 : currentPage + 1}" var="i">
                                        <a href="${baseUrl}?page=${i}&sizeValue=${sizeValue}&keyword=${keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}"
                                           class="${i == currentPage ? 'active-page' : ''}">
                                            ${i}
                                        </a>
                                    </c:forEach>
                                    <c:if test="${currentPage < totalPages - 2}">
                                        <span class="dots">...</span>
                                    </c:if>
                                    <a href="${baseUrl}?page=${totalPages}&sizeValue=${sizeValue}&keyword=${keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}"
                                       class="${currentPage == totalPages ? 'active-page' : ''}">${totalPages}</a>
                                </c:otherwise>
                            </c:choose>

                            <c:if test="${currentPage < totalPages}">
                                <a href="${baseUrl}?page=${totalPages}&sizeValue=${sizeValue}&keyword=${keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${dateFrom}&dateTo=${dateTo}">
                                    >>
                                </a>
                            </c:if>

                        </div>
                    </c:if>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
