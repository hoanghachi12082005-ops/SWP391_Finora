<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-3">
        <h5>Lịch Sử Xuất Nhập Kho</h5>
        
        <form action="" method="GET" class="d-flex gap-2">
            <input type="hidden" name="tab" value="history">
            <c:if test="${not empty warehouses && sessionScope.currentUser.roleName == 'Admin'}">
                <select name="warehouseId" class="form-select" onchange="this.form.submit()">
                    <option value="">Tất cả kho</option>
                    <c:forEach var="w" items="${warehouses}">
                        <option value="${w.warehouseId}" ${selectedWarehouseId == w.warehouseId ? 'selected' : ''}>${w.warehouseName}</option>
                    </c:forEach>
                </select>
            </c:if>
            <select name="typeFilter" class="form-select" onchange="this.form.submit()">
                <option value="">Tất cả loại</option>
                <option value="IN" ${typeFilter == 'IN' ? 'selected' : ''}>Nhập kho (IN)</option>
                <option value="OUT" ${typeFilter == 'OUT' ? 'selected' : ''}>Xuất kho (OUT)</option>
            </select>
            <select name="dateFilter" class="form-select" onchange="this.form.submit()">
                <option value="">Toàn thời gian</option>
                <option value="today" ${dateFilter == 'today' ? 'selected' : ''}>Hôm nay</option>
            </select>
            <button type="submit" class="btn btn-primary">Lọc</button>
        </form>
    </div>

    <div class="activity-feed-container" style="max-height: 600px; overflow-y: auto;">
        <c:choose>
            <c:when test="${empty history}">
                <p class="text-center text-muted p-4">Không có giao dịch xuất nhập kho nào.</p>
            </c:when>
            <c:otherwise>
                <div class="premium-table-container">
                    <table class="premium-table">
                        <thead>
                            <tr>
                                <th>Thời Gian</th>
                                <th>Sản Phẩm</th>
                                <th>Kho</th>
                                <th>Loại</th>
                                <th>Nguồn</th>
                                <th>Trước</th>
                                <th>Thay Đổi</th>
                                <th>Sau</th>
                                <th>Người Thực Hiện</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="tx" items="${history}">
                                <tr>
                                    <td>${tx.createdAt}</td>
                                    <td>
                                        <strong>${tx.productName}</strong><br>
                                        <small class="text-muted">${tx.productCodebar}</small>
                                    </td>
                                    <td>${tx.warehouseName}</td>
                                    <td>
                                        <c:if test="${tx.transactionType == 'IN'}">
                                            <span class="badge bg-success">NHẬP</span>
                                        </c:if>
                                        <c:if test="${tx.transactionType == 'OUT'}">
                                            <span class="badge bg-danger">XUẤT</span>
                                        </c:if>
                                    </td>
                                    <td>${tx.referenceType} <c:if test="${not empty tx.referenceId}">#${tx.referenceId}</c:if></td>
                                    <td>${tx.beforeQuantity}</td>
                                    <td>
                                        <span style="font-weight:bold; color: ${tx.transactionType == 'IN' ? 'green' : 'red'};">
                                            ${tx.transactionType == 'IN' ? '+' : '-'}${tx.quantity}
                                        </span>
                                    </td>
                                    <td>${tx.afterQuantity}</td>
                                    <td>${tx.createdByName}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
