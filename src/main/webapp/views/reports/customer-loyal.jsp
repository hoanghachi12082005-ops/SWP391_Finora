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
                    <a class="btn-primary" style="font-size:13px;padding:6px 14px;" href="${pageContext.request.contextPath}/reports/customer-loyal-export-excel?keyword=${empty keyword ? '' : keyword}">
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
                        <div class="overview-icon overview-icon-orders">
                            <span class="material-symbols-outlined">stars</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng điểm tích lũy</p>
                            <h3>${reportOverview.totalPoints} pts</h3>
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

                <div class="filter-grid">
                    <div class="form-group filter-search">
                        <label>Tìm kiếm khách hàng</label>
                        <input name="keyword"
                               value="${keyword}"
                               type="text"
                               placeholder="Tên, số điện thoại hoặc email..."/>
                    </div>

                    <input type="hidden" name="sizeValue" value="${sizeValue}"/>

                    <div class="filter-actions">
                        <button class="btn-primary" type="submit">Áp dụng</button>
                        <a class="btn-secondary" href="${pageContext.request.contextPath}/reports/customer-loyal-preview?keyword=${empty keyword ? '' : keyword}">
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
                            <th class="text-right">Điểm trọn đời</th>
                            <th class="text-right">Tổng chi tiêu</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty customerReports}">
                                <tr>
                                    <td colspan="8" class="empty-row">
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
                                        <td class="text-right" style="color: #64748b;">${row.lifetimePoints}</td>
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
                    <jsp:param name="queryString" value="&keyword=${empty keyword ? '' : keyword}"/>
                </jsp:include>
            </section>
        </main>
    </div>
</div>
</body>
</html>
