<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<header class="app-header">
    <a class="brand" href="${pageContext.request.contextPath}/dashboard" aria-label="Finora dashboard"><span class="brand-mark">F</span><span><strong>Finora</strong><small>Project Foundation</small></span></a>
    <nav class="top-nav" aria-label="Primary navigation"><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a><a href="${pageContext.request.contextPath}/product-management">Products</a><a href="${pageContext.request.contextPath}/sales-management">Sales</a><a href="${pageContext.request.contextPath}/financial-management">Finance</a></nav>
    <jsp:include page="/WEB-INF/views/common/role-selector.jsp" />
</header>
