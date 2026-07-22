<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Chi tiết chi nhánh"/>
    <jsp:param name="additionalCSS" value="branch.css"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <main class="main-content">
        
        <div class="container-fluid py-4"> 
            <c:if test="${param.success == 'add'}">
                <div style="background-color: #dcfce7; color: #15803d; padding: 15px; margin-bottom: 20px; border-radius: 8px; font-weight: 600;">
                    ✅ Thêm chi nhánh thành công!
                </div>
            </c:if>
            <c:if test="${param.success == 'edit'}">
                <div style="background-color: #dcfce7; color: #15803d; padding: 15px; margin-bottom: 20px; border-radius: 8px; font-weight: 600;">
                    ✅ Cập nhật chi nhánh thành công!
                </div>
            </c:if>

            <div class="page-header">

                <div class="page-title">

                    <h1>Chi tiết chi nhánh</h1>

                    <p>
                        Thông tin tổng quan chi nhánh
                    </p>

                </div>

                <div style="display: flex; gap: 10px;">
                    <c:if test="${sessionScope.currentUser.roleName == 'Admin' || sessionScope.currentUser.roleName == 'Owner'}">
                        <a href="branch" class="btn-cancel">
                            Quay lại
                        </a>

                        <a href="branch?action=edit&id=${branch.branchId}" class="btn-add">
                            Chỉnh sửa
                        </a>
                    </c:if>
                </div>

            </div>

            <!-- THÔNG TIN CHUNG -->

            <div class="detail-card">

                <div class="detail-header">

                    <div>

                        <h2>${branch.branchName}</h2>

                        <span class="branch-code">

                            ${branch.branchCode}

                        </span>

                    </div>

                    <div>
                        <c:set var="branchStatus" value="${fn:toLowerCase(branch.status)}"/>
                        <span class="${branchStatus == 'active'
                                       ? 'status-active'
                                       : 'status-inactive'}">
                                  ${branchStatus == 'active' ? 'Active' : 'Inactive'}
                              </span>
                        </div>
                    </div>
                    <c:if test="${not empty branch.imageUrl}">
                        <div class="branch-detail-image-wrapper">

                            <img
                                src="${pageContext.request.contextPath}/assets/images/images_branch/${branch.imageUrl}"
                                alt="${branch.branchName}"
                                class="branch-detail-image">

                        </div>
                    </c:if>

                    <div class="detail-grid">
                        <div>
                            <label>Mã chi nhánh</label>
                            <p>${empty branch.branchCode ? '—' : branch.branchCode}</p>
                        </div>

                        <div>
                            <label>Điện thoại</label>
                            <p>${empty branch.phone ? '—' : branch.phone}</p>
                        </div>

                        <div>
                            <label>Email</label>
                            <p>${empty branch.email ? '—' : branch.email}</p>
                        </div>

                        <div>
                            <label>Địa chỉ</label>
                            <p>${empty branch.address ? '—' : branch.address}</p>
                        </div>
                        <div>
                            <label>Giờ mở cửa</label>
                            <p>${empty branch.openingTime ? '—' : branch.openingTime}</p>
                        </div>

                        <div>
                            <label>Giờ đóng cửa</label>
                            <p>${empty branch.closingTime ? '—' : branch.closingTime}</p>
                        </div>

                        <div>
                            <label>Quản lý</label>
                            <p>${empty branch.managerName ? 'Chưa phân công' : branch.managerName}</p>
                        </div>

                        <div>
                            <label>Tổng nhân viên</label>
                            <p>${employeeCount}</p>
                        </div>
                    </div>
                </div>

                <!-- BÁO CÁO TÀI CHÍNH CHI NHÁNH -->
                <div style="margin-top: 24px;">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h3 class="m-0" style="font-size: 18px; font-weight: 600; color: #0f172a;">Báo cáo tài chính chi nhánh</h3>
                        <div class="d-flex align-items-center gap-2" style="background: #f1f5f9; padding: 4px; border-radius: 8px;">
                            <a href="?action=detail&id=${branch.branchId}&range=day" class="btn btn-sm" style="font-weight:600; border-radius:6px; padding: 6px 14px; text-decoration:none; transition:0.2s; ${selectedRange == 'day' ? 'background:#93000b; color:white; border:none;' : 'color:#475569; background:transparent; border:none;'}">Hôm nay</a>
                            <a href="?action=detail&id=${branch.branchId}&range=week" class="btn btn-sm" style="font-weight:600; border-radius:6px; padding: 6px 14px; text-decoration:none; transition:0.2s; ${selectedRange == 'week' ? 'background:#93000b; color:white; border:none;' : 'color:#475569; background:transparent; border:none;'}">Tuần này</a>
                            <a href="?action=detail&id=${branch.branchId}&range=month" class="btn btn-sm" style="font-weight:600; border-radius:6px; padding: 6px 14px; text-decoration:none; transition:0.2s; ${selectedRange == 'month' ? 'background:#93000b; color:white; border:none;' : 'color:#475569; background:transparent; border:none;'}">Tháng này</a>
                        </div>
                    </div>

                    <!-- KPI Cards Grid -->
                    <div class="kpi-grid">
                        <div class="kpi-card">
                            <div class="kpi-card-info">
                                <p>Tổng doanh thu</p>
                                <h3><fmt:formatNumber value="${totalRevenue != null ? totalRevenue : 0}" type="number" maxFractionDigits="0"/> đ</h3>
                                <span class="kpi-subtext">Hóa đơn hoàn tất</span>
                            </div>
                            <div class="kpi-card-icon green" style="background: rgba(16, 185, 129, 0.08); color: #10b981;">
                                <span class="material-icons">payments</span>
                            </div>
                        </div>

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

                        <div class="kpi-card">
                            <div class="kpi-card-info">
                                <p>Lợi nhuận ròng</p>
                                <h3 style="color: ${netProfit >= 0 ? '#10b981' : '#ef4444'};"><fmt:formatNumber value="${netProfit != null ? netProfit : 0}" type="number" maxFractionDigits="0"/> đ</h3>
                                <span class="kpi-subtext">Doanh thu - Chi phí</span>
                            </div>
                            <div class="kpi-card-icon blue" style="background: rgba(59, 130, 246, 0.08); color: #3b82f6;">
                                <span class="material-icons">trending_up</span>
                            </div>
                        </div>

                        <div class="kpi-card">
                            <div class="kpi-card-info">
                                <p>Tổng hóa đơn đã bán</p>
                                <h3><fmt:formatNumber value="${totalInvoices != null ? totalInvoices : 0}"/></h3>
                                <span class="kpi-subtext">Đơn thành công</span>
                            </div>
                            <div class="kpi-card-icon orange" style="background: rgba(245, 158, 11, 0.08); color: #f59e0b;">
                                <span class="material-icons">receipt_long</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- BÁO CÁO PHÁT SINH CHI TIẾT -->
                <div class="table-card mt-4">
                    <div class="table-header">
                        <h3>Báo cáo doanh số và phát sinh chi tiết</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="branch-table align-middle" style="width: 100%;">
                            <thead>
                                <tr>
                                    <th>Mã giao dịch / Hóa đơn</th>
                                    <th>Thời gian</th>
                                    <th>Loại giao dịch</th>
                                    <th>Phương thức</th>
                                    <th style="text-align: right;">Số tiền</th>
                                    <th>Mô tả</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty branchPayments}">
                                        <tr>
                                            <td colspan="6" style="text-align:center;color:#777;padding:24px;">
                                                Không có phát sinh tài chính trong thời gian này
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${branchPayments}" var="payment">
                                            <tr>
                                                <td><strong>${payment.name}</strong></td>
                                                <td><fmt:formatDate value="${payment.paymentDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${payment.paymentType == 'INCOME'}">
                                                            <span class="badge" style="background: rgba(16, 185, 129, 0.1); color: #10b981; font-weight: 600; padding: 4px 8px; border-radius: 12px;">Thu (Doanh thu)</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge" style="background: rgba(239, 68, 68, 0.1); color: #ef4444; font-weight: 600; padding: 4px 8px; border-radius: 12px;">Chi (Chi phí)</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>${payment.method}</td>
                                                <td style="text-align: right; font-weight: 600; color: ${payment.paymentType == 'INCOME' ? '#10b981' : '#ef4444'};">
                                                    <fmt:formatNumber value="${payment.amount}" type="number" maxFractionDigits="0"/> đ
                                                </td>
                                                <td>${payment.description}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- DANH SÁCH NHÂN VIÊN -->
                <div class="table-card">
                    <div class="table-header">
                        <h3>Danh sách nhân viên</h3>
                    </div>
                    <table class="branch-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Họ tên</th>
                                <th>Chức vụ</th>
                                <th>SĐT</th>
                                <th>Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty employeeList}">
                                    <tr>
                                        <td colspan="5" style="text-align:center;color:#777;padding:24px;">
                                            Chưa có nhân viên tại chi nhánh này
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${employeeList}" var="emp">
                                        <tr>
                                            <td>${emp.empId}</td>
                                            <td>${emp.fullName}</td>
                                            <td>${empty emp.roleName ? '—' : emp.roleName}</td>
                                            <td>${empty emp.phone ? '—' : emp.phone}</td>
                                            <td>
                                                <c:set var="empStatus" value="${fn:toLowerCase(emp.status)}"/>
                                                <span class="${empStatus == 'active'
                                                               ? 'status-active'
                                                               : 'status-inactive'}">
                                                          ${empStatus == 'active' ? 'Active' : 'Inactive'}
                                                      </span>
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
    </main>
</div>
<jsp:include page="/views/common/footer.jsp"/>