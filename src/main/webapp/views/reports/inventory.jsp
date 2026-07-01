<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Báo cáo tồn kho"); %>
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />
<main class="main">
    <div class="card">
        <h1>Báo cáo tồn kho</h1>
        <p>Đây là trang JSP mẫu cho chức năng <strong>Báo cáo tồn kho</strong>.</p>
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

    <div class="grid">
        <div class="card"><h3>Tổng doanh thu</h3><p>0 VND</p></div>
        <div class="card"><h3>Tổng đơn hàng</h3><p>0</p></div>
        <div class="card"><h3>Tổng khách hàng</h3><p>0</p></div>
    </div>

</main>
<jsp:include page="/views/common/footer.jsp" />
