<%-- 
    Document   : customer-list
    Created on : 27 Jun 2026
    Author     : Dzung
    Pattern    : Based on user-list.jsp design, updated to remove levels and enforce role rules.
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
        <title>${pageTitle} - Finora</title>

        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/base.css?v=20260601"/>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/layout.css?v=20260601"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/user-management.css?v=2"/>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/customer-management.css?v=20260601"/>

        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    </head>

    <body class="user-page">
    <div class="app-layout">
        <jsp:include page="/views/common/sidebar.jsp"/>

        <div class="main-wrapper">
            <main class="page-content">

                <%-- ========================= Flash Messages ========================= --%>
                <c:if test="${not empty sessionScope.successMessage}">
                    <div class="alert alert-success">${sessionScope.successMessage}</div>
                    <c:remove var="successMessage" scope="session"/>
                </c:if>

                <c:if test="${not empty sessionScope.errorMessage}">
                    <div class="alert alert-error">${sessionScope.errorMessage}</div>
                    <c:remove var="errorMessage" scope="session"/>
                </c:if>

                <%-- ========================= Page Header ========================= --%>
                <section class="page-header">
                    <div>
                        <h2>${pageTitle}</h2>
                        <p>${pageSubtitle}</p>
                    </div>

                    <c:if test="${canCreate}">
                        <a class="btn-primary" href="${baseUrl}?action=add">
                            <span class="material-symbols-outlined">person_add</span>
                            ${addButtonText}
                        </a>
                    </c:if>
                </section>

                <%-- ========================= Overview Cards ========================= --%>
                <c:if test="${not empty customerOverview}">
                    <section class="overview-grid">

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-users">
                                <span class="material-symbols-outlined">groups</span>
                            </div>
                            <div class="overview-info">
                                <p>Tổng khách hàng</p>
                                <h3>${customerOverview.totalCustomers}</h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-users">
                                <span class="material-symbols-outlined">person_add</span>
                            </div>
                            <div class="overview-info">
                                <p>Khách hàng mới trong tháng</p>
                                <h3>${customerOverview.newCustomersThisMonth}</h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-revenue">
                                <span class="material-symbols-outlined">payments</span>
                            </div>
                            <div class="overview-info">
                                <p>Tổng chi tiêu</p>
                                <h3>
                                    <fmt:formatNumber value="${customerOverview.totalSpent}" type="number" groupingUsed="true"/> ₫
                                </h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-warning">
                                <span class="material-symbols-outlined">emoji_events</span>
                            </div>
                            <div class="overview-info">
                                <p>Điểm tích lũy cao nhất</p>
                                <h3>${empty customerOverview.topCustomerName ? '—' : customerOverview.topCustomerName}</h3>
                                <small>
                                    Điểm: ${customerOverview.topCustomerPoints}
                                </small>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon overview-icon-orders">
                                <span class="material-symbols-outlined">bar_chart</span>
                            </div>
                            <div class="overview-info">
                                <p>Kết quả lọc</p>
                                <h3>${empty totalCustomers ? 0 : totalCustomers}</h3>
                            </div>
                        </div>

                    </section>
                </c:if>

                <%-- ========================= Filter Card ========================= --%>
                <form class="filter-card" method="get" action="${baseUrl}">
                    <input type="hidden" name="page" value="1"/>

                    <div class="filter-grid">

                        <div class="form-group filter-search">
                            <label>Tìm kiếm</label>
                            <input name="keyword"
                                   value="${keyword}"
                                   type="text"
                                   placeholder="Tên, email hoặc số điện thoại..."/>
                        </div>

                        <c:if test="${not empty branches}">
                            <div class="form-group">
                                <label>Chi nhánh</label>
                                <select name="branchId">
                                    <option value="">Tất cả chi nhánh</option>
                                    <c:forEach var="br" items="${branches}">
                                        <option value="${br.branchID}" ${branchFilter == br.branchID ? 'selected' : ''}>
                                            ${br.name}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </c:if>

                        <input type="hidden" name="sizeValue" value="${sizeValue}"/>

                        <div class="filter-actions">
                            <button class="btn-primary" type="submit">Áp dụng</button>
                            <a class="btn-secondary" href="${baseUrl}">Đặt lại</a>
                        </div>

                    </div>
                </form>

                <%-- ========================= Data Table ========================= --%>
                <section class="table-card">
                    <div class="table-scroll">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Khách hàng</th>
                                <th>Số điện thoại</th>
                                <th>Điểm</th>
                                <th class="text-right">Thao tác</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:choose>
                                <c:when test="${empty customers}">
                                    <tr>
                                        <td colspan="4" class="empty-row">
                                            <div class="empty-state">
                                                <span class="material-symbols-outlined">group_off</span>
                                                <h4>Không tìm thấy khách hàng</h4>
                                                <p>Không có khách hàng nào phù hợp với tiêu chí tìm kiếm.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach var="cust" items="${customers}">
                                        <tr>
                                            <td>
                                                <div class="user-cell">
                                                    <div class="avatar-text">
                                                        <c:choose>
                                                            <c:when test="${not empty cust.fullName}">
                                                                ${fn:substring(cust.fullName, 0, 1)}
                                                            </c:when>
                                                            <c:otherwise>C</c:otherwise>
                                                        </c:choose>
                                                    </div>

                                                    <div>
                                                        <strong>${cust.fullName}</strong>
                                                        <span>${empty cust.email ? '' : cust.email}</span>
                                                    </div>
                                                </div>
                                            </td>

                                            <td>${empty cust.phone ? '—' : cust.phone}</td>

                                            <td>${cust.loyaltyPoint}</td>

                                            <td>
                                                <div class="table-actions">

                                                    <a href="${baseUrl}?action=detail&id=${cust.customerId}" title="Xem chi tiết">
                                                        <span class="material-symbols-outlined">visibility</span>
                                                    </a>

                                                    <c:if test="${canEdit}">
                                                        <a href="${baseUrl}?action=edit&id=${cust.customerId}" title="Chỉnh sửa">
                                                            <span class="material-symbols-outlined">edit</span>
                                                        </a>
                                                    </c:if>

                                                    <c:if test="${canDelete}">
                                                        <form method="post" action="${baseUrl}" onsubmit="return confirm('Xóa mềm khách hàng này?')">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                            <input type="hidden" name="action" value="delete"/>
                                                            <input type="hidden" name="customerId" value="${cust.customerId}"/>
                                                            <button type="submit" title="Xóa">
                                                                <span class="material-symbols-outlined">delete</span>
                                                            </button>
                                                        </form>
                                                    </c:if>

                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <%-- ========================= Pagination ========================= --%>
                    <jsp:include page="/views/common/pagination.jsp">
                        <jsp:param name="baseUrl" value="${baseUrl}"/>
                        <jsp:param name="queryString" value="&keyword=${empty keyword ? '' : keyword}&branchId=${branchFilter == -1 ? '' : branchFilter}"/>
                    </jsp:include>
                </section>

            </main>
        </div>
    </div>

    <%-- ========================= MODAL: ADD / EDIT ========================= --%>
    <c:if test="${formMode == 'add' || formMode == 'edit'}">

        <c:set var="isEdit" value="${formMode == 'edit'}"/>
        <c:set var="formCust" value="${isEdit ? editingCustomer : null}"/>

        <div class="modal-overlay">
            <form class="modal-box" method="post" action="${baseUrl}">

                <input type="hidden"
                       name="csrfToken"
                       value="${sessionScope.csrfToken}">
                <input type="hidden"
                       name="action"
                       value="${isEdit ? 'update' : 'create'}"/>

                <c:if test="${isEdit}">
                    <input type="hidden"
                           name="customerId"
                           value="${formCust.customerId}"/>
                </c:if>

                <div class="modal-header">
                    <h3>${isEdit ? 'Chỉnh sửa khách hàng' : 'Thêm khách hàng'}</h3>

                    <a href="${baseUrl}" class="modal-close">
                        <span class="material-symbols-outlined">close</span>
                    </a>
                </div>

                <div class="modal-body">

                    <div class="form-row">
                        <div class="form-group">
                            <label>Họ tên *</label>
                            <input type="text"
                                   name="fullName"
                                   value="${isEdit ? formCust.fullName : ''}"
                                   placeholder="Nhập họ tên"
                                   required/>
                        </div>

                        <div class="form-group">
                            <label>Số điện thoại *</label>
                            <input type="text"
                                   name="phone"
                                   value="${isEdit ? formCust.phone : ''}"
                                   placeholder="Nhập số điện thoại"
                                   required/>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>Email</label>
                            <input type="email"
                                   name="email"
                                   value="${isEdit ? formCust.email : ''}"
                                   placeholder="Nhập email"/>
                        </div>

                        <div class="form-group">
                            <label>Giới tính</label>
                            <select name="gender">
                                <option value="">-- Chọn --</option>
                                <option value="Nam" ${isEdit && formCust.gender == 'Nam' ? 'selected' : ''}>Nam</option>
                                <option value="Nữ" ${isEdit && formCust.gender == 'Nữ' ? 'selected' : ''}>Nữ</option>
                                <option value="Khác" ${isEdit && formCust.gender == 'Khác' ? 'selected' : ''}>Khác</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>Ngày sinh</label>
                            <input type="date"
                                   name="dateOfBirth"
                                   value="${isEdit && formCust.dateOfBirth != null ? formCust.dateOfBirth : ''}"/>
                        </div>

                        <div class="form-group">
                            <label>Địa chỉ</label>
                            <input type="text"
                                   name="address"
                                   value="${isEdit ? formCust.address : ''}"
                                   placeholder="Nhập địa chỉ"/>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>Tổng chi tiêu</label>
                            <input type="number"
                                   name="totalSpent"
                                   value="${isEdit && formCust.totalSpent != null ? formCust.totalSpent : '0'}"
                                   min="0"
                                   step="1000"
                                   ${isAdmin ? '' : 'readonly'}/>
                        </div>
                    </div>

                    <c:if test="${isEdit}">
                        <div class="form-row">
                            <div class="form-group">
                                <label>Điểm hiện tại</label>
                                <input type="number"
                                       name="loyaltyPoint"
                                       value="${formCust.loyaltyPoint}"
                                       min="0"
                                       ${isAdmin ? '' : 'readonly'}/>
                            </div>
                            <div class="form-group">
                                <label>Điểm trọn đời</label>
                                <input type="number"
                                       name="lifetimePoints"
                                       value="${formCust.lifetimePoints}"
                                       min="0"
                                       ${isAdmin ? '' : 'readonly'}/>
                            </div>
                        </div>
                    </c:if>

                </div>

                <div class="modal-footer">
                    <a href="${baseUrl}" class="btn-secondary">Hủy</a>

                    <button type="submit" class="btn-primary">
                        ${isEdit ? 'Cập nhật khách hàng' : 'Thêm khách hàng'}
                    </button>
                </div>

            </form>
        </div>

    </c:if>

    <%-- ========================= MODAL: DETAIL ========================= --%>
    <c:if test="${formMode == 'detail' && not empty detailCustomer}">
        <div class="modal-overlay">
            <div class="modal-box">

                <div class="modal-header">
                    <h3>Chi tiết khách hàng</h3>

                    <a href="${baseUrl}" class="modal-close">
                        <span class="material-symbols-outlined">close</span>
                    </a>
                </div>

                <div class="modal-body detail-list">
                    <div class="form-row">
                        <div>
                            <p><strong>Họ tên:</strong> ${detailCustomer.fullName}</p>
                            <p><strong>Số điện thoại:</strong> ${empty detailCustomer.phone ? '—' : detailCustomer.phone}</p>
                            <p><strong>Email:</strong> ${empty detailCustomer.email ? '—' : detailCustomer.email}</p>
                            <p><strong>Giới tính:</strong> ${empty detailCustomer.gender ? '—' : detailCustomer.gender}</p>
                            <p><strong>Ngày sinh:</strong> ${empty detailCustomer.dateOfBirth ? '—' : detailCustomer.dateOfBirth}</p>
                        </div>
                        <div>
                            <p><strong>Địa chỉ:</strong> ${empty detailCustomer.address ? '—' : detailCustomer.address}</p>
                            <p><strong>Tổng chi tiêu:</strong>
                                <fmt:formatNumber value="${detailCustomer.totalSpent}" type="number" groupingUsed="true"/> ₫
                            </p>
                        </div>
                    </div>

                    <div class="form-row">
                        <p><strong>Điểm hiện tại:</strong> ${detailCustomer.loyaltyPoint}</p>
                        <p><strong>Điểm trọn đời:</strong> ${detailCustomer.lifetimePoints}</p>
                    </div>

                    <%-- Point Transaction History --%>
                    <div class="detail-section">
                        <h4>Lịch sử giao dịch điểm</h4>
                        <div style="max-height: 150px; overflow-y: auto; border: 1px solid var(--outline-variant); border-radius: var(--radius-md); margin-top: 8px;">
                            <table class="data-table" style="font-size: 12px; width: 100%; margin: 0;">
                                <thead>
                                    <tr>
                                        <th>Ngày</th>
                                        <th>Trước</th>
                                        <th>Sau</th>
                                        <th>Thay đổi</th>
                                        <th>Mô tả</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="tx" items="${detailCustomerTransactions}">
                                        <tr>
                                            <td><fmt:formatDate value="${tx.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                            <td>${tx.beforePoints}</td>
                                            <td>${tx.afterPoints}</td>
                                            <td>
                                                <span style="color: ${tx.afterPoints >= tx.beforePoints ? '#166534' : '#b91c1c'}; font-weight: bold;">
                                                    ${tx.afterPoints - tx.beforePoints >= 0 ? '+' : ''}${tx.afterPoints - tx.beforePoints}
                                                </span>
                                            </td>
                                            <td>${tx.description} ${empty tx.orderCode ? '' : fn:escapeXml(tx.orderCode)}</td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty detailCustomerTransactions}">
                                        <tr><td colspan="5" style="text-align: center; color: var(--secondary); padding: 12px;">Không có giao dịch</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <%-- Order History --%>
                    <div class="detail-section">
                        <h4>Lịch sử đơn hàng</h4>
                        <div style="max-height: 150px; overflow-y: auto; border: 1px solid var(--outline-variant); border-radius: var(--radius-md); margin-top: 8px;">
                            <table class="data-table" style="font-size: 12px; width: 100%; margin: 0;">
                                <thead>
                                    <tr>
                                        <th>Ngày</th>
                                        <th>Mã đơn</th>
                                        <th>Loại</th>
                                        <th>Tổng</th>
                                        <th>Trạng thái</th>
                                        <th>Chi nhánh</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="ord" items="${detailCustomerOrders}">
                                        <tr>
                                            <td><fmt:formatDate value="${ord.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                            <td>${ord.orderCode}</td>
                                            <td>${ord.orderType}</td>
                                            <td><fmt:formatNumber value="${ord.totalAmount}" type="number" groupingUsed="true"/> ₫</td>
                                            <td>${ord.status}</td>
                                            <td>${ord.branchName}</td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty detailCustomerOrders}">
                                        <tr><td colspan="6" style="text-align: center; color: var(--secondary); padding: 12px;">Không có đơn hàng</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <c:if test="${canRedeem}">
                        <%-- Sync Loyalty Section --%>
                        <div class="detail-section">
                            <h4>Đồng bộ điểm tích lũy</h4>
                            <form method="post" action="${baseUrl}" class="inline-form">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="action" value="sync-loyalty"/>
                                <input type="hidden" name="customerId" value="${detailCustomer.customerId}"/>
                                <button type="submit" class="btn-primary"
                                        onclick="return confirm('Đồng bộ điểm từ đơn hàng đã thanh toán?')">
                                    <span class="material-symbols-outlined">sync</span>
                                    Đồng bộ từ đơn hàng
                                </button>
                            </form>
                        </div>

                        <%-- Redeem Points Section --%>
                        <div class="detail-section">
                            <h4>Đổi điểm</h4>
                            <form method="post" action="${baseUrl}" class="inline-form">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="action" value="redeem-points"/>
                                <input type="hidden" name="customerId" value="${detailCustomer.customerId}"/>
                                <input type="number" name="redeemPoints" min="1" placeholder="Điểm" required/>
                                <button type="submit" class="btn-primary"
                                        onclick="return confirm('Đổi điểm này?')">
                                    <span class="material-symbols-outlined">redeem</span>
                                    Đổi điểm
                                </button>
                            </form>
                        </div>
                    </c:if>
                </div>

                <div class="modal-footer">
                    <a class="btn-secondary" href="${baseUrl}">Đóng</a>

                    <c:if test="${canEdit}">
                        <a class="btn-primary" href="${baseUrl}?action=edit&id=${detailCustomer.customerId}">
                            <span class="material-symbols-outlined">edit</span>
                            Chỉnh sửa khách hàng
                        </a>
                    </c:if>
                </div>

            </div>
        </div>
    </c:if>

    </body>
</html>
