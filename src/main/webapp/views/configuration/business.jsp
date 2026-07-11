<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    request.setAttribute("pageTitle", "Cấu hình kinh doanh");
    String successMessage = (String) session.getAttribute("successMessage");
    String errorMessage = (String) session.getAttribute("errorMessage");
    session.removeAttribute("successMessage");
    session.removeAttribute("errorMessage");
    if (successMessage != null) request.setAttribute("_success", successMessage);
    if (errorMessage != null) request.setAttribute("_error", errorMessage);
%>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Cấu hình kinh doanh"/>
</jsp:include>
<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />
    <div class="main-content">

        <c:if test="${not empty _success}"><div class="alert alert-success">${_success}</div></c:if>
        <c:if test="${not empty _error}"><div class="alert alert-error">${_error}</div></c:if>

        <div class="p-4">
            <h1>Cấu hình kinh doanh</h1>
            <p>Cấu hình các thiết lập hệ thống</p>

            <!-- Cấu hình đổi điểm ra tiền (dùng POINT_CONFIG) -->
            <div style="background: #fff; padding: 1.5rem; border-radius: 8px; border: 1px solid #e5e7eb; margin-top: 1.5rem;">
                <h3>Cài đặt quy đổi điểm tích lũy</h3>
                <c:if test="${pointConfig == null}">
                    <p class="text-danger">Chưa có cấu hình POINT_CONFIG. Hãy thêm dòng dữ liệu POINT_CONFIG vào bảng voucher.</p>
                </c:if>
                <form method="post" action="${pageContext.request.contextPath}/configuration/business" style="margin-top: 1rem;">
                    <div style="margin-bottom: 1rem;">
                        <label style="display: block; font-weight: 600; margin-bottom: 0.25rem;">1 điểm = ? VNĐ</label>
                        <input type="number" name="pointValue" class="form-control"
                               value="<fmt:formatNumber value="${pointConfig.discountValue}" type="number" groupingUsed="false" maxFractionDigits="2"/>"
                               min="0" step="0.01" required/>
                        <small class="form-text text-muted">Tỉ lệ quy đổi: 1 điểm khách hàng tương ứng với bao nhiêu VNĐ. Mặc định: 1.00</small>
                    </div>
                    <button class="btn btn-danger" type="submit">Lưu</button>
                </form>
            </div>

            <!-- Cấu hình VAT -->
            <div style="background: #fff; padding: 1.5rem; border-radius: 8px; border: 1px solid #e5e7eb; margin-top: 1.5rem;">
                <h3>Cấu hình VAT chung</h3>
                <form method="post" action="${pageContext.request.contextPath}/configuration/business" style="margin-top: 1rem;">
                    <div style="margin-bottom: 1rem;">
                        <label style="display: block; font-weight: 600; margin-bottom: 0.25rem;">Tỷ lệ VAT (%)</label>
                        <input type="number" name="vatPercentage" class="form-control"
                               value="<fmt:formatNumber value="${vatSetting.vatPercentage}" type="number" groupingUsed="false" maxFractionDigits="2"/>"
                               min="0" max="100" step="0.1" required/>
                        <small class="form-text text-muted">Phần trăm thuế VAT áp dụng cho tất cả đơn hàng bán tại POS. Mặc định: 8%</small>
                    </div>
                    <button class="btn btn-danger" type="submit">Lưu</button>
                </form>
            </div>
        </div>

    </div>
</div>
<jsp:include page="/views/common/footer.jsp" />
