<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<% request.setAttribute("pageTitle", "Tổng quan tài chính"); %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Tổng quan tài chính"/>
</jsp:include>
<jsp:include page="/views/common/sidebar.jsp" />

<div class="page-content">
    <section class="page-header">
        <div>
            <h2>Tổng quan tài chính</h2>
            <p>Tổng quan doanh thu, chi phí và lợi nhuận</p>
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
                <p>Tổng doanh thu</p>
                <h3><fmt:formatNumber value="${totalRevenue}" type="number" groupingUsed="true"/> ₫</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-warning">
                <span class="material-symbols-outlined">money_off</span>
            </div>
            <div class="overview-info">
                <p>Tổng chi phí</p>
                <h3><fmt:formatNumber value="${totalExpenses}" type="number" groupingUsed="true"/> ₫</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-revenue">
                <span class="material-symbols-outlined">trending_up</span>
            </div>
            <div class="overview-info">
                <p>Lợi nhuận ròng</p>
                <h3><fmt:formatNumber value="${netProfit}" type="number" groupingUsed="true"/> ₫</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-orders">
                <span class="material-symbols-outlined">receipt_long</span>
            </div>
            <div class="overview-info">
                <p>Tổng hóa đơn</p>
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
                        <th>Mô tả</th>
                        <th>Số tiền</th>
                        <th>Trạng thái</th>
                        <th class="text-right">Thao tác</th>
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
                                    <td><span class="status-badge active">Hoàn thành</span></td>
                                    <td>
                                        <div class="table-actions">
                                            <a href="#" title="Xem"><span class="material-symbols-outlined">visibility</span></a>
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
                                        <h4>Không có dữ liệu tài chính</h4>
                                        <p>Dữ liệu tài chính sẽ xuất hiện sau khi có giao dịch.</p>
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
