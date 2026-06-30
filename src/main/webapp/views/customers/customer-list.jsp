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

        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/base.css?v=20260528"/>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/layout.css?v=20260528"/>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/assets/css/form-modal.css?v=20260528"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer-management.css?v=2">

        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    </head>

    <body class="customer-page">
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
                            <div class="overview-icon">
                                <span class="material-symbols-outlined">groups</span>
                            </div>
                            <div class="overview-info">
                                <p>Total Customers</p>
                                <h3>${customerOverview.totalCustomers}</h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon">
                                <span class="material-symbols-outlined">person_add</span>
                            </div>
                            <div class="overview-info">
                                <p>New This Month</p>
                                <h3>${customerOverview.newCustomersThisMonth}</h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon">
                                <span class="material-symbols-outlined">payments</span>
                            </div>
                            <div class="overview-info">
                                <p>Total Spent</p>
                                <h3>
                                    <fmt:formatNumber value="${customerOverview.totalSpent}" type="number" groupingUsed="true"/> ₫
                                </h3>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon">
                                <span class="material-symbols-outlined">emoji_events</span>
                            </div>
                            <div class="overview-info">
                                <p>Top Current Points</p>
                                <h3>${empty customerOverview.topCustomerName ? '—' : customerOverview.topCustomerName}</h3>
                                <small>
                                    Points: ${customerOverview.topCustomerPoints}
                                </small>
                            </div>
                        </div>

                        <div class="overview-card">
                            <div class="overview-icon">
                                <span class="material-symbols-outlined">bar_chart</span>
                            </div>
                            <div class="overview-info">
                                <p>Filtered Results</p>
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
                            <label>Search</label>
                            <input name="keyword"
                                   value="${keyword}"
                                   type="text"
                                   placeholder="Name, email or phone..."/>
                        </div>

                        <c:if test="${not empty branches}">
                            <div class="form-group">
                                <label>Branch</label>
                                <select name="branchId">
                                    <option value="">All Branches</option>
                                    <c:forEach var="br" items="${branches}">
                                        <option value="${br.branchID}" ${branchFilter == br.branchID ? 'selected' : ''}>
                                            ${br.name}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </c:if>

                        <div class="form-group">
                            <label>Page Size</label>
                            <select name="pageSize">
                                <option value="5" ${empty pageSizeOption || pageSizeOption == '5' ? 'selected' : ''}>
                                    5 records/page
                                </option>

                                <option value="10" ${pageSizeOption == '10' ? 'selected' : ''}>
                                    10 records/page
                                </option>

                                <option value="30p" ${pageSizeOption == '30p' || pageSizeOption == '30%' || pageSizeOption == '30' ? 'selected' : ''}>
                                    30% records/page
                                </option>

                                <option value="50p" ${pageSizeOption == '50p' || pageSizeOption == '50%' || pageSizeOption == '50' ? 'selected' : ''}>
                                    50% records/page
                                </option>
                            </select>
                        </div>

                        <div class="filter-actions">
                            <button class="btn-primary" type="submit">Apply</button>
                            <a class="btn-secondary" href="${baseUrl}">Reset</a>
                        </div>

                    </div>
                </form>

                <%-- ========================= Data Table ========================= --%>
                <section class="table-card">
                    <div class="table-scroll">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Customer</th>
                                <th>Phone</th>
                                <th>Points</th>
                                <th class="text-right">Actions</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:choose>
                                <c:when test="${empty customers}">
                                    <tr>
                                        <td colspan="4" class="empty-row">
                                            No customers found.
                                        </td>
                                    </tr>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach var="cust" items="${customers}">
                                        <tr>
                                            <td>
                                                <div class="customer-cell">
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

                                                    <a href="${baseUrl}?action=detail&id=${cust.customerId}" title="View Detail">
                                                        <span class="material-symbols-outlined">visibility</span>
                                                    </a>

                                                    <c:if test="${canEdit}">
                                                        <a href="${baseUrl}?action=edit&id=${cust.customerId}" title="Edit">
                                                            <span class="material-symbols-outlined">edit</span>
                                                        </a>
                                                    </c:if>

                                                    <c:if test="${sessionScope.currentUser.roleName == 'Admin' || sessionScope.currentUser.roleName == 'Owner' || sessionScope.currentUser.roleName == 'Store Manager' || sessionScope.currentUser.roleName == 'StoreManager' || sessionScope.currentUser.roleName == 'Manager'}">
                                                        <form method="post" action="${baseUrl}" style="display:inline;" onsubmit="return confirm('Soft delete this customer?')">
                                                            <input type="hidden" name="action" value="delete"/>
                                                            <input type="hidden" name="customerId" value="${cust.customerId}"/>
                                                            <button type="submit" style="background:none; border:none; color:var(--secondary); cursor:pointer; padding:0; display:inline-flex; align-items:center; vertical-align: middle;">
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

                    <%-- ========================= Table Footer & Pagination ========================= --%>
                    <div class="table-footer">
                        <div>
                            Showing ${empty customers ? 0 : fn:length(customers)} of ${empty totalCustomers ? 0 : totalCustomers} entries

                            <c:if test="${not empty pageSize}">
                                <span class="page-size-note">
                                    / ${pageSize} per page
                                </span>
                            </c:if>
                        </div>

                        <c:if test="${totalPages > 1}">
                            <div class="pagination">

                                <c:url var="prevUrl" value="/customers">
                                    <c:param name="page" value="${currentPage - 1}"/>
                                    <c:param name="keyword" value="${keyword}"/>
                                    <c:param name="branchId" value="${branchFilter == -1 ? '' : branchFilter}"/>
                                    <c:param name="pageSize" value="${pageSizeOption}"/>
                                </c:url>

                                <c:choose>
                                    <c:when test="${currentPage > 1}">
                                        <a class="page-btn" href="${prevUrl}">Previous</a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="page-btn disabled">Previous</span>
                                    </c:otherwise>
                                </c:choose>

                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <c:url var="pageUrl" value="/customers">
                                        <c:param name="page" value="${i}"/>
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="branchId" value="${branchFilter == -1 ? '' : branchFilter}"/>
                                        <c:param name="pageSize" value="${pageSizeOption}"/>
                                    </c:url>

                                    <c:choose>
                                        <c:when test="${i == currentPage}">
                                            <span class="page-btn active">${i}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <a class="page-btn" href="${pageUrl}">${i}</a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>

                                <c:url var="nextUrl" value="/customers">
                                    <c:param name="page" value="${currentPage + 1}"/>
                                    <c:param name="keyword" value="${keyword}"/>
                                    <c:param name="branchId" value="${branchFilter == -1 ? '' : branchFilter}"/>
                                    <c:param name="pageSize" value="${pageSizeOption}"/>
                                </c:url>

                                <c:choose>
                                    <c:when test="${currentPage < totalPages}">
                                        <a class="page-btn" href="${nextUrl}">Next</a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="page-btn disabled">Next</span>
                                    </c:otherwise>
                                </c:choose>

                            </div>
                        </c:if>
                    </div>
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
                       name="action"
                       value="${isEdit ? 'update' : 'create'}"/>

                <c:if test="${isEdit}">
                    <input type="hidden"
                           name="customerId"
                           value="${formCust.customerId}"/>
                </c:if>

                <div class="modal-header">
                    <h3>${isEdit ? 'Edit Customer' : 'Add Customer'}</h3>

                    <a href="${baseUrl}" class="modal-close">
                        <span class="material-symbols-outlined">close</span>
                    </a>
                </div>

                <div class="modal-body">

                    <div class="form-row">
                        <div class="form-group">
                            <label>Full Name *</label>
                            <input type="text"
                                   name="fullName"
                                   value="${isEdit ? formCust.fullName : ''}"
                                   placeholder="Enter full name"
                                   required/>
                        </div>

                        <div class="form-group">
                            <label>Phone *</label>
                            <input type="text"
                                   name="phone"
                                   value="${isEdit ? formCust.phone : ''}"
                                   placeholder="Enter phone number"
                                   required/>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>Email</label>
                            <input type="email"
                                   name="email"
                                   value="${isEdit ? formCust.email : ''}"
                                   placeholder="Enter email"/>
                        </div>

                        <div class="form-group">
                            <label>Gender</label>
                            <select name="gender">
                                <option value="">-- Select --</option>
                                <option value="Nam" ${isEdit && formCust.gender == 'Nam' ? 'selected' : ''}>Nam</option>
                                <option value="Nữ" ${isEdit && formCust.gender == 'Nữ' ? 'selected' : ''}>Nữ</option>
                                <option value="Khác" ${isEdit && formCust.gender == 'Khác' ? 'selected' : ''}>Khác</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>Date of Birth</label>
                            <input type="date"
                                   name="dateOfBirth"
                                   value="${isEdit && formCust.dateOfBirth != null ? formCust.dateOfBirth : ''}"/>
                        </div>

                        <div class="form-group">
                            <label>Address</label>
                            <input type="text"
                                   name="address"
                                   value="${isEdit ? formCust.address : ''}"
                                   placeholder="Enter address"/>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>Total Spent</label>
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
                                <label>Current Point</label>
                                <input type="number"
                                       name="loyaltyPoint"
                                       value="${formCust.loyaltyPoint}"
                                       min="0"
                                       ${isAdmin ? '' : 'readonly'}/>
                            </div>
                            <div class="form-group">
                                <label>Lifetime Point</label>
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
                    <a href="${baseUrl}" class="btn-secondary">Cancel</a>

                    <button type="submit" class="btn-primary">
                        ${isEdit ? 'Update Customer' : 'Add Customer'}
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
                    <h3>Customer Detail</h3>

                    <a href="${baseUrl}" class="modal-close">
                        <span class="material-symbols-outlined">close</span>
                    </a>
                </div>

                <div class="modal-body detail-list">
                    <div class="form-row">
                        <div>
                            <p><strong>Full Name:</strong> ${detailCustomer.fullName}</p>
                            <p><strong>Phone:</strong> ${empty detailCustomer.phone ? '—' : detailCustomer.phone}</p>
                            <p><strong>Email:</strong> ${empty detailCustomer.email ? '—' : detailCustomer.email}</p>
                            <p><strong>Gender:</strong> ${empty detailCustomer.gender ? '—' : detailCustomer.gender}</p>
                            <p><strong>Date of Birth:</strong> ${empty detailCustomer.dateOfBirth ? '—' : detailCustomer.dateOfBirth}</p>
                        </div>
                        <div>
                            <p><strong>Address:</strong> ${empty detailCustomer.address ? '—' : detailCustomer.address}</p>
                            <p><strong>Total Spent:</strong>
                                <fmt:formatNumber value="${detailCustomer.totalSpent}" type="number" groupingUsed="true"/> ₫
                            </p>
                        </div>
                    </div>

                    <div class="form-row">
                        <p><strong>Current Points:</strong> ${detailCustomer.loyaltyPoint}</p>
                        <p><strong>Lifetime Points:</strong> ${detailCustomer.lifetimePoints}</p>
                    </div>

                    <%-- Point Transaction History --%>
                    <div class="detail-section">
                        <h4>Point Transaction History</h4>
                        <div style="max-height: 150px; overflow-y: auto; border: 1px solid var(--outline-variant); border-radius: var(--radius-md); margin-top: 8px;">
                            <table class="data-table" style="font-size: 12px; width: 100%; margin: 0;">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Before</th>
                                        <th>After</th>
                                        <th>Change</th>
                                        <th>Description</th>
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
                                        <tr><td colspan="5" style="text-align: center; color: var(--secondary); padding: 12px;">No transactions</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <%-- Order History --%>
                    <div class="detail-section">
                        <h4>Order History</h4>
                        <div style="max-height: 150px; overflow-y: auto; border: 1px solid var(--outline-variant); border-radius: var(--radius-md); margin-top: 8px;">
                            <table class="data-table" style="font-size: 12px; width: 100%; margin: 0;">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Order Code</th>
                                        <th>Type</th>
                                        <th>Total</th>
                                        <th>Status</th>
                                        <th>Branch</th>
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
                                        <tr><td colspan="6" style="text-align: center; color: var(--secondary); padding: 12px;">No orders</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <c:if test="${isAdmin}">
                        <%-- Sync Loyalty Section --%>
                        <div class="detail-section">
                            <h4>Sync Loyalty Points</h4>
                            <form method="post" action="${baseUrl}" class="inline-form">
                                <input type="hidden" name="action" value="sync-loyalty"/>
                                <input type="hidden" name="customerId" value="${detailCustomer.customerId}"/>
                                <button type="submit" class="btn-primary"
                                        onclick="return confirm('Sync loyalty points from paid orders?')">
                                    <span class="material-symbols-outlined">sync</span>
                                    Sync from Orders
                                </button>
                            </form>
                        </div>

                        <%-- Redeem Points Section --%>
                        <div class="detail-section">
                            <h4>Redeem Points</h4>
                            <form method="post" action="${baseUrl}" class="inline-form">
                                <input type="hidden" name="action" value="redeem-points"/>
                                <input type="hidden" name="customerId" value="${detailCustomer.customerId}"/>
                                <input type="number" name="redeemPoints" min="1" placeholder="Points" required/>
                                <button type="submit" class="btn-primary"
                                        onclick="return confirm('Redeem these points?')">
                                    <span class="material-symbols-outlined">redeem</span>
                                    Redeem
                                </button>
                            </form>
                        </div>
                    </c:if>
                </div>

                <div class="modal-footer">
                    <a class="btn-secondary" href="${baseUrl}">Close</a>

                    <c:if test="${canEdit}">
                        <a class="btn-primary" href="${baseUrl}?action=edit&id=${detailCustomer.customerId}">
                            <span class="material-symbols-outlined">edit</span>
                            Edit Customer
                        </a>
                    </c:if>
                </div>

            </div>
        </div>
    </c:if>

    </body>
</html>
