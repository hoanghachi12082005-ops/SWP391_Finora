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
                <div class="bg-white p-3 rounded-4 shadow-sm border border-light mb-4 d-flex flex-wrap align-items-center justify-content-between gap-3">
                    <form action="" method="GET" id="filterForm" class="d-flex flex-wrap align-items-center gap-3 flex-grow-1 m-0">
                        <input type="hidden" name="tab" value="stock">
                        <input type="hidden" name="warehouseId" id="warehouseIdInput" value="${selectedWarehouseId}">
                        
                        <!-- Search Bar -->
                        <div class="position-relative flex-grow-1" style="max-width: 400px; min-width: 250px;">
                            <span class="material-icons position-absolute text-muted" style="left: 16px; top: 50%; transform: translateY(-50%); pointer-events: none;">search</span>
                            <input type="text" name="keyword" class="form-control rounded-pill border-light bg-light w-100" 
                                   style="padding-left: 48px; padding-right: 20px; padding-top: 10px; padding-bottom: 10px; box-shadow: none; font-size: 14.5px; transition: all 0.2s;" 
                                   placeholder="Tìm tên, mã sản phẩm..." value="${keyword}"
                                   onfocus="this.style.backgroundColor='#fff'; this.style.borderColor='#10b981'; this.style.boxShadow='0 0 0 3px rgba(16,185,129,0.1)';"
                                   onblur="this.style.backgroundColor='#f8f9fa'; this.style.borderColor='#f8f9fa'; this.style.boxShadow='none';">
                        </div>

                        <!-- Filter Dropdowns -->
                        <div class="d-flex align-items-center gap-2">
                            <div class="position-relative">
                                <span class="material-icons position-absolute text-muted" style="left: 14px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">inventory_2</span>
                                <select name="status" class="form-select rounded-pill border-light bg-light" 
                                        style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; cursor: pointer; font-size: 14px; box-shadow: none; appearance: none; background-image: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%239CA3AF%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E'); background-repeat: no-repeat; background-position: right 14px top 50%; background-size: 10px auto; transition: all 0.2s;" 
                                        onchange="this.form.submit()"
                                        onfocus="this.style.backgroundColor='#fff'; this.style.borderColor='#10b981'; this.style.boxShadow='0 0 0 3px rgba(16,185,129,0.1)';"
                                        onblur="this.style.backgroundColor='#f8f9fa'; this.style.borderColor='#f8f9fa'; this.style.boxShadow='none';">
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="ACTIVE" ${statusFilter == 'ACTIVE' ? 'selected' : ''}>Bình thường</option>
                                    <option value="LOW_STOCK" ${statusFilter == 'LOW_STOCK' ? 'selected' : ''}>Tồn thấp</option>
                                    <option value="OUT_OF_STOCK" ${statusFilter == 'OUT_OF_STOCK' ? 'selected' : ''}>Hết hàng</option>
                                </select>
                            </div>
                            
                            <div class="position-relative">
                                <span class="material-icons position-absolute text-muted" style="left: 14px; top: 50%; transform: translateY(-50%); font-size: 18px; pointer-events: none;">sort</span>
                                <select name="sort" class="form-select rounded-pill border-light bg-light" 
                                        style="padding-left: 42px; padding-right: 36px; padding-top: 10px; padding-bottom: 10px; cursor: pointer; font-size: 14px; box-shadow: none; appearance: none; background-image: url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%239CA3AF%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E'); background-repeat: no-repeat; background-position: right 14px top 50%; background-size: 10px auto; transition: all 0.2s;" 
                                        onchange="this.form.submit()"
                                        onfocus="this.style.backgroundColor='#fff'; this.style.borderColor='#10b981'; this.style.boxShadow='0 0 0 3px rgba(16,185,129,0.1)';"
                                        onblur="this.style.backgroundColor='#f8f9fa'; this.style.borderColor='#f8f9fa'; this.style.boxShadow='none';">
                                    <option value="qty_asc" ${sortParam == 'qty_asc' ? 'selected' : ''}>Tồn kho tăng dần</option>
                                    <option value="qty_desc" ${sortParam == 'qty_desc' ? 'selected' : ''}>Tồn kho giảm dần</option>
                                    <option value="name_asc" ${sortParam == 'name_asc' ? 'selected' : ''}>Tên A-Z</option>
                                </select>
                            </div>

                            <button type="submit" class="btn btn-dark rounded-pill px-4 py-2 ms-1 d-flex align-items-center" style="font-weight: 500; font-size: 14px; background-color: #1f2937; border-color: #1f2937; transition: all 0.2s;" onmouseover="this.style.backgroundColor='#111827'; this.style.transform='translateY(-1px)';" onmouseout="this.style.backgroundColor='#1f2937'; this.style.transform='none';">
                                Lọc
                            </button>
                        </div>
                    </form>

                    <div class="border-start ps-3 ms-1 d-flex align-items-center">
                        <button type="button" class="btn btn-success rounded-pill px-4 py-2 d-flex align-items-center gap-2 shadow-sm" 
                                style="background: linear-gradient(135deg, #10b981 0%, #059669 100%); border: none; font-weight: 600; font-size: 14px; transition: all 0.2s;" 
                                data-bs-toggle="modal" data-bs-target="#importStockModal"
                                onmouseover="this.style.transform='translateY(-2px)'; this.style.boxShadow='0 4px 12px rgba(16, 185, 129, 0.4)';"
                                onmouseout="this.style.transform='none'; this.style.boxShadow='0 2px 4px rgba(0,0,0,0.05)';">
                            <span class="material-icons" style="font-size: 20px;">add_circle_outline</span>
                            Nhập Hàng
                        </button>
                    </div>
                </div>

    <div class="premium-table-container">
        <table class="premium-table table-hover">
            <thead>
                <tr>
                    <th>Sản Phẩm</th>
                    <th>Mã SKU</th>
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
                <jsp:include page="/views/common/pagination.jsp">
                    <jsp:param name="currentPage" value="${currentPage}"/>
                    <jsp:param name="totalPages" value="${totalPages}"/>
                    <jsp:param name="url" value="?tab=stock&warehouseId=${selectedWarehouseId}&status=${statusFilter}&sort=${sortParam}&keyword=${keyword}&page="/>
                </jsp:include>
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

<style>
    .import-search-box { position: relative; width: 100%; margin-bottom: 20px; }
    .import-search-input { width: 100%; padding: 12px 16px 12px 40px; border-radius: 8px; border: 1px solid #e2e8f0; font-size: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); transition: all 0.2s; }
    .import-search-input:focus { outline: none; border-color: #10b981; box-shadow: 0 0 0 3px rgba(16,185,129,0.1); }
    .import-search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; font-size: 20px; }
    .import-search-results { position: absolute; top: 100%; left: 0; right: 0; background: #fff; border-radius: 8px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.15); margin-top: 4px; z-index: 1050; display: none; max-height: 300px; overflow-y: auto; border: 1px solid #e2e8f0; }
    .import-search-item { padding: 10px 16px; border-bottom: 1px solid #f1f5f9; cursor: pointer; display: flex; justify-content: space-between; align-items: center; transition: background 0.2s; }
    .import-search-item:hover { background: #f8fafc; }
</style>

<!-- Modal Nhập Hàng -->
<div class="modal fade" id="importStockModal" tabindex="-1" aria-labelledby="importStockModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/inventory" method="POST" id="importStockForm">
                <input type="hidden" name="action" value="saveImport">
                <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                
                <div class="modal-header border-bottom-0 pb-0">
                    <h5 class="modal-title fw-bold" id="importStockModalLabel" style="color: #111827;">Nhập Hàng Từ Nhà Cung Cấp</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                
                <div class="modal-body pt-4">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Tìm Sản Phẩm Nhập</label>
                        <div class="import-search-box">
                            <span class="material-icons import-search-icon">search</span>
                            <input type="text" id="importSearchInput" class="import-search-input" placeholder="Gõ tên hoặc mã sản phẩm..." autocomplete="off">
                            <div class="import-search-results" id="importSearchResults"></div>
                        </div>
                    </div>
                    
                    <div class="table-responsive" style="max-height: 300px; overflow-y: auto; border: 1px solid #e2e8f0; border-radius: 8px;">
                        <table class="table table-sm align-middle mb-0" id="importProductTable">
                            <thead style="background: #f8fafc; position: sticky; top: 0; z-index: 1;">
                                <tr>
                                    <th class="ps-3 py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="35%">Sản Phẩm</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="30%">Nhà Cung Cấp</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="15%">Giá Nhập</th>
                                    <th class="py-2 text-muted text-center" width="15%" style="font-weight: 600; font-size: 13px;">Số Lượng</th>
                                    <th class="py-2 text-center text-muted" width="5%" style="font-weight: 600; font-size: 13px;">Xóa</th>
                                </tr>
                            </thead>
                            <tbody id="importProductTableBody">
                                <tr id="importEmptyRow">
                                    <td colspan="5">
                                        <div class="text-center text-muted py-4">
                                            <span class="material-icons mb-2" style="font-size: 32px; color: #cbd5e1;">inventory</span>
                                            <p class="mb-0 small">Chưa có sản phẩm nào.<br>Tìm và chọn ở trên để thêm vào phiếu.</p>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    
                    <div class="mb-3 mt-4">
                        <label class="form-label fw-semibold text-muted small">Ghi chú (Tùy chọn)</label>
                        <textarea name="note" class="form-control" rows="2" placeholder="Ghi chú nhập hàng..." style="border-radius: 8px;"></textarea>
                    </div>
                </div>
                
                <div class="modal-footer border-top-0 pt-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal" style="border-radius: 8px; font-weight: 500;">Hủy</button>
                    <button type="submit" class="btn btn-success" id="importSubmitBtn" disabled style="background: #10b981; border-color: #10b981; border-radius: 8px; font-weight: 500;">Tạo Phiếu Nhập & Cập Nhật Tồn Kho</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    const iSearchInput = document.getElementById('importSearchInput');
    const iSearchResults = document.getElementById('importSearchResults');
    const iTableBody = document.getElementById('importProductTableBody');
    const iEmptyRow = document.getElementById('importEmptyRow');
    const iSubmitBtn = document.getElementById('importSubmitBtn');
    const currentWarehouseId = '${selectedWarehouseId}';
    let iSearchTimeout;
    let iSearchActive = false;

    // Trigger empty search on focus
    iSearchInput.addEventListener('focus', function() {
        iSearchActive = true;
        if (this.value.trim() === '') {
            triggerSearch('');
        }
    });

    iSearchInput.addEventListener('input', function() {
        iSearchActive = true;
        clearTimeout(iSearchTimeout);
        const keyword = this.value.trim();
        iSearchTimeout = setTimeout(() => {
            triggerSearch(keyword);
        }, 300);
    });

    function triggerSearch(keyword) {
        let url = '${pageContext.request.contextPath}/inventory?action=searchImportProductsApi&keyword=' + encodeURIComponent(keyword) + '&warehouseId=' + currentWarehouseId;
        
        fetch(url)
            .then(res => res.json())
            .then(data => {
                iSearchResults.innerHTML = '';
                if(data.length === 0) {
                    iSearchResults.innerHTML = '<div class="p-3 text-center text-muted small">Không tìm thấy sản phẩm nào</div>';
                } else {
                    if (keyword === '') {
                        const header = document.createElement('div');
                        header.className = 'suggestion-header';
                        header.innerHTML = '<span class="material-icons" style="font-size:16px; vertical-align:text-bottom;">warning</span> Đề xuất sản phẩm sắp hết cần nhập hàng';
                        iSearchResults.appendChild(header);
                    }
                    data.forEach(p => {
                        const item = document.createElement('div');
                        item.className = 'import-search-item';
                        item.style.cursor = 'default';
                        
                        // Format currency
                        const formattedPrice = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p.importPrice || 0);
                        
                        let optionsHtml = '';
                        if (p.suppliers && p.suppliers.length > 0) {
                            p.suppliers.forEach(sup => {
                                optionsHtml += `<option value="\${sup.supplierId}">\${sup.supplierName}</option>`;
                            });
                        } else {
                            optionsHtml = `<option value="">-- Chưa gắn Nhà Cung Cấp --</option>`;
                        }

                        item.innerHTML = `
                            <div>
                                <div class="fw-semibold" style="color: #1e293b;">\${p.productName}</div>
                                <div class="small text-muted mt-1">
                                    Tồn hiện tại: <strong class="\${p.myStock > 0 ? 'text-success' : 'text-danger'}">\${p.myStock}</strong>
                                </div>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <select class="form-select form-select-sm i-supplier-select" style="width: 200px; font-size: 13px; cursor: pointer;">
                                    \${optionsHtml}
                                </select>
                                <span class="material-icons text-success i-add-btn" style="font-size: 28px; cursor: pointer; transition: transform 0.2s;" onmouseover="this.style.transform='scale(1.1)'" onmouseout="this.style.transform='scale(1)'" title="Thêm vào phiếu">add_circle</span>
                            </div>
                        `;
                        
                        const selectEl = item.querySelector('.i-supplier-select');
                        const addBtn = item.querySelector('.i-add-btn');
                        
                        addBtn.onclick = () => {
                            if (!selectEl.value) {
                                alert('Sản phẩm này chưa được gắn nhà cung cấp!');
                                return;
                            }
                            
                            const selectedId = parseInt(selectEl.value);
                            const sup = p.suppliers.find(s => s.supplierId === selectedId);
                            
                            addImportRow(p, formattedPrice, sup, p.suppliers);
                            iSearchActive = false;
                            iSearchResults.style.display = 'none';
                            iSearchInput.value = '';
                            iSearchInput.focus();
                        };
                        
                        iSearchResults.appendChild(item);
                    });
                }
                if (iSearchActive) {
                    iSearchResults.style.display = 'block';
                }
            })
            .catch(err => {
                iSearchResults.innerHTML = '<div class="p-3 text-center text-danger small">Lỗi tìm kiếm</div>';
                if (iSearchActive) {
                    iSearchResults.style.display = 'block';
                }
            });
    }

    // Hide search results when clicking outside
    document.addEventListener('click', function(e) {
        if (!iSearchInput.contains(e.target) && !iSearchResults.contains(e.target)) {
            iSearchActive = false;
            iSearchResults.style.display = 'none';
        }
    });

    function addImportRow(product, formattedPrice, selectedSupplier, allSuppliers) {
        // Check duplicate product AND supplier
        let isDuplicate = false;
        iTableBody.querySelectorAll('tr').forEach(tr => {
            if (tr.id === 'importEmptyRow') return;
            const pidInput = tr.querySelector('input[name="productId[]"]');
            const sidInput = tr.querySelector('select[name="supplierId[]"]');
            if (pidInput && sidInput) {
                if (pidInput.value == product.productId && sidInput.value == selectedSupplier.supplierId) {
                    isDuplicate = true;
                    // Highlight row briefly
                    tr.style.backgroundColor = '#fef3c7';
                    setTimeout(() => tr.style.backgroundColor = '', 600);
                }
            }
        });

        if (isDuplicate) {
            return;
        }

        if (iEmptyRow) iEmptyRow.style.display = 'none';
        iSubmitBtn.disabled = false;

        let supplierOptions = '';
        allSuppliers.forEach(s => {
            const selected = s.supplierId === selectedSupplier.supplierId ? 'selected' : '';
            supplierOptions += `<option value="\${s.supplierId}" \${selected}>\${s.supplierName}</option>`;
        });

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="ps-3">
                <div class="fw-medium text-dark">\${product.productName}</div>
                <input type="hidden" name="productId[]" value="\${product.productId}">
            </td>
            <td>
                <select name="supplierId[]" class="form-select form-select-sm" style="font-size: 13px; background-color: #f8fafc; font-weight: 500;">
                    \${supplierOptions}
                </select>
            </td>
            <td class="text-primary fw-medium small">\${formattedPrice}</td>
            <td>
                <input type="number" name="quantity[]" class="form-control form-control-sm text-center fw-bold i-qty-input" required value="1" min="1">
            </td>
            <td class="text-center">
                <button type="button" class="btn btn-sm btn-light text-danger p-1" onclick="this.closest('tr').remove(); checkImportEmpty();" title="Xóa khỏi phiếu">
                    <span class="material-icons" style="font-size: 18px;">delete</span>
                </button>
            </td>
        `;
        iTableBody.appendChild(tr);
    }

    function checkImportEmpty() {
        if (iTableBody.querySelectorAll('tr').length === 1 && iTableBody.querySelector('#importEmptyRow')) {
            iEmptyRow.style.display = 'table-row';
            iSubmitBtn.disabled = true;
        } else if (iTableBody.querySelectorAll('tr').length === 0) {
            iSubmitBtn.disabled = true;
        }
    }

    document.getElementById('importStockForm').addEventListener('submit', function(e) {
        if (iTableBody.querySelectorAll('.i-qty-input').length === 0) {
            e.preventDefault();
            alert('Vui lòng tìm và thêm ít nhất một sản phẩm vào phiếu nhập.');
        }
    });
</script>

