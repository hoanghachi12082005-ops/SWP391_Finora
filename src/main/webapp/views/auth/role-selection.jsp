<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="role" value="${sessionScope.currentUser.roleName}" />

<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="Chọn chức năng"/>
</jsp:include>

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/role-selection.css">

<div class="role-page">
    <div class="container">
        <div class="welcome-section">
            <h2>
                Xin chào, ${sessionScope.currentUser.fullName}
            </h2>


            <p>
                Vui lòng chọn vai trò để tiếp tục
            </p>
        </div>

        <div class="row g-4">

            <!-- POS -->
            <c:if test="${role == 'Admin'
                          || role == 'Owner'
                          || role == 'StoreManager'
                          || role == 'SalesStaff'}">
                  <div class="col-lg-6 col-md-6">
                      <form method="post" action="${pageContext.request.contextPath}/role-selection">
                          <input type="hidden" name="role" value="pos">

                          <button type="submit" class="module-card">

                              <div class="icon-wrapper pos">
                                  <span class="material-icons">
                                      point_of_sale
                                  </span>
                              </div>

                              <h5>Bán hàng</h5>

                              <p>
                                  Xử lý đơn hàng và thanh toán nhanh chóng.
                              </p>

                          </button>
                      </form>
                  </div>
            </c:if>

            <!-- Management -->
            <c:if test="${role == 'Admin'
                          || role == 'Owner'
                          || role == 'StoreManager'
                          || role == 'WarehouseStaff'}">
                  <div class="col-lg-6 col-md-6">
                      <form method="post"
                            action="${pageContext.request.contextPath}/role-selection">

                          <input type="hidden" name="role" value="management">

                          <button type="submit" class="module-card">
                              <div class="icon-wrapper management">
                                  <span class="material-icons">
                                      inventory_2
                                  </span>
                              </div>

                              <h5>Quản lý</h5>

                              <p>
                                  Quản lý sản phẩm, khách hàng và nhân viên.
                              </p>

                          </button>
                      </form>
                  </div> 
            </c:if>

            <!-- Report -->
            <c:if test="${role == 'Admin'
                          || role == 'Owner'
                          || role == 'StoreManager'
                          || role == 'SalesStaff'
                          || role == 'WarehouseStaff'}">
                  <div class="col-lg-6 col-md-6">
                      <form method="post" action="${pageContext.request.contextPath}/role-selection">
                          <input type="hidden" name="role" value="report">
                          <button type="submit" class="module-card">
                              <div class="icon-wrapper report">
                                  <span class="material-icons">
                                      bar_chart
                                  </span>
                              </div>

                              <h5>Thống kê</h5>

                              <p>
                                  Theo dõi doanh thu và hiệu quả kinh doanh.
                              </p>

                          </button>
                      </form>
                  </div>
            </c:if>

            <!-- System -->
            <c:if test="${role == 'Admin'
                          || role == 'Owner'}">
                  <div class="col-lg-6 col-md-6">
                      <form method="post" action="${pageContext.request.contextPath}/role-selection">
                          <input type="hidden" name="role" value="system">
                          <button type="submit" class="module-card">
                              <div class="icon-wrapper system">
                                  <span class="material-icons">
                                      settings
                                  </span>
                              </div>

                              <h5>Cấu hình</h5>

                              <p>
                                  Quản lý hệ thống và phân quyền tài khoản.
                              </p>
                          </button>
                      </form>
                  </div>
            </c:if>
        </div>

        <div class="logout-section">
            <a href="${pageContext.request.contextPath}/logout" class="logout-btn">
                <span class="material-icons">logout</span>
                Đăng xuất
            </a>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>