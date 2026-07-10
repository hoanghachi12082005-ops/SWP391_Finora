<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Xuất báo cáo"); %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Xuất báo cáo"/>
</jsp:include>
<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />
    <div class="main-content">

        <div class="p-4">
            <h1>Xuất báo cáo</h1>
            <p>Đây là trang JSP mẫu cho chức năng <strong>Xuất báo cáo</strong>.</p>
            <% if (request.getAttribute("message") != null) { %>
                <div class="message"><%= request.getAttribute("message") %></div>
            <% } %>

            <form method="post" style="margin-top: 1rem; background: #fff; padding: 1.5rem; border-radius: 8px; border: 1px solid #e5e7eb;">
                <div style="margin-bottom: 1rem;"><label style="display: block; font-weight: 600; margin-bottom: 0.25rem;">Tên / Tiêu đề</label><input name="name" class="form-control" placeholder="Nhập tên hoặc tiêu đề"></div>
                <div style="margin-bottom: 1rem;"><label style="display: block; font-weight: 600; margin-bottom: 0.25rem;">Trạng thái</label><select name="status" class="form-control"><option>ACTIVE</option><option>INACTIVE</option><option>PENDING</option></select></div>
                <div style="margin-bottom: 1rem;"><label style="display: block; font-weight: 600; margin-bottom: 0.25rem;">Mô tả</label><textarea name="description" class="form-control" rows="4"></textarea></div>
                <button class="btn btn-danger" type="submit">Lưu</button>
                <a class="btn btn-secondary" href="javascript:history.back()">Quay lại</a>
            </form>

            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; margin-top: 1.5rem;">
                <div style="background: #fff; padding: 1.5rem; border-radius: 8px; border: 1px solid #e5e7eb;"><h3>Tổng doanh thu</h3><p>0 VND</p></div>
                <div style="background: #fff; padding: 1.5rem; border-radius: 8px; border: 1px solid #e5e7eb;"><h3>Tổng đơn hàng</h3><p>0</p></div>
                <div style="background: #fff; padding: 1.5rem; border-radius: 8px; border: 1px solid #e5e7eb;"><h3>Tổng khách hàng</h3><p>0</p></div>
            </div>
        </div>

    </div>
</div>
<jsp:include page="/views/common/footer.jsp" />
