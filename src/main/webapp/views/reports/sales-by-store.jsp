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
                    <a class="btn-primary" style="font-size:13px;padding:6px 14px;" href="${pageContext.request.contextPath}/reports/sales-by-store-export-excel?keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}">
                        <span class="material-symbols-outlined" style="font-size:16px;">file_download</span> Xuất Excel
                    </a>
                </div>
            </section>

            <c:if test="${not empty reportOverview}">
                <section class="overview-grid">
                    <div class="overview-card">
                        <div class="overview-icon overview-icon-users">
                            <span class="material-symbols-outlined">storefront</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng chi nhánh</p>
                            <h3>${reportOverview.totalBranches}</h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-orders">
                            <span class="material-symbols-outlined">receipt_long</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng đơn hàng</p>
                            <h3>${reportOverview.totalOrders}</h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-revenue">
                            <span class="material-symbols-outlined">payments</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng doanh thu</p>
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
                            <p>Chi nhánh xuất sắc</p>
                            <h3>${empty reportOverview.topBranchName ? '—' : reportOverview.topBranchName}</h3>
                            <small>
                                Doanh thu:
                                <fmt:formatNumber value="${reportOverview.topBranchRevenue}" type="number" groupingUsed="true"/> ₫
                            </small>
                        </div>
                    </div>
                </section>
            </c:if>

            <form class="filter-card" method="get" action="${baseUrl}">
                <input type="hidden" name="page" value="1"/>

                <div class="filter-grid">
                    <div class="form-group filter-search">
                        <label>Tìm kiếm chi nhánh</label>
                        <input name="keyword"
                               value="${keyword}"
                               type="text"
                               placeholder="Tên, mã chi nhánh hoặc địa chỉ..."/>
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
                        <a class="btn-secondary" href="${pageContext.request.contextPath}/reports/sales-by-store-preview?keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}">
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
                            <th>Tên chi nhánh</th>
                            <th>Địa chỉ</th>
                            <th class="text-right">Tổng đơn</th>
                            <th class="text-right">Hoàn tất</th>
                            <th class="text-right">Đã hủy</th>
                            <th class="text-right">Doanh thu</th>
                            <th class="text-right">TB Đơn hàng</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty salesReports}">
                                <tr>
                                    <td colspan="8" class="empty-row">
                                        <div class="empty-state">
                                            <span class="material-symbols-outlined">storefront</span>
                                            <h4>Không tìm thấy dữ liệu doanh số chi nhánh</h4>
                                            <p>Hãy điều chỉnh bộ lọc hoặc khoảng thời gian.</p>
                                        </div>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="row" items="${salesReports}" varStatus="st">
                                    <tr>
                                        <td>${(currentPage - 1) * pageSize + st.index + 1}</td>
                                        <td>
                                            <strong>${row.branchName}</strong>
                                        </td>
                                        <td>${row.address}</td>
                                        <td class="text-right">${row.totalOrders}</td>
                                        <td class="text-right text-success">${row.completedOrders}</td>
                                        <td class="text-right text-danger">${row.cancelledOrders}</td>
                                        <td class="text-right font-weight-bold">
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

                <jsp:include page="/views/common/pagination.jsp">
                    <jsp:param name="baseUrl" value="${baseUrl}"/>
                    <jsp:param name="queryString" value="&keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}"/>
                </jsp:include>
            </section>
        </main>
    </div>
</div>
</body>
</html>
