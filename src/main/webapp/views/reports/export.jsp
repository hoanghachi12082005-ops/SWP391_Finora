<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Export Report"); %>
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />
<main class="main">
    <div class="card">
        <h1>Export Report</h1>
        <p>Đây là trang JSP mẫu cho chức năng <strong>Export Report</strong>.</p>
        <% if (request.getAttribute("message") != null) { %>
            <div class="message"><%= request.getAttribute("message") %></div>
        <% } %>
    </div>

    <div class="card">
        <form method="post">
            <div class="form-row"><label>Name / Title</label><input name="name" placeholder="Nhập tên hoặc tiêu đề"></div>
            <div class="form-row"><label>Status</label><select name="status"><option>ACTIVE</option><option>INACTIVE</option><option>PENDING</option></select></div>
            <div class="form-row"><label>Description</label><textarea name="description" rows="4"></textarea></div>
            <button class="btn" type="submit">Save</button>
            <a class="btn secondary" href="javascript:history.back()">Back</a>
        </form>
    </div>

    <div class="grid">
        <div class="card"><h3>Total Sales</h3><p>0 VND</p></div>
        <div class="card"><h3>Total Orders</h3><p>0</p></div>
        <div class="card"><h3>Total Customers</h3><p>0</p></div>
    </div>

</main>
<jsp:include page="/views/common/footer.jsp" />
