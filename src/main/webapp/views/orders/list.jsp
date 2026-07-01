<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Danh sách đơn hàng - FinoraRetail</title>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/base.css?v=20260528"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/layout.css?v=20260528"/>
</head>
<body>
<div class="app-layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-wrapper">
        <main class="page-content">
            <section class="page-header">
                <div>
                    <h2>Danh sách đơn hàng</h2>
                    <p>
                        <c:choose>
                            <c:when test="${isSalesStaffView}">Đơn hàng của bạn</c:when>
                            <c:otherwise>Đơn hàng hiển thị cho quản lý và quản trị viên</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </section>

            <section class="profile-card">
                <div class="card-header">
                    <h3>Đơn hàng</h3>
                </div>
                <p>Trang này đã có thể truy cập. Kết nối với DAO/Service để hiển thị dữ liệu đơn hàng thực tế.</p>
            </section>
        </main>
    </div>
</div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
