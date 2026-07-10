<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Tổng quan quản trị"/>
</jsp:include>

<%-- Helper format tiền tệ rút gọn (M / B) --%>
<c:set var="ov" value="${overview}" />

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />
    
    <div class="main-content">
        
        
        <div class="page-container">
            <!-- Breadcrumbs -->
            <div class="page-breadcrumb">
                <a href="#">Dashboard</a>
                <span class="material-icons">chevron_right</span>
                <span>Tổng quan</span>
            </div>

            <!-- Page Header -->
            <div class="page-header">
                <div class="page-title">
                    <h2>Quản lý chuỗi cửa hàng</h2>
                    <p>Tổng hợp kết quả kinh doanh và quản lý toàn bộ chi nhánh</p>
                </div>
                
            </div>

            <c:if test="${not empty overviewError}">
                <div class="alert alert-danger" style="background:#fee2e2;color:#b91c1c;padding:10px 14px;border-radius:8px;margin-bottom:16px;font-size:13px;">
                    ${overviewError}
                </div>
            </c:if>

            <!-- KPI Cards Grid -->
            <div class="kpi-grid">
                <!-- Card 1: Doanh thu hôm nay -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Doanh thu hôm nay</p>
                        <h3><fmt:formatNumber value="${ov.revenueToday}" type="number" maxFractionDigits="0"/> đ</h3>
                        <c:choose>
                            <c:when test="${ov.revenueChangeVsYesterday == null}">
                                <span class="kpi-trend neutral">
                                    <span>-- so với hôm qua</span>
                                </span>
                            </c:when>
                            <c:when test="${ov.revenueChangeVsYesterday >= 0}">
                                <span class="kpi-trend up">
                                    <span class="material-icons" style="font-size: 14px;">trending_up</span>
                                    <span>+<fmt:formatNumber value="${ov.revenueChangeVsYesterday}" maxFractionDigits="1"/>% so với hôm qua</span>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="kpi-trend down">
                                    <span class="material-icons" style="font-size: 14px;">trending_down</span>
                                    <span><fmt:formatNumber value="${ov.revenueChangeVsYesterday}" maxFractionDigits="1"/>% so với hôm qua</span>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="kpi-card-icon orange">
                        <span class="material-icons">payments</span>
                    </div>
                </div>

                <!-- Card 2: Đơn hàng hôm nay -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Đơn hàng hôm nay</p>
                        <h3><fmt:formatNumber value="${ov.ordersToday}"/></h3>
                        <span class="kpi-subtext">
                            Tháng này: <strong><fmt:formatNumber value="${ov.ordersThisMonth}"/></strong> đơn
                        </span>
                    </div>
                    <div class="kpi-card-icon blue">
                        <span class="material-icons">receipt_long</span>
                    </div>
                </div>

                <!-- Card 3: Khách hàng -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Tổng khách hàng</p>
                        <h3><fmt:formatNumber value="${ov.totalCustomers}"/></h3>
                        <c:choose>
                            <c:when test="${ov.newCustomersThisMonth > 0}">
                                <span class="kpi-trend up">
                                    <span class="material-icons" style="font-size: 14px;">person_add</span>
                                    <span>+<fmt:formatNumber value="${ov.newCustomersThisMonth}"/> tháng này</span>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="kpi-trend neutral">
                                    <span>0 khách mới tháng này</span>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="kpi-card-icon green">
                        <span class="material-icons">group</span>
                    </div>
                </div>

                <!-- Card 4: Tồn kho -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Sản phẩm trong kho</p>
                        <h3><fmt:formatNumber value="${ov.totalProducts}"/></h3>
                        <c:choose>
                            <c:when test="${ov.outOfStockItems > 0 or ov.lowStockItems > 0}">
                                <span class="kpi-trend down">
                                    <span class="material-icons" style="font-size: 14px;">warning</span>
                                    <span>${ov.outOfStockItems} hết hàng, ${ov.lowStockItems} sắp hết</span>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="kpi-trend up">
                                    <span class="material-icons" style="font-size: 14px;">check_circle</span>
                                    <span>Tồn kho ổn định</span>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="kpi-card-icon red">
                        <span class="material-icons">inventory_2</span>
                    </div>
                </div>
            </div>

            <!-- Hàng thứ 2: Tổng hợp tháng -->
            <div class="kpi-grid">
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Doanh thu tháng này</p>
                        <h3><fmt:formatNumber value="${ov.revenueThisMonth}" type="number" maxFractionDigits="0"/> đ</h3>
                        <c:choose>
                            <c:when test="${ov.revenueChangeVsLastMonth == null}">
                                <span class="kpi-trend neutral"><span>-- so với tháng trước</span></span>
                            </c:when>
                            <c:when test="${ov.revenueChangeVsLastMonth >= 0}">
                                <span class="kpi-trend up">
                                    <span class="material-icons" style="font-size: 14px;">trending_up</span>
                                    <span>+<fmt:formatNumber value="${ov.revenueChangeVsLastMonth}" maxFractionDigits="1"/>% so với tháng trước</span>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="kpi-trend down">
                                    <span class="material-icons" style="font-size: 14px;">trending_down</span>
                                    <span><fmt:formatNumber value="${ov.revenueChangeVsLastMonth}" maxFractionDigits="1"/>% so với tháng trước</span>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="kpi-card-icon orange">
                        <span class="material-icons">calendar_month</span>
                    </div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Giá trị trung bình/đơn</p>
                        <h3><fmt:formatNumber value="${ov.averageOrderValue}" type="number" maxFractionDigits="0"/> đ</h3>
                        <span class="kpi-subtext">Đơn chờ xử lý: <strong>${ov.pendingOrders}</strong></span>
                    </div>
                    <div class="kpi-card-icon blue">
                        <span class="material-icons">analytics</span>
                    </div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Giá trị tồn kho</p>
                        <h3><fmt:formatNumber value="${ov.totalStockValue}" type="number" maxFractionDigits="0"/> đ</h3>
                        <span class="kpi-subtext">Tổng <strong>${ov.totalProducts}</strong> sản phẩm</span>
                    </div>
                    <div class="kpi-card-icon green">
                        <span class="material-icons">warehouse</span>
                    </div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Cửa hàng &amp; Nhân viên</p>
                        <h3>${ov.totalStores} <small style="font-size:14px;color:#64748b;">cửa hàng</small></h3>
                        <span class="kpi-subtext">${ov.totalEmployees} nhân viên đang hoạt động</span>
                    </div>
                    <div class="kpi-card-icon red">
                        <span class="material-icons">storefront</span>
                    </div>
                </div>
            </div>

            <!-- Dashboard Body: Chart and Recent Activity -->
            <div class="dashboard-grid-2">
                <!-- Column 1: Chart Card -->
                <div class="dashboard-card">
                    <div class="dashboard-card-title">
                        <h5>Doanh thu theo chi nhánh (Tháng này)</h5>
                        <c:if test="${not empty ov.topStoreName}">
                            <span style="font-size:12px;color:#64748b;">
                                Top: <strong style="color:#0f172a;">${ov.topStoreName}</strong>
                                (<fmt:formatNumber value="${ov.topStoreRevenue}" type="number" maxFractionDigits="0"/> đ)
                            </span>
                        </c:if>
                    </div>

                    <c:choose>
                        <c:when test="${empty ov.branchRevenues}">
                            <div class="text-center text-muted py-3" style="font-size: 13px;padding:24px;">
                                Chưa có dữ liệu doanh thu theo chi nhánh trong tháng này.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <%-- Tìm max để tính chiều cao tương đối --%>
                            <c:set var="maxRev" value="0" />
                            <c:forEach var="br" items="${ov.branchRevenues}">
                                <c:if test="${br.revenue.doubleValue() > maxRev}">
                                    <c:set var="maxRev" value="${br.revenue.doubleValue()}" />
                                </c:if>
                            </c:forEach>

                            <div class="chart-box">
                                <c:forEach var="br" items="${ov.branchRevenues}">
                                    <c:set var="height" value="${maxRev > 0 ? (br.revenue.doubleValue() / maxRev) * 100 : 0}" />
                                    <div class="chart-bar-group">
                                        <div class="chart-bar-container">
                                            <div class="chart-bar" style="height: ${height}%;"
                                                 title="<fmt:formatNumber value='${br.revenue}' type='number' maxFractionDigits='0'/> đ"></div>
                                        </div>
                                        <span class="chart-label">${br.branchName}</span>
                                    </div>
                                </c:forEach>
                            </div>

                            <div class="chart-legend">
                                <div class="legend-item">
                                    <span class="legend-color primary"></span>
                                    <span>Doanh thu thực tế (VNĐ)</span>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Column 2: Recent Activity (chỉ hiển thị cho Owner) -->
                <c:set var="ownerRole" value="${sessionScope.currentUser != null ? sessionScope.currentUser.roleName : ''}" />
                <c:if test="${ownerRole == 'Owner'}">
                <div class="dashboard-card">
                    <div class="dashboard-card-title">
                        <h5>Hoạt động gần đây</h5>
                        <a href="${pageContext.request.contextPath}/activity-log" style="font-size: 12px; font-weight: 600;">Xem tất cả</a>
                    </div>
                    <div class="activity-feed">
                        <c:choose>
                            <c:when test="${empty recentActivities}">
                                <div class="text-center text-muted py-3" style="font-size: 13px;">Chưa có hoạt động nào.</div>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="act" items="${recentActivities}">
                                    <div class="activity-item">
                                        <div class="activity-icon ${act.iconColor}">
                                            <span class="material-icons" style="font-size: 16px;">${act.iconName}</span>
                                        </div>
                                        <div class="activity-details">
                                            <p>
                                                ${act.description}
                                                <c:if test="${not empty act.entityCode}"> &middot; <strong>${act.entityCode}</strong></c:if>
                                            </p>
                                            <small>
                                                <c:if test="${not empty act.createdAtFormatted}">${act.createdAtFormatted} - </c:if>
                                                ${act.actorLabel}
                                            </small>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                </c:if>
            </div>

            <!-- Top sản phẩm bán chạy -->
            <div class="dashboard-card">
                <div class="dashboard-card-title">
                    <h5>Top 5 sản phẩm bán chạy (Tháng này)</h5>
                    <a href="${pageContext.request.contextPath}/reports/inventory" style="font-size:12px;font-weight:600;">Xem báo cáo</a>
                </div>

                <div class="premium-table-container">
                    <table class="premium-table">
                        <thead>
                            <tr>
                                <th style="width:60px;">#</th>
                                <th>Sản phẩm</th>
                                <th style="text-align:right;">Số lượng bán</th>
                                <th style="text-align:right;">Doanh thu</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty ov.topProducts}">
                                    <tr>
                                        <td colspan="4" class="text-center text-muted" style="padding:20px;font-size:13px;">
                                            Chưa có sản phẩm nào được bán trong tháng này.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="p" items="${ov.topProducts}" varStatus="loop">
                                        <tr>
                                            <td><strong>#${loop.index + 1}</strong></td>
                                            <td>${p.productName}</td>
                                            <td style="text-align:right;"><fmt:formatNumber value="${p.quantitySold}"/></td>
                                            <td style="text-align:right;"><strong><fmt:formatNumber value="${p.revenue}" type="number" maxFractionDigits="0"/> đ</strong></td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Bảng chi tiết theo chi nhánh -->
            <div class="dashboard-card">
                <div class="dashboard-card-title">
                    <h5>Hiệu suất các chi nhánh</h5>
                    <a href="${pageContext.request.contextPath}/stores" style="font-size:12px;font-weight:600;">Quản lý chi nhánh</a>
                </div>
                
                <div class="premium-table-container">
                    <table class="premium-table">
                        <thead>
                            <tr>
                                <th>Chi nhánh &amp; Mã</th>
                                <th style="text-align:right;">Số đơn (Tháng này)</th>
                                <th style="text-align:right;">Doanh thu (Tháng này)</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty ov.branchRevenues}">
                                    <tr>
                                        <td colspan="5" class="text-center text-muted" style="padding:20px;font-size:13px;">
                                            Chưa có chi nhánh nào.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="br" items="${ov.branchRevenues}">
                                        <tr>
                                            <td>
                                                <div class="product-cell">
                                                    <div class="product-img-box">
                                                        <span class="material-icons">store</span>
                                                    </div>
                                                    <div class="product-details">
                                                        <h6>${br.branchName}</h6>
                                                        <small>${not empty br.branchCode ? br.branchCode : '—'}</small>
                                                    </div>
                                                </div>
                                            </td>
                                            <td style="text-align:right;"><fmt:formatNumber value="${br.orderCount}"/></td>
                                            <td style="text-align:right;"><strong><fmt:formatNumber value="${br.revenue}" type="number" maxFractionDigits="0"/> đ</strong></td>
                                            <td>
                                                <span class="badge-status active">
                                                    <span class="material-icons" style="font-size: 10px;">fiber_manual_record</span>
                                                    <span>Đang hoạt động</span>
                                                </span>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/stores/detail?id=${br.branchId}" class="table-action-link">Chi tiết</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
            
        </div>
    </div>
</div>

<jsp:include page="/views/common/footer.jsp" />
