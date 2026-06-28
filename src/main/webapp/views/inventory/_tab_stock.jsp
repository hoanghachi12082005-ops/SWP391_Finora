<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div class="dashboard-card">
    <c:choose>
        <%-- ==================== CHẾ ĐỘ DANH SÁCH KHO ==================== --%>
        <c:when test="${empty selectedWarehouseId}">
            <div class="card-header border-bottom-0 pb-0 mb-3">
                <h5 class="mb-3">Danh sách Kho Hàng</h5>
                <c:if test="${fn:length(warehouses) > 1 || sessionScope.currentUser.roleName == 'Admin' || sessionScope.currentUser.roleName == 'Owner'}">
                    <div class="warehouse-cards w-100 mb-4">
                        <c:forEach var="w" items="${warehouses}">
                            <div class="warehouse-card" onclick="selectWarehouse('${w.warehouseId}')">
                                <span class="material-icons warehouse-card-icon">storefront</span>
                                <div class="warehouse-card-title">${w.warehouseName}</div>
                                <div class="warehouse-card-subtitle">Chi nhánh ${w.branchId}</div>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
            </div> <!-- end card-header -->
            
            <!-- Hidden form just for JavaScript to submit when a card is clicked -->
            <form action="" method="GET" id="filterForm">
                <input type="hidden" name="tab" value="stock">
                <input type="hidden" name="warehouseId" id="warehouseIdInput" value="">
            </form>
        </c:when>

        <%-- ==================== CHẾ ĐỘ CHI TIẾT TỒN KHO ==================== --%>
        <c:otherwise>
            <div class="px-4 pt-3 pb-3">
                <div class="d-flex justify-content-between mb-3">
                    <form action="" method="GET" class="inventory-filter-bar flex-grow-1" id="filterForm">
                        <input type="hidden" name="tab" value="stock">
                        <input type="hidden" name="warehouseId" id="warehouseIdInput" value="${selectedWarehouseId}">
                        
                        <div class="filter-group">
                            <span class="material-icons filter-icon">filter_list</span>
                            
                            <select name="status" class="premium-select" onchange="this.form.submit()">
                                <option value="">Tất cả trạng thái</option>
                                <option value="ACTIVE" ${statusFilter == 'ACTIVE' ? 'selected' : ''}>Bình thường</option>
                                <option value="LOW_STOCK" ${statusFilter == 'LOW_STOCK' ? 'selected' : ''}>Tồn thấp</option>
                                <option value="OUT_OF_STOCK" ${statusFilter == 'OUT_OF_STOCK' ? 'selected' : ''}>Hết hàng</option>
                            </select>
                            
                            <select name="sort" class="premium-select" onchange="this.form.submit()">
                                <option value="qty_asc" ${sortParam == 'qty_asc' ? 'selected' : ''}>Sắp xếp: Tồn kho tăng dần</option>
                                <option value="qty_desc" ${sortParam == 'qty_desc' ? 'selected' : ''}>Sắp xếp: Tồn kho giảm dần</option>
                                <option value="name_asc" ${sortParam == 'name_asc' ? 'selected' : ''}>Sắp xếp: Tên A-Z</option>
                            </select>
                        </div>
                        
                        <div class="search-group">
                            <span class="material-icons search-icon">search</span>
                            <input type="text" name="keyword" class="premium-input" placeholder="Tìm tên/mã SP..." value="${keyword}">
                            <button type="submit" class="premium-btn">Tìm kiếm</button>
                        </div>
                    </form>

                    <div class="ms-3 action-buttons shrink-0 d-flex align-items-center">
                        <button type="button" class="btn premium-btn" style="background: #10b981; border-color: #10b981; display: flex; align-items: center; gap: 4px;" onclick="Swal.fire({icon: 'info', title: 'Đang phát triển', text: 'Tính năng lập phiếu nhập kho đang được phát triển!'})">
                            <span class="material-icons" style="font-size: 18px;">add_box</span>
                            Nhập kho
                        </button>
                    </div>
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
            <nav aria-label="Page navigation" class="mt-4">
                <ul class="pagination justify-content-center">
                    <c:forEach begin="1" end="${totalPages}" var="p">
                        <li class="page-item ${p == currentPage ? 'active' : ''}">
                            <a class="page-link" href="?tab=stock&page=${p}&warehouseId=${selectedWarehouseId}&status=${statusFilter}&sort=${sortParam}&keyword=${keyword}">${p}</a>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </c:if>
    </div>
            </c:otherwise>
        </c:choose>
</div>

<script>
function selectWarehouse(id) {
    document.getElementById('warehouseIdInput').value = id;
    document.getElementById('warehouseIdInput').form.submit();
}
</script>
