<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<% request.setAttribute("pageTitle", "Inventory Dashboard"); %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Inventory Dashboard"/>
</jsp:include>
<jsp:include page="/views/common/sidebar.jsp" />

<div class="page-content">
    <section class="page-header">
        <div>
            <h2>Inventory Dashboard</h2>
            <p>Monitor stock levels and inventory movements</p>
        </div>
    </section>

    <c:if test="${not empty message}">
        <div class="alert alert-success">${message}</div>
    </c:if>

    <section class="overview-grid">
        <div class="overview-card">
            <div class="overview-icon overview-icon-revenue">
                <span class="material-symbols-outlined">inventory</span>
            </div>
            <div class="overview-info">
                <p>Total Products</p>
                <h3>${totalProducts}</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-warning">
                <span class="material-symbols-outlined">warning</span>
            </div>
            <div class="overview-info">
                <p>Low Stock Items</p>
                <h3>${lowStockCount}</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-orders">
                <span class="material-symbols-outlined">assignment</span>
            </div>
            <div class="overview-info">
                <p>Pending Transfers</p>
                <h3>${pendingTransfers}</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-users">
                <span class="material-symbols-outlined">warehouse</span>
            </div>
            <div class="overview-info">
                <p>Warehouses</p>
                <h3>${totalWarehouses}</h3>
            </div>
        </div>
    </section>

    <section class="table-card">
        <div class="table-scroll">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Product</th>
                        <th>Stock</th>
                        <th>Status</th>
                        <th class="text-right">Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty sampleData}">
                            <c:forEach var="item" items="${sampleData}">
                                <tr>
                                    <td>${item.id}</td>
                                    <td>${item.name}</td>
                                    <td>${item.stock}</td>
                                    <td><span class="status-badge active">In Stock</span></td>
                                    <td>
                                        <div class="table-actions">
                                            <a href="#" title="View"><span class="material-symbols-outlined">visibility</span></a>
                                            <a href="#" title="Edit"><span class="material-symbols-outlined">edit</span></a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="5" class="empty-row">
                                    <div class="empty-state">
                                        <span class="material-symbols-outlined">inventory_2</span>
                                        <h4>No inventory data</h4>
                                        <p>Products will appear here once added to the system.</p>
                                    </div>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </section>
</div>

<jsp:include page="/views/common/footer.jsp" />
