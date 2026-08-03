<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<form class="filter-card" method="get" action="${baseUrl}">
    <input type="hidden" name="page" value="1"/>
    <input type="hidden" name="sizeValue" value="${sizeValue}"/>

    <div class="filter-grid">
        <div class="form-group">
            <label>Khoảng thời gian</label>
            <select name="datePreset" id="datePreset" onchange="toggleDateRange()">
                <option value="">Tùy chọn</option>
                <option value="today" ${datePreset == 'today' ? 'selected' : ''}>Hôm nay</option>
                <option value="yesterday" ${datePreset == 'yesterday' ? 'selected' : ''}>Hôm qua</option>
                <option value="this_week" ${datePreset == 'this_week' ? 'selected' : ''}>Tuần này</option>
                <option value="this_month" ${datePreset == 'this_month' ? 'selected' : ''}>Tháng này</option>
            </select>
        </div>

        <div class="form-group">
            <label>Từ ngày</label>
            <input type="date" name="dateFrom" id="dateFrom" value="${filter.dateFrom}"/>
        </div>

        <div class="form-group">
            <label>Đến ngày</label>
            <input type="date" name="dateTo" id="dateTo" value="${filter.dateTo}"/>
        </div>

        <div class="form-group">
            <label>Mã giao dịch / Hóa đơn</label>
            <input name="transactionCode" value="${filter.transactionCode}" type="text" placeholder="Tìm mã..."/>
        </div>

        <div class="form-group">
            <label>Loại đơn hàng</label>
            <select name="orderType">
                <option value="">Tất cả loại đơn</option>
                <option value="SALE" ${filter.orderType == 'SALE' ? 'selected' : ''}>Bán hàng</option>
                <option value="PURCHASE" ${filter.orderType == 'PURCHASE' ? 'selected' : ''}>Nhập hàng</option>
                <option value="OTHER" ${filter.orderType == 'OTHER' ? 'selected' : ''}>Thu/Chi khác</option>
            </select>
        </div>

        <div class="form-group">
            <label>Loại phiếu</label>
            <select name="transactionType">
                <option value="">Tất cả loại</option>
                <option value="INCOME" ${filter.transactionType == 'INCOME' ? 'selected' : ''}>Thu</option>
                <option value="EXPENSE" ${filter.transactionType == 'EXPENSE' ? 'selected' : ''}>Chi</option>
                <c:forEach var="t" items="${transactionTypes}">
                    <c:if test="${t != 'INCOME' && t != 'EXPENSE'}">
                        <option value="${t}" ${filter.transactionType == t ? 'selected' : ''}>${t}</option>
                    </c:if>
                </c:forEach>
            </select>
        </div>

        <div class="form-group">
            <label>Phương thức thanh toán</label>
            <select name="paymentMethod">
                <option value="">Tất cả</option>
                <option value="CASH" ${filter.paymentMethod == 'CASH' ? 'selected' : ''}>Tiền mặt</option>
                <option value="BANK_TRANSFER" ${filter.paymentMethod == 'BANK_TRANSFER' ? 'selected' : ''}>Chuyển khoản</option>
            </select>
        </div>

        <div class="form-group">
            <label>Số tiền từ</label>
            <input type="number" name="amountFrom" value="${filter.amountFrom}" placeholder="Thấp nhất..." step="0.01"/>
        </div>

        <div class="form-group">
            <label>Số tiền đến</label>
            <input type="number" name="amountTo" value="${filter.amountTo}" placeholder="Cao nhất..." step="0.01"/>
        </div>

        <c:choose>
            <c:when test="${not empty managerBranchId}">
                <input type="hidden" name="branchId" value="${managerBranchId}"/>
            </c:when>
            <c:otherwise>
                <div class="form-group">
                    <label>Chi nhánh</label>
                    <select name="branchId">
                        <option value="">Tất cả chi nhánh</option>
                        <c:forEach var="branch" items="${branches}">
                            <option value="${branch.branchID}" ${filter.branchId == branch.branchID ? 'selected' : ''}>${branch.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="form-group">
            <label>Nhân viên</label>
            <select name="empId">
                <option value="">Tất cả nhân viên</option>
                <c:forEach var="emp" items="${employees}">
                    <option value="${emp.employeeID}" ${filter.empId == emp.employeeID ? 'selected' : ''}>${emp.fullName}</option>
                </c:forEach>
            </select>
        </div>

        <div class="form-group filter-search">
            <label>Từ khóa</label>
            <input name="keyword" value="${filter.keyword}" type="text" placeholder="Mã, mô tả, nhân viên..."/>
        </div>

        <div class="form-group">
            <label>Sắp xếp</label>
            <select name="sortBy">
                <option value="payment_date" ${empty filter.sortBy || filter.sortBy == 'payment_date' ? 'selected' : ''}>Ngày</option>
                <option value="payment_amount" ${filter.sortBy == 'payment_amount' ? 'selected' : ''}>Số tiền</option>
                <option value="PaymentType" ${filter.sortBy == 'PaymentType' ? 'selected' : ''}>Loại giao dịch</option>
                <option value="branch_name" ${filter.sortBy == 'branch_name' ? 'selected' : ''}>Chi nhánh</option>
                <option value="employee_name" ${filter.sortBy == 'employee_name' ? 'selected' : ''}>Nhân viên</option>
            </select>
            <select name="sortDir" style="margin-top:4px;">
                <option value="DESC" ${empty filter.sortDir || filter.sortDir == 'DESC' ? 'selected' : ''}>Mới nhất / Cao nhất</option>
                <option value="ASC" ${filter.sortDir == 'ASC' ? 'selected' : ''}>Cũ nhất / Thấp nhất</option>
            </select>
        </div>

        <div class="filter-actions" style="align-self:flex-end;">
            <button class="btn-primary" type="submit">Áp dụng</button>
            <a class="btn-secondary" href="${baseUrl}">Đặt lại</a>
        </div>
    </div>
</form>

<script>
function toggleDateRange() {
    var preset = document.getElementById('datePreset').value;
    var dateFrom = document.getElementById('dateFrom');
    var dateTo = document.getElementById('dateTo');
    if (preset) {
        dateFrom.disabled = true;
        dateTo.disabled = true;
    } else {
        dateFrom.disabled = false;
        dateTo.disabled = false;
    }
}
toggleDateRange();
</script>
