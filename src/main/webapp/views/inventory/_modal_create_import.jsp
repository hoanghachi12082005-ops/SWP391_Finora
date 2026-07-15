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
                    <!-- Tabs -->
                    <ul class="nav nav-tabs mt-3 w-100 border-bottom-0" id="importModeTabs" role="tablist" style="gap: 4px;">
                        <li class="nav-item" role="presentation">
                            <button class="nav-link active px-3 py-2" id="manual-tab" data-bs-toggle="tab" data-bs-target="#manualImportPane" type="button" role="tab" aria-controls="manualImportPane" aria-selected="true" style="font-size: 14px; font-weight: 600; border-radius: 8px 8px 0 0;">
                                <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">edit_note</span>
                                Nhập tay
                            </button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link px-3 py-2" id="excel-tab" data-bs-toggle="tab" data-bs-target="#excelImportPane" type="button" role="tab" aria-controls="excelImportPane" aria-selected="false" style="font-size: 14px; font-weight: 600; border-radius: 8px 8px 0 0;">
                                <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">upload_file</span>
                                Nhập từ Excel
                            </button>
                        </li>
                    </ul>
                </div>
                
                <div class="modal-body pt-0">
                <div class="tab-content" id="importModeTabContent">
                <!-- ===== TAB 1: NHẬP TAY (existing, untouched) ===== -->
                <div class="tab-pane fade show active" id="manualImportPane" role="tabpanel" aria-labelledby="manual-tab">
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
                </div><!-- end manualImportPane -->
                
                <!-- ===== TAB 2: NHẬP TỪ EXCEL (new) ===== -->
                <div class="tab-pane fade" id="excelImportPane" role="tabpanel" aria-labelledby="excel-tab">
                    <div class="mb-3 pt-3">
                        <div class="d-flex align-items-center gap-2 mb-3">
                            <button type="button" class="btn btn-outline-success btn-sm" onclick="downloadExcelTemplate()" style="border-radius: 8px; font-weight: 600;">
                                <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">download</span>
                                Tải file mẫu
                            </button>
                            <button type="button" class="btn btn-primary btn-sm" onclick="document.getElementById('excelFileInput').click()" style="border-radius: 8px; font-weight: 600;">
                                <span class="material-icons" style="font-size: 16px; vertical-align: text-bottom; margin-right: 4px;">upload_file</span>
                                Chọn file Excel
                            </button>
                            <input type="file" id="excelFileInput" accept=".xlsx,.xls" style="display:none;" onchange="handleExcelUpload(this)">
                            <span id="excelFileName" class="text-muted small" style="font-style: italic;"></span>
                        </div>
                        <div class="alert alert-info small py-2 px-3 mb-3" style="border-radius: 8px; font-size: 12.5px;">
                            <span class="material-icons" style="font-size: 14px; vertical-align: text-bottom; margin-right: 4px;">info</span>
                            File Excel cần có 4 cột: <strong>Tên sản phẩm</strong>, <strong>Mã NCC (Supplier ID)</strong>, <strong>Tên NCC (không cần điền)</strong>, <strong>Số lượng</strong>. Cột Tên NCC chỉ để tham khảo, hệ thống sẽ bỏ qua.
                        </div>
                    </div>

                    <div class="table-responsive" style="max-height: 300px; overflow-y: auto; border: 1px solid #e2e8f0; border-radius: 8px;">
                        <table class="table table-sm align-middle mb-0" id="excelImportProductTable">
                            <thead style="background: #f8fafc; position: sticky; top: 0; z-index: 1;">
                                <tr>
                                    <th class="ps-3 py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="30%">Sản Phẩm</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="40%">Nhà Cung Cấp</th>
                                    <th class="py-2 text-muted" style="font-weight: 600; font-size: 13px;" width="18%">Giá Nhập</th>
                                    <th class="py-2 text-muted text-center" width="8%" style="font-weight: 600; font-size: 13px;">Số Lượng</th>
                                    <th class="py-2 text-center text-muted" width="4%" style="font-weight: 600; font-size: 13px;">Xóa</th>
                                </tr>
                            </thead>
                            <tbody id="excelImportProductTableBody">
                                <tr id="excelImportEmptyRow">
                                    <td colspan="5">
                                        <div class="text-center text-muted py-4">
                                            <span class="material-icons mb-2" style="font-size: 32px; color: #cbd5e1;">cloud_upload</span>
                                            <p class="mb-0 small">Chưa có dữ liệu.<br>Tải lên file Excel để bắt đầu.</p>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="mb-3 mt-4">
                        <label class="form-label fw-semibold text-muted small">Ghi chú (Tùy chọn)</label>
                        <textarea name="excelNote" class="form-control" rows="2" placeholder="Ghi chú nhập hàng từ Excel..." style="border-radius: 8px;" id="excelImportNote"></textarea>
                    </div>
                </div><!-- end excelImportPane -->

                </div><!-- end tab-content -->
                </div><!-- end modal-body -->
                
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
        const isExcelTabActive = document.getElementById('excelImportPane').classList.contains('active');
        if (!isExcelTabActive) {
            if (iTableBody.querySelectorAll('.i-qty-input').length === 0) {
                e.preventDefault();
                alert('Vui lòng tìm và thêm ít nhất một sản phẩm vào phiếu nhập.');
            }
        }
    });
</script>

<!-- SheetJS CDN for Excel parsing -->
<script src="https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.full.min.js"></script>

<script>
    // ========== EXCEL IMPORT TAB LOGIC (completely separate from manual tab) ==========

    const excelTableBody = document.getElementById('excelImportProductTableBody');
    const excelEmptyRow = document.getElementById('excelImportEmptyRow');

    // Download Excel template with real product-supplier data
    function downloadExcelTemplate() {
        if (typeof XLSX === 'undefined') {
            alert('Thư viện xử lý Excel chưa tải xong. Vui lòng thử lại sau vài giây.');
            return;
        }

        // Fetch all products × suppliers from server
        fetch('${pageContext.request.contextPath}/inventory?action=getImportTemplateDataApi')
            .then(res => res.json())
            .then(data => {
                // data = [[productName, supplierId, supplierName], ...]
                const wsData = [
                    ['Tên sản phẩm', 'Mã NCC (Supplier ID)', 'Tên NCC (không cần điền)', 'Số lượng']
                ];
                data.forEach(row => {
                    wsData.push([row[0], row[1], row[2], '']);
                });

                const ws = XLSX.utils.aoa_to_sheet(wsData);
                ws['!cols'] = [{wch: 35}, {wch: 22}, {wch: 30}, {wch: 12}];
                const wb = XLSX.utils.book_new();
                XLSX.utils.book_append_sheet(wb, ws, 'Nhập hàng');
                XLSX.writeFile(wb, 'Mau_Nhap_Hang_Excel.xlsx');
            })
            .catch(err => {
                alert('Lỗi khi tải dữ liệu mẫu: ' + err.message);
            });
    }

    // Handle Excel file upload
    function handleExcelUpload(inputEl) {
        const file = inputEl.files[0];
        if (!file) return;

        // Validate file type (Excel only)
        const allowedExtensions = /(\.xlsx|\.xls)$/i;
        if (!allowedExtensions.exec(file.name)) {
            alert('Vui lòng chọn đúng định dạng file Excel (.xlsx hoặc .xls).');
            inputEl.value = '';
            document.getElementById('excelFileName').textContent = '';
            return;
        }

        // Validate file size (under 5MB)
        const maxSize = 5 * 1024 * 1024; // 5MB in bytes
        if (file.size > maxSize) {
            alert('Kích thước file vượt quá giới hạn cho phép (Tối đa 5MB). Vui lòng chọn file nhỏ hơn.');
            inputEl.value = '';
            document.getElementById('excelFileName').textContent = '';
            return;
        }

        document.getElementById('excelFileName').textContent = file.name;

        if (typeof XLSX === 'undefined') {
            alert('Thư viện xử lý Excel chưa tải xong. Vui lòng thử lại sau vài giây.');
            inputEl.value = '';
            return;
        }

        const reader = new FileReader();
        reader.onload = function(e) {
            try {
                const data = new Uint8Array(e.target.result);
                const workbook = XLSX.read(data, {type: 'array'});
                const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
                const rows = XLSX.utils.sheet_to_json(firstSheet, {header: 1, defval: ''});

                if (rows.length < 2) {
                    alert('File Excel không có dữ liệu (chỉ có header hoặc trống).');
                    return;
                }

                // Build compressed string: productName\tsupplierId\tquantity|...
                // Column layout: A=Tên SP, B=Mã NCC, C=Tên NCC (skip), D=Số lượng
                let packedLines = [];
                for (let i = 1; i < rows.length; i++) {
                    const row = rows[i];
                    const productName = String(row[0] || '').trim();
                    const supplierId = String(row[1] || '').trim();
                    // row[2] = Tên NCC → bỏ qua
                    const quantity = String(row[3] || '').trim();
                    if (productName === '' && supplierId === '' && quantity === '') continue; // skip empty rows
                    packedLines.push(productName + '\t' + supplierId + '\t' + quantity);
                }

                if (packedLines.length === 0) {
                    alert('Không tìm thấy dòng dữ liệu hợp lệ trong file Excel.');
                    return;
                }

                // Send to server for validation
                const packedData = packedLines.join('|');
                const formData = new URLSearchParams();
                formData.append('action', 'checkImportExcel');
                formData.append('warehouseId', currentWarehouseId);
                formData.append('data', packedData);

                fetch('${pageContext.request.contextPath}/inventory', {
                    method: 'POST',
                    body: formData,
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
                })
                .then(res => res.json())
                .then(result => {
                    processExcelResult(result);
                })
                .catch(err => {
                    alert('Lỗi khi xử lý file Excel: ' + err.message);
                });
            } catch (ex) {
                alert('Không thể đọc file Excel. Vui lòng kiểm tra định dạng file.\n' + ex.message);
            }
        };
        reader.readAsArrayBuffer(file);
        inputEl.value = ''; // Reset for re-upload
    }

    function processExcelResult(result) {
        // Clear existing excel rows (keep manualImportPane untouched)
        excelTableBody.innerHTML = '';

        // Show errors if any
        if (result.errors && result.errors.length > 0) {
            let errorMsg = 'Các dòng bị bỏ qua do lỗi:\n\n';
            result.errors.forEach(err => { errorMsg += '• ' + err + '\n'; });
            alert(errorMsg);
        }

        // Add valid rows to the Excel table
        if (result.rows && result.rows.length > 0) {
            result.rows.forEach(row => {
                addExcelImportRow(row);
            });
        } else if (!result.errors || result.errors.length === 0) {
            alert('Không có dòng hợp lệ nào để nhập.');
        }

        // Show empty row if no valid rows
        if (excelTableBody.querySelectorAll('tr.excel-data-row').length === 0) {
            const emptyTr = document.createElement('tr');
            emptyTr.id = 'excelImportEmptyRow';
            emptyTr.innerHTML = '<td colspan="5"><div class="text-center text-muted py-4"><span class="material-icons mb-2" style="font-size: 32px; color: #cbd5e1;">cloud_upload</span><p class="mb-0 small">Chưa có dữ liệu.<br>Tải lên file Excel để bắt đầu.</p></div></td>';
            excelTableBody.appendChild(emptyTr);
        }

        checkExcelImportState();
    }

    function addExcelImportRow(row) {
        const product = row.product;
        const supplier = row.supplier;
        const allSuppliers = row.allSuppliers || [];
        const price = row.price || 0;
        const quantity = row.quantity || 1;
        const isErrorRow = row.isErrorRow || false;
        const rowError = row.rowError || '';

        const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

        // Build supplier options (only suppliers with importPrice > 0)
        let supplierOptions = '';
        allSuppliers.forEach(s => {
            const selected = s.supplierId === supplier.supplierId ? 'selected' : '';
            supplierOptions += '<option value="' + s.supplierId + '" data-price="' + (s.importPrice || 0) + '" ' + selected + '>' + s.supplierName + '</option>';
        });

        const tr = document.createElement('tr');
        tr.className = 'excel-data-row';
        if (isErrorRow) {
            tr.classList.add('table-danger');
            tr.style.border = '2px solid #dc3545';
            tr.setAttribute('data-error', 'true');
        }

        tr.innerHTML =
            '<td class="ps-3 py-3">' +
                '<div class="fw-bold text-dark">' + product.productName + '</div>' +
                '<div class="text-muted small" style="font-size: 11px;">Mã SP: ' + product.productId + '</div>' +
                '<div class="small" style="font-size: 11px;">Tồn kho: <strong class="' + (product.myStock > 0 ? 'text-success' : 'text-danger') + '">' + product.myStock + ' SP</strong></div>' +
                (isErrorRow ? '<div class="text-danger small fw-semibold mt-1 excel-row-warning" style="font-size: 11px;"><span class="material-icons" style="font-size: 13px; vertical-align: text-bottom;">warning</span> ' + rowError + '</div>' : '') +
                '<input type="hidden" name="productId[]" value="' + product.productId + '">' +
            '</td>' +
            '<td>' +
                '<select name="supplierId[]" class="form-select form-select-sm excel-row-supplier-select" style="font-size: 13px; font-weight: 500; border-radius: 8px;' + (isErrorRow ? ' border-color: #dc3545;' : '') + '">' +
                    supplierOptions +
                '</select>' +
            '</td>' +
            '<td>' +
                '<div class="position-relative">' +
                    '<input type="number" name="importPrice[]" class="form-control form-control-sm text-end fw-bold excel-price-input" required value="' + price + '" min="0" step="1000" style="padding-right: 22px; border-radius: 8px; background-color: #f1f5f9; cursor: not-allowed;" readonly>' +
                    '<span style="position: absolute; right: 8px; top: 50%; transform: translateY(-50%); font-size: 12px; color: #64748b; font-weight: bold; pointer-events: none;">đ</span>' +
                '</div>' +
            '</td>' +
            '<td>' +
                '<input type="number" name="quantity[]" class="form-control form-control-sm text-center fw-bold excel-qty-input" required value="' + quantity + '" min="1" style="width: 80px; margin: 0 auto; border-radius: 8px;">' +
            '</td>' +
            '<td class="text-center">' +
                '<button type="button" class="btn btn-sm btn-outline-danger border-0 rounded-circle p-1" onclick="removeExcelRow(this)" title="Xóa khỏi phiếu" style="width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center;">' +
                    '<span class="material-icons" style="font-size: 18px;">delete</span>' +
                '</button>' +
            '</td>';

        // Revalidate entire row state (both supplier and quantity)
        function revalidateExcelRow(tr) {
            const selectEl = tr.querySelector('.excel-row-supplier-select');
            const qtyInput = tr.querySelector('.excel-qty-input');
            const priceInput = tr.querySelector('.excel-price-input');
            let errorMsgs = [];

            // Check supplier price
            const opt = selectEl.options[selectEl.selectedIndex];
            const pVal = parseFloat(opt ? opt.getAttribute('data-price') : 0) || 0;
            priceInput.value = pVal;
            if (pVal <= 0) {
                errorMsgs.push('Nhà cung cấp này không bán sản phẩm này.');
                selectEl.style.borderColor = '#dc3545';
            } else {
                selectEl.style.borderColor = '';
            }

            // Check quantity
            const qVal = parseInt(qtyInput.value);
            if (isNaN(qVal) || qVal <= 0) {
                errorMsgs.push('Số lượng không hợp lệ');
                qtyInput.style.borderColor = '#dc3545';
            } else {
                qtyInput.style.borderColor = '';
            }

            // Update row error state
            const existingWarning = tr.querySelector('.excel-row-warning');
            if (errorMsgs.length > 0) {
                tr.classList.add('table-danger');
                tr.style.border = '2px solid #dc3545';
                tr.setAttribute('data-error', 'true');
                if (existingWarning) {
                    existingWarning.innerHTML = '<span class="material-icons" style="font-size: 13px; vertical-align: text-bottom;">warning</span> ' + errorMsgs.join('; ');
                }
            } else {
                tr.classList.remove('table-danger');
                tr.style.border = '';
                tr.removeAttribute('data-error');
                if (existingWarning) existingWarning.remove();
                selectEl.style.borderColor = '';
                qtyInput.style.borderColor = '';
            }
            checkExcelImportState();
        }

        // Supplier change handler
        const selectEl = tr.querySelector('.excel-row-supplier-select');
        selectEl.onchange = function() { revalidateExcelRow(tr); };

        // Quantity change handler (frontend validation)
        const qtyInput = tr.querySelector('.excel-qty-input');
        qtyInput.addEventListener('input', function() { revalidateExcelRow(tr); });

        excelTableBody.appendChild(tr);
        checkExcelImportState();
    }

    function removeExcelRow(btn) {
        btn.closest('tr').remove();
        // Show empty row if no data rows left
        if (excelTableBody.querySelectorAll('tr.excel-data-row').length === 0) {
            const emptyTr = document.createElement('tr');
            emptyTr.id = 'excelImportEmptyRow';
            emptyTr.innerHTML = '<td colspan="5"><div class="text-center text-muted py-4"><span class="material-icons mb-2" style="font-size: 32px; color: #cbd5e1;">cloud_upload</span><p class="mb-0 small">Chưa có dữ liệu.<br>Tải lên file Excel để bắt đầu.</p></div></td>';
            excelTableBody.appendChild(emptyTr);
        }
        checkExcelImportState();
    }

    function checkExcelImportState() {
        const isExcelTabActive = document.getElementById('excelImportPane').classList.contains('active');
        if (!isExcelTabActive) return; // Don't affect button when manual tab is active

        const dataRows = excelTableBody.querySelectorAll('tr.excel-data-row');
        const errorRows = excelTableBody.querySelectorAll('tr.excel-data-row[data-error="true"]');
        const hasData = dataRows.length > 0;
        const hasErrors = errorRows.length > 0;

        iSubmitBtn.disabled = !hasData || hasErrors;
    }

    // Tab switching: sync the submit button state and swap the note field
    document.getElementById('manual-tab').addEventListener('shown.bs.tab', function() {
        // Restore manual tab state for submit button
        const manualRows = iTableBody.querySelectorAll('.i-qty-input');
        iSubmitBtn.disabled = manualRows.length === 0;

        // Ensure form note comes from manual tab
        document.querySelectorAll('#excelImportPane [name="productId[]"], #excelImportPane [name="supplierId[]"], #excelImportPane [name="importPrice[]"], #excelImportPane [name="quantity[]"]').forEach(el => el.disabled = true);
        document.querySelectorAll('#manualImportPane [name="productId[]"], #manualImportPane [name="supplierId[]"], #manualImportPane [name="importPrice[]"], #manualImportPane [name="quantity[]"]').forEach(el => el.disabled = false);
    });

    document.getElementById('excel-tab').addEventListener('shown.bs.tab', function() {
        checkExcelImportState();

        // Ensure form data comes from excel tab
        document.querySelectorAll('#manualImportPane [name="productId[]"], #manualImportPane [name="supplierId[]"], #manualImportPane [name="importPrice[]"], #manualImportPane [name="quantity[]"]').forEach(el => el.disabled = true);
        document.querySelectorAll('#excelImportPane [name="productId[]"], #excelImportPane [name="supplierId[]"], #excelImportPane [name="importPrice[]"], #excelImportPane [name="quantity[]"]').forEach(el => el.disabled = false);
    });

    // On form submit, sync note if excel tab active
    const origFormSubmitHandler = document.getElementById('importStockForm').onsubmit;
    document.getElementById('importStockForm').addEventListener('submit', function(e) {
        const isExcelTabActive = document.getElementById('excelImportPane').classList.contains('active');
        if (isExcelTabActive) {
            // Check for error rows
            const errorRows = excelTableBody.querySelectorAll('tr.excel-data-row[data-error="true"]');
            if (errorRows.length > 0) {
                e.preventDefault();
                alert('Vui lòng sửa các dòng có lỗi nhà cung cấp (viền đỏ) trước khi nhập hàng.\nHãy đổi sang nhà cung cấp có lịch sử giá nhập hợp lệ.');
                return;
            }
            // Check has data
            const dataRows = excelTableBody.querySelectorAll('tr.excel-data-row');
            if (dataRows.length === 0) {
                e.preventDefault();
                alert('Vui lòng tải lên file Excel và thêm ít nhất một sản phẩm.');
                return;
            }
            // Copy excel note to the form's main note textarea
            const noteTA = document.querySelector('#manualImportPane textarea[name="note"]');
            if (noteTA) {
                noteTA.value = document.getElementById('excelImportNote').value;
            }
        }
    });
</script>

