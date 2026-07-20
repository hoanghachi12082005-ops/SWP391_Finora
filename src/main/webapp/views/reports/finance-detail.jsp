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
                    <p>${pageSubtitle}</p>
                </div>
                <div class="filter-actions">
                    <a class="btn-primary" style="font-size:13px;padding:6px 14px;" href="${pageContext.request.contextPath}/reports/finance-detail-export-excel?keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&typeFilter=${empty typeFilter ? '' : typeFilter}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}">
                        <span class="material-symbols-outlined" style="font-size:16px;">file_download</span> Xuất Excel
                    </a>
                </div>
            </section>

            <c:if test="${not empty reportOverview}">
                <section class="overview-grid">
                    <div class="overview-card">
                        <div class="overview-icon overview-icon-orders">
                            <span class="material-symbols-outlined">analytics</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng giao dịch</p>
                            <h3>${reportOverview.totalTransactions}</h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-revenue">
                            <span class="material-symbols-outlined">trending_up</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng Thu (Inflow)</p>
                            <h3>
                                <fmt:formatNumber value="${reportOverview.totalIncome}" type="number" groupingUsed="true"/> ₫
                            </h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-warning" style="background-color: #fef2f2; color: #ef4444;">
                            <span class="material-symbols-outlined" style="color: #ef4444;">trending_down</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng Chi (Outflow)</p>
                            <h3>
                                <fmt:formatNumber value="${reportOverview.totalExpense}" type="number" groupingUsed="true"/> ₫
                            </h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-users" style="background-color: #f0fdf4; color: #22c55e;">
                            <span class="material-symbols-outlined" style="color: #22c55e;">account_balance_wallet</span>
                        </div>
                        <div class="overview-info">
                            <p>Lợi nhuận ròng</p>
                            <h3 style="color: ${reportOverview.netProfit >= 0 ? '#16a34a' : '#dc2626'};">
                                <fmt:formatNumber value="${reportOverview.netProfit}" type="number" groupingUsed="true"/> ₫
                            </h3>
                        </div>
                    </div>
                </section>
            </c:if>

            <form class="filter-card" method="get" action="${baseUrl}">
                <input type="hidden" name="page" value="1"/>

                <div class="filter-grid">
                    <div class="form-group filter-search">
                        <label>Tìm kiếm giao dịch</label>
                        <input name="keyword"
                               value="${keyword}"
                               type="text"
                               placeholder="Mã giao dịch hoặc mô tả..."/>
                    </div>

                    <c:if test="${not empty branches}">
                        <div class="form-group">
                            <label>Chi nhánh</label>
                            <select name="branchId">
                                <option value="">Tất cả chi nhánh</option>
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
                        <label>Loại giao dịch</label>
                        <select name="typeFilter">
                            <option value="">Tất cả</option>
                            <option value="INCOME" ${typeFilter == 'INCOME' ? 'selected' : ''}>Thu (Inflow)</option>
                            <option value="EXPENSE" ${typeFilter == 'EXPENSE' ? 'selected' : ''}>Chi (Outflow)</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Từ ngày</label>
                        <input type="date" name="dateFrom" value="${dateFrom}"/>
                    </div>

                    <div class="form-group">
                        <label>Đến ngày</label>
                        <input type="date" name="dateTo" value="${dateTo}"/>
                    </div>

                    <input type="hidden" name="sizeValue" value="${sizeValue}"/>

                    <div class="filter-actions">
                        <button class="btn-primary" type="submit">Áp dụng</button>
                        <a class="btn-secondary" href="${pageContext.request.contextPath}/reports/finance-detail-preview?keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&typeFilter=${empty typeFilter ? '' : typeFilter}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}">
                            <span class="material-symbols-outlined">visibility</span> Xem trước
                        </a>
                        <a class="btn-secondary" href="${baseUrl}">Đặt lại</a>
                    </div>
                </div>
            </form>

            <section class="table-card">
                <div class="table-scroll">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>#</th>
                            <th>Mã giao dịch</th>
                            <th>Loại</th>
                            <th class="text-right">Số tiền</th>
                            <th>Phương thức</th>
                            <th>Thời gian</th>
                            <th>Chi nhánh</th>
                            <th>Người thực hiện</th>
                            <th>Mô tả</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty financeReports}">
                                <tr>
                                    <td colspan="9" class="empty-row">
                                        <div class="empty-state">
                                            <span class="material-symbols-outlined">account_balance_wallet</span>
                                            <h4>Không tìm thấy dữ liệu phát sinh tài chính</h4>
                                            <p>Hãy điều chỉnh bộ lọc hoặc khoảng thời gian.</p>
                                        </div>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="row" items="${financeReports}" varStatus="st">
                                    <tr>
                                        <td>${(currentPage - 1) * pageSize + st.index + 1}</td>
                                        <td>
                                            <strong>${row.name}</strong>
                                        </td>
                                        <td>
                                            <span class="badge ${row.paymentType == 'INCOME' ? 'bg-success' : 'bg-danger'}" 
                                                  style="padding: 4px 8px; border-radius: 4px; color: #fff; font-size: 11px; font-weight: 600; background-color: ${row.paymentType == 'INCOME' ? '#10b981' : '#ef4444'};">
                                                ${row.paymentType == 'INCOME' ? 'Thu' : 'Chi'}
                                            </span>
                                        </td>
                                        <td class="text-right font-weight-bold" style="color: ${row.paymentType == 'INCOME' ? '#059669' : '#dc2626'}">
                                            <fmt:formatNumber value="${row.amount}" type="number" groupingUsed="true"/> ₫
                                        </td>
                                        <td>
                                            <span style="font-weight: 500; font-size: 12px; background-color: #f1f5f9; padding: 2px 6px; border-radius: 4px;">
                                                ${row.method}
                                            </span>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${row.paymentDate}" pattern="dd/MM/yyyy HH:mm"/>
                                        </td>
                                        <td>${empty row.branchName ? '—' : row.branchName}</td>
                                        <td>${empty row.creatorName ? '—' : row.creatorName}</td>
                                        <td style="max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                            ${row.description}
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>

                <jsp:include page="/views/common/pagination.jsp">
                    <jsp:param name="baseUrl" value="${baseUrl}"/>
                    <jsp:param name="queryString" value="&keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&typeFilter=${empty typeFilter ? '' : typeFilter}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}"/>
                </jsp:include>
            </section>
        </main>
    </div>
</div>
</body>
</html>
