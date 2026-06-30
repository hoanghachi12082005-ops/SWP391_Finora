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
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>

    <title>
        ${empty profileTitle ? 'My Profile' : profileTitle} - FinoraRetail
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
                    <h2>${empty profileTitle ? 'My Profile' : profileTitle}</h2>
                    <p>
                        ${empty profileSubtitle ? 'View your account information and sales performance' : profileSubtitle}
                    </p>
                </div>

                <c:if test="${not empty backUrl}">
                    <a class="btn-secondary" href="${backUrl}">
                        <span class="material-symbols-outlined">arrow_back</span>
                        Back
                    </a>
                </c:if>
            </section>

            <c:choose>
                <c:when test="${empty profile}">
                    <section class="profile-card">
                        <div class="empty-profile">
                            <span class="material-symbols-outlined">person_off</span>
                            <h3>Profile not found</h3>
                            <p>The selected employee profile could not be loaded.</p>
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
                                         alt="Profile Avatar"/>
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
                                        <span class="status-badge active">Active</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge locked">Locked</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </section>

                    <section class="profile-grid">

                        <div class="profile-card">
                            <div class="card-header">
                                <h3>Account Information</h3>
                            </div>

                            <div class="detail-list">
                                <div class="detail-item">
                                    <span>Full Name</span>
                                    <strong>${profile.fullName}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Email</span>
                                    <strong>${profile.email}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Phone</span>
                                    <strong>${empty profile.phone ? '—' : profile.phone}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Role</span>
                                    <strong>${profile.roleName}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Branch</span>
                                    <strong>${empty profile.branchName ? '—' : profile.branchName}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Status</span>
                                    <strong>${profile.status}</strong>
                                </div>

                                <div class="detail-item">
                                    <span>Created At</span>
                                    <strong>
                                        <fmt:formatDate value="${profile.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                    </strong>
                                </div>
                            </div>
                        </div>

                        <c:if test="${showSalesSection}">
                            <div class="profile-card">
                                <div class="card-header">
                                    <h3>Sales Performance</h3>
                                </div>

                                <div class="sales-grid">
                                    <div class="sales-card">
                                        <span class="material-symbols-outlined">receipt_long</span>
                                        <p>Total Orders</p>
                                        <h4>${empty salesSummary ? 0 : salesSummary.totalOrders}</h4>
                                    </div>

                                    <div class="sales-card">
                                        <span class="material-symbols-outlined">payments</span>
                                        <p>Total Revenue</p>
                                        <h4>
                                            <fmt:formatNumber value="${empty salesSummary ? 0 : salesSummary.totalRevenue}"
                                                              type="number"
                                                              groupingUsed="true"/> ₫
                                        </h4>
                                    </div>

                                    <div class="sales-card">
                                        <span class="material-symbols-outlined">trending_up</span>
                                        <p>Average Order</p>
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
                                    <h3>Order History</h3>
                                    <p>Your recent orders</p>
                                </div>

                                <c:choose>
                                    <c:when test="${empty orderHistory}">
                                        <p style="color:var(--secondary);font-size:13px;">No orders found.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="profile-order-table-wrap">
                                            <table class="profile-order-table">
                                                <thead>
                                                    <tr>
                                                        <th>Code</th>
                                                        <th>Customer</th>
                                                        <th>Total</th>
                                                        <th>Payment</th>
                                                        <th>Status</th>
                                                        <th>Date</th>
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
                                    <h3>Update Profile</h3>
                                    <p>Update your personal information and profile image.</p>
                                </div>

                                <form method="post"
                                      action="${pageContext.request.contextPath}/profile"
                                      enctype="multipart/form-data"
                                      class="profile-form">

                                    <input type="hidden" name="action" value="updateProfile"/>

                                    <div class="form-group">
                                        <label>Profile Image</label>
                                        <input type="file" name="avatarFile" accept="image/*"/>
                                        <small>Allowed: JPG, JPEG, PNG, WEBP. Maximum size depends on servlet config.</small>
                                    </div>

                                    <div class="form-row">
                                        <div class="form-group">
                                            <label>Full Name</label>
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
                                        <label>Phone</label>
                                        <input type="text"
                                               name="phone"
                                               value="${profile.phone}"/>
                                    </div>

                                    <div class="form-actions">
                                        <button type="submit" class="btn-primary">
                                            <span class="material-symbols-outlined">save</span>
                                            Save Changes
                                        </button>
                                    </div>
                                </form>
                            </div>

                            <div class="profile-card">
                                <div class="card-header">
                                    <h3>Change Password</h3>
                                    <p>Use a strong password to protect your account.</p>
                                </div>

                                <form method="post"
                                      action="${pageContext.request.contextPath}/profile"
                                      class="profile-form">

                                    <input type="hidden" name="action" value="changePassword"/>

                                    <div class="form-group">
                                        <label>Old Password</label>
                                        <input type="password"
                                               name="oldPassword"
                                               required/>
                                    </div>

                                    <div class="form-group">
                                        <label>New Password</label>
                                        <input type="password"
                                               name="newPassword"
                                               required/>
                                    </div>

                                    <div class="form-group">
                                        <label>Confirm Password</label>
                                        <input type="password"
                                               name="confirmPassword"
                                               required/>
                                    </div>

                                    <div class="form-actions">
                                        <button type="submit" class="btn-primary">
                                            <span class="material-symbols-outlined">lock_reset</span>
                                            Change Password
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