<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:choose>
  <c:when test="${kpi.totalOrders == 0}">
    <div class="empty-state" style="margin:40px 0;">
      <span class="material-symbols-outlined" style="font-size:48px;color:#94a3b8;">bar_chart</span>
      <h4>Không có dữ liệu KPI</h4>
      <p>Không có dữ liệu KPI cho bộ lọc hiện tại.</p>
    </div>
  </c:when>
  <c:otherwise>
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-card-info">
          <p>Tổng đơn hàng</p>
          <h3><fmt:formatNumber value="${kpi.totalOrders}"/></h3>
          <span class="kpi-subtext">Tổng số đơn</span>
        </div>
        <div class="kpi-card-icon blue">
          <span class="material-symbols-outlined">receipt_long</span>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-card-info">
          <p>Tổng doanh thu</p>
          <h3><fmt:formatNumber value="${kpi.totalRevenue}" maxFractionDigits="0"/> ₫</h3>
          <span class="kpi-subtext">Đơn hoàn thành</span>
        </div>
        <div class="kpi-card-icon green">
          <span class="material-symbols-outlined">payments</span>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-card-info">
          <p>Giá trị TB (AOV)</p>
          <h3><fmt:formatNumber value="${kpi.aov}" maxFractionDigits="0"/> ₫</h3>
          <span class="kpi-subtext">Tổng doanh thu / Đơn hoàn thành</span>
        </div>
        <div class="kpi-card-icon blue">
          <span class="material-symbols-outlined">trending_up</span>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-card-info">
          <p>Đơn hoàn thành</p>
          <h3><fmt:formatNumber value="${kpi.completedOrders}"/></h3>
          <span class="kpi-subtext">
            <c:choose>
              <c:when test="${kpi.completionRate >= 70}"><span class="kpi-trend up">Tốt</span></c:when>
              <c:when test="${kpi.completionRate >= 40}"><span class="kpi-trend neutral">TB</span></c:when>
              <c:otherwise><span class="kpi-trend down">Thấp</span></c:otherwise>
            </c:choose>
          </span>
        </div>
        <div class="kpi-card-icon green">
          <span class="material-symbols-outlined">check_circle</span>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-card-info">
          <p>Đơn đã hủy</p>
          <h3><fmt:formatNumber value="${kpi.cancelledOrders}"/></h3>
          <span class="kpi-subtext">
            <c:choose>
              <c:when test="${kpi.cancelledOrders == 0}"><span class="kpi-trend up">Tốt</span></c:when>
              <c:when test="${kpi.cancelledOrders <= kpi.totalOrders * 0.1}"><span class="kpi-trend neutral">Chấp nhận</span></c:when>
              <c:otherwise><span class="kpi-trend down">Cao</span></c:otherwise>
            </c:choose>
          </span>
        </div>
        <div class="kpi-card-icon orange">
          <span class="material-symbols-outlined">cancel</span>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-card-info">
          <p>Tỉ lệ hoàn thành</p>
          <h3><fmt:formatNumber value="${kpi.completionRate}" maxFractionDigits="1"/>%</h3>
          <span class="kpi-subtext">
            <c:choose>
              <c:when test="${kpi.completionRate >= 70}"><span class="kpi-trend up">Tốt</span></c:when>
              <c:when test="${kpi.completionRate >= 40}"><span class="kpi-trend neutral">TB</span></c:when>
              <c:otherwise><span class="kpi-trend down">Thấp</span></c:otherwise>
            </c:choose>
          </span>
        </div>
        <div class="kpi-card-icon red">
          <span class="material-symbols-outlined">percent</span>
        </div>
      </div>
    </div>

    <c:if test="${not empty employeeKpi}">
      <div class="kpi-section">
        <div class="kpi-section-title">
          <span class="material-symbols-outlined">person</span>
          <span>KPI: ${employeeKpi.employeeName}</span>
        </div>
        <div class="kpi-grid" style="margin-bottom:0;">
          <div class="kpi-card kpi-card-sm">
            <div class="kpi-card-info">
              <p>Hoàn thành</p>
              <h3><fmt:formatNumber value="${employeeKpi.completedOrders}"/></h3>
            </div>
            <div class="kpi-card-icon green">
              <span class="material-symbols-outlined">check_circle</span>
            </div>
          </div>
          <div class="kpi-card kpi-card-sm">
            <div class="kpi-card-info">
              <p>Đã hủy</p>
              <h3><fmt:formatNumber value="${employeeKpi.cancelledOrders}"/></h3>
            </div>
            <div class="kpi-card-icon orange">
              <span class="material-symbols-outlined">cancel</span>
            </div>
          </div>
          <div class="kpi-card kpi-card-sm">
            <div class="kpi-card-info">
              <p>Doanh thu</p>
              <h3><fmt:formatNumber value="${employeeKpi.revenue}" maxFractionDigits="0"/> ₫</h3>
            </div>
            <div class="kpi-card-icon green">
              <span class="material-symbols-outlined">payments</span>
            </div>
          </div>
          <div class="kpi-card kpi-card-sm">
            <div class="kpi-card-info">
              <p>AOV</p>
              <h3><fmt:formatNumber value="${employeeKpi.aov}" maxFractionDigits="0"/> ₫</h3>
            </div>
            <div class="kpi-card-icon blue">
              <span class="material-symbols-outlined">trending_up</span>
            </div>
          </div>
          <div class="kpi-card kpi-card-sm">
            <div class="kpi-card-info">
              <p>Hoàn thành</p>
              <h3><fmt:formatNumber value="${employeeKpi.completionRate}" maxFractionDigits="1"/>%</h3>
            </div>
            <div class="kpi-card-icon red">
              <span class="material-symbols-outlined">percent</span>
            </div>
          </div>
        </div>
      </div>
    </c:if>

    <c:if test="${isOwner and not empty branchKpis}">
      <div class="kpi-section">
        <div class="kpi-section-title">
          <span class="material-symbols-outlined">store</span>
          <span>Doanh thu theo chi nhánh</span>
        </div>
        <div class="table-scroll">
          <table class="data-table kpi-branch-table">
            <thead>
              <tr>
                <th>Chi nhánh</th>
                <th class="text-right">Số đơn</th>
                <th class="text-right">Doanh thu</th>
                <th class="text-right">Tỉ lệ</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="bk" items="${branchKpis}">
                <tr>
                  <td><strong>${bk.branchName}</strong></td>
                  <td class="text-right"><fmt:formatNumber value="${bk.orders}"/></td>
                  <td class="text-right"><fmt:formatNumber value="${bk.revenue}" maxFractionDigits="0"/> ₫</td>
                  <td class="text-right"><fmt:formatNumber value="${bk.revenuePercent}" maxFractionDigits="1"/>%</td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </c:if>
  </c:otherwise>
</c:choose>
