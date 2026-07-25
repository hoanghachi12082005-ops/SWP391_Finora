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
                    <a class="btn-primary" style="font-size:13px;padding:6px 14px;" href="${pageContext.request.contextPath}/reports/customer-loyal-export-excel?keyword=${empty keyword ? '' : keyword}&branchId=${empty branchFilter ? '' : branchFilter}&datePreset=${empty datePreset ? '' : datePreset}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}">
                        <span class="material-symbols-outlined" style="font-size:16px;">file_download</span> Xuất Excel
                    </a>
                </div>
            </section>

            <c:if test="${not empty reportOverview}">
                <section class="overview-grid">
                    <div class="overview-card">
                        <div class="overview-icon overview-icon-users">
                            <span class="material-symbols-outlined">groups</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng số khách hàng</p>
                            <h3>${reportOverview.totalCustomers}</h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-revenue">
                            <span class="material-symbols-outlined">payments</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng doanh thu từ KH</p>
                            <h3>
                                <fmt:formatNumber value="${reportOverview.totalSpent}" type="number" groupingUsed="true"/> ₫
                            </h3>
                        </div>
                    </div>



                    <div class="overview-card">
                        <div class="overview-icon overview-icon-warning">
                            <span class="material-symbols-outlined">emoji_events</span>
                        </div>
                        <div class="overview-info">
                            <p>Khách hàng VIP</p>
                            <h3>${empty reportOverview.topCustomerName ? '—' : reportOverview.topCustomerName}</h3>
                            <small>
                                Chi tiêu:
                                <fmt:formatNumber value="${reportOverview.topCustomerSpent}" type="number" groupingUsed="true"/> ₫
                            </small>
                        </div>
                    </div>
                </section>
            </c:if>

            <form class="filter-card" method="get" action="${baseUrl}">
                <input type="hidden" name="page" value="1"/>
                <input type="hidden" name="sizeValue" value="${sizeValue}"/>

                <div class="filter-grid">
                    <div class="form-group">
                        <label>Khoảng thời gian</label>
                        <select name="datePreset" id="datePreset" onchange="toggleDateRange()">
                            <option value="">Tùy chọn</option>
                            <option value="today" ${datePreset == 'today' ? 'selected' : ''}>Hôm nay</option>
                            <option value="yesterday" ${datePreset == 'yesterday' ? 'selected' : ''}>Hôm qua</option>
                            <option value="7days" ${datePreset == '7days' ? 'selected' : ''}>7 ngày qua</option>
                            <option value="30days" ${datePreset == '30days' ? 'selected' : ''}>30 ngày qua</option>
                            <option value="this_month" ${datePreset == 'this_month' ? 'selected' : ''}>Tháng này</option>
                            <option value="last_month" ${datePreset == 'last_month' ? 'selected' : ''}>Tháng trước</option>
                            <option value="this_year" ${datePreset == 'this_year' ? 'selected' : ''}>Năm nay</option>
                            <option value="1year" ${datePreset == '1year' ? 'selected' : ''}>1 năm qua</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Từ ngày</label>
                        <input type="date" name="dateFrom" id="dateFrom" value="${dateFrom}"/>
                    </div>

                    <div class="form-group">
                        <label>Đến ngày</label>
                        <input type="date" name="dateTo" id="dateTo" value="${dateTo}"/>
                    </div>

                    <c:choose>
                        <c:when test="${not empty managerBranchId}">
                            <input type="hidden" name="branchId" value="${managerBranchId}"/>
                        </c:when>
                        <c:when test="${showBranch && not empty branches}">
                            <div class="form-group">
                                <label>Chi nhánh</label>
                                <select name="branchId">
                                    <option value="">Tất cả chi nhánh</option>
                                    <c:forEach var="branch" items="${branches}">
                                        <option value="${branch.branchID}" ${branchFilter == branch.branchID ? 'selected' : ''}>${branch.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </c:when>
                    </c:choose>

                    <div class="form-group filter-search">
                        <label>Tìm kiếm khách hàng</label>
                        <input name="keyword"
                               value="${keyword}"
                               type="text"
                               placeholder="Tên, số điện thoại hoặc email..."/>
                    </div>

                    <div class="filter-actions" style="align-self: flex-end;">
                        <button class="btn-primary" type="submit">Áp dụng</button>
                        <a class="btn-secondary" href="${pageContext.request.contextPath}/reports/customer-loyal-preview?keyword=${empty keyword ? '' : keyword}&branchId=${empty branchFilter ? '' : branchFilter}&datePreset=${empty datePreset ? '' : datePreset}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}">
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
                            <th>Khách hàng</th>
                            <th>Số điện thoại</th>
                            <th>Email</th>
                            <th class="text-right">Tổng đơn mua</th>
                            <th class="text-right">Điểm hiện tại</th>
                            <th class="text-right">Tổng chi tiêu</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty customerReports}">
                                <tr>
                                    <td colspan="7" class="empty-row">
                                        <div class="empty-state">
                                            <span class="material-symbols-outlined">groups</span>
                                            <h4>Không tìm thấy khách hàng thân thiết</h4>
                                            <p>Hãy điều chỉnh bộ lọc tìm kiếm.</p>
                                        </div>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="row" items="${customerReports}" varStatus="st">
                                    <tr>
                                        <td>${(currentPage - 1) * pageSize + st.index + 1}</td>
                                        <td>
                                            <div class="user-cell">
                                                <div class="avatar-text">
                                                    <c:choose>
                                                        <c:when test="${not empty row.fullName}">
                                                            ${fn:substring(row.fullName, 0, 1)}
                                                        </c:when>
                                                        <c:otherwise>C</c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div>
                                                    <strong>${row.fullName}</strong>
                                                </div>
                                            </div>
                                        </td>
                                        <td>${row.phone}</td>
                                        <td>${empty row.email ? '—' : row.email}</td>
                                        <td class="text-right">${row.totalOrders}</td>
                                        <td class="text-right text-success" style="font-weight: 600;">${row.currentPoints}</td>
                                        <td class="text-right font-weight-bold">
                                            <fmt:formatNumber value="${row.totalSpent}" type="number" groupingUsed="true"/> ₫
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
                    <jsp:param name="queryString" value="&keyword=${empty keyword ? '' : keyword}&branchId=${empty branchFilter ? '' : branchFilter}&datePreset=${empty datePreset ? '' : datePreset}&dateFrom=${empty dateFrom ? '' : dateFrom}&dateTo=${empty dateTo ? '' : dateTo}"/>
                </jsp:include>
            </section>
        </main>
    </div>
</div>
<script>
function toggleDateRange() {
    var preset = document.getElementById('datePreset').value;
    var dateFrom = document.getElementById('dateFrom');
    var dateTo = document.getElementById('dateTo');
    if (preset) {
        dateFrom.disabled = true;
        dateTo.disabled = true;
    } else {
        dateFrom.disabled = false;
        dateTo.disabled = false;
    }
}
toggleDateRange();
</script>
</body>
</html>
