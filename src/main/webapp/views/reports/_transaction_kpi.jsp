<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:choose>
    <c:when test="${kpi.totalTransactions == 0}">
        <div class="empty-state" style="margin:40px 0;">
            <span class="material-symbols-outlined" style="font-size:48px;color:#94a3b8;">payments</span>
            <h4>Không tìm thấy dữ liệu giao dịch.</h4>
        </div>
    </c:when>
    <c:otherwise>
        <div class="kpi-grid">
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Tổng số giao dịch</p>
                    <h3><fmt:formatNumber value="${kpi.totalTransactions}"/></h3>
                </div>
                <div class="kpi-card-icon blue">
                    <span class="material-symbols-outlined">receipt_long</span>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Tổng doanh thu</p>
                    <h3><fmt:formatNumber value="${kpi.totalRevenue}" maxFractionDigits="0"/> ₫</h3>
                </div>
                <div class="kpi-card-icon green">
                    <span class="material-symbols-outlined">payments</span>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Tổng chi phí</p>
                    <h3><fmt:formatNumber value="${kpi.totalExpense}" maxFractionDigits="0"/> ₫</h3>
                </div>
                <div class="kpi-card-icon red">
                    <span class="material-symbols-outlined">money_off</span>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Dòng tiền ròng</p>
                    <h3><fmt:formatNumber value="${kpi.netCashFlow}" maxFractionDigits="0"/> ₫</h3>
                </div>
                <div class="kpi-card-icon ${kpi.netCashFlow >= 0 ? 'green' : 'red'}">
                    <span class="material-symbols-outlined">trending_up</span>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Giá trị giao dịch TB</p>
                    <h3><fmt:formatNumber value="${kpi.avgTransactionValue}" maxFractionDigits="0"/> ₫</h3>
                </div>
                <div class="kpi-card-icon blue">
                    <span class="material-symbols-outlined">bar_chart</span>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Tổng đơn hàng</p>
                    <h3><fmt:formatNumber value="${kpi.totalSalesOrders}"/></h3>
                </div>
                <div class="kpi-card-icon orange">
                    <span class="material-symbols-outlined">shopping_cart</span>
                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>
