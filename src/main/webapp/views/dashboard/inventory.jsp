<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<% request.setAttribute("pageTitle", "Tổng quan kho hàng"); %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Tổng quan kho hàng"/>
</jsp:include>
<jsp:include page="/views/common/sidebar.jsp" />

<div class="page-content">
    <section class="page-header">
        <div>
            <h2>Tổng quan kho hàng</h2>
            <p>Theo dõi mức tồn kho và biến động hàng hóa</p>
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
                <p>Tổng sản phẩm</p>
                <h3>${totalProducts}</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-warning">
                <span class="material-symbols-outlined">warning</span>
            </div>
            <div class="overview-info">
                <p>Hàng sắp hết</p>
                <h3>${lowStockCount}</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-orders">
                <span class="material-symbols-outlined">assignment</span>
            </div>
            <div class="overview-info">
                <p>Chuyển kho đang chờ</p>
                <h3>${pendingTransfers}</h3>
            </div>
        </div>

        <div class="overview-card">
            <div class="overview-icon overview-icon-users">
                <span class="material-symbols-outlined">warehouse</span>
            </div>
            <div class="overview-info">
                <p>Kho hàng</p>
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
                        <th>Sản phẩm</th>
                        <th>Tồn kho</th>
                        <th>Trạng thái</th>
                        <th class="text-right">Thao tác</th>
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
                                    <td><span class="status-badge active">Còn hàng</span></td>
                                    <td>
                                        <div class="table-actions">
                                            <a href="#" title="Xem"><span class="material-symbols-outlined">visibility</span></a>
                                            <a href="#" title="Sửa"><span class="material-symbols-outlined">edit</span></a>
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
                                        <h4>Không có dữ liệu kho</h4>
                                        <p>Sản phẩm sẽ xuất hiện sau khi được thêm vào hệ thống.</p>
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
