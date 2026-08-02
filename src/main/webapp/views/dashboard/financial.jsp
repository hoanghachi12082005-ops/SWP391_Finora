<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<% request.setAttribute("pageTitle", "Tổng quan tài chính"); %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Tổng quan tài chính"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />

    <div class="main-content">
        <div class="page-container">
            <!-- Breadcrumbs -->
            <div class="page-breadcrumb" style="display:flex; align-items:center; gap:8px; margin-bottom:16px; font-size:13px; color:#64748b;">
                <a href="${pageContext.request.contextPath}/dashboard/owner" style="color:#64748b; text-decoration:none;">Tổng quan</a>
                <span class="material-icons" style="font-size: 16px;">chevron_right</span>
                <span>Tài chính</span>
            </div>

            <!-- Page Header -->
            <div class="page-header d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 style="font-weight:700; color:#0f172a; margin:0;">Tổng quan tài chính</h2>
                    <p style="color:#64748b; margin:4px 0 0 0;">Tổng quan doanh thu, chi phí và lợi nhuận của toàn chuỗi cửa hàng</p>
                </div>
                <!-- Time range filter form -->
                <form method="get" class="d-flex align-items-center gap-2">
                    <div class="d-flex align-items-center gap-1">
                        <label style="font-size: 13px; color: #64748b; margin-bottom: 0; font-weight: 500;">Từ ngày:</label>
                        <input type="date" name="fromDate" value="${selectedFromDate}" style="padding: 6px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 13px; background: white; width: 140px;">
                    </div>
                    <div class="d-flex align-items-center gap-1">
                        <label style="font-size: 13px; color: #64748b; margin-bottom: 0; font-weight: 500;">Đến ngày:</label>
                        <input type="date" name="toDate" value="${selectedToDate}" style="padding: 6px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 13px; background: white; width: 140px;">
                    </div>
                    <button type="submit" class="btn btn-sm" style="background:#93000b; color:white; font-weight:600; border-radius:6px; padding: 6px 16px; border:none; height: 33px; transition: 0.2s;">Lọc</button>
                </form>
            </div>

            <c:if test="${not empty financialError}">
                <div class="alert alert-danger" style="background:#fee2e2;color:#b91c1c;padding:10px 14px;border-radius:8px;margin-bottom:16px;font-size:13px;">
                    ${financialError}
                </div>
            </c:if>

            <!-- KPI Cards -->
            <div class="kpi-grid">
                <!-- Card 1: Doanh thu -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Tổng doanh thu</p>
                        <h3><fmt:formatNumber value="${totalRevenue != null ? totalRevenue : 0}" type="number" maxFractionDigits="0"/> đ</h3>
                        <span class="kpi-subtext">Đã hoàn thành</span>
                    </div>
                    <div class="kpi-card-icon green" style="background: rgba(16, 185, 129, 0.08); color: #10b981;">
                        <span class="material-icons">payments</span>
                    </div>
                </div>

                <!-- Card 2: Chi phí -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Tổng chi phí phát sinh</p>
                        <h3><fmt:formatNumber value="${totalExpenses != null ? totalExpenses : 0}" type="number" maxFractionDigits="0"/> đ</h3>
                        <span class="kpi-subtext">Chi phí giao dịch</span>
                    </div>
                    <div class="kpi-card-icon red" style="background: rgba(239, 68, 68, 0.08); color: #ef4444;">
                        <span class="material-icons">money_off</span>
                    </div>
                </div>



                <!-- Card 4: Tổng hóa đơn -->
                <div class="kpi-card">
                    <div class="kpi-card-info">
                        <p>Tổng hóa đơn đã bán</p>
                        <h3><fmt:formatNumber value="${totalInvoices != null ? totalInvoices : 0}"/></h3>
                        <span class="kpi-subtext">Hóa đơn thành công</span>
                    </div>
                    <div class="kpi-card-icon orange" style="background: rgba(245, 158, 11, 0.08); color: #f59e0b;">
                        <span class="material-icons">receipt_long</span>
                    </div>
                </div>
            </div>

            <!-- Two Column Layout: Chart & Branch Performance -->
            <div class="row mt-4">
                <!-- Column 1: Chart -->
                <div class="col-lg-5 col-md-12 mb-4">
                    <div class="card border-0 shadow-sm" style="border-radius: 12px; background: #fff; height: 100%;">
                        <div class="card-body p-4">
                            <h5 class="card-title mb-4" style="font-size: 16px; font-weight: 600; color: #0f172a;">Doanh thu theo chi nhánh</h5>
                            <div style="position: relative; height: 280px; width: 100%;">
                                <canvas id="branchRevenueChart"></canvas>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Column 2: Table -->
                <div class="col-lg-7 col-md-12 mb-4">
                    <div class="card border-0 shadow-sm" style="border-radius: 12px; background: #fff; height: 100%;">
                        <div class="card-body p-4">
                            <div class="d-flex justify-content-between align-items-center mb-4">
                                <h5 class="card-title m-0" style="font-size: 16px; font-weight: 600; color: #0f172a;">Hiệu suất các chi nhánh</h5>
                                <a href="${pageContext.request.contextPath}/branch" class="text-decoration-none" style="font-size: 13px; color: #93000b; font-weight: 600;">Quản lý chi nhánh</a>
                            </div>

                            <div class="table-responsive">
                                <table class="table align-middle" style="margin: 0;">
                                    <thead>
                                        <tr style="font-size: 11px; text-transform: uppercase; letter-spacing: 0.5px; color: #64748b; border-bottom: 2px solid #f1f5f9;">
                                            <th style="font-weight: 700; padding: 12px 8px;">Chi nhánh & Mã</th>
                                            <th style="font-weight: 700; padding: 12px 8px; text-align: right;">Số đơn</th>
                                            <th style="font-weight: 700; padding: 12px 8px; text-align: right;">Doanh thu</th>
                                            <th style="font-weight: 700; padding: 12px 8px; text-align: center;">Trạng thái</th>
                                            <th style="font-weight: 700; padding: 12px 8px; text-align: center;">Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${not empty branchRevenues}">
                                                <c:forEach var="br" items="${branchRevenues}">
                                                    <tr style="border-bottom: 1px solid #f1f5f9;">
                                                        <td style="padding: 16px 8px;">
                                                            <div class="d-flex align-items-center gap-3">
                                                                <div class="d-flex align-items-center justify-content-center" style="width: 40px; height: 40px; border-radius: 8px; background: #f8fafc; color: #64748b;">
                                                                    <span class="material-icons">store</span>
                                                                </div>
                                                                <div>
                                                                    <h6 class="m-0" style="font-size: 14px; font-weight: 600; color: #0f172a;">${br.branchName}</h6>
                                                                    <small style="color: #94a3b8;">${br.branchCode}</small>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td style="padding: 16px 8px; text-align: right; font-weight: 500; color: #334155;">
                                                            <fmt:formatNumber value="${br.orderCount}"/>
                                                        </td>
                                                        <td style="padding: 16px 8px; text-align: right; font-weight: 600; color: #0f172a;">
                                                            <fmt:formatNumber value="${br.revenue}" type="number" maxFractionDigits="0"/> đ
                                                        </td>
                                                        <td style="padding: 16px 8px; text-align: center;">
                                                            <c:choose>
                                                                <c:when test="${br.status == 'ACTIVE'}">
                                                                    <span class="badge" style="background: rgba(16, 185, 129, 0.1); color: #10b981; font-weight: 600; font-size: 12px; padding: 6px 12px; border-radius: 30px;">
                                                                        ● Đang hoạt động
                                                                    </span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge" style="background: rgba(100, 116, 139, 0.1); color: #64748b; font-weight: 600; font-size: 12px; padding: 6px 12px; border-radius: 30px;">
                                                                        ● Ngừng hoạt động
                                                                    </span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td style="padding: 16px 8px; text-align: center;">
                                                            <a href="${pageContext.request.contextPath}/branch?action=detail&id=${br.branchId}" class="btn btn-sm btn-light" style="font-size: 12px; font-weight: 600; color: #475569; border: 1px solid #e2e8f0; border-radius: 6px;">Chi tiết</a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <tr>
                                                    <td colspan="5" class="text-center py-4 text-muted" style="font-size: 14px;">Chưa có dữ liệu chi nhánh.</td>
                                                </tr>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Detailed transaction log for the whole system -->
            <div class="card border-0 shadow-sm mt-4 mb-4" style="border-radius: 12px; background: #fff;">
                <div class="card-body p-4">
                    <h5 class="card-title mb-4" style="font-size: 16px; font-weight: 600; color: #0f172a;">Báo cáo doanh số và phát sinh chi tiết (Toàn hệ thống)</h5>
                    <div class="table-responsive">
                        <table class="table align-middle" style="margin: 0;">
                            <thead>
                                <tr style="font-size: 11px; text-transform: uppercase; letter-spacing: 0.5px; color: #64748b; border-bottom: 2px solid #f1f5f9;">
                                    <th style="font-weight: 700; padding: 12px 8px;">Mã giao dịch / Hóa đơn</th>
                                    <th style="font-weight: 700; padding: 12px 8px;">Thời gian</th>
                                    <th style="font-weight: 700; padding: 12px 8px;">Loại giao dịch</th>
                                    <th style="font-weight: 700; padding: 12px 8px;">Phương thức</th>
                                    <th style="font-weight: 700; padding: 12px 8px; text-align: right;">Số tiền</th>
                                    <th style="font-weight: 700; padding: 12px 8px;">Mô tả</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty globalPayments}">
                                        <tr>
                                            <td colspan="6" class="text-center py-4 text-muted" style="font-size: 14px;">Không có phát sinh tài chính trong thời gian này.</td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${globalPayments}" var="payment">
                                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                                <td style="padding: 16px 8px;"><strong>${payment.name}</strong></td>
                                                <td style="padding: 16px 8px;"><fmt:formatDate value="${payment.paymentDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                                <td style="padding: 16px 8px;">
                                                    <c:choose>
                                                        <c:when test="${payment.paymentType == 'INCOME'}">
                                                            <span class="badge" style="background: rgba(16, 185, 129, 0.1); color: #10b981; font-weight: 600; font-size: 12px; padding: 4px 8px; border-radius: 12px;">Thu (Doanh thu)</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge" style="background: rgba(239, 68, 68, 0.1); color: #ef4444; font-weight: 600; font-size: 12px; padding: 4px 8px; border-radius: 12px;">Chi (Chi phí)</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="padding: 16px 8px;">${payment.method}</td>
                                                <td style="padding: 16px 8px; text-align: right; font-weight: 600; color: ${payment.paymentType == 'INCOME' ? '#10b981' : '#ef4444'};">
                                                    <fmt:formatNumber value="${payment.amount}" type="number" maxFractionDigits="0"/> đ
                                                </td>
                                                <td style="padding: 16px 8px; color: #64748b;">${payment.description}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                    
                    <!-- Phân trang -->
                    <jsp:include page="/views/common/pagination.jsp">
                        <jsp:param name="queryString" value="&range=${selectedRange}&fromDate=${selectedFromDate}&toDate=${selectedToDate}" />
                    </jsp:include>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", function() {
        var ctx = document.getElementById('branchRevenueChart').getContext('2d');
        
        var labels = [];
        var revenues = [];
        
        <c:forEach var="br" items="${branchRevenues}">
            labels.push('${br.branchName}');
            revenues.push(${br.revenue.doubleValue()});
        </c:forEach>
        
        if (labels.length === 0) {
            labels.push('Không có dữ liệu');
            revenues.push(0);
        }

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Doanh thu (đ)',
                    data: revenues,
                    backgroundColor: [
                        '#93000b',
                        '#c2410c',
                        '#0369a1',
                        '#0f766e',
                        '#4d7c0f'
                    ],
                    borderRadius: 6,
                    borderWidth: 0,
                    maxBarThickness: 32
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.raw.toLocaleString('vi-VN') + ' đ';
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                if (value >= 1e6) return (value / 1e6) + 'M';
                                if (value >= 1e3) return (value / 1e3) + 'K';
                                return value;
                            },
                            font: {
                                family: 'Inter',
                                size: 11
                            }
                        },
                        grid: {
                            color: '#f1f5f9'
                        }
                    },
                    x: {
                        ticks: {
                            font: {
                                family: 'Inter',
                                size: 11
                            }
                        },
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    });
</script>

<jsp:include page="/views/common/footer.jsp" />
