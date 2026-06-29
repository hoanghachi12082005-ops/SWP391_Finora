<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Tổng quan quản trị"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />
    
    <div class="main-content">
        <jsp:include page="/views/common/topbar.jsp" />
        
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
                <button class="page-action-btn">
                    <span class="material-icons">add</span>
                    <span>Thêm cửa hàng</span>
                </button>
            </div>

            <!-- KPI Cards Grid -->
            <div class="kpi-grid">
                <!-- Card 1 -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Tổng số cửa hàng</p>
                        <h3>12</h3>
                        <span class="kpi-trend up">
                            <span class="material-icons" style="font-size: 14px;">trending_up</span>
                            <span>+2 tháng này</span>
                        </span>
                    </div>
                    <div class="kpi-card-icon red">
                        <span class="material-icons">storefront</span>
                    </div>
                </div>

                <!-- Card 2 -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Doanh thu hôm nay</p>
                        <h3>150M</h3>
                        <span class="kpi-trend up">
                            <span class="material-icons" style="font-size: 14px;">trending_up</span>
                            <span>+15% so với hôm qua</span>
                        </span>
                    </div>
                    <div class="kpi-card-icon orange">
                        <span class="material-icons">payments</span>
                    </div>
                </div>

                <!-- Card 3 -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Tổng nhân viên</p>
                        <h3>85</h3>
                        <span class="kpi-trend neutral">
                            <span>-- 0</span>
                        </span>
                    </div>
                    <div class="kpi-card-icon blue">
                        <span class="material-icons">group</span>
                    </div>
                </div>

                <!-- Card 4 -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Cửa hàng tốt nhất</p>
                        <h3 style="font-size: 18px; margin-top: 6px;">Quận 1</h3>
                        <span class="kpi-subtext">Doanh thu 1.25B</span>
                    </div>
                    <div class="kpi-card-icon green">
                        <span class="material-icons">workspace_premium</span>
                    </div>
                </div>
            </div>

            <!-- Dashboard Body: Chart and Recent Activity -->
            <div class="dashboard-grid-2">
                <!-- Column 1: Chart Card -->
                <div class="dashboard-card">
                    <div class="dashboard-card-title">
                        <h5>Doanh thu theo chi nhánh</h5>
                        <div class="card-filter-tabs">
                            <button class="filter-tab active">Hôm nay</button>
                            <button class="filter-tab">Tuần này</button>
                            <button class="filter-tab">Tháng này</button>
                        </div>
                    </div>

                    <!-- Fake Bar Chart using pure CSS -->
                    <div class="chart-box">
                        <div class="chart-bar-group">
                            <div class="chart-bar-container">
                                <div class="chart-bar" style="height: 85%;"></div>
                            </div>
                            <span class="chart-label">CN Q.1</span>
                        </div>
                        <div class="chart-bar-group">
                            <div class="chart-bar-container">
                                <div class="chart-bar" style="height: 65%;"></div>
                            </div>
                            <span class="chart-label">CN Q.7</span>
                        </div>
                        <div class="chart-bar-group">
                            <div class="chart-bar-container">
                                <div class="chart-bar" style="height: 45%;"></div>
                            </div>
                            <span class="chart-label">CN Q.3</span>
                        </div>
                        <div class="chart-bar-group">
                            <div class="chart-bar-container">
                                <div class="chart-bar" style="height: 55%;"></div>
                            </div>
                            <span class="chart-label">CN Q.10</span>
                        </div>
                        <div class="chart-bar-group">
                            <div class="chart-bar-container">
                                <div class="chart-bar" style="height: 35%;"></div>
                            </div>
                            <span class="chart-label">CN Bình Thạnh</span>
                        </div>
                        <div class="chart-bar-group">
                            <div class="chart-bar-container">
                                <div class="chart-bar" style="height: 70%;"></div>
                            </div>
                            <span class="chart-label">CN Thủ Đức</span>
                        </div>
                    </div>

                    <!-- Chart Legend -->
                    <div class="chart-legend">
                        <div class="legend-item">
                            <span class="legend-color primary"></span>
                            <span>Doanh thu thực tế (triệu VNĐ)</span>
                        </div>
                    </div>
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

            <!-- Branch List Table -->
            <div class="dashboard-card">
                <div class="dashboard-card-title">
                    <h5>Danh sách chi nhánh</h5>
                    <div style="display: flex; gap: 8px;">
                        <button class="filter-tab active">Tất cả</button>
                        <button class="filter-tab">Mới</button>
                    </div>
                </div>
                
                <div class="premium-table-container">
                    <table class="premium-table">
                        <thead>
                            <tr>
                                <th>Chi nhánh & Mã</th>
                                <th>Địa chỉ</th>
                                <th>Liên hệ</th>
                                <th>Quản lý</th>
                                <th>Nhân viên</th>
                                <th>Doanh thu</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <div class="product-cell">
                                        <div class="product-img-box">
                                            <span class="material-icons">store</span>
                                        </div>
                                        <div class="product-details">
                                            <h6>Chi nhánh Quận 1</h6>
                                            <small>BM-001</small>
                                        </div>
                                    </div>
                                </td>
                                <td>123 Lê Lợi, P. Bến Thành, Q1</td>
                                <td>028.3822.XXXX</td>
                                <td>Nguyễn Hùng</td>
                                <td>24</td>
                                <td><strong>1.25B</strong></td>
                                <td>
                                    <span class="badge-status active">
                                        <span class="material-icons" style="font-size: 10px;">fiber_manual_record</span>
                                        <span>Đang hoạt động</span>
                                    </span>
                                </td>
                                <td>
                                    <a href="#" class="table-action-link">Chi tiết</a>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <div class="product-cell">
                                        <div class="product-img-box">
                                            <span class="material-icons">store</span>
                                        </div>
                                        <div class="product-details">
                                            <h6>Chi nhánh Quận 7</h6>
                                            <small>BM-002</small>
                                        </div>
                                    </div>
                                </td>
                                <td>456 Nguyễn Văn Linh, Q7</td>
                                <td>028.5412.XXXX</td>
                                <td>Trần Thuỷ</td>
                                <td>18</td>
                                <td><strong>980M</strong></td>
                                <td>
                                    <span class="badge-status active">
                                        <span class="material-icons" style="font-size: 10px;">fiber_manual_record</span>
                                        <span>Đang hoạt động</span>
                                    </span>
                                </td>
                                <td>
                                    <a href="#" class="table-action-link">Chi tiết</a>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            
        </div>
    </div>
</div>

<jsp:include page="/views/common/footer.jsp" />
