<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Tổng quan kho hàng"/>
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
                <span>Kho hàng</span>
            </div>

            <!-- Page Header -->
            <div class="page-header">
                <div class="page-title">
                    <h2>Quản lý kho hàng</h2>
                    <p>Kiểm tra tồn kho thời gian thực, nhập kho, xuất kho và chuyển hàng nội bộ</p>
                </div>
                <div style="display: flex; gap: 10px;">
                    <a href="${pageContext.request.contextPath}/inventory/export" class="page-action-btn" style="background-color: #64748b; color: white !important;">
                        <span class="material-icons">remove</span>
                        <span>Tạo phiếu xuất</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/inventory/import" class="page-action-btn">
                        <span class="material-icons">add</span>
                        <span>Tạo phiếu nhập</span>
                    </a>
                </div>
            </div>

            <!-- KPI Cards Grid -->
            <div class="kpi-grid">
                <!-- Card 1 -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Tổng mặt hàng</p>
                        <h3>1,240</h3>
                        <span class="kpi-subtext">Danh mục sản phẩm</span>
                    </div>
                    <div class="kpi-card-icon blue">
                        <span class="material-icons">category</span>
                    </div>
                </div>

                <!-- Card 2 -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Sắp hết hàng</p>
                        <h3 style="color: var(--danger-color);">15</h3>
                        <span class="kpi-trend down">
                            <span class="material-icons" style="font-size: 14px;">warning</span>
                            <span>Cần nhập gấp</span>
                        </span>
                    </div>
                    <div class="kpi-card-icon red">
                        <span class="material-icons">notification_important</span>
                    </div>
                </div>

                <!-- Card 3 -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Phiếu chờ duyệt</p>
                        <h3>8</h3>
                        <span class="kpi-subtext">Yêu cầu nhập/xuất</span>
                    </div>
                    <div class="kpi-card-icon orange">
                        <span class="material-icons">pending_actions</span>
                    </div>
                </div>

                <!-- Card 4 -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Giá trị tồn kho</p>
                        <h3>450M</h3>
                        <span class="kpi-subtext">Tổng vốn lưu kho</span>
                    </div>
                    <div class="kpi-card-icon green">
                        <span class="material-icons">inventory</span>
                    </div>
                </div>
            </div>

            <!-- Dashboard Body: Chart and Recent Activity -->
            <div class="dashboard-grid-2">
                <!-- Column 1: Alert Stock Items Table -->
                <div class="dashboard-card">
                    <div class="dashboard-card-title">
                        <h5>Cảnh báo tồn kho (Dưới hạn mức)</h5>
                        <div class="card-filter-tabs">
                            <button class="filter-tab active">Tất cả</button>
                            <button class="filter-tab">Đã liên hệ NCC</button>
                        </div>
                    </div>

                    <div class="premium-table-container">
                        <table class="premium-table">
                            <thead>
                                <tr>
                                    <th>Sản phẩm</th>
                                    <th>Mã SKU</th>
                                    <th>Số lượng hiện tại</th>
                                    <th>Hạn mức tối thiểu</th>
                                    <th>Đơn vị</th>
                                    <th>Trạng thái</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>
                                        <div class="product-cell">
                                            <div class="product-img-box">
                                                <span class="material-icons">coffee</span>
                                            </div>
                                            <div class="product-details">
                                                <h6>Cà phê hạt Moka</h6>
                                                <small>SKU-COF-001</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td>COF-MOKA-01</td>
                                    <td style="color: var(--danger-color); font-weight: 700;">4.2 kg</td>
                                    <td>10.0 kg</td>
                                    <td>kg</td>
                                    <td>
                                        <span class="badge-status inactive">
                                            <span class="material-icons" style="font-size: 10px;">error</span>
                                            <span>Sắp hết</span>
                                        </span>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/inventory/import?sku=COF-MOKA-01" class="table-action-link" style="color: var(--primary-color);">Nhập thêm</a>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="product-cell">
                                            <div class="product-img-box">
                                                <span class="material-icons">water_drop</span>
                                            </div>
                                            <div class="product-details">
                                                <h6>Sữa tươi tiệt trùng 1L</h6>
                                                <small>SKU-MILK-002</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td>MLK-VIN-02</td>
                                    <td style="color: var(--danger-color); font-weight: 700;">8 hộp</td>
                                    <td>20 hộp</td>
                                    <td>Hộp</td>
                                    <td>
                                        <span class="badge-status inactive">
                                            <span class="material-icons" style="font-size: 10px;">error</span>
                                            <span>Sắp hết</span>
                                        </span>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/inventory/import?sku=MLK-VIN-02" class="table-action-link" style="color: var(--primary-color);">Nhập thêm</a>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Column 2: Recent Stock Logs -->
                <div class="dashboard-card">
                    <div class="dashboard-card-title">
                        <h5>Nhật ký kho gần đây</h5>
                        <a href="${pageContext.request.contextPath}/inventory/report" style="font-size: 12px; font-weight: 600;">Báo cáo</a>
                    </div>
                    <div class="activity-feed">
                        <div class="activity-item">
                            <div class="activity-icon green">
                                <span class="material-icons" style="font-size: 16px;">arrow_downward</span>
                            </div>
                            <div class="activity-details">
                                <p>Nhập thành công <strong>50kg Cà phê Robusta</strong></p>
                                <small>10 phút trước - NCC Trung Nguyên</small>
                            </div>
                        </div>

                        <div class="activity-item">
                            <div class="activity-icon red">
                                <span class="material-icons" style="font-size: 16px;">arrow_upward</span>
                            </div>
                            <div class="activity-details">
                                <p>Xuất kho <strong>20 hộp Sữa tươi 1L</strong> cho CN Q.1</p>
                                <small>45 phút trước - Thủ kho Lê Nam</small>
                            </div>
                        </div>

                        <div class="activity-item">
                            <div class="activity-icon blue">
                                <span class="material-icons" style="font-size: 16px;">sync</span>
                            </div>
                            <div class="activity-details">
                                <p>Điều chuyển <strong>10 máy POS</strong> sang CN Q.7</p>
                                <small>2 giờ trước - Mã phiếu #TRF-022</small>
                            </div>
                        </div>

                        <div class="activity-item">
                            <div class="activity-icon green">
                                <span class="material-icons" style="font-size: 16px;">arrow_downward</span>
                            </div>
                            <div class="activity-details">
                                <p>Nhập kho <strong>100 cốc thủy tinh</strong></p>
                                <small>4 giờ trước - Người nhận: Hoài An</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
        </div>
    </div>
</div>

<jsp:include page="/views/common/footer.jsp" />
