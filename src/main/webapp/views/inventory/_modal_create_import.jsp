<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<style>
    .import-search-box { position: relative; width: 100%; margin-bottom: 20px; }
    .import-search-input { width: 100%; padding: 14px 16px 14px 44px; border-radius: 12px; border: 1.5px solid #cbd5e1; font-size: 15px; font-weight: 500; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); transition: all 0.2s ease-in-out; }
    .import-search-input:focus { outline: none; border-color: var(--primary-color); box-shadow: 0 0 0 4px var(--primary-light-hover); }
    .import-search-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #64748b; font-size: 22px; }
    
    .import-search-results { 
        position: absolute; 
        top: 105%; 
        left: 0; 
        right: 0; 
        background: #ffffff; 
        border-radius: 12px; 
        box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04); 
        margin-top: 6px; 
        z-index: 1050; 
        display: none; 
        max-height: 380px; 
        overflow-y: auto; 
        border: 1px solid #e2e8f0; 
        padding: 6px;
    }
    
    .import-search-item { 
        padding: 12px 16px; 
        border-radius: 8px; 
        margin-bottom: 4px;
        cursor: default; 
        display: flex; 
        justify-content: space-between; 
        align-items: center; 
        transition: all 0.2s ease;
        border: 1px solid transparent;
    }
    .import-search-item:hover { 
        background: #f8fafc; 
        border-color: #e2e8f0;
    }
    
    .suggestion-header {
        padding: 8px 12px;
        font-size: 12px;
        font-weight: 700;
        color: #b91c1c;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        background: #fef2f2;
        border-radius: 6px;
        margin-bottom: 6px;
        display: flex;
        align-items: center;
        gap: 6px;
    }
    
    .btn-add-import {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        background-color: var(--primary-color);
        color: white;
        border: none;
        padding: 6px 12px;
        border-radius: 8px;
        font-size: 13px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }
    .btn-add-import:hover {
        background-color: var(--primary-hover);
        transform: translateY(-1px);
    }
    .btn-add-import:active {
        transform: translateY(0);
    }
</style>

<!-- Modal Nhập Hàng -->
<div class="modal fade" id="importStockModal" tabindex="-1" aria-labelledby="importStockModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/inventory" method="POST" id="importStockForm">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <input type="hidden" name="action" value="saveImport">
                <input type="hidden" name="currentWarehouseId" value="${selectedWarehouseId}">
                
                <div class="modal-header border-bottom-0 pb-0 flex-column align-items-start">
                    <div class="d-flex w-100 justify-content-between align-items-center">
                        <h5 class="modal-title fw-bold" id="importStockModalLabel" style="color: #111827;">Nhập Hàng Từ Nhà Cung Cấp</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="small text-muted mt-1">Đến: <strong>Kho hiện tại</strong></div>
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
                                    <th class="ps-3 py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="30%">Sản Phẩm</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="40%">Nhà Cung Cấp</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="18%">Giá Nhập</th>
                                    <th class="py-2 text-muted text-center" width="8%" style="font-weight: 600; font-size: 13px;">Số Lượng</th>
                                    <th class="py-2 text-center text-muted" width="4%" style="font-weight: 600; font-size: 13px;">Xóa</th>
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
                    <button type="submit" class="btn btn-import-submit" id="importSubmitBtn" disabled>
                        <span class="material-icons" style="font-size: 18px; vertical-align: text-bottom; margin-right: 6px;">local_shipping</span>
                        Nhập hàng
                    </button>
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

    // Danh sách tất cả nhà cung cấp đang hoạt động trong hệ thống
    const allActiveSuppliers = [
        <c:forEach var="s" items="${suppliers}" varStatus="status">
            { supplierId: ${s.supplierID}, supplierName: "${fn:escapeXml(s.name)}", importPrice: 0 }${!status.last ? ',' : ''}
        </c:forEach>
    ];

    function checkWarehouseAndOpenModal() {
        if (!currentWarehouseId || currentWarehouseId === '' || currentWarehouseId === 'null') {
            alert('Vui lòng chọn một kho hàng ở bộ lọc trước khi thực hiện nhập hàng!');
            return;
        }
        const modal = new bootstrap.Modal(document.getElementById('importStockModal'));
        modal.show();
    }
    let iSearchActive = false;

    // Trigger empty search on focus
    iSearchInput.addEventListener('focus', function() {
        iSearchActive = true;
        triggerSearch(this.value.trim());
    });

    iSearchInput.addEventListener('click', function() {
        iSearchActive = true;
        triggerSearch(this.value.trim());
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
                        
                        const hasSuppliers = p.suppliers && p.suppliers.length > 0;
                        const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

                        if (hasSuppliers) {
                            let defaultPrice = p.suppliers[0].importPrice || 0;
                            let optionsHtml = '';
                            p.suppliers.forEach(sup => {
                                optionsHtml += `<option value="\${sup.supplierId}" data-price="\${sup.importPrice || 0}">\${sup.supplierName} — \${formatCurrency(sup.importPrice || 0)}</option>`;
                            });

                            item.innerHTML = `
                                <div>
                                    <div class="fw-bold text-dark" style="font-size: 14.5px;">\${p.productName}</div>
                                    <div class="d-flex align-items-center gap-2 mt-1" style="font-size: 12px;">
                                        <span class="badge bg-light text-secondary border">Mã: SP\${p.productId}</span>
                                        <span class="text-muted">|</span>
                                        <span>Tồn kho: <strong class="\${p.myStock > 0 ? 'text-success' : 'text-danger'}">\${p.myStock} SP</strong></span>
                                    </div>
                                </div>
                                <div class="d-flex align-items-center gap-3">
                                    <div class="d-flex flex-column align-items-end">
                                        <span class="text-muted small" style="font-size: 11px; font-weight: 500;">Giá nhập đề xuất</span>
                                        <span class="text-success fw-bold i-price-display" style="font-size: 15px;">\${formatCurrency(defaultPrice)}</span>
                                    </div>
                                    <div class="d-flex align-items-center gap-2">
                                        <select class="form-select form-select-sm i-supplier-select" style="width: 220px; font-size: 13px; border-radius: 8px; cursor: pointer;">
                                            \${optionsHtml}
                                        </select>
                                        <button type="button" class="btn-add-import i-add-btn">
                                            <span class="material-icons" style="font-size: 16px;">add</span>
                                            <span>Thêm</span>
                                        </button>
                                    </div>
                                </div>
                            `;

                            const selectEl = item.querySelector('.i-supplier-select');
                            const priceDisplay = item.querySelector('.i-price-display');
                            const addBtn = item.querySelector('.i-add-btn');

                            selectEl.onchange = (e) => {
                                const selectedOption = e.target.options[e.target.selectedIndex];
                                const pVal = selectedOption.getAttribute('data-price') || 0;
                                priceDisplay.innerText = formatCurrency(pVal);
                            };

                            addBtn.onclick = () => {
                                const selectedId = parseInt(selectEl.value);
                                const sup = p.suppliers.find(s => s.supplierId === selectedId);
                                const finalPrice = selectEl.options[selectEl.selectedIndex].getAttribute('data-price') || 0;

                                addImportRow(p, finalPrice, sup, p.suppliers);
                                iSearchActive = false;
                                iSearchResults.style.display = 'none';
                                iSearchInput.value = '';
                                iSearchInput.focus();
                            };
                        } else {
                            // Sản phẩm chưa có nhà cung cấp nào liên kết trước đó hoặc không có lịch sử nhập hàng
                            item.style.opacity = '0.75';
                            item.style.cursor = 'not-allowed';
                            item.innerHTML = `
                                <div>
                                    <div class="fw-bold text-dark" style="font-size: 14.5px;">\${p.productName}</div>
                                    <div class="d-flex align-items-center gap-2 mt-1" style="font-size: 12px;">
                                        <span class="badge bg-light text-secondary border">Mã: SP\${p.productId}</span>
                                        <span class="text-muted">|</span>
                                        <span>Tồn kho: <strong class="\${p.myStock > 0 ? 'text-success' : 'text-danger'}">\${p.myStock} SP</strong></span>
                                        <span class="badge bg-danger-subtle text-danger ms-2" style="border: 1px solid #fecaca; font-weight: 500;">Chưa có NCC liên kết</span>
                                    </div>
                                </div>
                                <div class="d-flex align-items-center gap-3">
                                    <span class="text-muted small" style="font-size: 13px; font-style: italic;">
                                        <span class="material-icons" style="font-size: 14px; vertical-align: text-bottom; margin-right: 2px;">info</span>
                                        Không thể nhập (chưa có NCC)
                                    </span>
                                </div>
                            `;
                        }

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

    function addImportRow(product, finalPrice, selectedSupplier, allSuppliers) {
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
            supplierOptions += `<option value="\${s.supplierId}" data-price="\${s.importPrice || 0}" \${selected}>\${s.supplierName}</option>`;
        });

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="ps-3 py-3">
                <div class="fw-bold text-dark">\${product.productName}</div>
                <div class="text-muted small" style="font-size: 11px;">Mã SP: \${product.productId}</div>
                <div class="small" style="font-size: 11px;">Tồn kho: <strong class="\${product.myStock > 0 ? 'text-success' : 'text-danger'}">\${product.myStock} SP</strong></div>
                <input type="hidden" name="productId[]" value="\${product.productId}">
            </td>
            <td>
                <select name="supplierId[]" class="form-select form-select-sm i-row-supplier-select" style="font-size: 13px; font-weight: 500; border-radius: 8px;">
                     \${supplierOptions}
                </select>
            </td>
            <td>
                <div class="position-relative">
                    <input type="number" name="importPrice[]" class="form-control form-control-sm text-end fw-bold i-price-input" required value="\${finalPrice}" min="0" step="1000" style="padding-right: 22px; border-radius: 8px; background-color: #f1f5f9; cursor: not-allowed;" readonly>
                    <span style="position: absolute; right: 8px; top: 50%; transform: translateY(-50%); font-size: 12px; color: #64748b; font-weight: bold; pointer-events: none;">đ</span>
                </div>
            </td>
            <td>
                <input type="number" name="quantity[]" class="form-control form-control-sm text-center fw-bold i-qty-input" required value="1" min="1" style="width: 80px; margin: 0 auto; border-radius: 8px;">
            </td>
            <td class="text-center">
                <button type="button" class="btn btn-sm btn-outline-danger border-0 rounded-circle p-1" onclick="this.closest('tr').remove(); checkImportEmpty();" title="Xóa khỏi phiếu" style="width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center;">
                    <span class="material-icons" style="font-size: 18px;">delete</span>
                </button>
            </td>
        `;
        
        tr.querySelector('.i-row-supplier-select').onchange = (e) => {
            const opt = e.target.options[e.target.selectedIndex];
            const pVal = opt.getAttribute('data-price') || 0;
            tr.querySelector('.i-price-input').value = pVal;
        };
        
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

