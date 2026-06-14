<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Inventory Dashboard"); %>
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />
<main class="main">
    <div class="card">
        <h1>Inventory Dashboard</h1>
        <p>Đây là trang JSP mẫu cho chức năng <strong>Inventory Dashboard</strong>.</p>
        <% if (request.getAttribute("message") != null) { %>
            <div class="message"><%= request.getAttribute("message") %></div>
        <% } %>
    </div>

    <div class="card">
        <div style="margin-bottom:12px;display:flex;gap:8px;flex-wrap:wrap;">
            <input style="max-width:320px" placeholder="Search keyword...">
            <button class="btn">Search</button>
            <button class="btn secondary">Filter</button>
        </div>
        <table>
            <thead><tr><th>ID</th><th>Name</th><th>Status</th><th>Action</th></tr></thead>
            <tbody>
                <tr><td>1</td><td>Sample data</td><td>ACTIVE</td><td><a href="#">View</a> | <a href="#">Edit</a></td></tr>
            </tbody>
        </table>
    </div>

    <div class="grid">
        <div class="card"><h3>Total Sales</h3><p>0 VND</p></div>
        <div class="card"><h3>Total Orders</h3><p>0</p></div>
        <div class="card"><h3>Total Customers</h3><p>0</p></div>
    </div>

</main>
<jsp:include page="/views/common/footer.jsp" />
