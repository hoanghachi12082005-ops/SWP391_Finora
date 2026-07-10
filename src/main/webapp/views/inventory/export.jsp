<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Xuất kho"); %>
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />
<main class="main">
    <div class="card">
        <h1>Xuất kho</h1>
        <p>Đây là trang JSP mẫu cho chức năng <strong>Xuất kho</strong>.</p>
        <% if (request.getAttribute("message") != null) { %>
            <div class="message"><%= request.getAttribute("message") %></div>
        <% } %>
    </div>

    <div class="card">
        <form method="post">
            <div class="form-row"><label>Tên / Tiêu đề</label><input name="name" placeholder="Nhập tên hoặc tiêu đề"></div>
            <div class="form-row"><label>Trạng thái</label><select name="status"><option>ACTIVE</option><option>INACTIVE</option><option>PENDING</option></select></div>
            <div class="form-row"><label>Mô tả</label><textarea name="description" rows="4"></textarea></div>
            <button class="btn" type="submit">Lưu</button>
            <a class="btn secondary" href="javascript:history.back()">Quay lại</a>
        </form>
    </div>

</main>
<jsp:include page="/views/common/footer.jsp" />
