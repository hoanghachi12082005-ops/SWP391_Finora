<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Order List - FinoraRetail</title>
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
                    <h2>Order List</h2>
                    <p>
                        <c:choose>
                            <c:when test="${isSalesStaffView}">Your sales orders</c:when>
                            <c:otherwise>Sales orders visible to managers and admins</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </section>

            <section class="profile-card">
                <div class="card-header">
                    <h3>Orders</h3>
                </div>
                <p>This placeholder page is now reachable for the active role. Connect it to your DAO/service later if you want real order data.</p>
            </section>
        </main>
    </div>
</div>
</body>
</html>
