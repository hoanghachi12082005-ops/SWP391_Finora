<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    request.setAttribute("pageTitle", "Business Configuration");
    String successMessage = (String) session.getAttribute("successMessage");
    String errorMessage = (String) session.getAttribute("errorMessage");
    session.removeAttribute("successMessage");
    session.removeAttribute("errorMessage");
    if (successMessage != null) request.setAttribute("_success", successMessage);
    if (errorMessage != null) request.setAttribute("_error", errorMessage);
%>
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />
<main class="main">
    <c:if test="${not empty _success}"><div class="alert alert-success">${_success}</div></c:if>
    <c:if test="${not empty _error}"><div class="alert alert-error">${_error}</div></c:if>

    <div class="card">
        <h1>Business Configuration</h1>
        <p>Configure system-wide business settings</p>
    </div>

    <div class="card">
        <h3>Loyalty Point Settings</h3>
        <form method="post" action="${pageContext.request.contextPath}/configuration/business">
            <div class="form-row">
                <label>Amount per Point (VND)</label>
                <input type="number" name="amountPerPoint"
                       value="<fmt:formatNumber value="${loyaltySetting.amountPerPoint}" type="number" groupingUsed="false"/>"
                       min="1" step="1000" required/>
                <small>How much spending (in VND) earns 1 loyalty point. Default: 100,000 VND</small>
            </div>
            <button class="btn" type="submit">Save</button>
        </form>
    </div>
</main>
<jsp:include page="/views/common/footer.jsp" />
