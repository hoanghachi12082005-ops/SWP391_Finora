<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/views/common/header.jsp">
    <jsp:param name="title" value="Xử Lý Phiếu"/>
</jsp:include>

<div class="app-container">
    <jsp:include page="/views/common/sidebar.jsp" />
    
    <div class="main-content">
        <jsp:include page="/views/common/topbar.jsp" />
        
        <div class="page-container">

<style>
    .approval-page { padding: 24px 32px; max-width: 1400px; margin: 0 auto; }
    .approval-page-header h4 { font-weight: 700; color: #111827; letter-spacing: -0.5px; margin-bottom: 4px; }
    .approval-page-header p { color: #6b7280; font-size: 14px; margin-bottom: 0; }
    .approval-card { border: none; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 24px; overflow: hidden; }
    .approval-card-header { background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%); color: white; padding: 16px 24px; font-weight: 600; font-size: 15px; }
    .approval-card-header .badge { font-size: 13px; }
    .approval-table th { font-weight: 600; color: #475569; background-color: #f8fafc; border-bottom: 2px solid #e2e8f0; font-size: 13px; text-transform: uppercase; letter-spacing: 0.3px; }
    .approval-table td { vertical-align: middle; color: #334155; font-size: 14px; }
    .approval-table tbody tr:hover { background-color: #f1f5f9; }
    .action-btn { display: inline-flex; align-items: center; justify-content: center; gap: 4px; padding: 7px 14px; border-radius: 8px; font-weight: 500; font-size: 13px; transition: all 0.2s; border: none; cursor: pointer; }
    .btn-approve { background-color: var(--success-bg); color: var(--success-text); border: 1px solid rgba(16, 185, 129, 0.15); }
    .btn-approve:hover { background-color: var(--success-bg); opacity: 0.85; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(16,185,129,0.15); }
    .btn-reject { background-color: var(--danger-bg); color: var(--danger-text); border: 1px solid rgba(239, 68, 68, 0.15); }
    .btn-reject:hover { background-color: var(--danger-bg); opacity: 0.85; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(239,68,68,0.15); }
    .empty-state { padding: 48px 24px; text-align: center; }
    .empty-state .material-icons { font-size: 56px; color: #cbd5e1; margin-bottom: 12px; }
    .empty-state h5 { color: #64748b; font-weight: 600; }
    .empty-state p { color: #94a3b8; font-size: 14px; }
</style>

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
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                <input type="hidden" name="action" value="approveTransfer">
                                                <input type="hidden" name="transferId" value="${item.stockTransferId}">
                                                <button class="action-btn btn-approve" type="submit" onclick="return confirm('Duyệt phiếu điều chuyển này?')">
                                                    <span class="material-icons" style="font-size: 16px;">check_circle</span> Duyệt
                                                </button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/approval" method="POST" class="d-inline ms-2">
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
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
