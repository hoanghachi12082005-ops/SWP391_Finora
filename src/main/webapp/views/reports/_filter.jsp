<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<form class="filter-card" method="get" action="${baseUrl}">
    <input type="hidden" name="page" value="1"/>
    <input type="hidden" name="sizeValue" value="${sizeValue}"/>

    <div class="filter-grid">
        <div class="form-group">
            <label>Khoang thoi gian</label>
            <select name="datePreset" id="datePreset" onchange="toggleDateRange()">
                <option value="">Tuy chon</option>
                <option value="today" ${datePreset == 'today' ? 'selected' : ''}>Hom nay</option>
                <option value="yesterday" ${datePreset == 'yesterday' ? 'selected' : ''}>Hom qua</option>
                <option value="7days" ${datePreset == '7days' ? 'selected' : ''}>7 ngay</option>
                <option value="30days" ${datePreset == '30days' ? 'selected' : ''}>30 ngay</option>
                <option value="this_month" ${datePreset == 'this_month' ? 'selected' : ''}>Thang nay</option>
                <option value="last_month" ${datePreset == 'last_month' ? 'selected' : ''}>Thang truoc</option>
                <option value="this_year" ${datePreset == 'this_year' ? 'selected' : ''}>Nam nay</option>
                <option value="1year" ${datePreset == '1year' ? 'selected' : ''}>1 nam</option>
            </select>
        </div>

        <div class="form-group">
            <label>Tu ngay</label>
            <input type="date" name="dateFrom" id="dateFrom" value="${filter.dateFrom}"/>
        </div>

        <div class="form-group">
            <label>Den ngay</label>
            <input type="date" name="dateTo" id="dateTo" value="${filter.dateTo}"/>
        </div>

        <div class="form-group">
            <label>Nhan vien</label>
            <select name="empId">
                <option value="">Tat ca nhan vien</option>
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
                    <label>Chi nhanh</label>
                    <select name="branchId">
                        <option value="">Tat ca chi nhanh</option>
                        <c:forEach var="branch" items="${branches}">
                            <option value="${branch.branchID}" ${filter.branchId == branch.branchID ? 'selected' : ''}>${branch.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="form-group">
            <label>Ma don hang</label>
            <input type="number" name="orderId" value="${filter.orderId}" placeholder="Nhap ma don..."/>
        </div>

        <div class="form-group">
            <label>Trang thai don</label>
            <select name="orderStatus">
                <option value="">Tat ca</option>
                <option value="PENDING" ${filter.orderStatus == 'PENDING' ? 'selected' : ''}>Cho thanh toan</option>
                <option value="PAID" ${filter.orderStatus == 'PAID' ? 'selected' : ''}>Da thanh toan</option>
                <option value="COMPLETED" ${filter.orderStatus == 'COMPLETED' ? 'selected' : ''}>Hoan thanh</option>
                <option value="CANCELLED" ${filter.orderStatus == 'CANCELLED' ? 'selected' : ''}>Da huy</option>
            </select>
        </div>

        <div class="form-group">
            <label>Phuong thuc thanh toan</label>
            <select name="paymentMethod">
                <option value="">Tat ca</option>
                <option value="CASH" ${filter.paymentMethod == 'CASH' ? 'selected' : ''}>Tien mat</option>
                <option value="CARD" ${filter.paymentMethod == 'CARD' ? 'selected' : ''}>The</option>
                <option value="TRANSFER" ${filter.paymentMethod == 'TRANSFER' ? 'selected' : ''}>Chuyen khoan</option>
            </select>
        </div>

        <div class="form-group filter-search">
            <label>Tim kiem</label>
            <input name="keyword" value="${filter.keyword}" type="text" placeholder="Ma don, ten khach, nhan vien, chi nhanh..."/>
        </div>

        <div class="form-group">
            <label>Sap xep</label>
            <select name="sortBy">
                <option value="created_at" ${empty filter.sortBy || filter.sortBy == 'created_at' ? 'selected' : ''}>Ngay tao</option>
                <option value="total_amount" ${filter.sortBy == 'total_amount' ? 'selected' : ''}>Tong tien</option>
            </select>
            <select name="sortDir" style="margin-top:4px;">
                <option value="DESC" ${empty filter.sortDir || filter.sortDir == 'DESC' ? 'selected' : ''}>Moi nhat / Cao nhat</option>
                <option value="ASC" ${filter.sortDir == 'ASC' ? 'selected' : ''}>Cu nhat / Thap nhat</option>
            </select>
        </div>

        <div class="filter-actions" style="align-self:flex-end;">
            <button class="btn-primary" type="submit">Ap dung</button>
            <a class="btn-secondary" href="${baseUrl}">Dat lai</a>
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
