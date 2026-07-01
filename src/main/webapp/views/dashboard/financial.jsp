<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<% request.setAttribute("pageTitle", "Financial Dashboard"); %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Financial Dashboard"/>
</jsp:include>
<jsp:include page="/views/common/sidebar.jsp" />

<div class="page-content">
    <section class="page-header">
        <div>
            <h2>Financial Dashboard</h2>
            <p>Revenue, expenses and profit overview</p>
        </div>
    </section>

    <c:if test="${not empty message}">
        <div class="alert alert-success">${message}</div>
    </c:if>

    <section class="overview-grid">
        <div class="overview-card">
            <div class="overview-icon overview-icon-revenue">
                <span class="material-symbols-outlined">payments</span>
            </div>
            <div class="overview-info">
                <p>Total Revenue</p>
                <h3><fmt:formatNumber value="${totalRevenue}" type="number" groupingUsed="true"/> ₫</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-warning">
                <span class="material-symbols-outlined">money_off</span>
            </div>
            <div class="overview-info">
                <p>Total Expenses</p>
                <h3><fmt:formatNumber value="${totalExpenses}" type="number" groupingUsed="true"/> ₫</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-revenue">
                <span class="material-symbols-outlined">trending_up</span>
            </div>
            <div class="overview-info">
                <p>Net Profit</p>
                <h3><fmt:formatNumber value="${netProfit}" type="number" groupingUsed="true"/> ₫</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-orders">
                <span class="material-symbols-outlined">receipt_long</span>
            </div>
            <div class="overview-info">
                <p>Total Invoices</p>
                <h3>${totalInvoices}</h3>
            </div>
        </div>
    </section>

    <section class="table-card">
        <div class="table-scroll">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Description</th>
                        <th>Amount</th>
                        <th>Status</th>
                        <th class="text-right">Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty sampleData}">
                            <c:forEach var="item" items="${sampleData}">
                                <tr>
                                    <td>${item.id}</td>
                                    <td>${item.description}</td>
                                    <td><fmt:formatNumber value="${item.amount}" type="number" groupingUsed="true"/> ₫</td>
                                    <td><span class="status-badge active">Completed</span></td>
                                    <td>
                                        <div class="table-actions">
                                            <a href="#" title="View"><span class="material-symbols-outlined">visibility</span></a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="5" class="empty-row">
                                    <div class="empty-state">
                                        <span class="material-symbols-outlined">account_balance</span>
                                        <h4>No financial data</h4>
                                        <p>Financial records will appear here once transactions are recorded.</p>
                                    </div>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </section>
</div>

<jsp:include page="/views/common/footer.jsp" />
