<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<form class="filter-card" method="get" action="${baseUrl}">
    <input type="hidden" name="page" value="1"/>
    <input type="hidden" name="sizeValue" value="${sizeValue}"/>
    <input type="hidden" name="tab" value="${empty activeTab ? 'orders' : activeTab}"/>

    <div class="filter-grid">
        <div class="form-group">
            <label>Khoảng thời gian</label>
            <select name="datePreset" id="datePreset" onchange="toggleDateRange()">
                <option value="">Tùy chọn</option>
                <option value="today" ${datePreset == 'today' ? 'selected' : ''}>Hôm nay</option>
                <option value="yesterday" ${datePreset == 'yesterday' ? 'selected' : ''}>Hôm qua</option>
                <option value="7days" ${datePreset == '7days' ? 'selected' : ''}>7 ngày</option>
                <option value="30days" ${datePreset == '30days' ? 'selected' : ''}>30 ngày</option>
                <option value="this_month" ${datePreset == 'this_month' ? 'selected' : ''}>Tháng này</option>
                <option value="last_month" ${datePreset == 'last_month' ? 'selected' : ''}>Tháng trước</option>
                <option value="this_year" ${datePreset == 'this_year' ? 'selected' : ''}>Năm nay</option>
                <option value="1year" ${datePreset == '1year' ? 'selected' : ''}>1 năm</option>
            </select>
        </div>

        <div class="form-group">
            <label>Từ ngày</label>
            <input type="text" name="dateFrom" id="dateFrom" value="${filter.dateFrom}" placeholder="dd/MM/yyyy" maxlength="10"/>
        </div>

        <div class="form-group">
            <label>Đến ngày</label>
            <input type="text" name="dateTo" id="dateTo" value="${filter.dateTo}" placeholder="dd/MM/yyyy" maxlength="10"/>
        </div>

        <div class="form-group">
            <label>Nhân viên</label>
            <select name="empId">
                        <option value="">Tất cả nhân viên</option>
                <c:forEach var="emp" items="${employees}">
                    <option value="${emp.employeeID}" ${filter.empId == emp.employeeID ? 'selected' : ''}>${emp.fullName}</option>
                </c:forEach>
            </select>
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
            <label>Mã đơn hàng</label>
            <input type="number" name="orderId" value="${filter.orderId}" placeholder="Nhập mã đơn..."/>
        </div>

        <div class="form-group">
            <label>Trạng thái đơn</label>
            <select name="orderStatus">
                <option value="">Tất cả</option>
                <option value="PENDING" ${filter.orderStatus == 'PENDING' ? 'selected' : ''}>Chờ thanh toán</option>
                <option value="PAID" ${filter.orderStatus == 'PAID' ? 'selected' : ''}>Đã thanh toán</option>
                <option value="COMPLETED" ${filter.orderStatus == 'COMPLETED' ? 'selected' : ''}>Hoàn thành</option>
                <option value="CANCELLED" ${filter.orderStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
            </select>
        </div>

        <div class="form-group">
            <label>Phương thức thanh toán</label>
            <select name="paymentMethod">
                <option value="">Tất cả</option>
                <option value="CASH" ${filter.paymentMethod == 'CASH' ? 'selected' : ''}>Tiền mặt</option>
                <option value="CARD" ${filter.paymentMethod == 'CARD' ? 'selected' : ''}>Thẻ</option>
                <option value="TRANSFER" ${filter.paymentMethod == 'TRANSFER' ? 'selected' : ''}>Chuyển khoản</option>
            </select>
        </div>

        <div class="form-group filter-search">
            <label>Tìm kiếm</label>
            <input name="keyword" value="${filter.keyword}" type="text" placeholder="Mã đơn, tên khách, nhân viên, chi nhánh..."/>
        </div>

        <div class="form-group">
            <label>Sắp xếp</label>
            <select name="sortBy">
                <option value="created_at" ${empty filter.sortBy || filter.sortBy == 'created_at' ? 'selected' : ''}>Ngày tạo</option>
                <option value="total_amount" ${filter.sortBy == 'total_amount' ? 'selected' : ''}>Tổng tiền</option>
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

function dateToDisplay(v) {
    var m = v.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    return m ? m[3] + '/' + m[2] + '/' + m[1] : v;
}
function displayToISO(v) {
    var m = v.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    return m ? m[3] + '-' + m[2] + '-' + m[1] : '';
}
(function () {
    var form = document.querySelector('form.filter-card');
    var inputs = [document.getElementById('dateFrom'), document.getElementById('dateTo')];
    inputs.forEach(function (inp) { if (inp) inp.value = dateToDisplay(inp.value); });
    if (form) form.addEventListener('submit', function (e) {
        inputs.forEach(function (inp) {
            if (!inp || inp.disabled) return;
            var v = inp.value.trim();
            if (!v) { inp.value = ''; return; }
            var iso = displayToISO(v);
            if (!iso) { e.preventDefault(); alert('Ngày phải có định dạng dd/MM/yyyy'); inp.focus(); return; }
            inp.value = iso;
        });
    });
})();
</script>
