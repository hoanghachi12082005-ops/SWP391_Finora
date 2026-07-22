<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    model.Employee currentUser = (model.Employee) session.getAttribute("currentUser");
    if (currentUser == null) {
        currentUser = (model.Employee) session.getAttribute("employee");
    }
    
    // CHỈ HIỂN THỊ BANNER KHI NGƯỜI DÙNG ĐÃ ĐĂNG NHẬP VÀO HỆ THỐNG
    if (currentUser != null) {
        Integer selectedWId = (Integer) session.getAttribute("selectedWarehouseId");
        Integer branchId = currentUser.getBranchId();

        dao.inventory.InventoryCheckDAO checkDAO = new dao.inventory.InventoryCheckDAO();
        model.InventoryCheck activeCheck = checkDAO.getAnyActiveCheckInfo(selectedWId, branchId);
        if (activeCheck != null) {
            request.setAttribute("activeInventoryCheck", activeCheck);
        }
    }
%>

<c:if test="${not empty activeInventoryCheck}">
    <c:set var="userRole" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : sessionScope.employee.roleName}" />
    <c:set var="isManagerOrOwner" value="${userRole == 'Owner' || userRole == 'StoreManager' || userRole == 'Admin'}" />
    
    <div id="inventoryLockBanner" style="
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 99999;
        background: linear-gradient(90deg, #b91c1c 0%, #dc2626 100%);
        color: #ffffff;
        padding: 10px 16px;
        text-align: center;
        font-weight: 600;
        font-size: 14px;
        box-shadow: 0 4px 14px rgba(185, 28, 28, 0.4);
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12px;
    ">
        <span class="material-icons" style="font-size: 20px;">warning</span>
        <span>
            <c:choose>
                <c:when test="${isManagerOrOwner}">
                    Chi nhánh [${activeInventoryCheck.warehouseName}] đang trong quá trình kiểm kho bởi nhân viên <strong><c:out value="${activeInventoryCheck.createdByName}"/></strong> (Mã: ${activeInventoryCheck.checkCode}), yêu cầu dừng mọi hoạt động.
                </c:when>
                <c:otherwise>
                    Chi nhánh đang trong quá trình kiểm kho, yêu cầu dừng mọi hoạt động.
                </c:otherwise>
            </c:choose>
        </span>
        
        <c:if test="${isManagerOrOwner && activeInventoryCheck.status == 'IN_PROGRESS'}">
            <form action="${pageContext.request.contextPath}/inventory-check" method="POST" style="margin: 0; display: inline;">
                <input type="hidden" name="action" value="forceCancelCheck">
                <input type="hidden" name="warehouseId" value="${activeInventoryCheck.warehouseId}">
                <button type="submit" class="btn btn-sm btn-light text-danger fw-bold" style="border-radius: 6px; padding: 2px 10px; font-size: 12px;" onclick="return confirm('Xác nhận giải phóng kho khẩn cấp?')">
                    Mở Khóa Khẩn Cấp
                </button>
            </form>
        </c:if>
    </div>
    <style>
        body { padding-top: 42px !important; }
        .topbar { top: 42px !important; }
        .main-wrapper { margin-top: 42px !important; }
    </style>
</c:if>
