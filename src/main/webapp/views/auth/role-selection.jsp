<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Chọn vai trò"/>
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/role-selection.css">

<div class="role-page">
    <div class="container">

        <div class="welcome-section">
            <h2>
                Xin chào, ${sessionScope.currentUser.fullName}
            </h2>

            <p>
                Vui lòng chọn vai trò bạn muốn sử dụng trong hệ thống
            </p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger text-center">
                ${error}
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty employeeRoles}">
                <div class="empty-role-box">
                    <span class="material-icons">admin_panel_settings</span>

                    <h5>Chưa có vai trò</h5>

                    <p>
                        Tài khoản của bạn chưa được phân quyền trong bảng EmployeeRole.
                        Vui lòng liên hệ quản lý để được cấp quyền.
                    </p>
                </div>
            </c:when>

            <c:otherwise>
                <div class="row g-4">

                    <c:forEach var="role" items="${employeeRoles}">
                        <div class="col-lg-6 col-md-6">
                            <form method="post"
                                  action="${pageContext.request.contextPath}/role-selection">

                                <input type="hidden" name="roleId" value="${role.roleId}">

                                <button type="submit" class="module-card">

                                    <c:choose>

                                        <c:when test="${role.roleId == 1}">
                                            <div class="icon-wrapper system">
                                                <span class="material-icons">admin_panel_settings</span>
                                            </div>

                                            <h5>Admin</h5>

                                            <p>
                                                Vào trang quản trị hệ thống, cấu hình dữ liệu và quản lý quyền tổng.
                                            </p>
                                        </c:when>

                                        <c:when test="${role.roleId == 2}">
                                            <div class="icon-wrapper system">
                                                <span class="material-icons">business_center</span>
                                            </div>

                                            <h5>Owner</h5>

                                            <p>
                                                Vào dashboard tổng quan để quản lý toàn bộ cửa hàng,
                                                nhân viên, báo cáo và cấu hình kinh doanh.
                                            </p>
                                        </c:when>

                                        <c:when test="${role.roleId == 3}">
                                            <div class="icon-wrapper management">
                                                <span class="material-icons">store</span>
                                            </div>

                                            <h5>Store Manager</h5>

                                            <p>
                                                Vào dashboard quản lý chi nhánh, nhân viên,
                                                sản phẩm và hoạt động bán hàng.
                                            </p>
                                        </c:when>

                                        <c:when test="${role.roleId == 4}">
                                            <div class="icon-wrapper pos">
                                                <span class="material-icons">point_of_sale</span>
                                            </div>

                                            <h5>Sales Staff</h5>

                                            <p>
                                                Vào trang POS để tạo đơn hàng, thanh toán
                                                và xử lý bán hàng trực tiếp.
                                            </p>
                                        </c:when>

                                        <c:when test="${role.roleId == 5}">
                                            <div class="icon-wrapper warehouse">
                                                <span class="material-icons">inventory_2</span>
                                            </div>

                                            <h5>Warehouse Staff</h5>

                                            <p>
                                                Vào dashboard kho để quản lý nhập hàng,
                                                xuất hàng, tồn kho và điều chuyển sản phẩm.
                                            </p>
                                        </c:when>

                                        <c:otherwise>
                                            <div class="icon-wrapper report">
                                                <span class="material-icons">person</span>
                                            </div>

                                            <h5>${role.roleName}</h5>

                                            <p>
                                                Chọn vai trò này để tiếp tục sử dụng hệ thống.
                                            </p>
                                        </c:otherwise>

                                    </c:choose>

                                </button>
                            </form>
                        </div>
                    </c:forEach>

                </div>
            </c:otherwise>
        </c:choose>

        <div class="logout-section">
            <a href="${pageContext.request.contextPath}/logout" class="logout-btn">
                <span class="material-icons">logout</span>
                Đăng xuất
            </a>
        </div>

    </div>
</div>

<jsp:include page="../common/footer.jsp"/>