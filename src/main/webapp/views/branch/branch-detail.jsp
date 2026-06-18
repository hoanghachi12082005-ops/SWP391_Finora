<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<!DOCTYPE html>
<html>
<head>

    <meta charset="UTF-8">
    
    <title>Chi tiết chi nhánh</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/common.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/branch.css">

</head>

<body>

<jsp:include page="sidebar.jsp"/>

<div class="main-content">

    <jsp:include page="header.jsp"/>

    <c:if test="${param.success == 'add'}">
        <div style="background-color: #dcfce7; color: #15803d; padding: 15px; margin-bottom: 20px; border-radius: 8px; font-weight: 600;">
            ✅ Thêm chi nhánh thành công!
        </div>
    </c:if>
    <c:if test="${param.success == 'edit'}">
        <div style="background-color: #dcfce7; color: #15803d; padding: 15px; margin-bottom: 20px; border-radius: 8px; font-weight: 600;">
            ✅ Cập nhật chi nhánh thành công!
        </div>
    </c:if>

    <div class="page-header">

        <div class="page-title">

            <h1>Chi tiết chi nhánh</h1>

            <p>
                Thông tin tổng quan chi nhánh
            </p>

        </div>

        <div style="display: flex; gap: 10px;">

            <a href="branch"
               class="btn-cancel">
                Quay lại
            </a>

            <a href="branch?action=edit&id=${branch.branchId}"
               class="btn-add">
                Chỉnh sửa
            </a>

        </div>

    </div>

    <!-- THÔNG TIN CHUNG -->

    <div class="detail-card">

        <div class="detail-header">

            <div>

                <h2>${branch.branchName}</h2>

                <span class="branch-code">

                    ${branch.branchCode}

                </span>

            </div>

            <div>
                <c:set var="branchStatus" value="${fn:toLowerCase(branch.status)}"/>
                <span class="${branchStatus == 'active'
                              ? 'status-active'
                              : 'status-inactive'}">
                    ${branchStatus == 'active' ? 'Active' : 'Inactive'}
                </span>
            </div>
        </div>

        <div class="detail-grid">
            <div>
                <label>Mã chi nhánh</label>
                <p>${empty branch.branchCode ? '—' : branch.branchCode}</p>
            </div>

            <div>
                <label>Điện thoại</label>
                <p>${empty branch.phone ? '—' : branch.phone}</p>
            </div>

            <div>
                <label>Email</label>
                <p>${empty branch.email ? '—' : branch.email}</p>
            </div>
                
            <div>
                <label>Địa chỉ</label>
                <p>${empty branch.address ? '—' : branch.address}</p>
            </div>
            <div>
                <label>Giờ mở cửa</label>
                <p>${empty branch.openingTime ? '—' : branch.openingTime}</p>
            </div>

            <div>
                <label>Giờ đóng cửa</label>
                <p>${empty branch.closingTime ? '—' : branch.closingTime}</p>
            </div>

            <div>
                <label>Quản lý</label>
                <p>${empty branch.managerName ? 'Chưa phân công' : branch.managerName}</p>
            </div>

            <div>
                <label>Tổng nhân viên</label>
                <p>${employeeCount}</p>
            </div>
        </div>
    </div>

    <!-- THỐNG KÊ -->
    <div class="stats">
        <div class="stat-card">
            <h3 class="stat-label">DOANH THU THÁNG</h3>
            <p class="stat-value">${monthlyRevenue}</p>
        </div>

        <div class="stat-card">
            <h3 class="stat-label">TỔNG NHÂN VIÊN</h3>
            <p class="stat-value">${employeeCount}</p>
        </div>

        <div class="stat-card">
            <h3 class="stat-label">TỔNG ĐƠN HÀNG</h3>
            <p class="stat-value">${orderCount}</p>
        </div>

        <div class="stat-card">
            <h3 class="stat-label">LỢI NHUẬN</h3>
            <p class="stat-value">${profit}</p>
        </div>
    </div>
            
    <!-- DANH SÁCH NHÂN VIÊN -->
    <div class="table-card">
        <div class="table-header">
            <h3>Danh sách nhân viên</h3>
        </div>
        <table class="branch-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Họ tên</th>
                    <th>Chức vụ</th>
                    <th>SĐT</th>
                    <th>Trạng thái</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty employeeList}">
                        <tr>
                            <td colspan="5" style="text-align:center;color:#777;padding:24px;">
                                Chưa có nhân viên tại chi nhánh này
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${employeeList}" var="emp">
                            <tr>
                                <td>${emp.empId}</td>
                                <td>${emp.fullName}</td>
                                <td>—</td>
                                <td>${empty emp.phone ? '—' : emp.phone}</td>
                                <td>
                                    <c:set var="empStatus" value="${fn:toLowerCase(emp.status)}"/>
                                    <span class="${empStatus == 'active'
                                                  ? 'status-active'
                                                  : 'status-inactive'}">
                                        ${empStatus == 'active' ? 'Active' : 'Inactive'}
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>