<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Xử Lý Phiếu"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />
    
    <div class="main-content">
        <jsp:include page="/views/common/topbar.jsp" />
        
        <div class="page-container">

<link href="${pageContext.request.contextPath}/assets/css/inventory/inventory-approval.css" rel="stylesheet">

<div class="approval-page">
    <div class="approval-page-header mb-4">
        <h4><span class="material-icons" style="font-size: 28px; vertical-align: text-bottom; margin-right: 8px; color: var(--primary-color);">fact_check</span>Xử Lý Phiếu</h4>
        <p>Phê duyệt hoặc từ chối các phiếu điều chuyển đang chờ xử lý</p>
    </div>

    <c:if test="${not empty sessionScope.message}">
        <div class="alert alert-success alert-dismissible fade show" role="alert" style="border-radius: 10px; border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
            <span class="material-icons" style="font-size: 18px; vertical-align: text-bottom; margin-right: 6px;">check_circle</span>
            ${sessionScope.message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <c:remove var="message" scope="session" />
    </c:if>

    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert" style="border-radius: 10px; border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
            <span class="material-icons" style="font-size: 18px; vertical-align: text-bottom; margin-right: 6px;">error</span>
            ${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <c:remove var="error" scope="session" />
    </c:if>

    <!-- Phiếu Điều Chuyển -->
    <div class="card approval-card">
        <div class="approval-card-header d-flex justify-content-between align-items-center">
            <span><span class="material-icons" style="font-size: 18px; vertical-align: text-bottom; margin-right: 8px;">swap_horiz</span>Phiếu Điều Chuyển Chờ Duyệt</span>
            <span class="badge bg-light text-dark rounded-pill px-3">${pendingTransfers != null ? pendingTransfers.size() : 0} phiếu</span>
        </div>
        <div class="card-body p-0">
            <c:choose>
                <c:when test="${empty pendingTransfers}">
                    <div class="empty-state">
                        <span class="material-icons">task_alt</span>
                        <h5>Không có phiếu nào cần duyệt</h5>
                        <p>Tất cả phiếu điều chuyển đã được xử lý.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table approval-table mb-0">
                            <thead>
                                <tr>
                                    <th class="ps-4">Mã Phiếu</th>
                                    <th>Kho Xuất</th>
                                    <th>Kho Nhập</th>
                                    <th>Người Yêu Cầu</th>
                                    <th>Ngày Tạo</th>
                                    <th class="text-center" style="width: 220px;">Thao Tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${pendingTransfers}">
                                    <tr>
                                        <td class="ps-4"><strong style="color: var(--primary-color);">${item.transferCode}</strong></td>
                                        <td>${item.fromWarehouseName}</td>
                                        <td>${item.toWarehouseName}</td>
                                        <td>${item.createdByName}</td>
                                        <td><fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${item.transferDate}" /></td>
                                        <td class="text-center">
                                            <form action="${pageContext.request.contextPath}/approval" method="POST" class="d-inline">
                                                <input type="hidden" name="action" value="approveTransfer">
                                                <input type="hidden" name="transferId" value="${item.stockTransferId}">
                                                <button class="action-btn btn-approve" type="submit" onclick="return confirm('Duyệt phiếu điều chuyển này?')">
                                                    <span class="material-icons" style="font-size: 16px;">check_circle</span> Duyệt
                                                </button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/approval" method="POST" class="d-inline ms-2">
                                                <input type="hidden" name="action" value="rejectTransfer">
                                                <input type="hidden" name="transferId" value="${item.stockTransferId}">
                                                <button class="action-btn btn-reject" type="submit" onclick="return confirm('Từ chối phiếu điều chuyển này?')">
                                                    <span class="material-icons" style="font-size: 16px;">cancel</span> Từ chối
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
        </div>
    </div>
</div>

<jsp:include page="/views/common/footer.jsp" />
