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
                    <a class="btn-primary" style="font-size:13px;padding:6px 14px;" href="${pageContext.request.contextPath}/reports/inventory-export-excel?keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}">
                        <span class="material-symbols-outlined" style="font-size:16px;">file_download</span> Xuất Excel
                    </a>
                </div>
            </section>

            <c:if test="${not empty reportOverview}">
                <section class="overview-grid">
                    <div class="overview-card">
                        <div class="overview-icon overview-icon-users">
                            <span class="material-symbols-outlined">inventory_2</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng sản phẩm</p>
                            <h3>${reportOverview.totalProducts}</h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-orders">
                            <span class="material-symbols-outlined">numbers</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng số lượng tồn</p>
                            <h3>${reportOverview.totalQuantity}</h3>
                        </div>
                    </div>

                    <div class="overview-card">
                        <div class="overview-icon overview-icon-revenue">
                            <span class="material-symbols-outlined">payments</span>
                        </div>
                        <div class="overview-info">
                            <p>Tổng giá trị tồn</p>
                            <h3>
                                <fmt:formatNumber value="${reportOverview.totalValue}" type="number" groupingUsed="true"/> ₫
                            </h3>
                        </div>
                    </div>

                    <div class="overview-card" style="border-left: 4px solid var(--warning, #f59e0b);">
                        <div class="overview-icon overview-icon-warning">
                            <span class="material-symbols-outlined">warning</span>
                        </div>
                        <div class="overview-info">
                            <p>Sắp hết hàng (<=10)</p>
                            <h3 class="text-warning">${reportOverview.lowStockCount}</h3>
                        </div>
                    </div>

                    <div class="overview-card" style="border-left: 4px solid var(--danger, #ef4444);">
                        <div class="overview-icon overview-icon-danger" style="background: #fee2e2; color: #ef4444;">
                            <span class="material-symbols-outlined">error</span>
                        </div>
                        <div class="overview-info">
                            <p>Hết hàng (0)</p>
                            <h3 class="text-danger">${reportOverview.outOfStockCount}</h3>
                        </div>
                    </div>
                </section>
            </c:if>

            <form class="filter-card" method="get" action="${baseUrl}">
                <input type="hidden" name="page" value="1"/>

                <div class="filter-grid">
                    <div class="form-group filter-search">
                        <label>Tìm kiếm sản phẩm</label>
                        <input name="keyword"
                               value="${keyword}"
                               type="text"
                               placeholder="Nhập tên sản phẩm hoặc mã vạch..."/>
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

                    <input type="hidden" name="sizeValue" value="${sizeValue}"/>

                    <div class="filter-actions">
                        <button class="btn-primary" type="submit">Áp dụng</button>
                        <a class="btn-secondary" href="${pageContext.request.contextPath}/reports/inventory-preview?keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}">
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
                            <th>Sản phẩm</th>
                            <th>Kho hàng</th>
                            <th>Chi nhánh</th>
                            <th class="text-right">Số lượng tồn</th>
                            <th class="text-right">Đơn giá bán</th>
                            <th class="text-right">Tổng giá trị</th>
                            <th>Trạng thái</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty inventoryItems}">
                                <tr>
                                    <td colspan="8" class="empty-row">
                                        <div class="empty-state">
                                            <span class="material-symbols-outlined">inventory_2</span>
                                            <h4>Không tìm thấy dữ liệu tồn kho</h4>
                                            <p>Hãy điều chỉnh bộ lọc tìm kiếm.</p>
                                        </div>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="row" items="${inventoryItems}" varStatus="st">
                                    <tr style="${row.quantityInStock <= 0 ? 'background-color: #fff1f2;' : (row.quantityInStock <= 10 ? 'background-color: #fffbeb;' : '')}">
                                        <td>${(currentPage - 1) * pageSize + st.index + 1}</td>
                                        <td>
                                            <div class="user-cell">
                                                <img src="${empty row.imageUrl ? 'https://placehold.co/40x40' : row.imageUrl}" 
                                                     alt="${row.productName}" 
                                                     style="width: 40px; height: 40px; border-radius: 4px; object-fit: cover; border: 1px solid #e2e8f0;"/>
                                                <div>
                                                    <strong>${row.productName}</strong>
                                                </div>
                                            </div>
                                        </td>
                                        <td>${row.warehouseName}</td>
                                        <td>${row.branchName}</td>
                                        <td class="text-right">
                                            <span style="font-weight: 600; font-size: 1.05rem;" class="${row.quantityInStock <= 0 ? 'text-danger' : (row.quantityInStock <= 10 ? 'text-warning' : '')}">
                                                ${row.quantityInStock}
                                            </span>
                                        </td>
                                        <td class="text-right">
                                            <fmt:formatNumber value="${row.sellingPrice}" type="number" groupingUsed="true"/> ₫
                                        </td>
                                        <td class="text-right font-weight-bold">
                                            <fmt:formatNumber value="${row.totalValue}" type="number" groupingUsed="true"/> ₫
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${row.quantityInStock <= 0}">
                                                    <span class="status-badge status-inactive">Hết hàng</span>
                                                </c:when>
                                                <c:when test="${row.quantityInStock <= 10}">
                                                    <span class="status-badge" style="background: #fffbeb; color: #d97706; border: 1px solid #fef3c7;">Sắp hết</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-badge status-active">Bình thường</span>
                                                </c:otherwise>
                                            </c:choose>
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
                    <jsp:param name="queryString" value="&keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}"/>
                </jsp:include>
            </section>
        </main>
    </div>
</div>
</body>
</html>
