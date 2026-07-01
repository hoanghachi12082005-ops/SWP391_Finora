<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>${pageTitle} - Finora</title>

    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/base.css?v=20260528"/>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/layout.css?v=20260528"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/employee-sales-report.css?v=1">

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
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
                            <p>Tổng nhân viên</p>
                            <h3>${reportOverview.totalEmployees}</h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-users">
                            <span class="material-symbols-outlined">verified_user</span>
                        </div>
                        <div class="overview-info">
                            <p>Nhân viên đang hoạt động</p>
                            <h3>${reportOverview.activeEmployees}</h3>
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
                            <p>Nhân viên xuất sắc</p>
                            <h3>${empty reportOverview.topEmployeeName ? '—' : reportOverview.topEmployeeName}</h3>
                            <small>
                                Doanh thu:
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
                        <label>Tìm kiếm</label>
                        <input name="keyword"
                               value="${keyword}"
                               type="text"
                               placeholder="Tên, email hoặc số điện thoại..."/>
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

                    <div class="form-group">
                        <label>Kích thước trang</label>
                        <select name="pageSize">
                            <option value="5" ${pageSizeOption == '5' ? 'selected' : ''}>5 bản ghi/trang</option>
                            <option value="10" ${empty pageSizeOption || pageSizeOption == '10' ? 'selected' : ''}>10 bản ghi/trang</option>
                            <option value="20" ${pageSizeOption == '20' ? 'selected' : ''}>20 bản ghi/trang</option>
                            <option value="30p" ${pageSizeOption == '30p' ? 'selected' : ''}>30% bản ghi/trang</option>
                            <option value="50p" ${pageSizeOption == '50p' ? 'selected' : ''}>50% bản ghi/trang</option>
                        </select>
                    </div>

                    <div class="filter-actions">
                        <button class="btn-primary" type="submit">Áp dụng</button>
                        <a class="btn-primary" href="${pageContext.request.contextPath}/reports/employee-sales-preview?keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}"
                           style="background:#0f3460;text-decoration:none;display:inline-flex;align-items:center;gap:6px">
                            <span class="material-symbols-outlined" style="font-size:18px">visibility</span> Xem trước
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
                            <th>Nhân viên</th>
                            <th>Chi nhánh</th>
                            <th>Vai trò</th>
                            <th class="text-right">Đơn hàng</th>
                            <th class="text-right">Doanh thu</th>
                            <th class="text-right">TB Đơn hàng</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty salesReports}">
                                <tr>
                                        <td colspan="7" class="empty-row">
                                            <div class="empty-state">
                                                <span class="material-symbols-outlined">bar_chart</span>
                                                <h4>Không tìm thấy dữ liệu bán hàng</h4>
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
                                            <fmt:formatNumber value="${row.averageOrderValue}" type="number" groupingUsed="true"/> ₫
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>

                <div class="table-footer">
                    <div>
                        Hiển thị ${empty salesReports ? 0 : fn:length(salesReports)} / ${empty totalEmployees ? 0 : totalEmployees} nhân viên
                        <c:if test="${not empty pageSize}">
                            <span class="page-size-note">/ ${pageSize} / trang</span>
                        </c:if>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="pagination">
                            <c:url var="prevUrl" value="/reports/employee-sales">
                                <c:param name="page" value="${currentPage - 1}"/>
                                <c:param name="keyword" value="${keyword}"/>
                                <c:param name="branchId" value="${branchFilter == -1 ? '' : branchFilter}"/>
                                <c:param name="dateFrom" value="${dateFrom}"/>
                                <c:param name="dateTo" value="${dateTo}"/>
                                <c:param name="pageSize" value="${pageSizeOption}"/>
                            </c:url>

                            <c:choose>
                                <c:when test="${currentPage > 1}">
                                    <a class="page-btn" href="${prevUrl}">Trước</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="page-btn disabled">Trước</span>
                                </c:otherwise>
                            </c:choose>

                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <c:url var="pageUrl" value="/reports/employee-sales">
                                    <c:param name="page" value="${i}"/>
                                    <c:param name="keyword" value="${keyword}"/>
                                    <c:param name="branchId" value="${branchFilter == -1 ? '' : branchFilter}"/>
                                    <c:param name="dateFrom" value="${dateFrom}"/>
                                    <c:param name="dateTo" value="${dateTo}"/>
                                    <c:param name="pageSize" value="${pageSizeOption}"/>
                                </c:url>

                                <c:choose>
                                    <c:when test="${i == currentPage}">
                                        <span class="page-btn active">${i}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="page-btn" href="${pageUrl}">${i}</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>

                            <c:url var="nextUrl" value="/reports/employee-sales">
                                <c:param name="page" value="${currentPage + 1}"/>
                                <c:param name="keyword" value="${keyword}"/>
                                <c:param name="branchId" value="${branchFilter == -1 ? '' : branchFilter}"/>
                                <c:param name="dateFrom" value="${dateFrom}"/>
                                <c:param name="dateTo" value="${dateTo}"/>
                                <c:param name="pageSize" value="${pageSizeOption}"/>
                            </c:url>

                            <c:choose>
                                <c:when test="${currentPage < totalPages}">
                                    <a class="page-btn" href="${nextUrl}">Sau</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="page-btn disabled">Sau</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
