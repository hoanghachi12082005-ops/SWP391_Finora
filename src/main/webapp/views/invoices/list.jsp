<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Quản lý hóa đơn"); %>
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />
<main class="main">
    <div class="card">
        <h1>Quản lý hóa đơn</h1>
        <p>Đây là trang JSP mẫu cho chức năng <strong>Quản lý hóa đơn</strong>.</p>
        <% if (request.getAttribute("message") != null) { %>
            <div class="message"><%= request.getAttribute("message") %></div>
        <% } %>
    </div>

    <div class="card">
        <div style="margin-bottom:12px;display:flex;gap:8px;flex-wrap:wrap;">
            <input style="max-width:320px" placeholder="Nhập từ khóa...">
            <button class="btn">Tìm kiếm</button>
            <button class="btn secondary">Lọc</button>
        </div>
        <table>
            <thead><tr><th>ID</th><th>Tên</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
                <tr><td>1</td><td>Dữ liệu mẫu</td><td>ACTIVE</td><td><a href="#">Xem</a> | <a href="#">Sửa</a></td></tr>
            </tbody>
        </table>
    </div>

</main>
<jsp:include page="/views/common/footer.jsp" />
