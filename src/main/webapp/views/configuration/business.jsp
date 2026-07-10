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

            <div style="background: #fff; padding: 1.5rem; border-radius: 8px; border: 1px solid #e5e7eb; margin-top: 1.5rem;">
                <h3>Cài đặt điểm tích lũy</h3>
                <form method="post" action="${pageContext.request.contextPath}/configuration/business" style="margin-top: 1rem;">
                    <div style="margin-bottom: 1rem;">
                        <label style="display: block; font-weight: 600; margin-bottom: 0.25rem;">Số tiền trên mỗi điểm (VNĐ)</label>
                        <input type="number" name="amountPerPoint" class="form-control"
                               value="<fmt:formatNumber value="${loyaltySetting.amountPerPoint}" type="number" groupingUsed="false"/>"
                               min="1" step="1000" required/>
                        <small class="form-text text-muted">Số tiền chi tiêu (VNĐ) để nhận 1 điểm tích lũy. Mặc định: 100.000 VNĐ</small>
                    </div>
                    <button class="btn btn-danger" type="submit">Lưu</button>
                </form>
            </div>
        </div>

    </div>
</div>
<jsp:include page="/views/common/footer.jsp" />
