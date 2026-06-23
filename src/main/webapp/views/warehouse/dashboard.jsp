<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Warehouse - Quản Lý Kho"); %>
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />
<main class="main">
    <div class="card">
        <h1>Warehouse Dashboard — Quản Lý Kho</h1>
        <p>Đây là trang JSP cho chức năng <strong>Warehouse Management</strong>.</p>
        <% if (request.getAttribute("message") != null) { %>
            <div class="message"><%= request.getAttribute("message") %></div>
        <% } %>
        <p style="color:#888;">Tính năng đang được phát triển. Vui lòng kết nối Service/DAO.</p>
    </div>
</main>
<jsp:include page="/views/common/footer.jsp" />
