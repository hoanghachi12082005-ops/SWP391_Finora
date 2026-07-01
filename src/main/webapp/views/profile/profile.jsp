<%-- 
    Document   : profile
    Created on : 2026
    Author     : PCQN
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>

    <title>
        ${empty profileTitle ? 'Hồ sơ của tôi' : profileTitle} - FinoraRetail
    </title>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/base.css?v=20260528"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/layout.css?v=20260528"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/profile.css?v=1"/>

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
</head>

<body>
<div class="app-layout">

    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-wrapper">
        <main class="page-content">

            <c:if test="${not empty sessionScope.successMessage}">
                <div class="alert alert-success">
                    ${sessionScope.successMessage}
                </div>
                <c:remove var="successMessage" scope="session"/>
            </c:if>

            <c:if test="${not empty sessionScope.errorMessage}">
                <div class="alert alert-error">
                    ${sessionScope.errorMessage}
                </div>
                <c:remove var="errorMessage" scope="session"/>
            </c:if>

            <section class="page-header">
                <div>
                    <h2>${empty profileTitle ? 'Hồ sơ của tôi' : profileTitle}</h2>
                    <p>
                        ${empty profileSubtitle ? 'Xem thông tin tài khoản và hiệu suất bán hàng' : profileSubtitle}
                    </p>
                </div>

                <c:if test="${not empty backUrl}">
                    <a class="btn-secondary" href="${backUrl}">
                        <span class="material-symbols-outlined">arrow_back</span>
                        Quay lại
                    </a>
                </c:if>
            </section>

            <c:choose>
                <c:when test="${empty profile}">
                    <section class="profile-card">
                        <div class="empty-profile">
                            <span class="material-symbols-outlined">person_off</span>
                            <h3>Không tìm thấy hồ sơ</h3>
                            <p>Không thể tải hồ sơ nhân viên đã chọn.</p>
                        </div>
                    </section>
                </c:when>

                <c:otherwise>

                    <section class="profile-hero">
                        <div class="profile-avatar-wrap">
                            <c:choose>
                                <c:when test="${not empty profile.avatarUrl}">
                                    <img class="profile-avatar"
                                         src="${pageContext.request.contextPath}${profile.avatarUrl}"
                                         alt="Ảnh đại diện"/>
                                </c:when>

                                <c:otherwise>
                                    <div class="profile-avatar-placeholder">
                                        <c:choose>
                                            <c:when test="${not empty profile.fullName}">
                                                ${fn:substring(profile.fullName, 0, 1)}
                                            </c:when>
                                            <c:otherwise>U</c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="profile-main-info">
                            <h3>${profile.fullName}</h3>

                            <p>
                                <span class="material-symbols-outlined">mail</span>
                                ${profile.email}
                            </p>

                            <p>
                                <span class="material-symbols-outlined">call</span>
                                ${empty profile.phone ? '—' : profile.phone}
                            </p>

                            <div class="profile-badges">
                                <span class="role-badge">
                                    ${profile.roleName}
                                </span>

                                <c:choose>
                                    <c:when test="${profile.status == 'ACTIVE'}">
                                        <span class="status-badge active">Đang hoạt động</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge locked">Đã khóa</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </section>

                    <section class="profile-grid">

                        <div class="profile-card">
                            <div class="card-header">
                                <h3>Thông tin tài khoản</h3>
                            </div>

                            <div class="detail-list">
                                <div class="detail-item">
                                    <span>Họ và tên</span>
                                    <strong>${profile.fullName}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Email</span>
                                    <strong>${profile.email}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Số điện thoại</span>
                                    <strong>${empty profile.phone ? '—' : profile.phone}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Vai trò</span>
                                    <strong>${profile.roleName}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Chi nhánh</span>
                                    <strong>${empty profile.branchName ? '—' : profile.branchName}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Trạng thái</span>
                                    <strong>${profile.status}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Ngày tạo</span>
                                    <strong>
                                        <fmt:formatDate value="${profile.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                    </strong>
                                </div>
                            </div>
                        </div>

                        <c:if test="${showSalesSection}">
                            <div class="profile-card">
                                <div class="card-header">
                                    <h3>Hiệu suất bán hàng</h3>
                                </div>

                                <div class="sales-grid">
                                    <div class="sales-card">
                                        <span class="material-symbols-outlined">receipt_long</span>
                                        <p>Tổng đơn hàng</p>
                                        <h4>${empty salesSummary ? 0 : salesSummary.totalOrders}</h4>
                                    </div>

                                    <div class="sales-card">
                                        <span class="material-symbols-outlined">payments</span>
                                        <p>Tổng doanh thu</p>
                                        <h4>
                                            <fmt:formatNumber value="${empty salesSummary ? 0 : salesSummary.totalRevenue}"
                                                              type="number"
                                                              groupingUsed="true"/> ₫
                                        </h4>
                                    </div>

                                    <div class="sales-card">
                                        <span class="material-symbols-outlined">trending_up</span>
                                        <p>Giá trị trung bình</p>
                                        <h4>
                                            <fmt:formatNumber value="${empty salesSummary ? 0 : salesSummary.averageOrderValue}"
                                                              type="number"
                                                              groupingUsed="true"/> ₫
                                        </h4>
                                    </div>
                                </div>
                            </div>

                            <div class="profile-card">
                                <div class="card-header">
                                    <h3>Lịch sử đơn hàng</h3>
                                    <p>Đơn hàng gần đây của bạn</p>
                                </div>

                                <c:choose>
                                    <c:when test="${empty orderHistory}">
                                        <p style="color:var(--secondary);font-size:13px;">Không tìm thấy đơn hàng.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="profile-order-table-wrap">
                                            <table class="profile-order-table">
                                                <thead>
                                                    <tr>
                                                        <th>Mã</th>
                                                        <th>Khách hàng</th>
                                                        <th>Tổng tiền</th>
                                                        <th>Thanh toán</th>
                                                        <th>Trạng thái</th>
                                                        <th>Ngày</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="ord" items="${orderHistory}">
                                                        <tr>
                                                            <td>${ord.orderCode}</td>
                                                            <td>${empty ord.customerName ? '—' : ord.customerName}</td>
                                                            <td class="amount"><fmt:formatNumber value="${ord.totalAmount}" type="number" groupingUsed="true"/> ₫</td>
                                                            <td class="payment">${ord.paymentMethod}</td>
                                                            <td><span class="status-badge ${ord.status == 'COMPLETED' ? 'active' : 'locked'}">${ord.status}</span></td>
                                                            <td class="date"><fmt:formatDate value="${ord.createdAt}" pattern="dd/MM/yy HH:mm"/></td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:if>

                    </section>

                    <c:if test="${not readOnlyProfile}">
                        <section class="profile-grid">

                            <div class="profile-card">
                                <div class="card-header">
                                    <h3>Cập nhật hồ sơ</h3>
                                    <p>Cập nhật thông tin cá nhân và ảnh đại diện.</p>
                                </div>

                                <form method="post"
                                      action="${pageContext.request.contextPath}/profile"
                                      enctype="multipart/form-data"
                                      class="profile-form">

                                    <input type="hidden" name="action" value="updateProfile"/>

                                    <div class="form-group">
                                        <label>Ảnh đại diện</label>
                                        <input type="file" name="avatarFile" accept="image/*"/>
                                        <small>Chấp nhận: JPG, JPEG, PNG, WEBP. Kích thước tối đa phụ thuộc vào cấu hình.</small>
                                    </div>

                                    <div class="form-row">
                                        <div class="form-group">
                                            <label>Họ và tên</label>
                                            <input type="text"
                                                   name="fullName"
                                                   value="${profile.fullName}"
                                                   required/>
                                        </div>

                                        <div class="form-group">
                                            <label>Email</label>
                                            <input type="email"
                                                   name="email"
                                                   value="${profile.email}"
                                                   required/>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label>Số điện thoại</label>
                                        <input type="text"
                                               name="phone"
                                               value="${profile.phone}"/>
                                    </div>

                                    <div class="form-actions">
                                        <button type="submit" class="btn-primary">
                                            <span class="material-symbols-outlined">save</span>
                                            Lưu thay đổi
                                        </button>
                                    </div>
                                </form>
                            </div>

                            <div class="profile-card">
                                <div class="card-header">
                                    <h3>Đổi mật khẩu</h3>
                                    <p>Sử dụng mật khẩu mạnh để bảo vệ tài khoản.</p>
                                </div>

                                <form method="post"
                                      action="${pageContext.request.contextPath}/profile"
                                      class="profile-form">

                                    <input type="hidden" name="action" value="changePassword"/>

                                    <div class="form-group">
                                        <label>Mật khẩu cũ</label>
                                        <input type="password"
                                               name="oldPassword"
                                               required/>
                                    </div>

                                    <div class="form-group">
                                        <label>Mật khẩu mới</label>
                                        <input type="password"
                                               name="newPassword"
                                               required/>
                                    </div>

                                    <div class="form-group">
                                        <label>Xác nhận mật khẩu</label>
                                        <input type="password"
                                               name="confirmPassword"
                                               required/>
                                    </div>

                                    <div class="form-actions">
                                        <button type="submit" class="btn-primary">
                                            <span class="material-symbols-outlined">lock_reset</span>
                                            Đổi mật khẩu
                                        </button>
                                    </div>
                                </form>
                            </div>

                        </section>
                    </c:if>

                </c:otherwise>
            </c:choose>

        </main>
    </div>
</div>
</body>
</html>