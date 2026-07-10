<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<style>
    .approval-card { border: none; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 24px; }
    .approval-header { background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%); color: white; border-radius: 12px 12px 0 0; padding: 16px 24px; font-weight: 600; }
    .table th { font-weight: 600; color: #475569; background-color: #f8fafc; border-bottom: 2px solid #e2e8f0; }
    .table td { vertical-align: middle; color: #334155; }
    .action-btn { display: inline-flex; align-items: center; justify-content: center; gap: 4px; padding: 6px 12px; border-radius: 6px; font-weight: 500; font-size: 13px; transition: all 0.2s; border: none; }
    .btn-approve { background-color: #ecfdf5; color: #059669; border: 1px solid #a7f3d0; }
    .btn-approve:hover { background-color: #d1fae5; transform: translateY(-1px); }
    .btn-reject { background-color: #fef2f2; color: #e11d48; border: 1px solid #fecdd3; }
    .btn-reject:hover { background-color: #ffe4e6; transform: translateY(-1px); }
</style>

<div class="dashboard-card">
    <div class="d-flex justify-content-between align-items-center mb-4 p-3">
        <div>
            <h4 class="mb-1 fw-bold text-dark" style="letter-spacing: -0.5px;">Xử Lý Phiếu Điều Chuyển</h4>
            <p class="text-muted mb-0 small">Phê duyệt hoặc từ chối các phiếu điều chuyển đang chờ</p>
        </div>
    </div>

    <!-- Điều Chuyển -->
    <div class="card approval-card mx-3 mb-4">
        <div class="approval-header d-flex justify-content-between align-items-center">
            <span><span class="material-icons" style="font-size: 18px; vertical-align: text-bottom; margin-right: 8px;">swap_horiz</span>Phiếu Điều Chuyển Đang Chờ Xuất</span>
            <span class="badge bg-light text-dark rounded-pill px-3">${pendingTransfers != null ? pendingTransfers.size() : 0} phiếu</span>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table mb-0">
                    <thead>
                        <tr>
                            <th class="ps-4">Mã Phiếu</th>
                            <th>Kho Xuất</th>
                            <th>Kho Nhập</th>
                            <th>Người Yêu Cầu</th>
                            <th>Ngày Tạo</th>
                            <th class="text-center" width="200px">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty pendingTransfers}">
                                <tr>
                                    <td colspan="6" class="text-center py-4 text-muted">Không có phiếu điều chuyển nào cần duyệt.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="item" items="${pendingTransfers}">
                                    <tr>
                                        <td class="ps-4 fw-medium">${item.transferCode}</td>
                                        <td>${item.fromWarehouseName}</td>
                                        <td>${item.toWarehouseName}</td>
                                        <td>${item.createdByName}</td>
                                        <td><fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${item.transferDate}" /></td>
                                        <td class="text-center">
                                            <form action="${pageContext.request.contextPath}/inventory" method="POST" class="d-inline">
                                                <input type="hidden" name="action" value="approveTransfer">
                                                <input type="hidden" name="transferId" value="${item.stockTransferId}">
                                                <button class="action-btn btn-approve" type="submit" onclick="return confirm('Duyệt phiếu điều chuyển này cho phép nhân viên thực hiện xuất kho trung chuyển.')">
                                                    <span class="material-icons" style="font-size: 14px;">check</span> Duyệt
                                                </button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/inventory" method="POST" class="d-inline ms-1">
                                                <input type="hidden" name="action" value="rejectTransfer">
                                                <input type="hidden" name="transferId" value="${item.stockTransferId}">
                                                <button class="action-btn btn-reject" type="submit" onclick="return confirm('Từ chối phiếu điều chuyển này?')">
                                                    <span class="material-icons" style="font-size: 14px;">close</span> Hủy
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
