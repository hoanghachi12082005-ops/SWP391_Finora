<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- 
  ==========================================================================
  TAB THÔNG TIN TỒN KHO HÀNG HÓA (_tab_stock.jsp)
  - Hiển thị danh sách các sản phẩm và số lượng tồn tương ứng của từng sản phẩm.
  - Cung cấp tính năng tìm kiếm sản phẩm theo tên/SKU, lọc theo bộ lọc tồn kho (Tồn thấp, Hết hàng).
  - Có các nút Export Excel và Import Excel (hỗ trợ nhập kho qua file mẫu).
  ==========================================================================
--%>
<c:set var="roleName" value="${sessionScope.currentUser.roleName != null ? sessionScope.currentUser.roleName : ''}" />


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
                <div class="bg-white p-3 rounded-4 shadow-sm border border-light mb-4 d-flex flex-wrap align-items-center justify-content-between gap-3">
                    <form action="" method="GET" id="filterForm" class="d-flex flex-wrap align-items-center gap-3 flex-grow-1 m-0">
                        <input type="hidden" name="tab" value="stock">
                        <input type="hidden" name="warehouseId" id="warehouseIdInput" value="${selectedWarehouseId}">
                        
                        <!-- Search Bar -->
                        <div class="position-relative flex-grow-1" style="max-width: 400px; min-width: 250px;">
                            <span class="material-icons position-absolute text-muted" style="left: 16px; top: 50%; transform: translateY(-50%); pointer-events: none;">search</span>
                            <input type="text" name="keyword" class="form-control rounded-pill border-light bg-light w-100 inventory-search-input" 
                                   style="padding-left: 48px; padding-right: 20px; padding-top: 10px; padding-bottom: 10px; box-shadow: none; font-size: 14.5px; transition: all 0.2s;" 
                                   placeholder="Tìm tên, mã sản phẩm..." value="${keyword}">
                        </div>

                        <!-- Filter Dropdowns -->
                        <div class="d-flex align-items-center gap-2">
                            <div class="position-relative">
                                <span class="material-icons position-absolute text-muted" style="left: 14px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">inventory_2</span>
                                <select name="status" class="form-select rounded-pill border-light bg-light inventory-filter-select" 
                                        style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; cursor: pointer; font-size: 14px; box-shadow: none; appearance: none; background-image: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%239CA3AF%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E'); background-repeat: no-repeat; background-position: right 14px top 50%; background-size: 10px auto; transition: all 0.2s;" 
                                        onchange="this.form.submit()">
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="ACTIVE" ${statusFilter == 'ACTIVE' ? 'selected' : ''}>Bình thường</option>
                                    <option value="LOW_STOCK" ${statusFilter == 'LOW_STOCK' ? 'selected' : ''}>Tồn thấp</option>
                                    <option value="OUT_OF_STOCK" ${statusFilter == 'OUT_OF_STOCK' ? 'selected' : ''}>Hết hàng</option>
                                </select>
                            </div>
                            
                            <div class="position-relative">
                                <span class="material-icons position-absolute text-muted" style="left: 14px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">sort</span>
                                <select name="sort" class="form-select rounded-pill border-light bg-light inventory-filter-select" 
                                        style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; cursor: pointer; font-size: 14px; box-shadow: none; appearance: none; background-image: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%239CA3AF%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E'); background-repeat: no-repeat; background-position: right 14px top 50%; background-size: 10px auto; transition: all 0.2s;" 
                                        onchange="this.form.submit()">
                                    <option value="qty_asc" ${sortParam == 'qty_asc' ? 'selected' : ''}>Tồn kho tăng dần</option>
                                    <option value="qty_desc" ${sortParam == 'qty_desc' ? 'selected' : ''}>Tồn kho giảm dần</option>
                                    <option value="name_asc" ${sortParam == 'name_asc' ? 'selected' : ''}>Tên A-Z</option>
                                </select>
                            </div>

                            <button type="submit" class="btn inventory-btn-filter ms-1" style="height: 38px;">
                                <span class="material-icons" style="font-size: 18px; margin-right: 6px;">filter_alt</span>
                                <span>Lọc</span>
                            </button>
                        </div>
                    </form>
                </div>

    <div class="premium-table-container">
        <table class="premium-table table-hover">
            <thead>
                <tr>
                    <th>Sản Phẩm</th>
                    <!--<th>Mã SKU</th>-->
                    <th>Danh Mục</th>
                    <th>Giá Bán</th>
                    <th>Chi Nhánh</th>
                    <th>Tồn Kho</th>
                    <th>Trạng Thái</th>
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
                                            <c:choose>
                                                <c:when test="${not empty item.imageUrl}">
                                                    <img src="${item.imageUrl}" alt="${item.productName}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 8px;">
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="material-icons">inventory_2</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="product-details">
                                            <h6>${item.productName}</h6>
                                        </div>
                                    </div>
                                </td>
                                <!--<td>${item.productCodebar}</td>-->
                                <td>${item.categoryName}</td>
                                <td><fmt:formatNumber value="${item.sellingPrice}" type="currency" currencySymbol="VNĐ" maxFractionDigits="0"/></td>
                                <td>${item.warehouseName}</td>
                                <td style="font-weight: bold;">
                                     <c:choose>
                                         <c:when test="${roleName == 'Owner' || roleName == 'Admin'}">
                                             <div class="d-flex align-items-center gap-2">
                                                 <span>${item.quantityInStock} ${item.unitName}</span>
                                                 <button class="btn btn-sm btn-link p-0 text-primary d-print-none" onclick="editStockDirectly(${item.productId}, ${selectedWarehouseId}, ${item.quantityInStock}, '${item.productName.replace("'", "\\'")}')" title="Sửa tồn kho" style="text-decoration: none;">
                                                     <span class="material-icons" style="font-size: 16px;">edit</span>
                                                 </button>
                                             </div>
                                         </c:when>
                                         <c:otherwise>
                                             ${item.quantityInStock} ${item.unitName}
                                         </c:otherwise>
                                     </c:choose>
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
        <jsp:include page="/views/common/pagination.jsp" />
    </div>
            </c:otherwise>
        </c:choose>
</div>



