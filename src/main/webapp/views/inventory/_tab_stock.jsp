<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="dashboard-card">
    <div class="card-header d-flex justify-content-between align-items-center mb-3">
        <h5>Danh sách Tồn Kho</h5>
        
        <form action="" method="GET" class="d-flex gap-2">
            <input type="hidden" name="tab" value="stock">
            <c:if test="${not empty warehouses && sessionScope.currentUser.roleName == 'Admin'}">
                <select name="warehouseId" class="form-select" onchange="this.form.submit()">
                    <option value="">Tất cả kho</option>
                    <c:forEach var="w" items="${warehouses}">
                        <option value="${w.warehouseId}" ${selectedWarehouseId == w.warehouseId ? 'selected' : ''}>${w.warehouseName}</option>
                    </c:forEach>
                </select>
            </c:if>
            <select name="status" class="form-select" onchange="this.form.submit()">
                <option value="">Tất cả trạng thái</option>
                <option value="ACTIVE" ${statusFilter == 'ACTIVE' ? 'selected' : ''}>Bình thường</option>
                <option value="LOW_STOCK" ${statusFilter == 'LOW_STOCK' ? 'selected' : ''}>Tồn thấp</option>
                <option value="OUT_OF_STOCK" ${statusFilter == 'OUT_OF_STOCK' ? 'selected' : ''}>Hết hàng</option>
            </select>
            <select name="sort" class="form-select" onchange="this.form.submit()">
                <option value="qty_asc" ${sortParam == 'qty_asc' ? 'selected' : ''}>Tồn kho: Tăng dần</option>
                <option value="qty_desc" ${sortParam == 'qty_desc' ? 'selected' : ''}>Tồn kho: Giảm dần</option>
                <option value="name_asc" ${sortParam == 'name_asc' ? 'selected' : ''}>Tên A-Z</option>
            </select>
            <input type="text" name="keyword" class="form-control" placeholder="Tìm theo tên/mã..." value="${keyword}">
            <button type="submit" class="btn btn-primary">Tìm</button>
        </form>
    </div>

    <div class="premium-table-container">
        <table class="premium-table table-hover">
            <thead>
                <tr>
                    <th>Sản phẩm</th>
                    <th>Mã SKU</th>
                    <th>Danh mục</th>
                    <th>Giá bán</th>
                    <th>Kho</th>
                    <th>Tồn kho</th>
                    <th>Trạng thái</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty stockList}">
                        <tr><td colspan="7" class="text-center text-muted">Không có dữ liệu tồn kho.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="item" items="${stockList}">
                            <!-- Calculate red gradient class based on quantity -->
                            <c:set var="rowClass" value="" />
                            <c:set var="badgeClass" value="badge-conhang" />
                            <c:set var="badgeText" value="CÒN HÀNG" />
                            
                            <c:if test="${item.quantityInStock == 0}">
                                <c:set var="rowClass" value="stock-low-0" />
                                <c:set var="badgeClass" value="badge-hout" />
                                <c:set var="badgeText" value="HẾT HÀNG" />
                            </c:if>
                            <c:if test="${item.quantityInStock > 0 && item.quantityInStock <= 5}">
                                <c:set var="rowClass" value="stock-low-1-5" />
                                <c:set var="badgeClass" value="badge-saphet" />
                                <c:set var="badgeText" value="SẮP HẾT" />
                            </c:if>
                            <c:if test="${item.quantityInStock > 5 && item.quantityInStock <= 10}">
                                <c:set var="rowClass" value="stock-low-6-10" />
                                <c:set var="badgeClass" value="badge-saphet" />
                                <c:set var="badgeText" value="TỒN THẤP" />
                            </c:if>

                            <tr class="${rowClass}">
                                <td>
                                    <div class="product-cell">
                                        <div class="product-img-box">
                                            <span class="material-icons">inventory_2</span>
                                        </div>
                                        <div class="product-details">
                                            <h6>${item.productName}</h6>
                                        </div>
                                    </div>
                                </td>
                                <td>${item.productCodebar}</td>
                                <td>${item.categoryName}</td>
                                <td><fmt:formatNumber value="${item.sellingPrice}" type="currency" currencySymbol="VNĐ" maxFractionDigits="0"/></td>
                                <td>${item.warehouseName}</td>
                                <td style="font-weight: bold;">
                                    ${item.quantityInStock} ${item.unitName}
                                </td>
                                <td>
                                    <span class="badge-status ${badgeClass}">
                                        <span>${badgeText}</span>
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <!-- Pagination -->
    <c:if test="${totalPages > 1}">
        <div class="d-flex justify-content-end mt-3">
            <ul class="pagination">
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <li class="page-item ${currentPage == i ? 'active' : ''}">
                        <a class="page-link" href="?tab=stock&page=${i}&keyword=${keyword}&status=${statusFilter}&sort=${sortParam}&warehouseId=${selectedWarehouseId}">${i}</a>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </c:if>
</div>
